/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.mobile.activation.common;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.Set;
import org.openecard.common.event.EventObject;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.mobile.activation.ActivationInteraction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Neil Crossley
 */
public class CommonCardEventHandler {

    private static Logger LOG = LoggerFactory.getLogger(CommonCardEventHandler.class);

    private final ActivationInteraction interaction;
    private boolean cardRecognized;
    private final NFCDialogMsgSetter msgSetter;

    public CommonCardEventHandler(ActivationInteraction interaction, boolean cardRecognized, NFCDialogMsgSetter msgSetter) {
	this.interaction = interaction;
	this.msgSetter = msgSetter;
    }

    public void onCardInserted() {
    }

    public void onCardRemoved() {
	boolean wasRecognized = this.cardRecognized;
	this.cardRecognized = false;
	if (wasRecognized) {
	    this.interaction.onCardRemoved();
	}
    }

    public void onCardRecognized() {
	cardRecognized = true;
	interaction.onCardRecognized();
    }

    public void onCardInteractionComplete() {
	interaction.onCardInteractionComplete();
    }

    public static AutoCloseable hookUp(CommonCardEventHandler handler, Set<String> supportedCards, EventDispatcher eventDispatcher, ActivationInteraction interaction, NFCDialogMsgSetter msgSetter) {

	EventCallback cardInsertionHandler = new EventCallback() {
	    @Override
	    public void signalEvent(EventType eventType, EventObject eventData) {
		switch (eventType) {
		    case CARD_REMOVED:
			handler.onCardRemoved();
			break;
		    case CARD_INSERTED:
			handler.onCardInserted();
		    default:
			LOG.debug("Received an unsupported Event: " + eventType.name());
			break;
		}
	    }
	};

	EventCallback cardDetectHandler = new EventCallback() {
	    @Override
	    public void signalEvent(EventType eventType, EventObject eventData) {
		switch (eventType) {
		    case CARD_RECOGNIZED:
			final ConnectionHandleType handle = eventData.getHandle();
			if (handle == null) {
			    break;
			}
			final ConnectionHandleType.RecognitionInfo recognitionInfo = handle.getRecognitionInfo();
			if (recognitionInfo == null) {
			    break;
			}
			final String type = recognitionInfo.getCardType();

			if (supportedCards == null || supportedCards.isEmpty() || supportedCards.contains(type)) {

			    handler.onCardRecognized();
			}

			break;
		    default:
			LOG.debug("Received an unsupported Event: " + eventType.name());
			break;
		}
	    }
	};
	EventCallback removalHandler = new EventCallback() {
	    @Override
	    public void signalEvent(EventType eventType, EventObject eventData) {
		handler.onCardRemoved();
	    }
	};
	eventDispatcher.add(cardInsertionHandler, EventType.CARD_REMOVED, EventType.CARD_INSERTED);
	eventDispatcher.add(cardDetectHandler, EventType.CARD_RECOGNIZED);
	eventDispatcher.add(removalHandler, EventType.CARD_REMOVED);

	return new AutoCloseable() {
	    @Override
	    public void close() throws Exception {
		eventDispatcher.del(cardInsertionHandler);
		eventDispatcher.del(cardDetectHandler);
		eventDispatcher.del(removalHandler);
	    }

	};
    }

}
