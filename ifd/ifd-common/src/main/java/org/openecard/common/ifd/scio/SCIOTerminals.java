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
    }

    /**
     * Gets a list of all terminals satisfying the given state.
     * <p>If state is {@link State#ALL}, this method returns all terminals encapsulated by this object. If state is
     * {@link State#CARD_PRESENT} or {@link State#CARD_ABSENT}, it returns all terminals where a card is currently
     * present or absent, respectively.</p>
     *
     * @param state State the terminals in the result must satisfy.
     * @return An unmodifiable list of terminals satisfying the given state. The returned list may be empty.
     * @throws SCIOException Thrown if the operation failed.
     * @throws NullPointerException Thrown if no state instance has been given.
     */
    @Nonnull
    List<SCIOTerminal> list(@Nonnull State state) throws SCIOException;

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
     * @return The terminal instance.
     * @throws NoSuchTerminal Thrown if no terminal with the given name exists.
     * @throws NullPointerException Thrown if the given terminal name is null.
     */
    SCIOTerminal getTerminal(@Nonnull String name) throws NoSuchTerminal;

    /**
     * Gets a TerminalWatcher instance so state changes can be observed.
     * The watcher instance represents maintains its own state, so that it is possible to create several watchers in
     * parallel.
     *
     * @return A new instance of TerminalWatcher.
     * @throws SCIOException Thrown if the operation failed.
     */
    TerminalWatcher getWatcher() throws SCIOException;

}
