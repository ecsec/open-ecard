/** **************************************************************************
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
 ************************************************************************** */
package org.openecard.mobile.activation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecard.common.util.Promise;
import org.openecard.mobile.activation.ConfirmAttributeSelectionOperation;
import org.openecard.mobile.activation.ConfirmPasswordOperation;
import org.openecard.mobile.activation.ConfirmTwoPasswordsOperation;
import org.openecard.mobile.activation.EacInteraction;
import org.openecard.mobile.activation.SelectableItem;
import org.openecard.mobile.activation.ServerData;
import static org.openecard.mobile.activation.model.Timeout.WAIT_TIMEOUT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Neil Crossley
 */
public class EacCallbackReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(World.class);

    private Promise<Void> promisedRequestCardInsertion;
    private Promise<Void> promisedRecognizeCard;
    private Promise<ConfirmPasswordOperation> promisedOperationPinRequest;
    private Promise<ConfirmTwoPasswordsOperation> promisedOperationPinCanRequest;
    private Promise<Void> promisedRemoveCard;
    private Promise<ServerData> promisedServerData;
    private Promise<String> promisedTransactionData;
    private Promise<ConfirmAttributeSelectionOperation> promisedOperationConfirmAttributes;
    public final EacInteraction interaction;
    private final World world;

    public EacCallbackReceiver(World world) {
	this.world = world;
	interaction = mock(EacInteraction.class);
	promisedRequestCardInsertion = new Promise();
	promisedRecognizeCard = new Promise();
	promisedRemoveCard = new Promise();
	promisedOperationPinCanRequest = new Promise();
	promisedOperationPinRequest = new Promise();
	promisedServerData = new Promise();
	promisedTransactionData = new Promise();
	promisedOperationConfirmAttributes = new Promise();

	doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
	    LOG.debug("mockInteraction.requestCardInsertion().");
	    if (promisedRequestCardInsertion.isDelivered()) {
		promisedRequestCardInsertion = new Promise();
	    }
	    promisedRequestCardInsertion.deliver(null);
	    return null;
	}).when(interaction).requestCardInsertion();
	doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
	    LOG.debug("mockInteraction.onCardRemoved().");
	    if (promisedRemoveCard.isDelivered()) {
		promisedRemoveCard = new Promise();
	    }
	    promisedRemoveCard.deliver(null);
	    return null;
	}).when(interaction).onCardRemoved();

	doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
	    LOG.debug("mockInteraction.onCardRecognized().");
	    if (promisedRecognizeCard.isDelivered()) {
		promisedRecognizeCard = new Promise();
	    }
	    promisedRecognizeCard.deliver(null);
	    return null;
	}).when(interaction).onCardRecognized();
	doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
	    LOG.debug("mockInteraction.onPinCanRequest().");
	    if (promisedOperationPinCanRequest.isDelivered()) {
		promisedOperationPinCanRequest = new Promise();
	    }
	    promisedOperationPinCanRequest.deliver((ConfirmTwoPasswordsOperation) arg0.getArguments()[0]);
	    return null;
	}).when(interaction).onPinCanRequest(any());
	doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
	    LOG.debug("mockInteraction.onPinRequest().");
	    if (promisedOperationPinRequest.isDelivered()) {
		promisedOperationPinRequest = new Promise();
	    }
	    final Object[] arguments = arg0.getArguments();

	    promisedOperationPinRequest.deliver((ConfirmPasswordOperation) arguments[1]);
	    return null;
	}).when(interaction).onPinRequest(anyInt(), any());


	doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
	    LOG.debug("mockInteraction.onServerData().");
	    if (promisedServerData.isDelivered()) {
		promisedServerData = new Promise();
	    }
	    if (promisedTransactionData.isDelivered()) {
		promisedTransactionData = new Promise();
	    }
	    if (promisedOperationConfirmAttributes.isDelivered()) {
		promisedOperationConfirmAttributes = new Promise();
	    }
	    Object[] arguments = arg0.getArguments();
	    promisedServerData.deliver((ServerData) arguments[0]);
	    promisedTransactionData.deliver((String) arguments[1]);
	    promisedOperationConfirmAttributes.deliver((ConfirmAttributeSelectionOperation) arguments[2]);
	    return null;
	}).when(interaction).onServerData(any(), anyString(), any());
    }

    public void expectCardInsertionRequest() {
	LOG.debug("Expect card insertion.");
	try {
	    promisedRequestCardInsertion.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
	} catch (InterruptedException | TimeoutException ex) {
	    throw new RuntimeException(ex);
	}
    }

    public void expectRecognitionOfNpaCard() {
	LOG.debug("Expect recognition of NPA card.");
	try {
	    promisedRecognizeCard.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
	} catch (InterruptedException | TimeoutException ex) {
	    throw new RuntimeException(ex);
	}
    }

    public void expectPinEntryWithSuccess(String currentPin) {
	try {
	    ConfirmPasswordOperation operation = promisedOperationPinRequest.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
	    if (operation == null) {
		throw new IllegalStateException();
	    }
	    operation.enter(currentPin);
	} catch (InterruptedException | TimeoutException ex) {
	    throw new RuntimeException(ex);
	}
    }

    public void expectRemovalOfCard() {
	LOG.debug("Expect removal of card.");
	try {
	    promisedRemoveCard.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
	} catch (InterruptedException | TimeoutException ex) {
	    throw new RuntimeException(ex);
	}
    }

    public void expectOnServerData() {
	try {
	    promisedServerData.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
	    promisedTransactionData.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
	    promisedOperationConfirmAttributes.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
	} catch (InterruptedException | TimeoutException ex) {
	    throw new RuntimeException(ex);
	}
    }

    public void givenConfirmationOfServerData() {
	LOG.debug("Confirming server data.");
	if (!promisedServerData.isCancelled() && !promisedOperationConfirmAttributes.isDelivered()) {
	    this.expectOnServerData();
	    world.microSleep();
	    world.microSleep();
	}
	try {
	    ServerData serverData = promisedServerData.deref();
	    ConfirmAttributeSelectionOperation confirmOperation = promisedOperationConfirmAttributes.deref();
	    List<SelectableItem> readAttributes = new ArrayList<>(serverData.getReadAccessAttributes());
	    List<SelectableItem> writeAttributes = new ArrayList<>(serverData.getWriteAccessAttributes());

	    for (SelectableItem readAttribute : readAttributes) {
		readAttribute.setChecked(true);
	    }
	    for (SelectableItem writeAttribute : writeAttributes) {
		writeAttribute.setChecked(true);
	    }
	    confirmOperation.enter(readAttributes, writeAttributes);
	} catch (InterruptedException ex) {
	    throw new RuntimeException(ex);
	}
    }
}
