/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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

import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import java.util.Map;
import org.openecard.addon.Context;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.openecard.gui.StepResult;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;
import org.openecard.sal.protocol.eac.EACData;
import org.openecard.sal.protocol.eac.EACProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * StepAction for capturing the user PIN on the EAC GUI.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public class CANStepAction extends AbstractPasswordStepAction {

    private static final Logger LOG = LoggerFactory.getLogger(CANStepAction.class);

    // Translation constants
    private static final String ERROR_CARD_REMOVED = "action.error.card.removed";
    private static final String ERROR_TITLE = "action.error.title";
    private static final String ERROR_UNKNOWN = "action.error.unknown";

    private final I18n lang = I18n.getTranslation("pace");
    private final I18n langPin = I18n.getTranslation("pinplugin");

    public CANStepAction(Context addonCtx, EACData eacData, boolean capturePin, PINStep step) {
	super(addonCtx, eacData, capturePin, step);
    }

    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	try {
	    EstablishChannelResponse establishChannelResponse = performPACEWithPIN(oldResults);

	    if (establishChannelResponse.getResult().getResultMajor().equals(ECardConstants.Major.ERROR)) {
		if (establishChannelResponse.getResult().getResultMinor().equals(ECardConstants.Minor.IFD.AUTHENTICATION_FAILED)) {
		    // repeat the step
		    LOG.info("Wrong CAN entered, trying again.");
		    return new StepActionResult(StepActionResultStatus.REPEAT);
		} else {
		    WSHelper.checkResult(establishChannelResponse);
		}
	    }

	    eacData.paceResponse = establishChannelResponse;
	    // PACE completed successfully, proceed with next step
	    ctx.put(EACProtocol.PACE_EXCEPTION, null);
	    return new StepActionResult(StepActionResultStatus.NEXT);
	} catch (WSException ex) {
	    // This is for PIN Pad Readers in case the user pressed the cancel button on the reader.
	    if (ex.getResultMinor().equals(ECardConstants.Minor.IFD.CANCELLATION_BY_USER)) {
		LOG.error("User canceled the authentication manually.", ex);
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    }

	    // for people which think they have to remove the card in the process
	    if (ex.getResultMinor().equals(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE)) {
		LOG.error("The SlotHandle was invalid so probably the user removed the card or an reset occurred.", ex);
		return new StepActionResult(StepActionResultStatus.REPEAT,
			new ErrorStep(lang.translationForKey(ERROR_TITLE),
				langPin.translationForKey(ERROR_CARD_REMOVED), ex));
	    }

	    // repeat the step
	    LOG.error("An unknown error occured while trying to verify the PIN.");
	    return new StepActionResult(StepActionResultStatus.REPEAT,
		    new ErrorStep(langPin.translationForKey(ERROR_TITLE),
			    langPin.translationForKey(ERROR_UNKNOWN), ex));
	} catch (InterruptedException ex) {
	    LOG.warn("CAN step action interrupted.", ex);
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	} catch (PinOrCanEmptyException ex) {
	    LOG.warn("CAN was empty", ex);
	    return new StepActionResult(StepActionResultStatus.REPEAT);
	}
    }

}
