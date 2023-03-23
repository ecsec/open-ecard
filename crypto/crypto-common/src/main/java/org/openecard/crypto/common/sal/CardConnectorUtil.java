/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.crypto.common.sal;

import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;

import java.util.*;

import org.openecard.common.ECardException;
import org.openecard.common.WSHelper;
import org.openecard.common.event.EventObject;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.common.interfaces.EventFilter;
import org.openecard.common.util.HandlerBuilder;
import org.openecard.common.util.HandlerUtils;
import org.openecard.common.util.Promise;
import org.openecard.common.util.SysUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class CardConnectorUtil {

    private static final Logger LOG = LoggerFactory.getLogger(CardConnectorUtil.class);
    private final Collection<String> cardTypes;
    private final String session;
    private final byte[] ctxHandle;
    private final String ifdName;

    private final Dispatcher dispatcher;
    private final EventDispatcher eventHandler;

    public CardConnectorUtil(Dispatcher dispatcher, EventDispatcher eventHandler, Collection<String> cardTypes, String session, byte[] ctxHandle, String ifdName) {
	this.cardTypes = cardTypes;
	this.session = session;
	this.ctxHandle = ctxHandle;
	this.ifdName = ifdName;
	this.dispatcher = dispatcher;
	this.eventHandler = eventHandler;
    }


    public CardApplicationPathType waitForCard() throws InterruptedException {
	Promise<ConnectionHandleType> foundCardHandle = new Promise();
	List<EventCallback> callbacks = new ArrayList<>(2);

	CardFound commonCallback = new CardFound(foundCardHandle);
	callbacks.add(commonCallback);
	eventHandler.add(commonCallback, new TypeFilter());

	if (SysUtils.isIOS()) {
	    CancelOnCardRemovedFilter cancelCallback = new CancelOnCardRemovedFilter(foundCardHandle);
	    callbacks.add(cancelCallback);
	    eventHandler.add(cancelCallback, new CardRemovalFilter());
	}

	try {
	    // check if there is a card already present
	    for (String type : cardTypes) {
		CardApplicationPathType h = checkType();
		if (h != null) {
		    return h;
		}
	    }

	    ConnectionHandleType eventCardHandle = foundCardHandle.deref();
	    return HandlerUtils.copyPath(eventCardHandle);
	} finally {
	    for (EventCallback callback : callbacks) {
		eventHandler.del(callback);
	    }
	}
    }

    private CardApplicationPathType checkType() {
	CardApplicationPath preq = new CardApplicationPath();
	CardApplicationPathType pt = HandlerBuilder.create().setSessionId(session)
		.setContextHandle(ctxHandle)
		.setIfdName(ifdName)
		.buildAppPath();
	preq.setCardAppPathRequest(pt);

	CardApplicationPathResponse res = (CardApplicationPathResponse) dispatcher.safeDeliver(preq);
	CardApplicationPathResponse.CardAppPathResultSet resSet = res.getCardAppPathResultSet();
	if (resSet != null && ! resSet.getCardApplicationPathResult().isEmpty()) {
	    for (CardApplicationPathType path : resSet.getCardApplicationPathResult()) {
		// connect card and check type
		CardApplicationConnect con = new CardApplicationConnect();
		con.setCardApplicationPath(path);
		CardApplicationConnectResponse conRes = (CardApplicationConnectResponse) dispatcher.safeDeliver(con);
		try {
		    WSHelper.checkResult(conRes);
		    ConnectionHandleType card = conRes.getConnectionHandle();
		    try {
			if (cardTypes.contains(card.getRecognitionInfo().getCardType())) {
			    return HandlerUtils.copyPath(card);
			}
		    } finally {
			CardApplicationDisconnect dis = new CardApplicationDisconnect();
			dis.setConnectionHandle(card);
			dispatcher.safeDeliver(dis);
		    }
		} catch(ECardException ex) {
		    LOG.warn("Error occurred while checking a card.", ex);
		}
	    }
	}

	return null;
    }

    private class CardFound implements EventCallback {
	private final Promise<ConnectionHandleType> foundCardHandle;

	public CardFound(Promise<ConnectionHandleType> foundCardHandle) {
	    this.foundCardHandle = foundCardHandle;
	}

	@Override
	public void signalEvent(EventType eventType, EventObject eventData) {
	    try {
		if (eventType == EventType.CARD_RECOGNIZED) {
		    foundCardHandle.deliver(eventData.getHandle());
		}
	    } catch (IllegalStateException ex) {
		// caused if callback is called multiple times, but this is fine
		LOG.warn("Card in an illegal state.", ex);
	    }
	}
    }

    private class TypeFilter implements EventFilter {

	@Override
	public boolean matches(EventType t, EventObject o) {
	    if (t == EventType.CARD_RECOGNIZED) {
		ConnectionHandleType h = o.getHandle();
		if (ctxHandle != null && ifdName != null) {
		    if (Arrays.equals(ctxHandle, h.getContextHandle()) && ifdName.equals(h.getIFDName())) {
			return cardTypes.contains(h.getRecognitionInfo().getCardType());
		    }
		} else {
		    return cardTypes.contains(h.getRecognitionInfo().getCardType());
		}
	    }
	    return false;
	}
    }
}
