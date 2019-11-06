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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.openecard.binding.tctoken.TR03112Keys;
import org.openecard.common.DynamicContext;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.WSHelper;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.crypto.common.asn1.cvc.CHAT;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.BoxItem;
import org.openecard.gui.definition.Checkbox;
import org.openecard.gui.definition.Step;
import org.openecard.gui.executor.BackgroundTask;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepAction;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;
import org.openecard.sal.protocol.eac.EACData;
import org.openecard.sal.protocol.eac.EACProtocol;
import org.openecard.sal.protocol.eac.anytype.PACEMarkerType;
import org.openecard.sal.protocol.eac.anytype.PasswordID;


/**
 * StepAction for evaluation of CHAT value items on the EAC GUI.
 *
 * @author Tobias Wich
 */
public class CHATStepAction extends StepAction {

    static {
	I18n lang = I18n.getTranslation("pace");
	LANG = lang;
	PIN = lang.translationForKey("pin");
	PUK = lang.translationForKey("puk");
    }

    private static final I18n LANG;
    private static final String PIN;
    private static final String PUK;

    private final EACData eacData;
    private final BackgroundTask bTask;

    public CHATStepAction(EACData eacData, Step step) {
	super(step);
	this.eacData = eacData;
	this.bTask = step.getBackgroundTask();
    }


    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	if (result.isOK()) {
	    processResult(oldResults);

	    DynamicContext ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	    boolean nativePace = (boolean) ctx.get(EACProtocol.IS_NATIVE_PACE);
	    PACEMarkerType paceMarker = (PACEMarkerType) ctx.get(EACProtocol.PACE_MARKER);
	    PinState status = (PinState) ctx.get(EACProtocol.PIN_STATUS);
	    byte[] slotHandle = (byte[]) ctx.get(EACProtocol.SLOT_HANDLE);
	    Dispatcher dispatcher = (Dispatcher) ctx.get(EACProtocol.DISPATCHER);

	    Step nextStep;
	    assert(status != null);
	    if (status.isBlocked()) {
		nextStep = new ErrorStep(LANG.translationForKey("step_error_title_blocked", PIN),
			LANG.translationForKey("step_error_pin_blocked", PIN, PIN, PUK, PIN),
			WSHelper.createException(WSHelper.makeResultError(ECardConstants.Minor.IFD.PASSWORD_BLOCKED, "Password blocked.")));
	    } else if (status.isDeactivated()) {
		nextStep = new ErrorStep(LANG.translationForKey("step_error_title_deactivated"),
			LANG.translationForKey("step_error_pin_deactivated"),
			WSHelper.createException(WSHelper.makeResultError(ECardConstants.Minor.IFD.PASSWORD_SUSPENDED, "Card deactivated.")));
	    } else {
		PINStep pinStep = new PINStep(eacData, !nativePace, paceMarker, status);
		nextStep = pinStep;
		pinStep.setBackgroundTask(bTask);
		StepAction pinAction;
		if (eacData.pinID == PasswordID.CAN.getByte()) {
		    pinStep.setStatus(EacPinStatus.RC3);
		    pinAction = new CANStepAction(eacData, !nativePace, slotHandle, dispatcher, pinStep);
		} else {
		    pinAction = new PINStepAction(eacData, !nativePace, slotHandle, dispatcher, pinStep, status);
		}
		pinStep.setAction(pinAction);
	    }

	    return new StepActionResult(StepActionResultStatus.NEXT, nextStep);
	} else {
	    // cancel can not happen, so only back is left to be handled
	    return new StepActionResult(StepActionResultStatus.BACK);
	}
    }


    private void processResult(Map<String, ExecutionResults> results) {
	List<String> dataGroupsNames = getDataGroupNames();
	List<String> specialFunctionsNames = getSpecialFunctionNames();
	ExecutionResults executionResults = results.get(getStepID());

	// process read access and special functions
	Checkbox cbRead = (Checkbox) executionResults.getResult(CHATStep.READ_CHAT_BOXES);
	if (cbRead != null) {
	    CHAT selectedCHAT = eacData.selectedCHAT;
	    for (BoxItem item : cbRead.getBoxItems()) {
		if (dataGroupsNames.contains(item.getName())) {
		    selectedCHAT.setReadAccess(item.getName(), item.isChecked());
		} else if (specialFunctionsNames.contains(item.getName())) {
		    selectedCHAT.setSpecialFunction(item.getName(), item.isChecked());
		}
	    }
	}

	// process write access
	Checkbox cbWrite = (Checkbox) executionResults.getResult(CHATStep.WRITE_CHAT_BOXES);
	if (cbWrite != null) {
	    CHAT selectedCHAT = eacData.selectedCHAT;
	    for (BoxItem item : cbWrite.getBoxItems()) {
		if (dataGroupsNames.contains(item.getName())) {
		    selectedCHAT.setWriteAccess(item.getName(), item.isChecked());
		}
	    }
	}

	// change PIN ID to CAN if CAN ALLOWED is used
	if (eacData.selectedCHAT.getSpecialFunctions().getOrDefault(CHAT.SpecialFunction.CAN_ALLOWED, Boolean.FALSE)) {
	    eacData.pinID = PasswordID.CAN.getByte();
	    eacData.passwordType = PasswordID.parse(eacData.pinID).getString();
	}
    }

    /**
     * Returns a list containing the names of all special functions.
     * @return list containing the names of all special functions.
     */
    private List<String> getSpecialFunctionNames() {
	List<String> specialFunctionNames = new ArrayList<>();
	for (CHAT.SpecialFunction dg : CHAT.SpecialFunction.values()) {
	    specialFunctionNames.add(dg.name());
	}
	return specialFunctionNames;
    }

    /**
     * Returns a list containing the names of all data groups.
     * @return list containing the names of all data groups.
     */
    private List<String> getDataGroupNames() {
	List<String> dataGroupNames = new ArrayList<>();
	for (CHAT.DataGroup dg : CHAT.DataGroup.values()) {
	    dataGroupNames.add(dg.name());
	}
	return dataGroupNames;
    }

}
