/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 */
package org.openecard.crypto.common.sal

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.event.EventObject
import org.openecard.common.event.EventType
import org.openecard.common.interfaces.EventCallback
import org.openecard.common.util.Promise

private val LOG = KotlinLogging.logger { }

/**
 *
 * @author Neil Crossley
 */
class CancelOnCardRemovedFilter<T>(
	private val foundCardHandle: Promise<T>,
) : EventCallback {
	override fun signalEvent(
		eventType: EventType,
		eventData: EventObject,
	) {
		try {
			if (eventType == EventType.CARD_REMOVED) {
				LOG.debug { "Cancelling the given promise due to removal" }
				foundCardHandle.cancel()
			}
		} catch (ex: IllegalStateException) {
			// caused if callback is called multiple times, but this is fine
			LOG.warn(ex) { "Card in an illegal state." }
		}
	}
}
