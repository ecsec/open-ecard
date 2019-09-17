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
import java.util.AbstractMap;
import java.util.Map;
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
public class CardEventHandler {

    private static Logger LOG = LoggerFactory.getLogger(CardEventHandler.class);

    private final ActivationInteraction interaction;
    private boolean cardPresent;
    private boolean cardRecognized;

    public CardEventHandler(ActivationInteraction interaction, boolean cardPresent, boolean cardRecognized) {
	this.interaction = interaction;
	this.cardPresent = cardPresent;
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

    public void onCardRecognized(String type) {
	cardRecognized = true;
	interaction.onCardRecognized(type);
    }

    public static Map.Entry<CardEventHandler, AutoCloseable> create(Set<String> supportedCards, EventDispatcher eventDispatcher, ActivationInteraction interaction) {

	CardEventHandler created = new CardEventHandler(interaction, false, false);

	EventCallback cardInsertionHandler = new EventCallback() {
	    @Override
	    public void signalEvent(EventType eventType, EventObject eventData) {
		switch (eventType) {
		    case CARD_REMOVED:
			created.onCardRemoved();
			break;
		    case CARD_INSERTED:
			created.onCardInserted();
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
			ConnectionHandleType handle = eventData.getHandle();
			final String type = handle.getRecognitionInfo().getCardType();

			if (supportedCards == null || supportedCards.isEmpty() || supportedCards.contains(type)) {
			    // remove handler when the correct card is present
			    eventDispatcher.del(this);

			    created.onCardRecognized(type);
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
		created.onCardRemoved();
	    }
	};

	eventDispatcher.add(cardInsertionHandler, EventType.CARD_REMOVED, EventType.CARD_INSERTED);
	eventDispatcher.add(cardDetectHandler, EventType.RECOGNIZED_CARD_ACTIVE);
	eventDispatcher.add(removalHandler, EventType.CARD_REMOVED);

	return new AbstractMap.SimpleImmutableEntry<>(created, null);
    }
}
