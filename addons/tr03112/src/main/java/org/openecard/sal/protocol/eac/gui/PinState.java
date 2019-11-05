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

    public PinState() {
	attempts = 2;
	requestCan = false;
	blocked = false;
	deactivated = false;
    }

    public int getAttempts() {
	return attempts;
    }

    public void setAttempts(int attempts) {
	this.attempts = attempts;
    }

    public boolean isRequestCan() {
	return requestCan;
    }

    public void setRequestCan(boolean requestCan) {
	this.requestCan = requestCan;
    }

    public boolean isBlocked() {
	return blocked;
    }

    public void setBlocked(boolean blocked) {
	this.blocked = blocked;
    }

    public boolean isDeactivated() {
	return deactivated;
    }

    public void setDeactivated(boolean deactivated) {
	this.deactivated = deactivated;
    }

    public boolean isOperational() {
	return ! isBlocked() && ! isDeactivated();
    }

}
