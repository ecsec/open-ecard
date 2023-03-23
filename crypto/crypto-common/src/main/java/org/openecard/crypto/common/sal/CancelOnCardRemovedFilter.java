/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/
package org.openecard.crypto.common.sal;

import org.openecard.common.event.EventObject;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.util.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Neil Crossley
 */
public class CancelOnCardRemovedFilter<T> implements EventCallback {

    private static final Logger LOG = LoggerFactory.getLogger(CancelOnCardRemovedFilter.class);

    private final Promise<T> foundCardHandle;

    public CancelOnCardRemovedFilter(Promise<T> foundCardHandle) {
	this.foundCardHandle = foundCardHandle;
    }

    @Override
    public void signalEvent(EventType eventType, EventObject eventData) {
	try {
	    if (eventType == EventType.CARD_REMOVED) {
		LOG.debug("Cancelling the given promise due to removal");
		foundCardHandle.cancel();
	    }
	} catch (IllegalStateException ex) {
	    // caused if callback is called multiple times, but this is fine
	    LOG.warn("Card in an illegal state.", ex);
	}
    }
}
