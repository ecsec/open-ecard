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
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;

import java.util.*;

import org.openecard.addon.sal.SalStateView;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.common.util.Promise;
import org.openecard.common.util.SysUtils;
import org.openecard.crypto.common.sal.CancelOnCardRemovedFilter;
import org.openecard.crypto.common.sal.CardRemovalFilter;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.executor.ExecutionEngine;
import org.openecard.gui.executor.StepAction;
import org.openecard.i18n.I18N;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Implements a insert card dialog.
 * This dialog requests the user to insert a card of a specific type.
 *
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
public class InsertCardDialog {

    private static final Logger LOG = LoggerFactory.getLogger(InsertCardDialog.class);

    private static final String STEP_ID = "insert-card";

    private final UserConsent gui;
    private final Map<String, String> cardNameAndType;
    private final EventDispatcher evDispatcher;
    private final SalStateView salStateView;

    /**
     * Creates a new InsertCardDialog.
     *
     * @param gui The user consent implementation.
     * @param cardNameAndType Map containing the mapping of localized card names to card type URIs of cards which may be
     * inserted.
     * @param manager EventManager to register the EventCallbacks.
     * @param salStateView
     */
    public InsertCardDialog(UserConsent gui,
	    Map<String, String> cardNameAndType,
	    EventDispatcher manager,
	    SalStateView salStateView) {
	this.gui = gui;
	this.cardNameAndType = cardNameAndType;
	this.evDispatcher = manager;
	this.salStateView = salStateView;
    }

    /**
     * Shows this InsertCardDialog dialog.
     *
     * @return The ConnectionHandle of the inserted card or null if no card was inserted.
     */
    public List<ConnectionHandleType> show() {
	List<ConnectionHandleType> availableCards = checkAlreadyAvailable();
	if (! availableCards.isEmpty()) {
	    LOG.debug("Required card already available");
	    return availableCards;
	} else {
	    Promise<ConnectionHandleType> promise = new Promise<ConnectionHandleType>();
	    List<EventCallback> callbacks = new ArrayList<>(2);
	    InsertCardStepAction insertCardAction = new InsertCardStepAction(STEP_ID,
		    cardNameAndType.values(),
		    availableCards,
		    promise);
	    callbacks.add(insertCardAction);
	    evDispatcher.add(insertCardAction, EventType.CARD_RECOGNIZED);

	    if (SysUtils.isIOS()) {
		CancelOnCardRemovedFilter cancelCallback = new CancelOnCardRemovedFilter(promise);
		callbacks.add(cancelCallback);
		evDispatcher.add(cancelCallback, new CardRemovalFilter());
	    }

	    try {
		UserConsentNavigator ucr = gui.obtainNavigator(createInsertCardUserConsent(insertCardAction));
		ExecutionEngine exec = new ExecutionEngine(ucr);
		// run gui
		ResultStatus status = exec.process();

		if (status == ResultStatus.CANCEL) {
		    LOG.info("Waiting for cards dialog has been cancelled.");
		    return null;
		}
		return insertCardAction.getResponse();
	    } finally {
		for (EventCallback callback : callbacks) {
		    evDispatcher.del(callback);
		}
	    }
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

	Set<String> targetCardTypes = new HashSet<>(cardNameAndType.values());

	for (ConnectionHandleType currentHandle : this.salStateView.listCardHandles()) {
	    RecognitionInfo currentRecogInfo = currentHandle.getRecognitionInfo();

	    if (currentRecogInfo != null) {
		 String currentCardType = currentRecogInfo.getCardType();
		 if (currentCardType != null && targetCardTypes.contains(currentCardType)) {
		     handlesList.add(currentHandle);
		 }
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
	UserConsentDescription uc = new UserConsentDescription(
		I18N.strings.INSTANCE.getTctoken_title().localized(Locale.getDefault()),
		"insert_card_dialog");

	// create step
	Step s = new Step(STEP_ID, I18N.strings.INSTANCE.getTctoken_step_title().localized(Locale.getDefault()) );

	s.setInstantReturn(true);
	s.setAction(insertCardAction);

	// create and add text instructing user
	Text i1 = new Text();
	if (cardNameAndType.size() == 1) {
	    i1.setText(
			I18N.strings.INSTANCE.getTctoken_step_message_singletype().localized(Locale.getDefault())
		);
	} else {
	    i1.setText(
			I18N.strings.INSTANCE.getTctoken_step_message_multipletypes().localized(Locale.getDefault())
		);
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
