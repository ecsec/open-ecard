/****************************************************************************
 * Copyright (C) 2017-2018 ecsec GmbH.
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

package org.openecard.gui.android.pinmanagement;


/**
 *
 * @author Sebastian Schuberth
 */
public enum PinStatus {

    RC3,
    RC2,
    CAN,
    RC1,
    PIN_BLOCKED,
    PUK_BLOCKED,
    DEACTIVATED;

    public boolean isDead() {
	switch (this) {
	    case DEACTIVATED:
	    case PUK_BLOCKED:
		return true;
	    default:
		return false;
	}
    }

    public boolean isNormalPinEntry() {
	switch (this) {
	    case RC3:
	    case RC2:
	    case RC1:
		return true;
	    default:
		return false;
	}
    }

    public boolean needsCan() {
	return CAN == this;
    }

    public boolean needsPuk() {
	return PIN_BLOCKED == this;
    }

}
