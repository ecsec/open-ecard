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

package org.openecard.common.ifd.scio;

import java.util.List;
import javax.annotation.Nonnull;
import org.openecard.common.ifd.TerminalFactory;


/**
 * Interface for smart-card terminal manager.
 * This class is obtained from the system through a factory implementing the {@link TerminalFactory} interface.
 *
 * @author Wael Alkhatib
 * @author Tobias Wich
 */
public interface SCIOTerminals {

    /**
     * State attribute for matching terminals in the {@link #list(SCIOTerminals.State)} function.
     */
    public static enum State {
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
	/**
	 * Card has been inserted into the terminal since the last invocation of {@link #list(SCIOTerminals.State)}.
	 */
	CARD_INSERTION,
	/**
	 * Card has been removed from the terminal since the last invocation of {@link #list(SCIOTerminals.State)}.
	 */
	CARD_REMOVAL,
    }

    /**
     * Gets a list of all terminals satisfying the given state.
     * <p>If state is {@link State#ALL}, this method returns all terminals encapsulated by this object. If state is
     * {@link State#CARD_PRESENT} or {@link State#CARD_ABSENT}, it returns all terminals where a card is currently
     * present or absent, respectively.</p>
     * <p>If state is {@link State#CARD_INSERTION} or {@link State#CARD_REMOVAL}, it returns all terminals for which an
     * insertion (or removal, respectively) was detected during the last call to {@link #waitForChange()}. If
     * {@link #waitForChange()} has not been called on this object, {@code CARD_INSERTION} is equivalent to
     * {@code CARD_PRESENT} and {@code CARD_REMOVAL} is equivalent to {@code CARD_ABSENT}. For an example of the use of
     * {@code CARD_INSERTION}, see {@link #waitForChange()}.
     *
     * @param state State the terminals in the result must satisfy.
     * @return An unmodifiable list of terminals satisfying the given state. The returned list may be empty.
     * @throws SCIOException Thrown if the operation failed.
     */
    @Nonnull
    List<SCIOTerminal> list(State state) throws SCIOException;

    /**
     * Gets a list of all terminals.
     * This method is a simplification of the {@link #list(SCIOTerminals.State)} function. In general it should be
     * implemented as follows:
     * <pre>
     * return list(State.ALL);</pre>
     *
     * @return An unmodifiable list of all terminals. The returned list may be empty.
     * @throws SCIOException Thrown if the operation failed.
     */
    @Nonnull
    List<SCIOTerminal> list() throws SCIOException;

    /**
     * Gets the terminal with the specified name.
     *
     * @param name Name of the terminal to return.
     * @return The terminal instance, or {@code null} if no terminal with the given name exists.
     * @throws NullPointerException Thrown if the given terminal name is null.
     */
    SCIOTerminal getTerminal(@Nonnull String name);

    /**
     * Waits for a state change in the terminals managed by this instance.
     * This function is the same as {@link #waitForChange(long)} with a timeout set to 0.
     *
     * @throws SCIOException Thrown in case the card operation failed.
     */
    void waitForChange() throws SCIOException;

    /**
     * Waits for a state change in the terminals managed by this instance.
     * <p>This method examines each terminal of this object. If a card was inserted into or removed from a terminal
     * since the previous call to {@code waitForChange()}, it returns immediately. Otherwise, or if this is the first
     * call to {@code waitForChange()} on this object, it blocks until a card is inserted into or removed from a
     * terminal.</p>
     * <p>If timeout is greater than 0, the method returns after timeout milliseconds even if there is no change in
     * state. In that case, this method returns {@code false}, otherwise it returns {@code true}.</p>
     * <p>This method is often used in a loop in combination with list(State.CARD_INSERTION), for example:</p>
     * <pre>
     * TerminalFactory factory = ...;
     * CardTerminals terminals = factory.terminals();
     * while (true) {
     *   for (CardTerminal terminal : terminals.list(CARD_INSERTION)) {
     *     // examine Card in terminal, return if it matches
     *   }
     *   terminals.waitForChange();
     * }</pre>
     * <p>Note that in contrast to the Java SmartcardIO, this method does not throw an {@link IllegalStateException}
     * when no terminal is present.</p>
     *
     * @param timeout If positive, wait at most for the given ammount of milliseconds, if 0 wait indefinitely. Must not
     *   be negative.
     * @return {@code true} if a change in the terminals occured, {@code false} if the timeout has been exceeded.
     * @throws SCIOException Thrown in case the card operation failed.
     * @throws IllegalArgumentException Thrown in case the timeout is negative.
     */
    boolean waitForChange(long timeout) throws SCIOException;

}
