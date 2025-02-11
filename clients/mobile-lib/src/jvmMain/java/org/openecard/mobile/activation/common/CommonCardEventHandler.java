/**
 * *************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 * <p>
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 * <p>
 * *************************************************************************
 */
package org.openecard.mobile.activation.common;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import org.openecard.common.event.EventObject;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.common.util.SysUtils;
import org.openecard.mobile.activation.ActivationInteraction;
import org.openecard.mobile.activation.common.anonymous.NFCOverlayMessageHandlerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 *
 * @author Neil Crossley
 */
public class CommonCardEventHandler {

	private static Logger LOG = LoggerFactory.getLogger(CommonCardEventHandler.class);

	private final ActivationInteraction interaction;
	private final Object recognizedLock = new Object();
	private volatile boolean cardRecognized;
	private final NFCDialogMsgSetter msgSetter;
	private final boolean isAndroid;

	public CommonCardEventHandler(ActivationInteraction interaction, boolean cardRecognized, NFCDialogMsgSetter msgSetter) {
		this.interaction = interaction;
		this.msgSetter = msgSetter;
		this.isAndroid = SysUtils.isAndroid();
	}

	public void onCardInserted() {
		interaction.onCardInserted();
	}

	public void onCardRemoved() {
		boolean wasRecognized;
		synchronized (recognizedLock) {
			wasRecognized = this.cardRecognized;
			this.cardRecognized = false;
		}
		if (wasRecognized) {
			this.interaction.onCardRemoved();
		} else {
			if (isAndroid) {
				requestCardInsertion();
			}
		}
	}

	private void requestCardInsertion() {
		if (msgSetter.isSupported()) {
			interaction.requestCardInsertion(new NFCOverlayMessageHandlerImpl(msgSetter));
		} else {
			interaction.requestCardInsertion();
		}
	}

	public void onCardRecognized() {
		boolean wasRecognized;
		synchronized (recognizedLock) {
			wasRecognized = this.cardRecognized;
			cardRecognized = true;
		}
		if (!wasRecognized) {
			interaction.onCardRecognized();
		}
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
						break;
					default:
						LOG.debug("Card presence handler received an unsupported event: {}", eventType.name());
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
						LOG.debug("Card recognition handler received an unsupported event: {}", eventType.name());
						break;
				}
			}
		};
		eventDispatcher.add(cardInsertionHandler, EventType.CARD_REMOVED, EventType.CARD_INSERTED);
		eventDispatcher.add(cardDetectHandler, EventType.CARD_RECOGNIZED);

		return new AutoCloseable() {
			@Override
			public void close() throws Exception {
				eventDispatcher.del(cardInsertionHandler);
				eventDispatcher.del(cardDetectHandler);
			}

		};
	}

}
