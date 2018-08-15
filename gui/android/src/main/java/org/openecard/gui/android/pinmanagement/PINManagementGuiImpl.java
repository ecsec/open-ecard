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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.openecard.common.util.Promise;
import org.openecard.gui.definition.InputInfoUnit;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.plugins.pinplugin.RecognizedState;
import org.openecard.plugins.pinplugin.gui.GenericPINStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Sebastian Schuberth
 * @author Tobias Wich
 */
public class PINManagementGuiImpl implements PINManagementGui {

    private static final Logger LOG = LoggerFactory.getLogger(PINManagementGuiImpl.class);

    private final Promise<Boolean> cancelPromise = new Promise<>();

    private Promise<String> userPinOld = new Promise<>();
    private Promise<String> userPinNew = new Promise<>();
    private Promise<String> userCan = new Promise<>();
    private Promise<String> userPuk = new Promise<>();
    private Promise<Boolean> pinCorrect = new Promise<>();
    private Promise<Boolean> canCorrect = new Promise<>();
    private Promise<Boolean> pukCorrect = new Promise<>();
    private Promise<PinStatus> pinStatus = new Promise<>();

    @Override
    public String getProtocolType() {
	return "urn:oid:1.3.162.15480.3.0.9";
    }


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
	userPinOld.deliver(oldPin);
	userPinNew.deliver(newPin);

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
	    cancelPromise(userPinOld);
	    cancelPromise(userPinNew);
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
	this.userPinOld = new Promise<>();
	this.userPinNew = new Promise<>();
	this.userCan = new Promise<>();
	this.userPuk = new Promise<>();
	this.canCorrect = new Promise<>();
	this.pukCorrect = new Promise<>();

	pc.deliver(isCorrect);
    }

    public void setCanCorrect(boolean isCorrect) {
	Promise<Boolean> pc = this.canCorrect;

	this.pinCorrect = new Promise<>();
	this.userPinOld = new Promise<>();
	this.userPinNew = new Promise<>();
	this.userCan = new Promise<>();
	this.userPuk = new Promise<>();
	this.canCorrect = new Promise<>();
	this.pukCorrect = new Promise<>();

	pc.deliver(isCorrect);
    }

    public void setPukCorrect(boolean isCorrect) {
	Promise<Boolean> pc = this.pukCorrect;

	this.pinCorrect = new Promise<>();
	this.userPinOld = new Promise<>();
	this.userPinNew = new Promise<>();
	this.userCan = new Promise<>();
	this.userPuk = new Promise<>();
	this.canCorrect = new Promise<>();
	this.pukCorrect = new Promise<>();

	pc.deliver(isCorrect);
    }

    public List<OutputInfoUnit> getPinResult(Step step) throws InterruptedException {
	// read values
	String oldPinValue = this.userPinOld.deref();
	String newPinValue = this.userPinNew.deref();
	String canValue = this.userCan.deref();
	String pukValue = this.userPuk.deref();

	if (step instanceof GenericPINStep) {
	    ArrayList<OutputInfoUnit> result = new ArrayList<>();
	    for (InputInfoUnit nextIn : step.getInputInfoUnits()) {
		if (oldPinValue != null && nextIn instanceof PasswordField && nextIn.getID().equals("OLD_PIN_FIELD")) {
		    PasswordField pw = new PasswordField(nextIn.getID());
		    pw.copyContentFrom(nextIn);
		    pw.setValue(oldPinValue.toCharArray());
		    result.add(pw);
		} else if (newPinValue != null && nextIn instanceof PasswordField && nextIn.getID().equals("NEW_PIN_FIELD")) {
		    PasswordField pw = new PasswordField(nextIn.getID());
		    pw.copyContentFrom(nextIn);
		    pw.setValue(newPinValue.toCharArray());
		    result.add(pw);
		} else if (newPinValue != null && nextIn instanceof PasswordField && nextIn.getID().equals("NEW_PIN_REPEAT_FIELD")) {
		    PasswordField pw = new PasswordField(nextIn.getID());
		    pw.copyContentFrom(nextIn);
		    pw.setValue(newPinValue.toCharArray());
		    result.add(pw);
		} else if (canValue != null && nextIn instanceof PasswordField && nextIn.getID().equals("CAN_FIELD")) {
		    PasswordField pw = new PasswordField(nextIn.getID());
		    pw.copyContentFrom(nextIn);
		    pw.setValue(canValue.toCharArray());
		    result.add(pw);
		} else if (pukValue != null && nextIn instanceof PasswordField && nextIn.getID().equals("PUK_FIELD")) {
		    PasswordField pw = new PasswordField(nextIn.getID());
		    pw.copyContentFrom(nextIn);
		    pw.setValue(pukValue.toCharArray());
		    result.add(pw);
		}
	    }

	    return result;
	} else {
	    throw new InterruptedException("The given step is not a PinStep.");
	}
    }

    public void sendPinStatus(RecognizedState status) {
	switch (status) {
	    case PIN_activated_RC3:
		this.pinStatus.deliver(PinStatus.RC3);
		break;
	    case PIN_activated_RC2:
		this.pinStatus.deliver(PinStatus.RC2);
		break;
	    case PIN_suspended:
		this.pinStatus.deliver(PinStatus.CAN);
		break;
	    case PIN_resumed:
		this.pinStatus.deliver(PinStatus.RC1);
		break;
	    case PIN_blocked:
		this.pinStatus.deliver(PinStatus.PIN_BLOCKED);
		break;
	    case PUK_blocked:
		this.pinStatus.deliver(PinStatus.PUK_BLOCKED);
		break;
	    case PIN_deactivated:
		this.pinStatus.deliver(PinStatus.DEACTIVATED);
		break;
	    default:
		throw new IllegalArgumentException("Unhandled PIN status received from UI.");
	}
    }

    public void waitForUserCancel() {
	// wait
	try {
	    cancelPromise.deref();
	} catch (InterruptedException ex) {
	    // I don't care
	}
    }

}
