/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.sal.protocol.eac.gui;


/**
 *
 * @author Tobias Wich
 */
public class PinState {

    private int attempts;
    private boolean requestCan;
    private boolean blocked;
    private boolean deactivated;

    private EacPinStatus state;

    public PinState() {
	state = EacPinStatus.RC3;
	attempts = 2;
	requestCan = false;
	blocked = false;
	deactivated = false;
    }

    public void update(EacPinStatus status) {
	state = status;
	switch (status) {
	    case RC3:
		attempts = 2;
		requestCan = false;
		blocked = false;
		deactivated = false;
		break;
	    case RC2:
		attempts = 1;
		requestCan = false;
		blocked = false;
		deactivated = false;
		break;
	    case RC1:
		attempts = 0;
		requestCan = true;
		blocked = false;
		deactivated = false;
		break;
	    case BLOCKED:
		attempts = 0;
		requestCan = false;
		blocked = true;
		deactivated = false;
		break;
	    case DEACTIVATED:
		attempts = 0;
		requestCan = false;
		blocked = false;
		deactivated = true;
		break;
	    default:
		if (status == null) {
		    attempts = 0;
		    requestCan = false;
		    blocked = false;
		    deactivated = false;
		}
	}

    }

    public EacPinStatus getState() {
	return state;
    }

    public int getAttempts() {
	switch (state) {
	    case RC3:
		return 2;
	    case RC2:
		return 1;
	    case RC1:
		return 0;
	    default:
		return 0;
	}
    }

    public boolean isRequestCan() {
	return state == EacPinStatus.RC1;
    }

    public boolean isBlocked() {
	return state == EacPinStatus.BLOCKED;
    }

    public boolean isDeactivated() {
	return state == EacPinStatus.DEACTIVATED;
    }

    public boolean isOperational() {
	return ! isBlocked() && ! isDeactivated();
    }

    public boolean isUnknown() {
	return state == null;
    }

}
