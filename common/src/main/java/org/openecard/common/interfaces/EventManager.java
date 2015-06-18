/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

package org.openecard.common.interfaces;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.List;
import org.openecard.common.enums.EventType;


/**
 * Interface describing an EventManager.
 * The EventManager provides functions to register and unregister {@link EventCallback}s. Furthermore the reset of a
 * card is may be propagated to the system.
 *
 * @author Johannes Schmoelz
 */
public interface EventManager {

    /**
     * Initializes the components of the EventManager.
     */
    void initialize();

    /**
     * Terminates all components of the EventManger.
     */
    void terminate();

    /**
     * Registers an EventCallback implementation with the given EventFilter implementation.
     *
     * @param callback {@link EventCallback} implementation which reacts to an event if the given {@code filter} defines
     * it as valid for the {@code callback}.
     * @param filter {@link EventFilter} implementation to which filters the events for which the {@code callback} is
     * valid.
     */
    void register(EventCallback callback, EventFilter filter);

    /**
     * Registers an EventCallback implementation for a specific event type.
     *
     * @param callback {@link EventCallback} implementation which shall be registered for the given {@code type}.
     * @param type The {@link EventType} for which the {@code callback} shall be registered.
     */
    void register(EventCallback callback, EventType type);

    /**
     * Registers an EventCallback implementation for a list of specific events.
     *
     * @param callback {@link EventCallback} implementation which shall be registered for the given list of event types.
     * @param types A list of {@link EventType} objects for which the {@code callback} shall be registered.
     */
    void register(EventCallback callback, List<EventType> types);

    /**
     * Registers the given EventCallback implementation for all events.
     *
     * @param callback {@link EventCallback} implementation which shall be registered for all events.
     */
    void registerAllEvents(EventCallback callback);

    /**
     * Unregisters the given EventCallback implementation.
     *
     * @param callback An {@link EventCallback} implementation to unregister.
     */
    void unregister(EventCallback callback);
    
    /**
     * Resets a card given as connection handle.
     * 
     * @param removeHandle {@link ConnectionHandleType} object representing a card which shall be removed.
     * @param insertHandle {@link ConnectionHandleType} object representing a card which shall be inserted.
     */
    void resetCard(ConnectionHandleType removeHandle, ConnectionHandleType insertHandle);

}
