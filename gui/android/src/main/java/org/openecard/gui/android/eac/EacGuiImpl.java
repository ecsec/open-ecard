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

package org.openecard.gui.android.eac;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;
import org.openecard.binding.tctoken.TR03112Keys;
import org.openecard.common.DynamicContext;
import org.openecard.common.util.Promise;
import org.openecard.gui.android.eac.types.BoxItem;
import org.openecard.gui.android.eac.types.PinStatus;
import org.openecard.gui.android.eac.types.ServerData;
import org.openecard.gui.android.eac.types.TermsOfUsage;
import org.openecard.gui.definition.Checkbox;
import org.openecard.gui.definition.Document;
import org.openecard.gui.definition.InputInfoUnit;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.ToggleText;
import org.openecard.sal.protocol.eac.EACData;
import org.openecard.sal.protocol.eac.EACProtocol;
import org.openecard.sal.protocol.eac.gui.EacPinStatus;
import org.openecard.sal.protocol.eac.gui.PINStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class EacGuiImpl implements EacGui {

    private static final Logger LOG = LoggerFactory.getLogger(EacGuiImpl.class);

    private EacNavigator eacNav;

    private final Promise<Boolean> cancelPromise = new Promise<>();

    private final Promise<ServerData> serverData = new Promise<>();
    private final Promise<String> transactionInfo = new Promise<>();

    private final Promise<List<BoxItem>> userReadSelection = new Promise<>();
    private final Promise<List<BoxItem>> userWriteSelection = new Promise<>();

    private Promise<String> userPin = new Promise<>();
    private Promise<String> userCan = new Promise<>();
    private Promise<Boolean> pinCorrect = new Promise<>();
    private Promise<PinStatus> pinStatus = new Promise<>();

    private Checkbox readAccessBox;
    private Checkbox writeAccessBox;


    public EacGuiImpl() {
    }

    @Override
    public String getProtocolType() {
	// generic EAC2
	return "urn:oid:1.3.162.15480.3.0.14";
    }

    ///
    /// Functions for the visible UI
    ///

    @Override
    public ServerData getServerData() throws InterruptedException {
	try {
	    return serverData.deref();
	} catch (InterruptedException ex) {
	    throw new InterruptedException("Waiting for ServerData cancelled by thread termination.");
	}
    }

    @Override
    public String getTransactionInfo() throws InterruptedException {
	try {
	    return transactionInfo.deref();
	} catch (InterruptedException ex) {
	    throw new InterruptedException("Waiting for TransactionInfo cancelled by thread termination.");
	}
    }

    @Override
    public void selectAttributes(List<BoxItem> readAccessAttr, List<BoxItem> writeAccessAttr) {
	try {
	    userReadSelection.deliver(readAccessAttr);
	    userWriteSelection.deliver(writeAccessAttr);
	} catch (IllegalStateException ex) {
	    LOG.warn("Multiple invocations of selectAttributes function. Ignoring this call.");
	}
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
    public boolean enterPin(String can, String pin) throws InterruptedException {
	userPin.deliver(pin);
	userCan.deliver(can);

	// wait for the UI to set the value whether PIN is correct or not
	try {
	    return pinCorrect.deref();
	} catch (InterruptedException ex) {
	    throw new InterruptedException("Waiting for PIN result interrupted.");
	}
    }

    @Override
    public void cancel() {
	LOG.debug("Cancel of Android EAC GUI called.", new Exception("Print Stacktrace"));
	if (! cancelPromise.isDelivered() && ! cancelPromise.isCancelled()) {
	    cancelPromise.deliver(Boolean.FALSE);
	    cancelPromise(serverData);
	    cancelPromise(transactionInfo);
	    cancelPromise(userReadSelection);
	    cancelPromise(userWriteSelection);
	    cancelPromise(userPin);
	    cancelPromise(userCan);
	    cancelPromise(pinCorrect);
	    cancelPromise(pinStatus);

	    if (eacNav != null) {
		eacNav.cancel();
	    }
	}
    }

    @Override
    public boolean isDone() {
	return cancelPromise.isDelivered();
    }

    @Override
    public void waitForDone(long timeout) throws InterruptedException, TimeoutException {
	cancelPromise.deref(timeout, TimeUnit.MILLISECONDS);
    }

    private void cancelPromise(@Nullable Promise<?> p) {
	if (p != null) {
	    p.cancel();
	}
    }


    ///
    /// Functions for the UserConsent interface implementation
    ///

    public void setEacNav(EacNavigator eacNav) {
	this.eacNav = eacNav;
    }

    public boolean isCancelled() {
	Boolean v = cancelPromise.derefNonblocking();
	return v != null && v == false;
    }

    public void loadValuesFromSteps(Step step1, Step step2) {
	String subject = "", subjectUrl = "";
	TermsOfUsage termsOfUsage = new TermsOfUsage("text/plain", new byte[0]);
	String validity = "";
	String issuer = "", issuerUrl = "";

	for (InputInfoUnit next : step1.getInputInfoUnits()) {
	    if ("SubjectName".equals(next.getID()) && next instanceof ToggleText) {
		ToggleText tt = (ToggleText) next;
		subject = tt.getText();
	    } else if ("SubjectURL".equals(next.getID()) && next instanceof ToggleText) {
		ToggleText tt = (ToggleText) next;
		subjectUrl = tt.getText();
	    } else if ("TermsOfUsage".equals(next.getID()) && next instanceof ToggleText) {
		ToggleText tt = (ToggleText) next;
		Document d = tt.getDocument();
		termsOfUsage = new TermsOfUsage(d.getMimeType(), d.getValue());
	    } else if ("Validity".equals(next.getID()) && next instanceof ToggleText) {
		ToggleText tt = (ToggleText) next;
		validity = tt.getText();
	    } else if ("IssuerName".equals(next.getID()) && next instanceof ToggleText) {
		ToggleText tt = (ToggleText) next;
		issuer = tt.getText();
	    } else if ("IssuerURL".equals(next.getID()) && next instanceof ToggleText) {
		ToggleText tt = (ToggleText) next;
		issuerUrl = tt.getText();
	    }
	}

	ArrayList<BoxItem> readAccess = new ArrayList<>();
	ArrayList<BoxItem> writeAccess = new ArrayList<>();

	for (InputInfoUnit next : step2.getInputInfoUnits()) {
	    if ("ReadCHATCheckBoxes".equals(next.getID()) && next instanceof Checkbox) {
		Checkbox cb = (Checkbox) next;
		this.readAccessBox = cb;
		for (org.openecard.gui.definition.BoxItem nb : cb.getBoxItems()) {
		    BoxItem bi = new BoxItem(nb.getName(), nb.isChecked(), nb.isDisabled(), nb.getText());
		    readAccess.add(bi);
		}
	    } else if ("WriteCHATCheckBoxes".equals(next.getID()) && next instanceof Checkbox) {
		Checkbox cb = (Checkbox) next;
		this.writeAccessBox = cb;
		for (org.openecard.gui.definition.BoxItem nb : cb.getBoxItems()) {
		    BoxItem bi = new BoxItem(nb.getName(), nb.isChecked(), nb.isDisabled(), nb.getText());
		    writeAccess.add(bi);
		}
	    }
	}

	ServerData sd = new ServerData(subject, subjectUrl, termsOfUsage, validity, issuer, issuerUrl, readAccess, writeAccess);
	serverData.deliver(sd);

	DynamicContext ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	EACData eacData = (EACData) ctx.get(EACProtocol.EAC_DATA);
	String tInfo = null;
	if (eacData != null) {
	    tInfo = eacData.transactionInfo;
	}
	transactionInfo.deliver(tInfo);
    }

    public List<OutputInfoUnit> getSelection() throws InterruptedException {
	List<OutputInfoUnit> outInfos = new ArrayList<>();

	List<BoxItem> itemsRead = this.userReadSelection.deref();
	List<BoxItem> itemsWrite = this.userWriteSelection.deref();

	copyBox(outInfos, readAccessBox, itemsRead);
	copyBox(outInfos, writeAccessBox, itemsWrite);

	return outInfos;
    }

    private void copyBox(List<OutputInfoUnit> outInfos, Checkbox oldBox, List<BoxItem> items) {
	if (oldBox != null) {
	    // create copy of the checkbox
	    Checkbox newBox = new Checkbox(oldBox.getID());
	    newBox.copyContentFrom(oldBox);

	    // copy changed values
	    for (org.openecard.gui.definition.BoxItem next : newBox.getBoxItems()) {
		String name = next.getName();
		BoxItem receivedItem = getItem(name, items);
		if (receivedItem != null) {
		    next.setChecked(receivedItem.isSelected());
		}
	    }

	    outInfos.add(newBox);
	}
    }

    @Nullable
    private BoxItem getItem(String name, List<BoxItem> items) {
	for (BoxItem next : items) {
	    if (name.equals(next.getName())) {
		return next;
	    }
	}
	return null;
    }

    public List<OutputInfoUnit> getPinResult(Step step) throws InterruptedException {
	if (step instanceof PINStep) {
	    PINStep pinStep = (PINStep) step;

	    switch (pinStep.getStatus()) {
		case RC3:
		    this.pinStatus.deliver(PinStatus.RC3);
		    break;
		case RC2:
		    this.pinStatus.deliver(PinStatus.RC2);
		    break;
		case RC1:
		    this.pinStatus.deliver(PinStatus.CAN);
		    break;
	    }

	    // read values
	    String pinValue = this.userPin.deref();
	    String canValue = this.userCan.deref();

	    ArrayList<OutputInfoUnit> result = new ArrayList<>();
	    for (InputInfoUnit nextIn : pinStep.getInputInfoUnits()) {
		if (pinValue != null && nextIn instanceof PasswordField && nextIn.getID().equals("PACE_PIN_FIELD")) {
		    PasswordField pw = new PasswordField(nextIn.getID());
		    pw.copyContentFrom(nextIn);
		    pw.setValue(pinValue.toCharArray());
		    result.add(pw);
		} else if (canValue != null && nextIn instanceof PasswordField && nextIn.getID().equals("PACE_CAN_FIELD")) {
		    PasswordField pw = new PasswordField(nextIn.getID());
		    pw.copyContentFrom(nextIn);
		    pw.setValue(canValue.toCharArray());
		    result.add(pw);
		}
	    }

	    return result;
	} else {
	    throw new InterruptedException("The given step is not a PinStep.");
	}
    }

    public void setPinCorrect(boolean isCorrect) {
	Promise<Boolean> pc = this.pinCorrect;

	this.pinCorrect = new Promise<>();
	this.userPin = new Promise<>();
	this.userCan = new Promise<>();

	pc.deliver(isCorrect);
    }

    public void sendPinStatus(EacPinStatus status) {
	if (status == EacPinStatus.BLOCKED) {
	    this.pinStatus.deliver(PinStatus.BLOCKED);
	} else if (status == EacPinStatus.DEACTIVATED) {
	    this.pinStatus.deliver(PinStatus.DEACTIVATED);
	} else {
	    // break execution instantly
	    return;
	}
	// wait
	try {
	    LOG.debug("Waiting for call of the cancel function.");
	    cancelPromise.deref();
	} catch (InterruptedException ex) {
	    // I don't care
	}
    }

    public void setIsDone() {
	if (! cancelPromise.isDelivered()) {
	    this.cancelPromise.deliver(Boolean.TRUE);
	}
    }

}
