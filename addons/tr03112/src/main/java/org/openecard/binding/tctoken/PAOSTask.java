/****************************************************************************
 * Copyright (C) 2013-2014 ecsec GmbH.
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

package org.openecard.binding.tctoken;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.Callable;
import org.openecard.common.ECardConstants;
import org.openecard.common.AppVersion;
import org.openecard.common.event.EventObject;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.common.interfaces.EventFilter;
import org.openecard.common.util.HandlerUtils;
import org.openecard.gui.UserConsent;
import org.openecard.sal.protocol.eac.gui.CardRemovedFilter;
import org.openecard.transport.paos.PAOS;
import org.openecard.transport.paos.PAOSConnectionException;
import org.openecard.transport.paos.PAOSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public class PAOSTask implements Callable<StartPAOSResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(PAOSTask.class);

    private final Dispatcher dispatcher;
    private final ConnectionHandleType connectionHandle;
    private final List<String> supportedDIDs;
    private final TCTokenRequest tokenRequest;
    private final UserConsent gui;
    private final EventDispatcher evManager;

    public PAOSTask(Dispatcher dispatcher, ConnectionHandleType connectionHandle, List<String> supportedDIDs,
	    TCTokenRequest tokenRequest, UserConsent gui, EventDispatcher evManager) {
	this.dispatcher = dispatcher;
	this.connectionHandle = connectionHandle;
	this.supportedDIDs = supportedDIDs;
	this.tokenRequest = tokenRequest;
	this.gui = gui;
	this.evManager = evManager;
    }


    @Override
    public StartPAOSResponse call()
	    throws MalformedURLException, PAOSException, DispatcherException, InvocationTargetException,
	    ConnectionError, PAOSConnectionException {
	// add event listener terminating the whole process in case the card is removed
	final Thread execThread = Thread.currentThread();
	EventCallback disconnectEventSink = new EventCallback() {
	    @Override
	    public void signalEvent(EventType eventType, EventObject eventData) {
		if (eventType == EventType.CARD_REMOVED) {
		    LOG.info("Card has been removed during authentication. Shutting down EAC process.");
		    execThread.interrupt();
		}
	    }
	};
	EventFilter evFilter = new CardRemovedFilter(connectionHandle.getIFDName(), connectionHandle.getSlotIndex());
	evManager.add(disconnectEventSink, evFilter);

	try {
	    TlsConnectionHandler tlsHandler = new TlsConnectionHandler(dispatcher, tokenRequest, connectionHandle);
	    tlsHandler.setUpClient();

	    // Set up PAOS connection
	    PAOS p = new PAOS(dispatcher, tlsHandler);

	    // Create StartPAOS message
	    StartPAOS sp = new StartPAOS();
	    sp.setProfile(ECardConstants.Profile.ECARD_1_1);
	    sp.getConnectionHandle().add(getHandleForServer());
	    sp.setSessionIdentifier(tlsHandler.getSessionId());

	    StartPAOS.UserAgent ua = new StartPAOS.UserAgent();
	    ua.setName(AppVersion.getName());
	    ua.setVersionMajor(BigInteger.valueOf(AppVersion.getMajor()));
	    ua.setVersionMinor(BigInteger.valueOf(AppVersion.getMinor()));
	    ua.setVersionSubminor(BigInteger.valueOf(AppVersion.getPatch()));
	    sp.setUserAgent(ua);

	    StartPAOS.SupportedAPIVersions sv = new StartPAOS.SupportedAPIVersions();
	    sv.setMajor(ECardConstants.ECARD_API_VERSION_MAJOR);
	    sv.setMinor(ECardConstants.ECARD_API_VERSION_MINOR);
	    sv.setSubminor(ECardConstants.ECARD_API_VERSION_SUBMINOR);
	    sp.getSupportedAPIVersions().add(sv);

	    sp.getSupportedDIDProtocols().addAll(supportedDIDs);
	    return p.sendStartPAOS(sp);
	} finally {
	    evManager.del(disconnectEventSink);
	    TCTokenHandler.disconnectHandle(dispatcher, connectionHandle);
	    TCTokenHandler.killUserConsent();
	}
    }

    private ConnectionHandleType getHandleForServer() {
	ConnectionHandleType result = HandlerUtils.copyHandle(connectionHandle);
	// this is our own extension and servers might not understand it
	result.setSlotInfo(null);
	return result;
    }

}
