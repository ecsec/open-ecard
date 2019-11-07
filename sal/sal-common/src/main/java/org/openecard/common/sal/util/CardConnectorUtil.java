/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.common.sal.util;

import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.Arrays;
import java.util.Set;
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


/**
 *
 * @author Tobias Wich
 */
public class CardConnectorUtil {

    private final Set<String> cardTypes;
    private final String session;
    private final byte[] ctxHandle;
    private final String ifdName;

    private final Dispatcher dispatcher;
    private final EventDispatcher eventHandler;

    public CardConnectorUtil(Dispatcher dispatcher, EventDispatcher eventHandler, Set<String> cardTypes, String session, byte[] ctxHandle, String ifdName) {
	this.cardTypes = cardTypes;
	this.session = session;
	this.ctxHandle = ctxHandle;
	this.ifdName = ifdName;
	this.dispatcher = dispatcher;
	this.eventHandler = eventHandler;
    }


    public CardApplicationPathType waitForCard() throws InterruptedException {
	CardFound cb = new CardFound();
	eventHandler.add(cb, new TypeFilter());

	try {
	    // check if there is a card already present
	    for (String type : cardTypes) {
		CardApplicationPathType h = checkType();
		if (h != null) {
		    return h;
		}
	    }

	    ConnectionHandleType eventCardHandle = cb.foundCardHandle.deref();
	    return HandlerUtils.copyPath(eventCardHandle);
	} finally {
	    eventHandler.del(cb);
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
		if (WSHelper.resultIsOk(conRes)) {
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
		}
	    }
	}

	return null;
    }

    private class CardFound implements EventCallback {
	Promise<ConnectionHandleType> foundCardHandle;
	@Override
	public void signalEvent(EventType eventType, EventObject eventData) {
	    try {
		foundCardHandle.deliver(eventData.getHandle());
	    } catch (IllegalStateException ex) {
		// caused if callback is called multiple times, but this is fine
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
