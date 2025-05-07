/****************************************************************************
 * Copyright (C) 2018-2025 ecsec GmbH.
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
package org.openecard.plugins.pinplugin.gui

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.event.EventObject
import org.openecard.common.event.EventType
import org.openecard.common.interfaces.EventFilter
import java.math.BigInteger

private val logger = KotlinLogging.logger { }

/**
 *
 * @author Sebastian Schuberth
 */
class CardRemovedFilter(
	private val ifdName: String,
	private val slotIdx: BigInteger,
) : EventFilter {
	override fun matches(
		t: EventType,
		o: EventObject,
	): Boolean {
		logger.debug { "Received event." }
		if (t == EventType.CARD_REMOVED) {
			logger.debug { "Received CARD_REMOVED event." }
			val conHandle = o.handle

			if (conHandle != null && ifdName == conHandle.ifdName && slotIdx == conHandle.slotIndex) {
				logger.info { "Card removed during processing of PIN Management GUI." }
				return true
			} else {
				logger.debug { "An unrelated card has been removed." }
				return false
			}
		}

		return false
	}
}
