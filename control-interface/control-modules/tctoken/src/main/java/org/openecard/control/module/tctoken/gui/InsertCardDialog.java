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
import java.util.Set;
import org.openecard.common.I18n;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.executor.ExecutionEngine;
import org.openecard.gui.executor.StepAction;


/**
 * Implements a insert card dialog.
 * This dialog requests the user to insert a card of a specific type.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class InsertCardDialog {

    private static final String STEP_ID = "insert-card";

    private final I18n lang = I18n.getTranslation("tctoken");

    private final UserConsent gui;
    private final String cardType;
    private final String cardName;
    private CardStateMap cardStates;

    /**
     * Creates a new InsertCardDialog.
     *
     * @param gui The user consent implementation.
     * @param cardStates The card states instance managing all cards of this client.
     * @param cardType Type URI of the card that must be inserted.
     * @param cardName The localized name of the card type.
     */
    public InsertCardDialog(UserConsent gui, CardStateMap cardStates, String cardType, String cardName) {
	this.gui = gui;
	this.cardType = cardType;
	this.cardName = cardName;
	this.cardStates = cardStates;
    }

    /**
     * Shows this InsertCardDialog dialog.
     *
     * @return The ConnectionHandle of the inserted card or null if no card was inserted.
     */
    public ConnectionHandleType show() {
	ConnectionHandleType conHandle = new ConnectionHandleType();
	ConnectionHandleType.RecognitionInfo recInfo = new ConnectionHandleType.RecognitionInfo();
	recInfo.setCardType(cardType);
	conHandle.setRecognitionInfo(recInfo);
	Set<CardStateEntry> entries = cardStates.getMatchingEntries(conHandle);
	if (entries.size() == 1) {
	    return entries.iterator().next().handleCopy();
	} else {
	    InsertCardStepAction insertCardAction = new InsertCardStepAction(STEP_ID, cardStates, cardType);
	    UserConsentNavigator ucr = gui.obtainNavigator(createInsertCardUserConsent(insertCardAction));
	    ExecutionEngine exec = new ExecutionEngine(ucr);
	    // run gui
	    ResultStatus status = exec.process();

	    if (status == ResultStatus.CANCEL) {
		return null;
	    }
	    return insertCardAction.getResponse();
	}
    }

    private UserConsentDescription createInsertCardUserConsent(StepAction insertCardAction) {
	UserConsentDescription uc = new UserConsentDescription(lang.translationForKey("title"));

	// create step
	Step s = new Step(STEP_ID, lang.translationForKey("step.title"));
	s.setInstantReturn(true);
	s.setAction(insertCardAction);

	// create and add text instructing user
	Text i1 = new Text();
	i1.setText(lang.translationForKey("step.message", cardName));
	s.getInputInfoUnits().add(i1);

	// add step
	uc.getSteps().add(s);

	return uc;
    }

}
