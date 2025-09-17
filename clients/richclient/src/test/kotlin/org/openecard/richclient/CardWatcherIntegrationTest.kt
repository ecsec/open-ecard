package org.openecard.richclient

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.openecard.cif.bundled.CompleteTree
import org.openecard.richclient.sc.CardState
import org.openecard.richclient.sc.CardWatcher
import org.openecard.richclient.sc.CardWatcherCallback
import org.openecard.richclient.sc.CardWatcherCallback.Companion.registerWith
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.pcsc.PcscTerminalFactory
import kotlin.test.Ignore
import kotlin.test.Test

private val logger = KotlinLogging.logger { }

class CardWatcherIntegrationTest {
	private val dispatcher = Dispatchers.IO
	private val testScope = CoroutineScope(dispatcher)

	@Ignore
	@Test
	fun `should detect card via watcher`() =
		runBlocking {
			val callbacks =
				object : CardWatcherCallback {
					override fun onInitialState(cardState: CardState) {
						logger.warn { "Initial state: $cardState" }
					}

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
			val watcher = CardWatcher(testScope, recognizeCard, PcscTerminalFactory.instance)

			watcher.start()
			callbacks.registerWith(watcher)

			println("Please insert or remove card")
			delay(15000)

			watcher.stop()
		}
}
