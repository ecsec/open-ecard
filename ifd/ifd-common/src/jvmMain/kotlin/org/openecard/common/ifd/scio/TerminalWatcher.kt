/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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
package org.openecard.common.ifd.scio

/**
 * Watcher class for terminals.
 *
 * A `TerminalWatcher` instance is usually paired to a [SCIOTerminal] instance. However the watcher
 * maintains it's own type and is thus isolated from other watchers working in parallel.
 *
 * @author Tobias Wich
 */
interface TerminalWatcher {
	val terminals: SCIOTerminals

	/**
	 * Starts watching for changes in the terminals.
	 * The initial status is returned in this function and marks the reference for all successive changes.
	 *
	 * @return Unmodifiable list containing the current status of all terminals.
	 * @throws SCIOException Thrown if the operation failed.
	 * @throws IllegalStateException Thrown in case start has been called previously on this instance.
	 */
	@Throws(SCIOException::class)
	fun start(): List<TerminalState>

	/**
	 * Waits for a type change in the terminals managed by this instance.
	 *
	 * This method blocks until a type change happens in the managed terminal. In case a timeout happens, the method
	 * returns with an instance returning `false` in its [StateChangeEvent.isCancelled] method.
	 *
	 * @param timeout If positive, wait at most for the given ammount of milliseconds, if 0 wait indefinitely. Must not
	 * be negative.
	 * @return The event that has occurred. May represent a timeout.
	 * @throws SCIOException Thrown in case the card operation failed.
	 * @throws IllegalArgumentException Thrown in case the timeout is negative.
	 * @throws IllegalStateException Thrown in case this method is called without initializing the watcher with
	 * [.start].
	 */
	@Throws(SCIOException::class)
	fun waitForChange(timeout: Long): StateChangeEvent

	/**
	 * Waits for a type change in the terminals managed by this instance.
	 *
	 * This method blocks until a type change happens in the managed terminal. As this method has an infinite
	 * timeout, there is no possibility that the returned event returns `true` in its
	 * [StateChangeEvent.isCancelled] method.
	 *
	 * @return The event that has occurred. May not represent a timeout.
	 * @throws SCIOException Thrown in case the card operation failed.
	 * @throws IllegalStateException Thrown in case this method is called without initializing the watcher with
	 * [.start].
	 * @see .waitForChange
	 */
	@Throws(SCIOException::class)
	fun waitForChange(): StateChangeEvent

	/**
	 * Enum indicating the type of the event which has occurred.
	 */
	enum class EventType {
		TERMINAL_ADDED,
		TERMINAL_REMOVED,
		CARD_INSERTED,
		CARD_REMOVED,
	}

	/**
	 * Container for type change events.
	 *
	 * Instances of this class can have two states.
	 *
	 * The normal type is that it represents an event that occurred for a
	 * specific terminal.<br></br>
	 * The cancel type represents an event call which got cancelled due to a timeout.
	 */
	class StateChangeEvent {
		private val type: EventType?
		private val terminal: String?

		/**
		 * Creates an instance representing a event in the underlying `CardTerminals`.
		 *
		 * @param type Type of the event.
		 * @param terminal Terminal name associated with the event.
		 * @throws IllegalArgumentException Thrown in case either of the parameters is `null`.
		 */
		constructor(type: EventType, terminal: String) {
			this.type = type
			this.terminal = terminal
		}

		/**
		 * Creates an event representing no change.
		 * This is used for indicating a timeout in the [.waitForChange] method.
		 */
		constructor() {
			this.type = null
			this.terminal = null
		}

		val isCancelled: Boolean
			/**
			 * Gets whether this event represents a normal event, or the event got cancelled.
			 *
			 * @return `true` if this event got cancelled due to a timeout, `false` otherwise.
			 */
			get() = type == null

		val state: EventType
			/**
			 * Gets the event type that has triggered this event.
			 *
			 * @return
			 * @throws IllegalStateException Thrown if this instance has been returned due to a timeout.
			 */
			get() {
				return type ?: throw IllegalStateException("State requested for cancelled event.")
			}

		/**
		 * Gets the name of the terminal whose type has changed in this event.
		 *
		 * @return
		 * @throws IllegalStateException Thrown if this instance has been returned due to a timeout.
		 */
		fun getTerminal(): String = terminal ?: throw IllegalStateException("Terminal requested for cancelled event.")
	}
}
