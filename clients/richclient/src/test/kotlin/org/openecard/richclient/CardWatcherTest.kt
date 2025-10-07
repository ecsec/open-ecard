package org.openecard.richclient

import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentiallyReturns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.eq
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verifyNoMoreCalls
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.openecard.richclient.sc.CardState
import org.openecard.richclient.sc.CardWatcher
import org.openecard.richclient.sc.CardWatcherCallback
import org.openecard.richclient.sc.CardWatcherCallback.Companion.registerWith
import org.openecard.sal.sc.recognition.CardRecognition
import org.openecard.sc.iface.Card
import org.openecard.sc.iface.Terminal
import org.openecard.sc.iface.TerminalConnection
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.iface.Terminals
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CardWatcherTest {
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

	@Test
	fun `should detect a single terminal without cards`() =
		runTest {
			val terminal = mock<Terminal>()
			every { terminal.name } returns "mockTerminal"
			everySuspend { terminal.waitForCardAbsent() } calls { delay(100) }

			val terminals =
				mock<Terminals> {
					every { list() } returns listOf(terminal)
					every { getTerminal(eq("mockTerminal")) } returns terminal
					every { establishContext() } returns Unit
					every { releaseContext() } returns Unit
				}
			every { terminal.terminals } returns terminals

			val factory =
				mock<TerminalFactory> {
					every { load() } returns terminals
				}
			every { terminals.factory } returns factory

			val callbacks =
				mock<CardWatcherCallback> {
					every { onInitialState(any()) } returns Unit
					every { onTerminalAdded(any()) } returns Unit
					every { onTerminalRemoved(any()) } returns Unit
				}

			val recognizeCard = mock<CardRecognition>()

			val sut = CardWatcher(this, recognizeCard, factory)
			callbacks.registerWith(sut, this)
			sut.start()
			advanceTimeBy(2000)

			verify {
				callbacks.onInitialState(CardState.ImmutableCardState.Empty)
				callbacks.onTerminalAdded("mockTerminal")
			}
			verifyNoMoreCalls(callbacks)

			sut.stopSuspending()
		}

	@Test
	fun `should detect three terminals`() =
		runTest {
			val terminal1 = mock<Terminal> { every { name } returns "terminal1" }
			val terminal2 = mock<Terminal> { every { name } returns "terminal2" }
			val terminal3 = mock<Terminal> { every { name } returns "terminal3" }

			listOf(terminal1, terminal2, terminal3).forEach {
				everySuspend { it.waitForCardAbsent() } calls { delay(100) }
			}

			val terminals =
				mock<Terminals> {
					every { list() } returns listOf(terminal1, terminal2, terminal3)
					every { getTerminal(eq("terminal1")) } returns terminal1
					every { getTerminal(eq("terminal2")) } returns terminal2
					every { getTerminal(eq("terminal3")) } returns terminal3
					every { establishContext() } returns Unit
					every { releaseContext() } returns Unit
				}
			every { terminal1.terminals } returns terminals
			every { terminal2.terminals } returns terminals
			every { terminal3.terminals } returns terminals

			val factory =
				mock<TerminalFactory> {
					every { load() } returns terminals
				}
			every { terminals.factory } returns factory

			val callbacks =
				mock<CardWatcherCallback> {
					every { onInitialState(any()) } returns Unit
					every { onTerminalAdded(any()) } returns Unit
				}

			val recognizeCard = mock<CardRecognition>()

			val sut = CardWatcher(this, recognizeCard, factory)
			callbacks.registerWith(sut, this)
			sut.start()
			advanceTimeBy(2000)

			verify {
				callbacks.onTerminalAdded("terminal1")
				callbacks.onTerminalAdded("terminal2")
				callbacks.onTerminalAdded("terminal3")
			}

			sut.stopSuspending()
		}

	@Test
	fun `should detect added terminal`() =
		runTest {
			val terminal1 = mock<Terminal> { every { name } returns "terminal1" }
			val terminal2 = mock<Terminal> { every { name } returns "terminal2" }

			listOf(terminal1, terminal2).forEach {
				everySuspend { it.waitForCardAbsent() } calls { delay(100) }
			}

			val sequence =
				listOf(
					emptyList(),
					emptyList(),
					listOf(terminal1, terminal2),
					listOf(terminal1, terminal2),
				)

			val terminals =
				mock<Terminals> {
					every { list() } sequentiallyReturns sequence
					every { getTerminal(eq("terminal1")) } returns terminal1
					every { getTerminal(eq("terminal2")) } returns terminal2
					every { establishContext() } returns Unit
					every { releaseContext() } returns Unit
				}
			every { terminal1.terminals } returns terminals
			every { terminal2.terminals } returns terminals

			val factory =
				mock<TerminalFactory> {
					every { load() } returns terminals
				}
			every { terminals.factory } returns factory

			val callbacks =
				mock<CardWatcherCallback> {
					every { onInitialState(any()) } returns Unit
					every { onTerminalAdded(any()) } returns Unit
				}

			val recognizeCard = mock<CardRecognition>()

			val sut = CardWatcher(this, recognizeCard, factory)
			callbacks.registerWith(sut, this)
			sut.start()
			advanceTimeBy(4000)

			verify { callbacks.onTerminalAdded("terminal1") }
			verify { callbacks.onTerminalAdded("terminal2") }

			sut.stopSuspending()
		}

	@Test
	fun `should detect removed terminal`() =
		runTest {
			val terminal1 = mock<Terminal> { every { name } returns "terminal1" }
			val terminal2 = mock<Terminal> { every { name } returns "terminal2" }

			listOf(terminal1, terminal2).forEach {
				everySuspend { it.waitForCardAbsent() } calls { delay(100) }
			}

			val terminals =
				mock<Terminals> {
					every { list() } sequentiallyReturns
						listOf(
							listOf(terminal1, terminal2),
							listOf(terminal1),
							listOf(terminal1),
						)
					every { getTerminal(eq("terminal1")) } returns terminal1
					every { getTerminal(eq("terminal2")) } returns terminal2
					every { establishContext() } returns Unit
					every { releaseContext() } returns Unit
				}
			every { terminal1.terminals } returns terminals
			every { terminal2.terminals } returns terminals

			val factory =
				mock<TerminalFactory> {
					every { load() } returns terminals
				}
			every { terminals.factory } returns factory

			val callbacks =
				mock<CardWatcherCallback> {
					every { onInitialState(any()) } returns Unit
					every { onTerminalAdded(any()) } returns Unit
					every { onTerminalRemoved(any()) } returns Unit
				}

			val recognizeCard = mock<CardRecognition>()

			val sut = CardWatcher(this, recognizeCard, factory)
			callbacks.registerWith(sut, this)
			sut.start()
			advanceTimeBy(3000)

			verify { callbacks.onTerminalRemoved("terminal2") }

			sut.stopSuspending()
		}

	@Test
	fun `should recognize card and call callbacks`() =
		runTest {
			val terminal = mock<Terminal>()
			every { terminal.name } returns "mockTerminal"
			everySuspend { terminal.waitForCardPresent() } calls { delay(100) }
			everySuspend { terminal.waitForCardAbsent() } calls { delay(100) }

			val connection = mock<TerminalConnection>()
			val card = mock<Card>()
			val recognizeCard = mock<CardRecognition>()

			every { terminal.connect() } returns connection
			every { connection.card } returns card
			every { card.basicChannel } returns mock()
			every { recognizeCard.recognizeCard(any()) } returns "mockCardType"
			every { connection.disconnect() } returns Unit

			val terminals =
				mock<Terminals> {
					every { list() } returns listOf(terminal)
					every { getTerminal(eq("mockTerminal")) } returns terminal
					every { establishContext() } returns Unit
					every { releaseContext() } returns Unit
				}
			every { terminal.terminals } returns terminals

			val factory =
				mock<TerminalFactory> {
					every { load() } returns terminals
				}
			every { terminals.factory } returns factory

			val callbacks =
				mock<CardWatcherCallback> {
					every { onInitialState(any()) } returns Unit
					every { onTerminalAdded(any()) } returns Unit
					every { onCardInserted(any()) } returns Unit
					every { onCardRecognized(any(), any()) } returns Unit
					every { onCardRemoved(any()) } returns Unit
				}

			val sut = CardWatcher(this, recognizeCard, factory)
			callbacks.registerWith(sut, this)
			sut.start()
			advanceTimeBy(2000)

			verify {
				callbacks.onTerminalAdded("mockTerminal")
				callbacks.onCardInserted("mockTerminal")
				callbacks.onCardRecognized("mockTerminal", "mockCardType")
				callbacks.onCardRemoved("mockTerminal")
			}

			sut.stopSuspending()
		}
}
