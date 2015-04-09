/****************************************************************************
 * Copyright (C) 2012-2015 HS Coburg.
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

package org.openecard.common.sal.util;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openecard.common.I18n;
import org.openecard.common.enums.EventType;
import org.openecard.common.interfaces.EventManager;
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
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
public class InsertCardDialog {

    private static final String STEP_ID = "insert-card";

    private final I18n lang = I18n.getTranslation("tctoken");

    private final UserConsent gui;
    private final Map<String, String> cardNameAndType;
    private final CardStateMap cardStates;
    private final EventManager manager;

    /**
     * Creates a new InsertCardDialog.
     *
     * @param gui The user consent implementation.
     * @param cardStates The card states instance managing all cards of this client.
     * @param cardNameAndType Map containing the mapping of localized card names to card type URIs of cards which may be
     * inserted.
     * @param manager EventManager to register the EventCallbacks.
     */
    public InsertCardDialog(UserConsent gui, CardStateMap cardStates, Map<String, String> cardNameAndType,
	    EventManager manager) {
	this.gui = gui;
	this.cardNameAndType = cardNameAndType;
	this.cardStates = cardStates;
	this.manager = manager;
    }

    /**
     * Shows this InsertCardDialog dialog.
     *
     * @return The ConnectionHandle of the inserted card or null if no card was inserted.
     */
    public List<ConnectionHandleType> show() {
	List<ConnectionHandleType> availableCards = checkAlreadyAvailable();
	if (! availableCards.isEmpty()) {
	    return availableCards;
	} else {
	    InsertCardStepAction insertCardAction = new InsertCardStepAction(STEP_ID, cardStates, 
		    cardNameAndType.values());
	    manager.register(insertCardAction, EventType.CARD_RECOGNIZED);
	    UserConsentNavigator ucr = gui.obtainNavigator(createInsertCardUserConsent(insertCardAction));
	    ExecutionEngine exec = new ExecutionEngine(ucr);
	    // run gui
	    ResultStatus status = exec.process();

	    if (status == ResultStatus.CANCEL) {
		return null;
	    }
	    manager.unregister(insertCardAction);
	    return insertCardAction.getResponse();
	}
    }

    /**
     * Check whether there are already cards with the required type(s) are available.
     *
     * @return A list containing {@link ConnectionHandleType} objects referencing the cards which fit the requirements or
     * an empty list if there are no cards which met the requirements are present.
     */
    private List<ConnectionHandleType> checkAlreadyAvailable() {
	List<ConnectionHandleType> handlesList = new ArrayList<>();
	for (String type : cardNameAndType.values()) {
	    ConnectionHandleType conHandle = new ConnectionHandleType();
	    ConnectionHandleType.RecognitionInfo recInfo = new ConnectionHandleType.RecognitionInfo();
	    recInfo.setCardType(type);
	    conHandle.setRecognitionInfo(recInfo);
	    Set<CardStateEntry> entries = cardStates.getMatchingEntries(conHandle);
	    if (! entries.isEmpty()) {
		handlesList.add(entries.iterator().next().handleCopy());
	    }
	}

	return handlesList;
    }

    /**
     * Create the UI dialog.
     *
     * @param insertCardAction A action for this step.
     * @return A {@link UserConsentDescription} which may be executed by the {@link ExecutionEngine}.
     */
    private UserConsentDescription createInsertCardUserConsent(StepAction insertCardAction) {
	UserConsentDescription uc = new UserConsentDescription(lang.translationForKey("title"));

	// create step
	Step s = new Step(STEP_ID, lang.translationForKey("step.title"));
	s.setInstantReturn(true);
	s.setAction(insertCardAction);

	// create and add text instructing user
	Text i1 = new Text();
	if (cardNameAndType.size() == 1) {
	    i1.setText(lang.translationForKey("step.message.singletype"));
	} else {
	    i1.setText(lang.translationForKey("step.message.multipletypes"));
	}

	s.getInputInfoUnits().add(i1);
	for (String key : cardNameAndType.keySet()) {
	    Text item = new Text(" - " + key);
	    s.getInputInfoUnits().add(item);
	}

	// add step
	uc.getSteps().add(s);

	return uc;
    }

}
