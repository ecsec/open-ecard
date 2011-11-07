package org.openecard.client.common.interfaces;

import org.openecard.client.common.enums.EventType;


/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 */
public interface EventCallback {
    
    public void signalEvent(EventType eventType, Object eventData);
}
