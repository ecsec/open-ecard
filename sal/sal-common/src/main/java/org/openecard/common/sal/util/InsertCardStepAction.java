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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.event.EventObject;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.common.util.Promise;
import org.openecard.gui.StepResult;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepAction;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;


/**
 * Action to wait for a card insertion in the GUI executor.
 * The action contains getters for the obtained values.
 *
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
public class InsertCardStepAction extends StepAction implements EventCallback {

    private final Collection<String> cardTypes;
    private final List<ConnectionHandleType> response = new ArrayList<>();
    private final CardStateMap cardStates;
    private final List<ConnectionHandleType> insertableCards;

    private final Promise<ConnectionHandleType> promise = new Promise<>();

    /**
     * Creates a new InsertCardStep Action.
     *
     * @param stepName The name of the step this action is run in.
     * @param cardStates The card states instance to look for a matching ConnectionHandle.
     * @param cardTypes Collection of valid card types.
     */
    public InsertCardStepAction(String stepName, CardStateMap cardStates, Collection<String> cardTypes) {
	super(stepName);
	this.cardStates = cardStates;
	this.cardTypes = cardTypes;
	insertableCards = createHandleList();
    }

    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	List<ConnectionHandleType> availableCards = new ArrayList<>();
	// create session for wait for change

	availableCards.addAll(checkAvailability());
	while (availableCards.isEmpty()) {
	    try {
		availableCards.add(promise.deref());
	    } catch (InterruptedException ex) {
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    }
	}

	response.addAll(availableCards);
	return new StepActionResult(StepActionResultStatus.NEXT);
    }

    /**
     * Gets the connection handle of the inserted card.
     *
     * @return The connection handle of the first card of the specified type.
     */
    public List<ConnectionHandleType> getResponse() {
	return response;
    }

    /**
     * Creates a list of {@link ConnectionHandleType} objects created from the card types.
     *
     * @return List {@link ConnectionHandleType} objects created from the card types.
     */
    @Nonnull
    private List<ConnectionHandleType> createHandleList() {
	List<ConnectionHandleType> handles = new ArrayList<>();

	for (String type : cardTypes) {
	    ConnectionHandleType conHandle = new ConnectionHandleType();
	    ConnectionHandleType.RecognitionInfo recInfo = new ConnectionHandleType.RecognitionInfo();
	    recInfo.setCardType(type);
	    conHandle.setRecognitionInfo(recInfo);
	    handles.add(conHandle);
	}

	return handles;
    }

    /**
     * Checks whether a card according to the input list is connected.
     *
     * @param insertableCards List of {@link ConnectionHandleType} object which identify card which may be present in the
     * cardStateMap.
     * @return A list with matching entries or an empty list.
     */
    @Nonnull
    private List<ConnectionHandleType> checkAvailability() {
	List<ConnectionHandleType> available = new ArrayList<>();
	for (ConnectionHandleType conHandle : insertableCards) {
	    Set<CardStateEntry> entries = cardStates.getMatchingEntries(conHandle);
	    if (! entries.isEmpty()) {
		available.add(entries.iterator().next().handleCopy());
	    }
	}

	return available;
    }

    @Override
    public void signalEvent(EventType eventType, EventObject eventData) {
	if (eventType == EventType.RECOGNIZED_CARD_ACTIVE && ! promise.isDelivered()) {
	    ConnectionHandleType handle = eventData.getHandle();
	    for (String cardType : cardTypes) {
		if (cardType.equals(handle.getRecognitionInfo().getCardType())) {
		    promise.deliver(handle);
		    break;
		}
	    }
	}
    }

}
