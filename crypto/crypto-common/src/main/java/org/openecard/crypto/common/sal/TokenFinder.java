/****************************************************************************
 * Copyright (C) 2023 ecsec GmbH.
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


package org.openecard.crypto.common.sal;

import iso.std.iso_iec._24727.tech.schema.*;
import org.openecard.common.WSHelper;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.common.util.FuturePromise;
import org.openecard.common.util.Promise;

import java.util.List;
import java.util.concurrent.Callable;


/**
 * Utility to find cards of a specific type, or all.
 *
 * @author Tobias Wich
 */
public class TokenFinder {

    private final Dispatcher dispatcher;
    private final EventDispatcher eventHandler;
    private final byte[] contextHandle;
    private final List<String> cardTypes;

    private boolean cardsWokenUp = false;

    public TokenFinder(Dispatcher dispatcher, EventDispatcher eventHandler, byte[] contextHandle, List<String> cardTypes) {
	this.dispatcher = dispatcher;
	this.eventHandler = eventHandler;
	this.contextHandle = contextHandle;
	this.cardTypes = cardTypes;
    }

    /**
     * Sends a PrepareDevices call to the IFD identified by the context of this instance.
     */
    public void wakeCards() throws WSHelper.WSException {
	TokenFinder.wakeCards(dispatcher, contextHandle);
	cardsWokenUp = true;
    }

    /**
     * Sends a PrepareDevices call to the IFD identified by the given context.
     *
     * @param dispatcher
     * @param contextHandle
     * @throws WSHelper.WSException
     */
    public static void wakeCards(Dispatcher dispatcher, byte[] contextHandle) throws WSHelper.WSException {
	// signal cards to be activated
	PrepareDevices pdReq = new PrepareDevices();
	pdReq.setContextHandle(contextHandle);
	PrepareDevicesResponse response = (PrepareDevicesResponse) dispatcher.safeDeliver(pdReq);
	WSHelper.checkResult(response);
    }

    public TokenFinderWatcher startWatching() throws WSHelper.WSException {
	if (! cardsWokenUp) {
	    wakeCards();
	}

	return new TokenFinderWatcher();
    }

    public class TokenFinderWatcher implements AutoCloseable {

	public Promise<ConnectionHandleType> waitForNext() {
	    Promise<ConnectionHandleType> nextResult = new FuturePromise<>(new Callable<ConnectionHandleType>() {
		    @Override
		    public ConnectionHandleType call() throws Exception {
			CardConnectorUtil ccu = new CardConnectorUtil(dispatcher, eventHandler, cardTypes, null, contextHandle, null);
			CardApplicationPathType path = ccu.waitForCard();

			// connect card
			CardApplicationConnect cc = new CardApplicationConnect();
			cc.setCardApplicationPath(path);
			CardApplicationConnectResponse cr = (CardApplicationConnectResponse) dispatcher.safeDeliver(cc);
			WSHelper.checkResult(cr);

			return cr.getConnectionHandle();
		    }
		});

	    return nextResult;
	}

	public void releaseHandle(ConnectionHandleType handle) {

	}

	@Override
	public void close() {

	}
    }

}
