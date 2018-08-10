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

import javax.annotation.Nullable;
import org.openecard.common.util.Promise;
import org.openecard.plugins.pinplugin.RecognizedState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sebastian Schuberth
 */
public class PINManagementGuiImpl implements PINManagementGui {

    private static final Logger LOG = LoggerFactory.getLogger(PINManagementGuiImpl.class);

    private final Promise<Boolean> cancelPromise = new Promise<>();

    private Promise<String> userPin = new Promise<>();
    private Promise<String> newPin = new Promise<>();
    private Promise<String> userCan = new Promise<>();
    private Promise<String> userPuk = new Promise<>();
    private Promise<Boolean> pinCorrect = new Promise<>();
    private Promise<Boolean> canCorrect = new Promise<>();
    private Promise<Boolean> pukCorrect = new Promise<>();
    private Promise<PinStatus> pinStatus = new Promise<>();

    @Override
    public PinStatus getPinStatus() throws InterruptedException {
	try {
	    PinStatus status = pinStatus.deref();

	    // renew pin status promise
	    this.pinStatus = new Promise<>();

	    return status;
	} catch (InterruptedException ex) {
	    throw new InterruptedException("Waiting for PIN status interrupted.");
	}
    }

    @Override
    public boolean changePin(String oldPin, String newPin) throws InterruptedException {
	userPin.deliver(oldPin);
	userCan.deliver(newPin);

	// wait for the UI to set the value whether PIN is correct or not
	try {
	    return pinCorrect.deref();
	} catch (InterruptedException ex) {
	    throw new InterruptedException("Waiting for PIN result interrupted.");
	}
    }

    @Override
    public boolean enterCan(String can) throws InterruptedException {
	userCan.deliver(can);

	// wait for the UI to set the value whether CAN is correct or not
	try {
	    return canCorrect.deref();
	} catch (InterruptedException ex) {
	    throw new InterruptedException("Waiting for CAN result interrupted.");
	}
    }

    @Override
    public boolean unblockPin(String puk) throws InterruptedException {
	userPuk.deliver(puk);

	// wait for the UI to set the value whether PUK is correct or not
	try {
	    return pukCorrect.deref();
	} catch (InterruptedException ex) {
	    throw new InterruptedException("Waiting for PUK result interrupted.");
	}
    }

    @Override
    public void cancel() {
	if (! cancelPromise.isDelivered() && ! cancelPromise.isCancelled()) {
	    cancelPromise.deliver(Boolean.TRUE);
	    cancelPromise(userPin);
	    cancelPromise(userCan);
	    cancelPromise(pinCorrect);
	    cancelPromise(pinStatus);
	}
    }

    private void cancelPromise(@Nullable Promise<?> p) {
	if (p != null) {
	    p.cancel();
	}
    }

    ///
    /// Functions for the UserConsent interface implementation
    ///

    public boolean isCancelled() {
	return cancelPromise.isDelivered();
    }

    public void setPinCorrect(boolean isCorrect) {
	Promise<Boolean> pc = this.pinCorrect;

	this.pinCorrect = new Promise<>();
	this.userPin = new Promise<>();
	this.userCan = new Promise<>();
	this.newPin = new Promise<>();
	this.userPuk = new Promise<>();
	this.canCorrect = new Promise<>();
	this.pukCorrect = new Promise<>();

	pc.deliver(isCorrect);
    }

    public void setPukCorrect(boolean isCorrect) {
	Promise<Boolean> pc = this.pukCorrect;

	this.pukCorrect = new Promise<>();
	this.userPin = new Promise<>();
	this.userCan = new Promise<>();
	this.newPin = new Promise<>();
	this.userPuk = new Promise<>();
	this.canCorrect = new Promise<>();
	this.pukCorrect = new Promise<>();

	pc.deliver(isCorrect);
    }

    public void setCanCorrect(boolean isCorrect) {
	Promise<Boolean> pc = this.canCorrect;

	this.canCorrect = new Promise<>();
	this.userPin = new Promise<>();
	this.userCan = new Promise<>();
	this.newPin = new Promise<>();
	this.userPuk = new Promise<>();
	this.pukCorrect = new Promise<>();
	this.pukCorrect = new Promise<>();

	pc.deliver(isCorrect);
    }

    public void sendPinStatus(RecognizedState status) {
	if (status == RecognizedState.PIN_blocked) {
	    this.pinStatus.deliver(PinStatus.PIN_BLOCKED);
	} else if (status == RecognizedState.PIN_deactivated) {
	    this.pinStatus.deliver(PinStatus.DEACTIVATED);
	} else {
	    // break execution instantly
	    return;
	}
	// wait
	try {
	    cancelPromise.deref();
	} catch (InterruptedException ex) {
	    // I don't care
	}
    }

}
