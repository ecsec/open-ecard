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

package org.openecard.client.control.module.tctoken.gui;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.Map;
import java.util.Set;
import org.openecard.client.common.sal.state.CardStateEntry;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.gui.StepResult;
import org.openecard.client.gui.executor.ExecutionResults;
import org.openecard.client.gui.executor.StepAction;
import org.openecard.client.gui.executor.StepActionResult;
import org.openecard.client.gui.executor.StepActionResultStatus;


/**
 * Action to wait for a card insertion in the GUI executor.
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 *
 */
public class InsertCardStepAction extends StepAction {

    private ConnectionHandleType conHandle;
    private ConnectionHandleType response;
    private CardStateMap cardStates;

    /**
     * Create a new InsertCardStep Action.
     * @param cardStates cardStates to look for a matching ConnectionHandle
     * @param stepName name of the step
     * @param conHandle ConnectionHandle the inserted card must match
     */
    public InsertCardStepAction(CardStateMap cardStates, String stepName, ConnectionHandleType conHandle) {
	super(stepName);
	this.cardStates = cardStates;
	this.conHandle = conHandle;
    }

    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	Set<CardStateEntry> entries = cardStates.getMatchingEntries(conHandle);

	if (entries.size() > 0) 
	    setResponse(entries.iterator().next().handleCopy());
	return new StepActionResult(StepActionResultStatus.NEXT);
    }

    public ConnectionHandleType getResponse() {
	return response;
    }

    public void setResponse(ConnectionHandleType response) {
	this.response = response;
    }

}