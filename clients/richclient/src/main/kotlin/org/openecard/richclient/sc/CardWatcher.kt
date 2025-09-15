package org.openecard.richclient.sc

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.openecard.sal.sc.recognition.CardRecognition
import org.openecard.sc.iface.Terminal
import org.openecard.sc.iface.TerminalConnection
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.iface.Terminals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger { }

class CardWatcher(
	val context: CoroutineScope,
	val recognition: CardRecognition,
	val factory: TerminalFactory,
	val pollingDelay: Duration = 1000.milliseconds,
) {
	private val mutex = Mutex()

	private var state: ProcessState? = null

	private class ProcessState(
		val terminals: Terminals,
		val job: Job,
		val activeJobs: MutableList<Job> = mutableListOf(),
	)

	private var curCardState: CardState.ImmutableCardState = CardState.ImmutableCardState.Empty

	private var receivers: Map<Flow<CardStateEvent>, Channel<CardStateEvent>> = mapOf()

	suspend fun registerSink(): Flow<CardStateEvent> =
		mutex.withLock {
			val newChannel = Channel<CardStateEvent>()
			val initialState = curCardState
			val flow =
				flow {
					// emit current state
					this.emit(CardStateEvent.InitialCardState(initialState))

					// wait for data from the channel
					for (msg in newChannel) {
						this.emit(msg)
					}
				}

			receivers = receivers + (flow to newChannel)
			flow
		}

	suspend fun unregisterSink(receiver: Flow<CardStateEvent>) {
		mutex.withLock {
			val channel = receivers[receiver]
			if (channel != null) {
				receivers = receivers - receiver

				channel.close()
			}
		}
	}

	suspend fun unregisterAll() {
		mutex.withLock {
			for (channel in receivers.values) {
				channel.close()
			}
			receivers = mapOf()
		}
	}

	fun start() {
		val factoryLoad = factory.load()
		factoryLoad.establishContext()

		logger.info { "Starting card watcher job" }
		val job =
			context.launch {
				while (isActive) {
					mutex.withLock {
						val currentTerminals = factoryLoad.list()
						val currentNames = currentTerminals.map { it.name }

						val added = currentNames - curCardState.terminals
						for (name in added) {
							val terminal = currentTerminals.find { it.name == name }
							if (terminal != null) {
								curCardState = curCardState.addTerminal(name)

								// send events to all receivers
								val evt = CardStateEvent.TerminalAdded(name)
								receivers.values.forEach { it.send(evt) }

								// register worker
								val monitorJob = monitorTerminal(terminal)
								state?.activeJobs?.add(monitorJob)
							} else {
								logger.warn { "Terminal name='$terminal' removed while processing status update" }
							}
						}

						val removed = curCardState.terminals - currentNames.toSet()
						for (name in removed) {
							curCardState = curCardState.removeTerminal(name)

							// send events to all receivers
							val evt = CardStateEvent.TerminalRemoved(name)
							receivers.values.forEach { it.send(evt) }
						}
					}

					// polling as there is no api for checking terminal events in the current implementation
					delay(pollingDelay)
				}
			}
		state = ProcessState(factoryLoad, job)
	}

	private fun monitorTerminal(terminal: Terminal): Job =
		context.launch {
			while (isActive) {
				var connection: TerminalConnection? = null

				try {
					terminal.waitForCardPresent()

					connection = terminal.connect()

					mutex.withLock {
						// send events to all receivers
						val evt = CardStateEvent.CardInserted(terminal.name)
						receivers.values.forEach { it.send(evt) }
					}

					val channel = connection.card?.basicChannel
					if (channel != null) {
						recognition.recognizeCard(channel)?.let { cardType ->
							mutex.withLock {
								// send events to all receivers
								val evt = CardStateEvent.CardRecognized(terminal.name, cardType)
								receivers.values.forEach { it.send(evt) }
							}
						}
					}

					terminal.waitForCardAbsent()
					mutex.withLock {
						// send events to all receivers
						val evt = CardStateEvent.CardRemoved(terminal.name)
						receivers.values.forEach { it.send(evt) }
					}
				} catch (e: Exception) {
					// TODO: handle terminal removed and stop this job
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
		runBlocking {
			stopSuspending()
		}
	}

	suspend fun stopSuspending() {
		unregisterAll()

		val state = checkNotNull(this.state)
		mutex.withLock {
			state.activeJobs.forEach { job ->
				job.cancel()
				try {
					withTimeout(5000) {
						job.join()
					}
				} catch (e: TimeoutCancellationException) {
					logger.warn { "Timeout while waiting for monitor job to finish" }
				}
			}
			state.activeJobs.clear()

			state.job.cancel()
			try {
				withTimeout(5000) {
					state.job.join()
				}
			} catch (e: TimeoutCancellationException) {
				logger.warn { "Timeout while waiting for watcher job to finish" }
			}
		}

		state.terminals.releaseContext()
		this.state = null
	}
}
