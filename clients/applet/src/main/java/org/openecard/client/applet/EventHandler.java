/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.applet;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.EventCallback;


/**
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public class EventHandler implements EventCallback {
    
    private final LinkedBlockingQueue<ConnectionHandleType> eventQueue;
    
    public EventHandler() {
        eventQueue = new LinkedBlockingQueue<ConnectionHandleType>();
    }
    
    public ConnectionHandleType next() {
        ConnectionHandleType handle = null;

        do {
            try {
                handle = eventQueue.poll(30, TimeUnit.SECONDS);
            } catch (InterruptedException ignore) {
            }
        } while (handle == null);

        return handle;
    }
    
    @Override
    public void signalEvent(EventType eventType, Object eventData) {
        if (eventData instanceof ConnectionHandleType) {
            try {
                eventQueue.put((ConnectionHandleType) eventData);
            } catch (InterruptedException ignore) {
            }
        }
    }
    
}
