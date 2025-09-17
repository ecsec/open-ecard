package org.openecard.richclient

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.openecard.cif.bundled.CompleteTree
import org.openecard.richclient.sc.PcscCardWatcher
import org.openecard.richclient.sc.PcscCardWatcherCallbacks
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.pcsc.PcscTerminalFactory
import kotlin.test.Ignore
import kotlin.test.Test

private val logger = KotlinLogging.logger { }

class PcscCardWatcherIntegrationTest {
	private lateinit var watcher: PcscCardWatcher
	private val dispatcher = Dispatchers.IO
	private val testScope = CoroutineScope(dispatcher)

	@Ignore
	@Test
	fun `should detect card via watcher`() =
		runBlocking {
			val callbacks =
				object : PcscCardWatcherCallbacks {
					override fun onTerminalAdded(terminalName: String) {
						logger.warn { "Terminal added: $terminalName" }
					}

					override fun onTerminalRemoved(terminalName: String) {
						logger.warn { "Terminal removed: $terminalName" }
					}

					override fun onCardInserted(terminalName: String) {
						logger.warn { "Card added: $terminalName" }
					}

					override fun onCardRecognized(
						terminalName: String,
						cardType: String,
					) {
						logger.warn { "Card recognized: $cardType on $terminalName" }
					}

					override fun onCardRemoved(terminalName: String) {
						logger.warn { "Card removed: $terminalName" }
					}
				}

			val recognizeCard = DirectCardRecognition(CompleteTree.calls)
			watcher = PcscCardWatcher(callbacks, testScope, recognizeCard, PcscTerminalFactory.Companion.instance)

			watcher.start()

			println("Please insert or remove card")
			delay(15000)

			watcher.stop()
		}
}
