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
 ***************************************************************************/

package org.openecard.mdlw.sal.enums;

import org.openecard.mdlw.sal.MwToken;
import static org.openecard.mdlw.sal.enums.Flag.*;


/**
 * Enum reflecting the state of the PIN.
 *
 * @author Tobias Wich
 */
public enum PinState {

    PIN_NOT_INITIALIZED,
    PIN_OK,
    PIN_COUNT_LOW,
    PIN_FINAL_TRY,
    PIN_LOCKED,
    PIN_NEEDS_CHANGE;

    public static PinState getUserPinState(MwToken tokenInfo) {
	if (! tokenInfo.containsFlag(CKF_USER_PIN_INITIALIZED)) {
	    return PIN_NOT_INITIALIZED;
	} else if (tokenInfo.containsFlag(CKF_USER_PIN_COUNT_LOW)) {
	    return PIN_COUNT_LOW;
	} else if (tokenInfo.containsFlag(CKF_USER_PIN_FINAL_TRY)) {
	    return PIN_FINAL_TRY;
	} else if (tokenInfo.containsFlag(CKF_USER_PIN_LOCKED)) {
	    return PIN_LOCKED;
	} else if (tokenInfo.containsFlag(CKF_USER_PIN_TO_BE_CHANGED)) {
	    return PIN_NEEDS_CHANGE;
	} else {
	    return PIN_OK;
	}
    }

    public static PinState getSOPinState(MwToken tokenInfo) {
	if (tokenInfo.containsFlag(CKF_SO_PIN_COUNT_LOW)) {
	    return PIN_COUNT_LOW;
	} else if (tokenInfo.containsFlag(CKF_SO_PIN_FINAL_TRY)) {
	    return PIN_FINAL_TRY;
	} else if (tokenInfo.containsFlag(CKF_SO_PIN_LOCKED)) {
	    return PIN_LOCKED;
	} else if (tokenInfo.containsFlag(CKF_SO_PIN_TO_BE_CHANGED)) {
	    return PIN_NEEDS_CHANGE;
	} else {
	    return PIN_OK;
	}
    }

}
