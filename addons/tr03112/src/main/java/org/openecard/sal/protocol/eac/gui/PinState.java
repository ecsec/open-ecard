/****************************************************************************
 * Copyright (C) 2019-2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.sal.protocol.eac.gui;

import org.openecard.common.ifd.PacePinStatus;


/**
 *
 * @author Tobias Wich
 */
public class PinState {

    private PacePinStatus state;

    public PinState() {
	state = PacePinStatus.RC3;
    }

    public void update(PacePinStatus status) {
	if (status == null) {
	    status = PacePinStatus.UNKNOWN;
	}
	state = status;
    }

    public PacePinStatus getState() {
	return state;
    }

    public int getAttempts() {
	switch (state) {
	    case RC3:
		return 3;
	    case RC2:
		return 2;
	    case RC1:
		return 1;
	    default:
		return 0;
	}
    }

    public boolean isRequestCan() {
	return state == PacePinStatus.RC1;
    }

    public boolean isBlocked() {
	return state == PacePinStatus.BLOCKED;
    }

    public boolean isDeactivated() {
	return state == PacePinStatus.DEACTIVATED;
    }

    public boolean isOperational() {
	return ! isBlocked() && ! isDeactivated();
    }

    public boolean isUnknown() {
	return state == PacePinStatus.UNKNOWN;
    }

}
