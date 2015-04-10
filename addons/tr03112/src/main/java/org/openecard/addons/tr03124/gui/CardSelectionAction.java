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

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.List;
import java.util.Map;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.BoxItem;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.Radiobox;
import org.openecard.gui.definition.Step;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepAction;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;


/**
 * StepAction implementation which processes the results of a previous executed CardSelectionStep.
 *
 * @author Hans-Martin Haase
 */
public class CardSelectionAction extends StepAction {

    private final List<ConnectionHandleType> avCard;

    private String resulCardTypetName;

    /**
     * Creates a new CardSelectionAction from the given step and the card represented by a list of Connection Handles.
     *
     * @param step Step used to initialize the action.
     * @param availableCard List of {@link ConnectionHandleType} objects which represents the available credentials.
     */
    public CardSelectionAction(Step step, List<ConnectionHandleType> availableCard) {
	super(step);
	avCard = availableCard;
    }

    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	if (result.isOK()) {
	    ExecutionResults results = oldResults.get(getStepID());
	    OutputInfoUnit out = results.getResult("credentialSelectionBox");
	    Radiobox rBox = (Radiobox) out;

	    for (BoxItem item : rBox.getBoxItems()) {
		if (item.isChecked()) {
		    this.resulCardTypetName = item.getName();
		    break;
		}
	    }

	    if (resulCardTypetName != null) {
		return new StepActionResult(StepActionResultStatus.NEXT);
	    } else {
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    }
	} else {
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
	    if (handle.getRecognitionInfo().getCardType().equals(resulCardTypetName)) {
		return handle;
	    }
	}

	return null;
    }

}
