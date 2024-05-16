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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.mockito.Mockito.doAnswer;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecard.common.util.Promise;
import org.openecard.mobile.activation.ActivationInteraction;
import org.openecard.mobile.activation.NFCOverlayMessageHandler;
import static org.openecard.mobile.activation.model.Timeout.WAIT_TIMEOUT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Neil Crossley
 */
public class ActivationCallbackReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(World.class);

    private Promise<NFCOverlayMessageHandler> promisedRequestCardInsertion;
    private Promise<Void> promisedRecognizeCard;
    private Promise<Void> promisedRemoveCard;
    public final ActivationInteraction interaction;
    private final World world;

    public ActivationCallbackReceiver(World world, ActivationInteraction interaction) {
	this.interaction = interaction;
	this.world = world;

	promisedRequestCardInsertion = new Promise();
	promisedRecognizeCard = new Promise();
	promisedRemoveCard = new Promise();

	doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
	    LOG.debug("mockInteraction.requestCardInsertion().");
	    if (promisedRequestCardInsertion.isDelivered()) {
		promisedRequestCardInsertion = new Promise();
	    }
	    promisedRequestCardInsertion.deliver(null);
	    return null;
	}).when(interaction).requestCardInsertion();
	doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
	    LOG.debug("mockInteraction.requestCardInsertion(NFCOverlayMessageHandler).");
	    if (promisedRequestCardInsertion.isDelivered()) {
		promisedRequestCardInsertion = new Promise();
	    }
	    promisedRequestCardInsertion.deliver(null);
	    return null;
	}).when(interaction).requestCardInsertion(null);
	doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
	    LOG.debug("mockInteraction.onCardRecognized().");
	    if (promisedRecognizeCard.isDelivered()) {
		promisedRecognizeCard = new Promise();
	    }
	    promisedRecognizeCard.deliver(null);
	    return null;
	}).when(interaction).onCardRecognized();
	doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
	    LOG.debug("mockInteraction.onCardRemoved().");
	    if (promisedRemoveCard.isDelivered()) {
		promisedRemoveCard = new Promise();
	    }
	    promisedRemoveCard.deliver(null);
	    return null;
	}).when(interaction).onCardRemoved();
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

    public void expectRemovalOfCard() {
	LOG.debug("Expect removal of card.");
	try {
	    promisedRemoveCard.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
	} catch (InterruptedException | TimeoutException ex) {
	    throw new RuntimeException(ex);
	}
    }

}
