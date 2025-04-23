/****************************************************************************
 * Copyright (C) 2014-2015 TU Darmstadt.
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
 * Interface for smart-card terminal manager.
 * This class is obtained from the system through a factory implementing the [TerminalFactory] interface.
 *
 * @author Wael Alkhatib
 * @author Tobias Wich
 */
interface SCIOTerminals {
	/**
	 * State attribute for matching terminals in the [.list] function.
	 */
	enum class State {
		/**
		 * State matching all terminals regardless of their state.
		 */
		ALL,

		/**
		 * Card is present in the terminal.
		 */
		CARD_PRESENT,

		/**
		 * Card is not present in the terminal.
		 */
		CARD_ABSENT,
	}

	/**
	 * Prepare devices, so they can be used and connected to. This is intended to handle iOS style APIs where the
	 * card can only be used for a very short time and no permament connection is desireable.
	 * There one would start a session and once that is present the IFD can connect to the card.
	 * @return Returns true if the operation caused a change in the terminal. Otherwise, false for no-op.
	 * @throws SCIOException
	 */
	@Throws(SCIOException::class)
	fun prepareDevices(): Boolean

	/**
	 * Power down any previously prepared devices.
	 * This is the companion operation to PrepareDevices.
	 * @return Returns true if the operation caused a change in the terminal. Otherwise, false for no-op.
	 */
	fun powerDownDevices(): Boolean

	/**
	 * Gets a list of all terminals satisfying the given state.
	 *
	 * If state is [State.ALL], this method returns all terminals encapsulated by this object. If state is
	 * [State.CARD_PRESENT] or [State.CARD_ABSENT], it returns all terminals where a card is currently
	 * present or absent, respectively.
	 *
	 * @param state State the terminals in the result must satisfy.
	 * @return An unmodifiable list of terminals satisfying the given state. The returned list may be empty.
	 * @throws SCIOException Thrown if the operation failed.
	 * @throws NullPointerException Thrown if no state instance has been given.
	 */
	@Throws(SCIOException::class)
	fun list(state: State): List<SCIOTerminal>

	/**
	 * Gets a list of all terminals.
	 * This method is a simplification of the [.list] function. In general it should be
	 * implemented as follows:
	 * <pre>
	 * return list(State.ALL);</pre>
	 *
	 * @return An unmodifiable list of all terminals. The returned list may be empty.
	 * @throws SCIOException Thrown if the operation failed.
	 */
	@Throws(SCIOException::class)
	fun list(): List<SCIOTerminal>

	/**
	 * Gets the terminal with the specified name.
	 *
	 * @param name Name of the terminal to return.
	 * @return The terminal instance.
	 * @throws NoSuchTerminal Thrown if no terminal with the given name exists.
	 */
	@Throws(NoSuchTerminal::class)
	fun getTerminal(name: String): SCIOTerminal

	@get:Throws(SCIOException::class)
	val watcher: TerminalWatcher
}
