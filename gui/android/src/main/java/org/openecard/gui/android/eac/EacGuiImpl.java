/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

import android.os.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class EacGuiImpl extends EacGui.Stub {

    private static final Logger LOG = LoggerFactory.getLogger(EacGuiImpl.class);

    private final Promise<ServerData> serverData = new Promise<>();

    private final Promise<List<BoxItem>> userReadSelection = new Promise<>();
    private final Promise<List<BoxItem>> userWriteSelection = new Promise<>();

    private Promise<String> userPin = new Promise<>();
    private Promise<String> userCan = new Promise<>();
    private Promise<Boolean> pinCorrect = new Promise<>();
    private Promise<String> pinStatus = new Promise<>();

    private Checkbox readAccessBox;
    private Checkbox writeAccessBox;

    public EacGuiImpl() {
    }

    ///
    /// Functions for the visible UI
    ///

    @Override
    public ServerData getServerData() throws RemoteException {
	try {
	    return serverData.deref();
	} catch (InterruptedException ex) {
	    throw new RemoteException("Waiting for ServerData cancelled by thread termination.");
	}
    }

    @Override
    public void selectAttributes(List<BoxItem> readAccessAttr, List<BoxItem> writeAccessAttr)
	    throws RemoteException {
	try {
	    userReadSelection.deliver(readAccessAttr);
	    userWriteSelection.deliver(writeAccessAttr);
	} catch (IllegalStateException ex) {
	    LOG.warn("Multiple invocations of selectAttributes function. Ignoring this call.");
	}
    }

    @Override
    public String getPinStatus() throws RemoteException {
	try {
	    return pinStatus.deref();
	} catch (InterruptedException ex) {
	    throw new RemoteException("Waiting for PIN status interrupted.");
	}
    }

    @Override
    public boolean enterPin(String can, String pin) throws RemoteException {
	userPin.deliver(pin);
	userCan.deliver(can);

	// wait for the UI to set the value whether PIN is correct or not
	try {
	    return pinCorrect.deref();
	} catch (InterruptedException ex) {
	    throw new RemoteException("Waiting for PIN result interrupted.");
	}
    }

    @Override
    public void cancel() throws RemoteException {
	throw new UnsupportedOperationException("Not supported yet.");
    }


    ///
    /// Functions for the UserConsent interface implementation
    ///

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
	    if ("ReadAccess".equals(next.getID()) && next instanceof Checkbox) {
		Checkbox cb = (Checkbox) next;
		this.readAccessBox = cb;
		for (org.openecard.gui.definition.BoxItem nb : cb.getBoxItems()) {
		    BoxItem bi = new BoxItem(nb.getName(), nb.isChecked(), nb.isDisabled(), nb.getText());
		    readAccess.add(bi);
		}
	    } else if ("WriteAccess".equals(next.getID()) && next instanceof Checkbox) {
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

    public List<OutputInfoUnit> getPinResult(Step pinStep) throws InterruptedException {
	boolean hasPin = false;
	boolean hasCan = false;
	for (InputInfoUnit nextIn : pinStep.getInputInfoUnits()) {
	    if (nextIn.getID().equals("PACE_PIN_FIELD")) {
		hasPin = true;
	    } else if (nextIn.getID().equals("PACE_CAN_FIELD")) {
		hasCan = true;
	    }
	}
	// set flags according to field values
	if (hasPin && ! hasCan) {
	    this.pinStatus.deliver(PinStatus.PIN.name());
	} else if (hasPin && hasCan) {
	    this.pinStatus.deliver(PinStatus.CAN.name());
	} else {
	    this.pinStatus.deliver(PinStatus.BLOCKED.name());
	    // return directly as there will be no pin entry
	    return Collections.EMPTY_LIST;
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
	    } else if (canValue != null && nextIn instanceof PasswordField && nextIn.getID().equals("PACE_CAN_FIELD")) {
		PasswordField pw = new PasswordField(nextIn.getID());
		pw.copyContentFrom(nextIn);
		pw.setValue(canValue.toCharArray());
	    }
	}


	return result;
    }

    public void setPinCorrect(boolean isCorrect) {
	Promise<Boolean> pc = this.pinCorrect;

	this.pinCorrect = new Promise<>();
	this.userPin = new Promise<>();
	this.userCan = new Promise<>();

	pc.deliver(isCorrect);
    }

}
