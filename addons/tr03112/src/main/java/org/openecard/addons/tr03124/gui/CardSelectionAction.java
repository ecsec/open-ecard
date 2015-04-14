/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

package org.openecard.addons.tr03124.gui;

import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import org.openecard.addon.Context;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.BoxItem;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.Radiobox;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepAction;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * StepAction implementation which processes the results of a previous executed CardSelectionStep.
 *
 * @author Hans-Martin Haase
 */
public class CardSelectionAction extends StepAction {

    private final static Logger logger = LoggerFactory.getLogger(CardSelectionAction.class);

    private final List<ConnectionHandleType> avCard;
    private final List<String> types;
    private final CardSelectionStep step;
    private final Context ctx;

    private String resultCardTypetName;

    /**
     * Creates a new CardSelectionAction from the given step and the card represented by a list of Connection Handles.
     *
     * @param step Step used to initialize the action.
     * @param availableCard List of {@link ConnectionHandleType} objects which represents the available credentials.
     * @param types Types which may be used for the authentication.
     * @param ctx Addon context used to communicate with the core.
     */
    public CardSelectionAction(CardSelectionStep step, List<ConnectionHandleType> availableCard, List<String> types,
	    Context ctx) {
	super(step);
	avCard = availableCard;
	this.types = types;
	this.step = step;
	this.ctx = ctx;
    }

    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	if (result.isOK()) {
	    ExecutionResults results = oldResults.get(getStepID());
	    OutputInfoUnit out = results.getResult("credentialSelectionBox");
	    Radiobox rBox = (Radiobox) out;

	    for (BoxItem item : rBox.getBoxItems()) {
		if (item.isChecked()) {
		    this.resultCardTypetName = item.getName();
		    break;
		}
	    }

	    if (resultCardTypetName != null) {
		return new StepActionResult(StepActionResultStatus.NEXT);
	    } else {
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    }
	} else {
	    // user has added or removed a card
	    if (result.isReload()) {
		updateCards();
		step.update(avCard);
		if (avCard.isEmpty()) {
		    return new StepActionResult(StepActionResultStatus.CANCEL);
		}
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    }
	    // user has canceled the dialog so return that
	    return new StepActionResult(StepActionResultStatus.CANCEL);
	}
    }

    /**
     * Get the ConnectionHandleTyp object of the chosen credential.
     *
     * @return The {@link ConnectionHandleType} corresponding to the selected credential or {@code NULL} if the user
     * canceled the dialog.
     */
    public ConnectionHandleType getResult() {
	for (ConnectionHandleType handle : avCard) {
	    if (handle.getRecognitionInfo().getCardType().equals(resultCardTypetName)) {
		return handle;
	    }
	}

	return null;
    }

    /**
     * Update the list of available and fitting cards.
     */
    private void updateCards() {
	try {
	    avCard.clear();
	    CardApplicationPath cap = new CardApplicationPath();
	    cap.setCardAppPathRequest(new CardApplicationPathType());
	    CardApplicationPathResponse resp = (CardApplicationPathResponse) ctx.getDispatcher().deliver(cap);
	    List<CardApplicationPathType> cards = resp.getCardAppPathResultSet().getCardApplicationPathResult();
	    for (CardApplicationPathType path : cards) {
		CardApplicationConnect connect = new CardApplicationConnect();
		connect.setCardApplicationPath(path);
		CardApplicationConnectResponse conResp = (CardApplicationConnectResponse) ctx.getDispatcher().deliver(connect);
		if (types.contains(conResp.getConnectionHandle().getRecognitionInfo().getCardType())) {
		    avCard.add(conResp.getConnectionHandle());
		} else {
		    CardApplicationDisconnect disconnect = new CardApplicationDisconnect();
		    disconnect.setConnectionHandle(conResp.getConnectionHandle());
		    ctx.getDispatcher().deliver(disconnect);
		}
	    }
	} catch (DispatcherException | InvocationTargetException ex) {
	    logger.error("Failed to get currently connected cards.", ex);
	}
    }

}
