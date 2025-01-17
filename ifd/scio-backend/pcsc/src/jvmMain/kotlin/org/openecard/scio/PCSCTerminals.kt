/****************************************************************************
 * Copyright (C) 2014-2019 TU Darmstadt.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/
package org.openecard.scio

import io.github.oshai.kotlinlogging.KotlinLogging
import jnasmartcardio.Smartcardio.EstablishContextException
import jnasmartcardio.Smartcardio.JnaPCSCException
import org.openecard.common.ifd.scio.*
import org.openecard.common.ifd.scio.SCIOErrorCode.Companion.getLong
import org.openecard.common.ifd.scio.TerminalWatcher
import org.openecard.common.util.Pair
import java.util.*
import javax.smartcardio.CardException
import javax.smartcardio.CardTerminal
import javax.smartcardio.CardTerminals

private val LOG = KotlinLogging.logger { }

/**
 * PC/SC terminals implementation of the SCIOTerminals.
 *
 * @author Wael Alkhatib
 * @author Tobias Wich
 */
class PCSCTerminals internal constructor(private val terminalFactory: PCSCFactory) : SCIOTerminals {
	private var terminals: CardTerminals

	init {
		terminals = object : CardTerminals() {
			@Throws(CardException::class)
			override fun list(arg0: State?): MutableList<CardTerminal> {
				if (loadTerminals()) {
					return terminals.list(arg0)
				} else {
					throw JnaPCSCException(getLong(SCIOErrorCode.SCARD_E_NO_SERVICE), "Error loading PCSC subsystem.")
				}
			}

			@Throws(CardException::class)
			override fun waitForChange(arg0: Long): Boolean {
				if (loadTerminals()) {
					return terminals.waitForChange(arg0)
				} else {
					throw JnaPCSCException(getLong(SCIOErrorCode.SCARD_E_NO_SERVICE), "Error loading PCSC subsystem.")
				}
			}
		}
	}

	private fun loadTerminals(): Boolean {
		try {
			terminals = terminalFactory.rawFactory.terminals()
			return true
		} catch (ex: EstablishContextException) {
			LOG.debug(ex) { "Failed to load PCSC terminals." }
			return false
		}
	}

	override fun prepareDevices(): Boolean {
		// no-op in the PCSC world
		return false
	}

	override fun powerDownDevices(): Boolean {
		// no-op in the PCSC world
		return false
	}

	@Throws(SCIOException::class)
	override fun list(): List<SCIOTerminal> {
		return list(SCIOTerminals.State.ALL)
	}

	@Throws(SCIOException::class)
	override fun list(state: SCIOTerminals.State): List<SCIOTerminal> {
		return list(state, true)
	}

	@Throws(SCIOException::class)
	fun list(state: SCIOTerminals.State, firstTry: Boolean): List<SCIOTerminal> {
		LOG.trace { "Entering list()." }
		try {
			val scState = convertState(state)
			// get terminals with the specified state from the SmartcardIO
			val scList = terminals.list(scState)
			val list = convertTerminals(scList)
			LOG.trace { "Leaving list()." }
			return Collections.unmodifiableList<SCIOTerminal?>(list)
		} catch (ex: CardException) {
			val code = PCSCExceptionExtractor.getCode(ex)
			if (code == SCIOErrorCode.SCARD_E_NO_READERS_AVAILABLE) {
				LOG.debug { "No reader available exception." }
				return mutableListOf<SCIOTerminal>()
			} else if (code == SCIOErrorCode.SCARD_E_NO_SERVICE || code == SCIOErrorCode.SCARD_E_SERVICE_STOPPED) {
				if (firstTry) {
					LOG.debug { "No service available exception, reloading PCSC and trying again." }
					loadTerminals()
					return list(state, false)
				} else {
					LOG.debug { "No service available exception, returning empty list." }
					return mutableListOf<SCIOTerminal>()
				}
			}
			val msg = "Failed to retrieve list from terminals instance."
			LOG.error(ex) { msg }
			throw SCIOException(msg, code, ex)
		}
	}

	private fun convertState(state: SCIOTerminals.State): CardTerminals.State {
		return when (state) {
			SCIOTerminals.State.ALL -> CardTerminals.State.ALL
			SCIOTerminals.State.CARD_PRESENT -> CardTerminals.State.CARD_PRESENT
			SCIOTerminals.State.CARD_ABSENT -> CardTerminals.State.CARD_ABSENT
		}
	}

	private fun convertTerminal(scTerminal: CardTerminal): SCIOTerminal {
		// TODO: check if we should only return the same instances (caching) here
		return PCSCTerminal(scTerminal)
	}

	private fun convertTerminals(terminals: List<CardTerminal>): List<SCIOTerminal> {
		val result = ArrayList<SCIOTerminal>(terminals.size)
		for (t in terminals) {
			result.add(convertTerminal(t))
		}
		return result
	}

	@Throws(NoSuchTerminal::class)
	override fun getTerminal(name: String): SCIOTerminal {
		val t = terminals.getTerminal(name)
		if (t == null) {
			throw NoSuchTerminal(String.format("Terminal '%s' does not exist in the system.", name))
		} else {
			return convertTerminal(t)
		}
	}

	@get:Throws(SCIOException::class)
	override val watcher: TerminalWatcher
		get() = PCSCWatcher(this)


	/**
	 * Terminal Watcher part
	 */
	private class PCSCWatcher(private val parent: PCSCTerminals) : TerminalWatcher {
		private val own: PCSCTerminals = PCSCTerminals(parent.terminalFactory)

		private var pendingEvents: Queue<TerminalWatcher.StateChangeEvent>? = null
		private var terminalList: MutableSet<String>? = null
		private var cardPresent: MutableSet<String>? = null

		override val terminals: SCIOTerminals
			get() {
				// the terminal used to create the watcher
				return parent
			}

		@Throws(SCIOException::class)
		override fun start(): List<TerminalState> {
			LOG.trace { "Entering start()." }
			check(pendingEvents == null) { "Trying to initialize already initialized watcher instance." }
			pendingEvents = LinkedList()
			terminalList = HashSet()
			cardPresent = HashSet()

			try {
				// call wait for change and directly afterwards get current list of cards
				// with a bit of luck no change has happened in between and the list is coherent
				own.terminals.waitForChange(1)
				val javaTerminals = own.terminals.list()
				val result = ArrayList<TerminalState>(javaTerminals.size)
				// fill sets according to state of the terminals
				LOG.debug { "Detecting initial terminal status." }
				for (next in javaTerminals) {
					val name = next.name
					val cardInserted = next.isCardPresent
					LOG.debug { "Terminal='${name}' cardPresent=${cardInserted}" }
					terminalList!!.add(name)
					if (cardInserted) {
						cardPresent!!.add(name)
						result.add(TerminalState(name, true))
					} else {
						result.add(TerminalState(name, false))
					}
				}
				// return list of our terminals
				LOG.trace { "Leaving start() with ${result.size} states." }
				return Collections.unmodifiableList<TerminalState?>(result)
			} catch (ex: CardException) {
				val msg = "Failed to retrieve status from the PCSC system."
				val code = PCSCExceptionExtractor.getCode(ex)
				if (code == SCIOErrorCode.SCARD_E_NO_READERS_AVAILABLE) {
					LOG.debug { "No reader available exception." }
					return listOf()
				} else if (code == SCIOErrorCode.SCARD_E_NO_SERVICE || code == SCIOErrorCode.SCARD_E_SERVICE_STOPPED || code == SCIOErrorCode.SCARD_E_INVALID_HANDLE) {
					LOG.debug { "No service available exception, reloading PCSC and returning empty list." }
					parent.loadTerminals()
					own.loadTerminals()
					return listOf()
				} else {
					LOG.error(ex) { msg }
				}
				throw SCIOException(msg, code, ex)
			} catch (ex: IllegalStateException) {
				LOG.debug { "No reader available exception." }
				return listOf()
			}
		}

		@Throws(SCIOException::class)
		override fun waitForChange(): TerminalWatcher.StateChangeEvent {
			return waitForChange(0)
		}

		@Throws(SCIOException::class)
		override fun waitForChange(timeout: Long): TerminalWatcher.StateChangeEvent {
			var timeout = timeout
			LOG.trace { "Entering waitForChange() with timeout${timeout}." }
			checkNotNull(pendingEvents) { "Calling wait on uninitialized watcher instance." }

			// set timeout to maximum when value says wait indefinitely
			if (timeout == 0L) {
				timeout = Long.Companion.MAX_VALUE
			}

			while (timeout > 0) {
				val startTime = System.nanoTime()
				// try to return any present events first
				val nextEvent = pendingEvents!!.poll()
				if (nextEvent != null) {
					LOG.trace { "Leaving waitForChange() with queued event." }
					return nextEvent
				} else {
					val waitResult: Pair<Boolean, Boolean>
					try {
						waitResult = internalWait(timeout)
					} catch (ex: CardException) {
						val msg = "Error while waiting for a state change in the terminals."
						LOG.error(ex) { msg }
						throw SCIOException(msg, PCSCExceptionExtractor.getCode(ex), ex)
					}
					val changed: Boolean = waitResult.p1
					val error: Boolean = waitResult.p2

					if (!changed) {
						LOG.trace { "Leaving waitForChange() with no event." }
						return TerminalWatcher.StateChangeEvent()
					} else {
						// something has changed, retrieve actual terminals from the system and see what has changed
						val newTerminals = HashSet<String>()
						val newCardPresent = HashSet<String>()
						// only ask for terminals if there is no error
						if (!error) {
							try {
								val newStates = own.terminals.list()
								for (next in newStates) {
									val name = next.name
									newTerminals.add(name)
									if (next.isCardPresent) {
										newCardPresent.add(name)
									}
								}
							} catch (ex: CardException) {
								val msg = "Failed to retrieve status of the observed terminals."
								LOG.error(ex) { msg }
								throw SCIOException(msg, PCSCExceptionExtractor.getCode(ex), ex)
							}
						}

						// calculate what has actually happened
						// removed cards
						val cardRemoved = subtract(cardPresent!!, newCardPresent)
						val crEvents = createEvents(TerminalWatcher.EventType.CARD_REMOVED, cardRemoved)
						// removed terminals
						val termRemoved = subtract(terminalList!!, newTerminals)
						val trEvents = createEvents(TerminalWatcher.EventType.TERMINAL_REMOVED, termRemoved)
						// added terminals
						val termAdded = subtract(newTerminals, terminalList!!)
						val taEvents = createEvents(TerminalWatcher.EventType.TERMINAL_ADDED, termAdded)
						// added cards
						val cardAdded = subtract(newCardPresent, cardPresent!!)
						val caEvents = createEvents(TerminalWatcher.EventType.CARD_INSERTED, cardAdded)

						// update internal status with the calculated state
						terminalList = newTerminals
						cardPresent = newCardPresent
						pendingEvents!!.addAll(crEvents)
						pendingEvents!!.addAll(trEvents)
						pendingEvents!!.addAll(taEvents)
						pendingEvents!!.addAll(caEvents)
						// use remove so we get an exception when no event has been recorded
						// this would mean our algorithm is corrupt
						if (!pendingEvents!!.isEmpty()) {
							LOG.trace { "Leaving waitForChange() with fresh event." }
							return pendingEvents!!.remove()
						}
					}
				}

				// calculate new timeout value
				val finishTime = System.nanoTime()
				val delta = finishTime - startTime
				timeout = timeout - (delta / 1000000)
				LOG.trace { "Start wait loop again with reduced timeout value (${timeout} ms)." }
			}

			LOG.trace { "Leaving waitForChange() with no event." }
			return TerminalWatcher.StateChangeEvent()
		}

		@Throws(SCIOException::class)
		fun sleep(millis: Long) {
			try {
				Thread.sleep(millis)
			} catch (ex2: InterruptedException) {
				val msg = "Wait interrupted by another thread."
				throw SCIOException(msg, SCIOErrorCode.SCARD_E_SERVICE_STOPPED)
			}
		}

		/**
		 * Wait for events in the system.
		 * The SmartcardIO wait function only reacts on card events, new and removed terminals go unseen. in order to
		 * fix this, we wait only a short time and check the terminal list periodically.
		 *
		 * @param timeout Timeout values as in [.waitForChange].
		 * @return The first value is the changed flag . It is `true` if a change the terminals happened,
		 * `false` if a timeout occurred. <br></br>
		 * The second value is the error flag. It is `true` if an error was used to indicate that no terminals
		 * are connected, `false` otherwise.
		 * @throws CardException Thrown if any error related to the SmartcardIO occured.
		 * @throws SCIOException Thrown if the thread was interrupted. Contains the code
		 * [SCIOErrorCode.SCARD_E_SERVICE_STOPPED].
		 */
		@Throws(CardException::class, SCIOException::class)
		fun internalWait(timeout: Long): Pair<Boolean, Boolean> {
			// the SmartcardIO wait function only reacts on card events, new and removed terminals go unseen
			// to fix this, we wait only a short time and check the terminal list periodically
			var timeout = timeout
			require(timeout >= 0) { "Negative timeout value given." }
			if (timeout == 0L) {
				timeout = Long.Companion.MAX_VALUE
			}

			while (true) {
				if (timeout == 0L) {
					// waited for all time and nothing happened
					return Pair(false, false)
				}
				// calculate next wait slice
				val waitTime: Long
				if (timeout < WAIT_DELTA) {
					waitTime = timeout
					timeout = 0
				} else {
					timeout = timeout - WAIT_DELTA
					waitTime = WAIT_DELTA
				}

				try {
					// check if there is something new on the card side
					// due to the wait call blocking every other smartcard operation, we only wait for the actual events
					// very shortly and sleep for the rest of the time
					var change = own.terminals.waitForChange(1)
					if (change) {
						return Pair(true, false)
					}
					sleep(waitTime)
					// try again after sleeping
					change = own.terminals.waitForChange(1)
					if (change) {
						return Pair(true, false)
					}
				} catch (ex: CardException) {
					when (PCSCExceptionExtractor.getCode(ex)) {
						SCIOErrorCode.SCARD_E_NO_SERVICE, SCIOErrorCode.SCARD_E_SERVICE_STOPPED -> {
							LOG.debug { "No service available exception, reloading PCSC." }
							parent.loadTerminals()
							own.loadTerminals()
							// send events that everything is removed if there are any terminals connected right now
							if (!terminalList!!.isEmpty()) {
								return Pair(true, true)
							} else {
								LOG.debug { "Waiting for PCSC system to become available again." }
								// if nothing changed, wait a bit and try again
								sleep(waitTime)
								continue
							}
						}

						SCIOErrorCode.SCARD_E_NO_READERS_AVAILABLE ->
							if (!terminalList!!.isEmpty()) {
								return Pair(true, true)
							} else {
								LOG.debug { "Waiting for PCSC system to become available again." }
								sleep(waitTime)
								continue
							}

						else -> throw ex
					}
				} catch (ex: IllegalStateException) {
					// send events that everything is removed if there are any terminals connected right now
					if (!terminalList!!.isEmpty()) {
						return Pair(true, true)
					} else {
						LOG.debug { "Waiting for PCSC system to become available again." }
						// if nothing changed, wait a bit and try again
						sleep(waitTime)
						continue
					}
				}

				// check if there is something new on the terminal side
				val currentTerms = ArrayList<CardTerminal>(own.terminals.list())
				if (currentTerms.size != terminalList!!.size) {
					return Pair(true, false)
				}
				// same size, but still compare terminal names
				val newTermNames = HashSet<String?>()
				for (next in currentTerms) {
					newTermNames.add(next.getName())
				}
				val sizeBefore = newTermNames.size
				if (sizeBefore != terminalList!!.size) {
					return Pair(false, false)
				}
				newTermNames.addAll(terminalList!!)
				val sizeAfter = newTermNames.size
				if (sizeBefore != sizeAfter) {
					return Pair(false, false)
				}
			}
		}
	}

}

private const val WAIT_DELTA: Long = 1500

private fun <T> subtract(a: Set<T>, b: Set<T>): Set<T> {
	val result = HashSet<T>(a)
	result.removeAll(b)
	return result
}

private fun createEvents(
	type: TerminalWatcher.EventType,
	list: Set<String>
): Collection<TerminalWatcher.StateChangeEvent> {
	val result = ArrayList<TerminalWatcher.StateChangeEvent>(list.size)
	for (next in list) {
		result.add(TerminalWatcher.StateChangeEvent(type, next))
	}
	return result
}
