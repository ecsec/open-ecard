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
import org.openecard.common.interfaces.EventFilter

private val LOG = KotlinLogging.logger { }

/**
 *
 * @author Neil Crossley
 */
class CardRemovalFilter : EventFilter {
	override fun matches(
		t: EventType,
		o: EventObject,
	): Boolean {
		if (t == EventType.CARD_REMOVED) {
			LOG.debug { "Card removal detected." }
			return true
		}

		return false
	}
}
