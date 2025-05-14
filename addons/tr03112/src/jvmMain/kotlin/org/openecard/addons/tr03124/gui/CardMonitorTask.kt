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
 */
package org.openecard.addons.tr03124.gui

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import org.openecard.common.event.EventObject
import org.openecard.common.event.EventType
import org.openecard.common.interfaces.EventCallback
import org.openecard.common.util.Promise
import org.openecard.gui.executor.BackgroundTask
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus

/**
 *
 * @author Hans-Martin Haase
 */
class CardMonitorTask(
	cardTypes: MutableList<String>,
	step: CardSelectionStep?,
) : EventCallback,
	BackgroundTask {
	private var cardAction: Promise<Void?>
	var result: ConnectionHandleType? = null
		private set

	init {
		cardAction = Promise<Void?>()
	}

	override fun signalEvent(
		eventType: EventType,
		eventData: EventObject,
	) {
		when (eventType) {
			EventType.CARD_RECOGNIZED -> {
				result = eventData.handle
				cardAction.deliver(null)
			}

			EventType.CARD_REMOVED -> {
				result = eventData.handle
				cardAction.deliver(null)
			}
			else -> {}
		}
	}

	override fun call(): StepActionResult {
		cardAction.deref()
		cardAction = Promise<Void?>()
		return StepActionResult(StepActionResultStatus.REPEAT)
	}
}
