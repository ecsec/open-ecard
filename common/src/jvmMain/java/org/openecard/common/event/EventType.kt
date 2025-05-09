/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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

/**
 * Event types of the Open eCard event system.
 *
 * @author Johannes Schmoelz
 * @author Benedikt Biallowons
 */
enum class EventType(
	val eventTypeIdentifier: String,
) {
	CARD_INSERTED("http://openecard.org/event/card_inserted"),
	CARD_REMOVED("http://openecard.org/event/card_removed"),
	CARD_RESET("http://openecard.org/event/card_reset"),
	CARD_RECOGNIZED("http://openecard.org/event/card_recognized"),
	CARD_RECOGNIZED_UNKNOWN("http://openecard.org/event/card_recognized_unknown"),
	TERMINAL_ADDED("http://openecard.org/event/terminal_added"),
	TERMINAL_REMOVED("http://openecard.org/event/terminal_removed"),
	PREPARE_DEVICES("http://openecard.org/event/prepare_devices"),
	POWER_DOWN_DEVICES("http://openecard.org/event/power_down_devices"),
	API_CALL_STARTED("http://openecard.org/event/api_call_started"),
	API_CALL_FINISHED("http://openecard.org/event/api_call_finished"),
}
