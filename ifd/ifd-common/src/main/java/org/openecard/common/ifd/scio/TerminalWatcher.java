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

package org.openecard.common.ifd.scio;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;


/**
 * Watcher class for terminals.
 * <p>A {@code TerminalWatcher} instance is usually paired to a {@link SCIOTerminal} instance. However the watcher
 * maintains it's own state and is thus isolated from other watchers working in parallel.
 *
 * @author Tobias Wich
 */
public interface TerminalWatcher {

    /**
     * Gets the associated terminals instance.
     *
     * @return The terminals instance associtated with this watcher.
     */
    @Nonnull
    SCIOTerminals getTerminals();

    /**
     * Starts watching for changes in the terminals.
     * The initial status is returned in this function and marks the reference for all successive changes.
     *
     * @return Unmodifyable list containing the current status of all terminals.
     * @throws SCIOException Thrown if the operation failed.
     * @throws IllegalStateException Thrown in case start has been called previously on this instance.
     */
    List<SCIOTerminal> start() throws SCIOException;

    /**
     * Waits for a state change in the terminals managed by this instance.
     * <p>This method blocks until a state change happens in the managed terminal. In case a timeout happens, the method
     * returns with an instance returning {@code false} in its {@link StateChangeEvent#isCancelled()} method.</p>
     *
     * @param timeout If positive, wait at most for the given ammount of milliseconds, if 0 wait indefinitely. Must not
     *   be negative.
     * @return The event that has occurred. May represent a timeout.
     * @throws SCIOException Thrown in case the card operation failed.
     * @throws IllegalArgumentException Thrown in case the timeout is negative.
     * @throws IllegalStateException Thrown in case this method is called without initializing the watcher with
     *   {@link #start()}.
     */
    @Nonnull
    StateChangeEvent waitForChange(long timeout) throws SCIOException;

    /**
     * Waits for a state change in the terminals managed by this instance.
     * <p>This method blocks until a state change happens in the managed terminal. As this method has an infinite
     * timeout, there is no possibility that the returned event returns {@code true} in its
     * {@link StateChangeEvent#isCancelled()} method.</p>
     *
     * @return The event that has occurred. May not represent a timeout.
     * @throws SCIOException Thrown in case the card operation failed.
     * @throws IllegalStateException Thrown in case this method is called without initializing the watcher with
     *   {@link #start()}.
     * @see #waitForChange(long)
     */
    @Nonnull
    StateChangeEvent waitForChange() throws SCIOException;

    /**
     * Container for state change events.
     * <p>Instances of this class can have two states.</p>
     * <p>The normal state is that it represents an event that occured for a
     * specific terminal.<br>
     * The cancel state represents an event call which got cancelled due to a timeout.</p>
     */
    @Immutable
    public final class StateChangeEvent {

	private final SCIOTerminals.State state;
	private final String terminal;

	/**
	 * Creates an instance representing a event in the underlying {@code CardTerminals}.
	 *
	 * @param state State of the event.
	 * @param terminal Terminal name associated with the event.
	 * @throws IllegalArgumentException Thrown in case either of the parameters is {@code null}.
	 */
	public StateChangeEvent(@Nonnull SCIOTerminals.State state, @Nonnull String terminal) {
	    if (state == null || terminal == null) {
		throw new IllegalArgumentException("Attempt to create invalid StateChangeEvent instance.");
	    }
	    this.state = state;
	    this.terminal = terminal;
	}

	/**
	 * Creates an event representing no change.
	 * This is used for indicating a timeout in the {@link #waitForChange(long)} method.
	 */
	public StateChangeEvent() {
	    this.state = null;
	    this.terminal = null;
	}

	/**
	 * Gets whether this event represents a normal event, or the event got cancelled.
	 *
	 * @return {@code true} if this event got cancelled due to a timeout, {@code false} otherwise.
	 */
	public boolean isCancelled() {
	    return state == null;
	}

	/**
	 * Gets the state of terminal that has changed in this event.
	 *
	 * @return
	 * @throws IllegalStateException Thrown if this instance has been returned due to a timeout.
	 */
	@Nonnull
	public SCIOTerminals.State getState() {
	    if (isCancelled()) {
		throw new IllegalStateException("State requested for cancelled event.");
	    }
	    return state;
	}

	/**
	 * Gets the name of the terminal whose state has changed in this event.
	 *
	 * @return
	 * @throws IllegalStateException Thrown if this instance has been returned due to a timeout.
	 */
	public String getTerminal() {
	    if (isCancelled()) {
		throw new IllegalStateException("Terminal requested for cancelled event.");
	    }
	    return terminal;
	}

    }

}
