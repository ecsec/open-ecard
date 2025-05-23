/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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
package org.openecard.plugins.pinplugin

/**
 * Enumeration for recognized PIN states.
 *
 * @author Dirk Petrautzki
 */
enum class RecognizedState {
	PIN_ACTIVATED_RC3, // RESPONSE_RC3
	PIN_ACTIVATED_RC2, // RESPONSE_RC2
	PIN_SUSPENDED, // RESPONSE_SUSPENDED
	PIN_RESUMED,
	PIN_DEACTIVATED, // RESPONSE_DEACTIVATED
	PIN_BLOCKED, // RESPONSE_BLOCKED
	PUK_BLOCKED,
	UNKNOWN,
}
