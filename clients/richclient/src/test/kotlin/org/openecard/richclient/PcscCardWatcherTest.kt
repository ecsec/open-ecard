package org.openecard.richclient

import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentiallyReturns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifyNoMoreCalls
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.openecard.sal.sc.recognition.CardRecognition
import org.openecard.sc.iface.Card
import org.openecard.sc.iface.Terminal
import org.openecard.sc.iface.TerminalConnection
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.iface.Terminals
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PcscCardWatcherTest {
	private val callbacks = mock<PcscCardWatcherCallbacks>()
	private val factory = mock<TerminalFactory>()
	private val factoryLoad = mock<Terminals>()

	private fun mockTerminal(
		name: String,
		waitForCard: Boolean = false,
	): Terminal {
		val terminal = mock<Terminal>()
		every { terminal.name } returns name

		if (waitForCard) {
			everySuspend { terminal.waitForCardPresent() } calls {
				delay(100)
			}
		}
		everySuspend { terminal.waitForCardAbsent() } calls {
			delay(100)
		}
		return terminal
	}

	private fun mockCardRecognition(
		terminal: Terminal,
		connection: TerminalConnection,
		card: Card,
		recognizeCard: CardRecognition,
		cardType: String = "mockCardType",
	) {
		every { terminal.connect() } returns connection
		every { connection.card } returns card
		every { card.basicChannel } returns mock()
		every { recognizeCard.recognizeCard(any()) } returns cardType
		every { connection.disconnect() } returns Unit
	}

	@BeforeTest
	fun setup() {
		every { factory.load() } returns factoryLoad
		every { factoryLoad.establishContext() } returns Unit
		every { factoryLoad.releaseContext() } returns Unit

		every { callbacks.onTerminalAdded(any()) } returns Unit
		every { callbacks.onCardInserted(any()) } returns Unit
		every { callbacks.onCardRecognized(any(), any()) } returns Unit
		every { callbacks.onCardRemoved(any()) } returns Unit
		every { callbacks.onTerminalRemoved(any()) } returns Unit
	}

	@Test
	fun `should detect a single terminal without cards`() =
		runTest {
			val terminal = mockTerminal("mockTerminal")

			val factoryLoad =
				mock<Terminals> {
					every { list() } returns listOf(terminal)
					every { establishContext() } returns Unit
					every { releaseContext() } returns Unit
				}

			val factory =
				mock<TerminalFactory> {
					every { load() } returns factoryLoad
				}

			val callbacks =
				mock<PcscCardWatcherCallbacks> {
					every { onTerminalAdded(any()) } returns Unit
					every { onTerminalRemoved(any()) } returns Unit
				}

			val recognizeCard = mock<CardRecognition>()

			val sut = PcscCardWatcher(callbacks, this, recognizeCard, factory)
			sut.start()
			advanceTimeBy(2000)

			verify { callbacks.onTerminalAdded("mockTerminal") }
			verifyNoMoreCalls(callbacks)

			sut.stop()
		}

	@Test
	fun `should detect three terminals`() =
		runTest {
			val terminals =
				listOf(
					mockTerminal("terminal1"),
					mockTerminal("terminal2"),
					mockTerminal("terminal3"),
				)

			val factoryLoad =
				mock<Terminals> {
					every { list() } returns terminals
					every { establishContext() } returns Unit
					every { releaseContext() } returns Unit
				}

			val factory =
				mock<TerminalFactory> {
					every { load() } returns factoryLoad
				}

			val callbacks =
				mock<PcscCardWatcherCallbacks> {
					every { onTerminalAdded(any()) } returns Unit
				}

			val recognizeCard = mock<CardRecognition>()

			val sut = PcscCardWatcher(callbacks, this, recognizeCard, factory)
			sut.start()
			advanceTimeBy(2000)

			verify {
				callbacks.onTerminalAdded("terminal1")
				callbacks.onTerminalAdded("terminal2")
				callbacks.onTerminalAdded("terminal3")
			}

			sut.stop()
		}

	@Test
	fun `should detect added terminal`() =
		runTest {
			val terminal1 = mockTerminal("terminal1")
			val terminal2 = mockTerminal("terminal2")

			val sequence =
				listOf(
					emptyList(),
					emptyList(),
					listOf(terminal1, terminal2),
					listOf(terminal1, terminal2),
				)

			val factoryLoad =
				mock<Terminals> {
					every { list() } sequentiallyReturns sequence
					every { establishContext() } returns Unit
					every { releaseContext() } returns Unit
				}

			val factory =
				mock<TerminalFactory> {
					every { load() } returns factoryLoad
				}

			val callbacks =
				mock<PcscCardWatcherCallbacks> {
					every { onTerminalAdded(any()) } returns Unit
				}

			val recognizeCard = mock<CardRecognition>()

			val sut = PcscCardWatcher(callbacks, this, recognizeCard, factory)
			sut.start()
			advanceTimeBy(4000)

			verify { callbacks.onTerminalAdded("terminal1") }
			verify { callbacks.onTerminalAdded("terminal2") }

			sut.stop()
		}

	@Test
	fun `should detect removed terminal`() =
		runTest {
			val terminal1 = mockTerminal("terminal1")
			val terminal2 = mockTerminal("terminal2")

			val sequence =
				listOf(
					listOf(terminal1, terminal2),
					listOf(terminal1),
					listOf(terminal1),
				)

			val factoryLoad =
				mock<Terminals> {
					every { list() } sequentiallyReturns sequence
					every { establishContext() } returns Unit
					every { releaseContext() } returns Unit
				}

			val factory =
				mock<TerminalFactory> {
					every { load() } returns factoryLoad
				}

			val callbacks =
				mock<PcscCardWatcherCallbacks> {
					every { onTerminalAdded(any()) } returns Unit
					every { onTerminalRemoved(any()) } returns Unit
				}

			val recognizeCard = mock<CardRecognition>()

			val sut = PcscCardWatcher(callbacks, this, recognizeCard, factory)
			sut.start()
			advanceTimeBy(3000)

			verify { callbacks.onTerminalRemoved("terminal2") }

			sut.stop()
		}

	@Test
	fun `should recognize card and call callbacks`() =
		runTest {
			val terminal = mockTerminal("mockTerminal", waitForCard = true)

			val factoryLoad =
				mock<Terminals> {
					every { list() } returns listOf(terminal)
					every { establishContext() } returns Unit
					every { releaseContext() } returns Unit
				}

			val factory =
				mock<TerminalFactory> {
					every { load() } returns factoryLoad
				}

			val callbacks =
				mock<PcscCardWatcherCallbacks> {
					every { onTerminalAdded(any()) } returns Unit
					every { onCardInserted(any()) } returns Unit
					every { onCardRecognized(any(), any()) } returns Unit
					every { onCardRemoved(any()) } returns Unit
				}

			val recognizeCard = mock<CardRecognition>()
			val connection = mock<TerminalConnection>()
			val card = mock<Card>()

			mockCardRecognition(terminal, connection, card, recognizeCard)

			val sut = PcscCardWatcher(callbacks, this, recognizeCard, factory)
			sut.start()
			advanceTimeBy(2000)

			verify {
				callbacks.onTerminalAdded("mockTerminal")
				callbacks.onCardInserted("mockTerminal")
				callbacks.onCardRecognized("mockTerminal", "mockCardType")
				callbacks.onCardRemoved("mockTerminal")
			}

			sut.stop()
		}
}
