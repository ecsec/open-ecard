/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.control.module.tctoken.gui;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.Map;
import java.util.Set;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.gui.StepResult;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepAction;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;


/**
 * Action to wait for a card insertion in the GUI executor.
 * The action contains getters for the obtained values.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class InsertCardStepAction extends StepAction {

    private String cardType;
    private ConnectionHandleType response;
    private CardStateMap cardStates;

    /**
     * Creates a new InsertCardStep Action.
     *
     * @param stepName The name of the step this action is run in.
     * @param cardStates The card states instance to look for a matching ConnectionHandle.
     * @param cardType Type URI of the card that must be inserted.
     */
    public InsertCardStepAction(String stepName, CardStateMap cardStates, String cardType) {
	super(stepName);
	this.cardStates = cardStates;
	this.cardType = cardType;
    }

    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	Set<CardStateEntry> entries;

	ConnectionHandleType conHandle = new ConnectionHandleType();
	ConnectionHandleType.RecognitionInfo recInfo = new ConnectionHandleType.RecognitionInfo();
	recInfo.setCardType(cardType);
	conHandle.setRecognitionInfo(recInfo);

	do {
	    // TODO: find a method without calling sleep, probably extend CardStateMap to wait for changes
	    entries = cardStates.getMatchingEntries(conHandle);
	    try {
		Thread.sleep(200);
	    } catch (InterruptedException e) {
		// action was cancelled by the user
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    }
	} while (entries.size() < 1);

	response = (entries.iterator().next().handleCopy());
	return new StepActionResult(StepActionResultStatus.NEXT);
    }

    /**
     * Gets the connection handle of the inserted card.
     *
     * @return The connection handle of the first card of the specified type.
     */
    public ConnectionHandleType getResponse() {
	return response;
    }

}
