/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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

package org.openecard.sal.protocol.eac.gui;

import org.openecard.common.ifd.PacePinStatus;
import java.util.List;
import org.openecard.binding.tctoken.TR03112Keys;
import org.openecard.common.DynamicContext;
import org.openecard.common.I18n;
import org.openecard.gui.definition.InputInfoUnit;
import org.openecard.ifd.protocol.pace.common.PasswordID;
import org.openecard.sal.protocol.eac.anytype.PACEMarkerType;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
import org.openecard.sal.protocol.eac.EACData;
import org.openecard.sal.protocol.eac.EACProtocol;


/**
 * PIN GUI step for EAC.
 * This GUI step behaves differently
 *
 * @author Tobias Wich
 * @author Moritz Horsch
 */
public final class PINStep extends Step {

    private static final I18n LANG_EAC = I18n.getTranslation("eac");
    private static final I18n LANG_PACE = I18n.getTranslation("pace");
    // step id
    public static final String STEP_ID = "PROTOCOL_EAC_GUI_STEP_PIN";
    // GUI translation constants
    private static final String TITLE = "step_pace_title";
    private static final String STEP_DESCRIPTION = "step_pace_step_description";
    private static final String DESCRIPTION = "step_pace_description";
    private static final String DESCRIPTION_NATIVE = "step_pace_native_description";
    private static final String NOTICE = "eac_forward_notice";
    private static final String TRANSACTION_INFO = "transaction_info";
    // GUI element IDs
    public static final String PIN_FIELD = "PACE_PIN_FIELD";
    public static final String CAN_FIELD = "PACE_CAN_FIELD";

    private static final String CAN_NOTICE_ID = "PACE_CAN_NOTICE";
    private static final String PIN_ATTEMPTS_ID = "PACE_PIN_ATTEMPTS";

    private final String pinType;
    private final PACEMarkerType paceMarker;
    private final boolean hasAttemptsCounter;
    private final boolean capturePin;

    private PinState status;
    private boolean hasCanEntry = false;

    public PINStep(EACData eacData, boolean capturePin, PACEMarkerType paceMarker) {
	super(STEP_ID, "Dummy-Title");
	this.pinType = LANG_PACE.translationForKey(PasswordID.parse(eacData.pinID).name());
	this.paceMarker = paceMarker;
	this.hasAttemptsCounter = eacData.pinID != PasswordID.CAN.getByte();
	this.capturePin = capturePin;
	setTitle(LANG_PACE.translationForKey(TITLE, pinType));
	setDescription(LANG_PACE.translationForKey(STEP_DESCRIPTION));
	setReversible(false);

	// TransactionInfo
	String transactionInfo = eacData.transactionInfo;
	if (transactionInfo != null) {
	    Text transactionInfoField = new Text();
	    transactionInfoField.setText(LANG_EAC.translationForKey(TRANSACTION_INFO, transactionInfo));
	    getInputInfoUnits().add(transactionInfoField);
	}

	DynamicContext ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	this.status = (PinState) ctx.get(EACProtocol.PIN_STATUS);;
	// create step elements
	if (capturePin) {
	    addSoftwareElements();
	} else {
	    addTerminalElements();
	}
	updateCanData();
	updateAttemptsDisplay();
    }

    public void setStatus(PacePinStatus status) {
	this.status.update(status);
	updateAttemptsDisplay();
	updateCanData();
    }

    public void setStatus(PinState status) {
	this.status = status;
	updateAttemptsDisplay();
	updateCanData();
    }

    public static Step createDummy(byte pinId) {
	Step s = new Step(STEP_ID);
	String pinType = LANG_PACE.translationForKey(PasswordID.parse(pinId).name());
	s.setTitle(LANG_PACE.translationForKey(TITLE, pinType));
	s.setDescription(LANG_PACE.translationForKey(STEP_DESCRIPTION));
	return s;
    }

    private void addSoftwareElements() {
	setResetOnLoad(true);
	Text description = new Text();
	description.setText(LANG_PACE.translationForKey(DESCRIPTION, pinType));
	getInputInfoUnits().add(description);

	PasswordField pinInputField = new PasswordField(PIN_FIELD);
	pinInputField.setDescription(pinType);
	pinInputField.setMinLength(paceMarker.getMinLength());
	pinInputField.setMaxLength(paceMarker.getMaxLength());
	getInputInfoUnits().add(pinInputField);

	if (hasAttemptsCounter) {
	    Text attemptCount = new Text();
	    attemptCount.setText(LANG_PACE.translationForKey("step_pin_retrycount", 3));
	    attemptCount.setID(PIN_ATTEMPTS_ID);
	    getInputInfoUnits().add(attemptCount);
	}

	Text notice = new Text();
	notice.setText(LANG_EAC.translationForKey(NOTICE, pinType));
	getInputInfoUnits().add(notice);
    }

    private void addTerminalElements() {
	setInstantReturn(true);
	Text description = new Text();
	description.setText(LANG_PACE.translationForKey(DESCRIPTION_NATIVE, pinType));
	getInputInfoUnits().add(description);

	Text notice = new Text();
	notice.setText(LANG_EAC.translationForKey(NOTICE, pinType));
	getInputInfoUnits().add(notice);

	if (hasAttemptsCounter) {
	    Text attemptCount = new Text();
	    attemptCount.setText(LANG_PACE.translationForKey("step_pin_retrycount", 3));
	    attemptCount.setID(PIN_ATTEMPTS_ID);
	    getInputInfoUnits().add(attemptCount);
	}
    }

    private void addCANEntry() {
	final List<InputInfoUnit> inputInfoUnits = getInputInfoUnits();
	boolean hasCanField = false;
	boolean hasCanNotice = false;
	for (InputInfoUnit inputInfoUnit : inputInfoUnits) {
	    if (CAN_FIELD.equals(inputInfoUnit.getID())) {
		hasCanField = true;
	    }
	    if (CAN_NOTICE_ID.equals(inputInfoUnit.getID())) {
		hasCanNotice = true;
	    }
	}
	if (!hasCanField) {
	    PasswordField canField = new PasswordField(CAN_FIELD);
	    canField.setDescription(LANG_PACE.translationForKey("can"));
	    canField.setMaxLength(6);
	    canField.setMinLength(6);
	    inputInfoUnits.add(canField);
	}
	if (!hasCanNotice) {
	    Text canNotice = new Text();
	    canNotice.setText(LANG_EAC.translationForKey("eac_can_notice"));
	    canNotice.setID(CAN_NOTICE_ID);
	    inputInfoUnits.add(canNotice);
	}
    }

    private void addNativeCANNotice() {
	Text canNotice = new Text();
	canNotice.setText(LANG_EAC.translationForKey("eac_can_notice_native"));
	canNotice.setID(CAN_NOTICE_ID);
	getInputInfoUnits().add(canNotice);
    }

    private void updateCanData() {
	if (! hasCanEntry && status.isRequestCan()) {
	    ensureCanData();
	}
    }

    public void ensureCanData() {
	if (capturePin) {
	    addCANEntry();
	} else {
	    addNativeCANNotice();
	}
    }

    private void updateAttemptsDisplay() {
	for (InputInfoUnit unit : getInputInfoUnits()) {
	    if (unit.getID().equals(PIN_ATTEMPTS_ID)) {
		int newValue;
		switch (status.getState()) {
		    case RC3:
			newValue = 3;
			break;
		    case RC2:
			newValue = 2;
			break;
		    case RC1:
			newValue = 1;
			break;
		    default:
			newValue = 0;
		}

		Text text = (Text) unit;
		text.setText(LANG_PACE.translationForKey("step_pin_retrycount", newValue));
	    }
	}
    }

}
