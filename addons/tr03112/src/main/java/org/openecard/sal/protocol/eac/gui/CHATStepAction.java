/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.crypto.common.asn1.cvc.CHAT;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.BoxItem;
import org.openecard.gui.definition.Checkbox;
import org.openecard.gui.definition.Step;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepAction;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;
import org.openecard.sal.protocol.eac.EACData;
import org.openecard.sal.protocol.eac.EACProtocol;
import org.openecard.sal.protocol.eac.anytype.PACEMarkerType;


/**
 * StepAction for evaluation of CHAT value items on the EAC GUI.
 *
 * @author Tobias Wich
 */
public class CHATStepAction extends StepAction {

    private final EACData eacData;

    public CHATStepAction(EACData eacData, Step step) {
	super(step);
	this.eacData = eacData;
    }


    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	if (result.isOK()) {
	    processResult(oldResults);

	    DynamicContext ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	    boolean nativePace = (boolean) ctx.get(EACProtocol.IS_NATIVE_PACE);
	    PACEMarkerType paceMarker = (PACEMarkerType) ctx.get(EACProtocol.PACE_MARKER);
	    byte[] status = (byte[]) ctx.get(EACProtocol.PIN_STATUS_BYTES);
	    byte[] slotHandle = (byte[]) ctx.get(EACProtocol.SLOT_HANDLE);
	    Dispatcher dispatcher = (Dispatcher) ctx.get(EACProtocol.DISPATCHER);

	    PINStep pinStep = new PINStep(eacData, ! nativePace, paceMarker);
	    StepAction pinAction = new PINStepAction(eacData, ! nativePace, slotHandle, dispatcher, pinStep, status);
	    pinStep.setAction(pinAction);

	    return new StepActionResult(StepActionResultStatus.NEXT, pinStep);
	} else {
	    // cancel can not happen, so only back is left to be handled
	    return new StepActionResult(StepActionResultStatus.BACK);
	}
    }


    private void processResult(Map<String, ExecutionResults> results) {
	List<String> dataGroupsNames = getDataGroupNames();
	List<String> specialFunctionsNames = getSpecialFunctionNames();
	ExecutionResults executionResults = results.get(getStepID());

	Checkbox cb = (Checkbox) executionResults.getResult(CHATStep.CHAT_BOXES);
	CHAT selectedCHAT = eacData.requiredCHAT;
	for (BoxItem item : cb.getBoxItems()) {
	    if (dataGroupsNames.contains(item.getName())) {
		selectedCHAT.setReadAccess(item.getName(), item.isChecked());
	    } else if (specialFunctionsNames.contains(item.getName())) {
		selectedCHAT.setSpecialFunction(item.getName(), item.isChecked());
	    }
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
