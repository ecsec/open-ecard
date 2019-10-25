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
import org.openecard.mobile.activation.NFCOverlayMessageHandler;
import org.openecard.mobile.activation.common.anonymous.NFCOverlayMessageHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Neil Crossley
 */
public class CommonCardEventHandler {

    private static Logger LOG = LoggerFactory.getLogger(CommonCardEventHandler.class);

    private final ActivationInteraction interaction;
    private boolean cardPresent;
    private boolean cardRecognized;
    private NFCDialogMsgSetter msgSetter;

    public CommonCardEventHandler(ActivationInteraction interaction, boolean cardPresent, boolean cardRecognized, NFCDialogMsgSetter msgSetter) {
	this.interaction = interaction;
	this.cardPresent = cardPresent;
	this.msgSetter = msgSetter;
    }

    public void onCardInserted() {
	this.cardPresent = true;
    }

    public void onCardRemoved() {
	this.cardPresent = false;
	boolean wasRecognized = this.cardRecognized;
	this.cardRecognized = false;
	if (wasRecognized) {
	    this.interaction.onCardRemoved();
	}
    }

    public void onCardRecognized() {
	cardRecognized = true;

	if (msgSetter.isSupported()) {
	    interaction.onCardRecognized(new NFCOverlayMessageHandlerImpl(msgSetter));
	} else {
	    interaction.onCardRecognized();
	}
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
		    case RECOGNIZED_CARD_ACTIVE:
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
			    // remove handler when the correct card is present
			    eventDispatcher.del(this);

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
	eventDispatcher.add(cardDetectHandler, EventType.RECOGNIZED_CARD_ACTIVE);
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

    public static AutoCloseable create(Set<String> supportedCards, EventDispatcher eventDispatcher, ActivationInteraction interaction, NFCDialogMsgSetter msgSetter) {

	CommonCardEventHandler created = new CommonCardEventHandler(interaction, false, false, msgSetter);

	return hookUp(created, supportedCards, eventDispatcher, interaction, msgSetter);
    }


}
