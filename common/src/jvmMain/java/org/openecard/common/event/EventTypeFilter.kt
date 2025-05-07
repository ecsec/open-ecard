/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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
 */
package org.openecard.common.event

import org.openecard.common.interfaces.EventFilter
import java.util.ArrayList

/**
 *
 * @author Tobias Wich
 */
class EventTypeFilter(
	vararg eventType: EventType?,
) : EventFilter {
	private var eventType: ArrayList<EventType>? = null

	init {
		if (eventType.isEmpty()) {
			this.eventType = ArrayList(listOf(*EventType.entries.toTypedArray()))
		} else {
			this.eventType = ArrayList(listOf(*eventType))
		}
	}

	override fun matches(
		t: EventType,
		o: EventObject,
	): Boolean {
		for (next in eventType!!) {
			if (t == next) {
				return true
			}
		}
		return false
	}
}
