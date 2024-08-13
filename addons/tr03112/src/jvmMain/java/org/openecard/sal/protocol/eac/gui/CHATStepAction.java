/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
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
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;

import java.util.*;

import org.openecard.addon.Context;
import org.openecard.binding.tctoken.TR03112Keys;
import org.openecard.common.DynamicContext;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.WSHelper;
import org.openecard.common.util.SysUtils;
import org.openecard.crypto.common.asn1.cvc.CHAT;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.BoxItem;
import org.openecard.gui.definition.Checkbox;
import org.openecard.gui.definition.Step;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepAction;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;
import org.openecard.ifd.protocol.pace.common.PasswordID;
import org.openecard.sal.protocol.eac.EACData;
import org.openecard.sal.protocol.eac.EACProtocol;
import org.openecard.sal.protocol.eac.anytype.PACEMarkerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * StepAction for evaluation of CHAT value items on the EAC GUI.
 *
 * @author Tobias Wich
 */
public class CHATStepAction extends StepAction {

    private static final Logger LOG = LoggerFactory.getLogger(CHATStepAction.class);

    static {
	I18n lang = I18n.getTranslation("pace");
	LANG = lang;
	PIN = lang.translationForKey("pin");
	PUK = lang.translationForKey("puk");
    }

    private static final I18n LANG;
    private static final String PIN;
    private static final String PUK;

    private final Context addonCtx;
    private final EACData eacData;

    public CHATStepAction(Context addonCtx, EACData eacData, Step step) {
	super(step);
	this.addonCtx = addonCtx;
	this.eacData = eacData;
    }


    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	if (result.isOK()) {
	    processResult(oldResults);

	    try {
		Step nextStep = preparePinStep();

		return new StepActionResult(StepActionResultStatus.NEXT, nextStep);
	    } catch (WSHelper.WSException ex) {
		LOG.error("Failed to prepare PIN step.", ex);
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    } catch (InterruptedException ex) {
		LOG.warn("CHAT step action interrupted.", ex);
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    }
	} else {
	    // cancel can not happen, so only back is left to be handled
	    return new StepActionResult(StepActionResultStatus.BACK);
	}
    }

    private Step preparePinStep() throws WSHelper.WSException, InterruptedException {
	DynamicContext ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);

	initContextVars(ctx);
	Step nextStep = buildPinStep(ctx);

	return nextStep;
    }

    private void initContextVars(DynamicContext ctx) throws WSHelper.WSException, InterruptedException {
	PinState status = (PinState) ctx.get(EACProtocol.PIN_STATUS);

	// only process once
	if (status == null) {
	    status = new PinState();
	    boolean nativePace;
	    ConnectionHandleType sessHandle = (ConnectionHandleType) ctx.get(TR03112Keys.SESSION_CON_HANDLE);
	    ConnectionHandleType cardHandle;
	    PACEMarkerType paceMarker;

	    PasswordID passwordType = PasswordID.parse(eacData.pinID);

	    PaceCardHelper ph = new PaceCardHelper(addonCtx, sessHandle);
	    if (! SysUtils.isMobileDevice()) {
		cardHandle = ph.connectCardIfNeeded(
			new HashSet<>() {{
				add(ECardConstants.NPA_CARD_TYPE);
			}}
		);
		if (passwordType == PasswordID.PIN) {
		    PacePinStatus pinState = ph.getPinStatus();
		    status.update(pinState);
		}
		nativePace = ph.isNativePinEntry();

		// get the PACEMarker
		paceMarker = ph.getPaceMarker(passwordType.name(), ECardConstants.NPA_CARD_TYPE);
	    } else {
		// mobile device, pick only available reader and proceed
		status.update(PacePinStatus.UNKNOWN);
		cardHandle = ph.getMobileReader();
		nativePace = false;
		paceMarker = ph.getPaceMarker(passwordType.name(), ECardConstants.NPA_CARD_TYPE);
	    }

	    // save values in dynctx
	    ctx.put(EACProtocol.PIN_STATUS, status);
	    ctx.put(EACProtocol.IS_NATIVE_PACE, nativePace);
	    ctx.put(TR03112Keys.CONNECTION_HANDLE, cardHandle);
	    ctx.put(EACProtocol.PACE_MARKER, paceMarker);
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

    private Step buildPinStep(DynamicContext ctx) {
	PinState status = (PinState) ctx.get(EACProtocol.PIN_STATUS);
	boolean nativePace = (boolean) ctx.get(EACProtocol.IS_NATIVE_PACE);
	PACEMarkerType paceMarker = (PACEMarkerType) ctx.get(EACProtocol.PACE_MARKER);
	assert(status != null);

	if (status.isBlocked()) {
	    return new ErrorStep(LANG.translationForKey("step_error_title_blocked", PIN),
		    LANG.translationForKey("step_error_pin_blocked", PIN, PIN, PUK, PIN),
		    WSHelper.createException(WSHelper.makeResultError(ECardConstants.Minor.IFD.PASSWORD_BLOCKED, "Password blocked.")));
	} else if (status.isDeactivated()) {
	    return new ErrorStep(LANG.translationForKey("step_error_title_deactivated"),
		    LANG.translationForKey("step_error_pin_deactivated"),
		    WSHelper.createException(WSHelper.makeResultError(ECardConstants.Minor.IFD.PASSWORD_SUSPENDED, "Card deactivated.")));
	} else {
	    PINStep pinStep = new PINStep(eacData, !nativePace, paceMarker);
	    StepAction pinAction;
	    if (eacData.pinID == PasswordID.CAN.getByte()) {
		pinAction = new CANStepAction(addonCtx, eacData, !nativePace, pinStep);
	    } else {
		pinAction = new PINStepAction(addonCtx, eacData, !nativePace, pinStep);
	    }
	    pinStep.setAction(pinAction);
	    return pinStep;
	}
    }

}
