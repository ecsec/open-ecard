package org.openecard.richclient

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.openecard.sal.sc.recognition.CardRecognition
import org.openecard.sc.iface.Terminal
import org.openecard.sc.iface.TerminalConnection
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.iface.Terminals

private val logger = KotlinLogging.logger { }

class PcscCardWatcher(
	val callbacks: PcscCardWatcherCallbacks,
	val context: CoroutineScope,
	val recognizeCard: CardRecognition,
	val factory: TerminalFactory,
) {
	private var knownTerminals = listOf<String>()

	private var state: State? = null

	private inner class State(
		val terminals: Terminals,
		val job: Job,
		val activeJobs: MutableList<Job> = mutableListOf(),
	)

	fun start() {
		val factoryLoad = factory.load()
		factoryLoad.establishContext()

		val job =
			context.launch {
				while (isActive) {
					val currentTerminals = factoryLoad.list()
					val currentNames = currentTerminals.map { it.name }

					val added = currentNames - knownTerminals
					for (name in added) {
						callbacks.onTerminalAdded(name)
						val terminal = currentTerminals.find { it.name == name }
						if (terminal != null) {
							val monitorJob = monitorTerminal(terminal)
							state?.activeJobs?.add(monitorJob)
						} else {
							logger.warn { "Could not find terminal: $terminal" }
						}
					}

					val removed = knownTerminals - currentNames
					for (name in removed) {
						callbacks.onTerminalRemoved(name)
					}

					knownTerminals = currentNames
					delay(1000)
				}
			}
		state = State(factoryLoad, job)
	}

	private fun monitorTerminal(terminal: Terminal): Job =
		context.launch {
			while (isActive) {
				var connection: TerminalConnection? = null

				try {
					terminal.waitForCardPresent()

					connection = terminal.connect()

					callbacks.onCardInserted(terminal.name)

					val channel = connection.card?.basicChannel
					if (channel != null) {
						val cardType = recognizeCard.recognizeCard(channel)
						callbacks.onCardRecognized(terminal.name, cardType)
					}

					terminal.waitForCardAbsent()
					callbacks.onCardRemoved(terminal.name)
				} catch (e: Exception) {
					logger.warn { "${terminal.name}: ${e.message}" }
					delay(1000)
				} finally {
					try {
						connection?.disconnect()
					} catch (e: Exception) {
						logger.warn { "Error disconnecting terminal ${terminal.name}: ${e.message}" }
					}
				}
			}
		}

	fun stop() {
		state?.let {
			runBlocking {
				it.activeJobs.forEach { job ->
					job.cancel()
					try {
						withTimeout(5000) {
							job.join()
						}
					} catch (e: TimeoutCancellationException) {
						logger.warn { "Timeout while waiting for monitor job to finish" }
					}
				}
				it.activeJobs.clear()

				it.job.cancel()
				try {
					withTimeout(5000) {
						it.job.join()
					}
				} catch (e: TimeoutCancellationException) {
					logger.warn { "Timeout while waiting for watcher job to finish" }
				}
			}

			it.terminals.releaseContext()
			state = null
		}
	}
}
