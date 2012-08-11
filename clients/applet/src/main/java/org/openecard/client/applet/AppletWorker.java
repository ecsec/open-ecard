/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.applet;

import iso.std.iso_iec._24727.tech.schema.*;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.EventCallback;
import org.openecard.client.common.sal.state.CardStateEntry;
import org.openecard.client.connector.client.ClientRequest;
import org.openecard.client.connector.client.ClientResponse;
import org.openecard.client.connector.client.ConnectorListener;
import org.openecard.client.connector.handler.tctoken.TCToken;
import org.openecard.client.connector.handler.tctoken.TCTokenRequest;
import org.openecard.client.connector.handler.tctoken.TCTokenResponse;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.transport.paos.PAOS;
import org.openecard.client.transport.tls.PSKTlsClientImpl;
import org.openecard.client.transport.tls.TlsClientSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class AppletWorker implements Runnable, EventCallback, ConnectorListener {

    private static final Logger logger = LoggerFactory.getLogger(AppletWorker.class);
    private final Thread thread;
    private ECardApplet applet;
    private String selection;
    private boolean eventOccurred;

    public AppletWorker(ECardApplet applet) {
	this.applet = applet;
	eventOccurred = false;
	thread = new Thread(this);
    }

    public synchronized void startPAOS(String ifdName) {
	selection = ifdName;
	notifyAll();
    }

    public void start() {
	thread.start();
    }

    public void interrupt() {
	thread.interrupt();
    }

    @Override
    public void run() {
	List<ConnectionHandleType> cHandles = null;

	if (applet.getSpBehavior().equals(ECardApplet.INSTANT)) {
	    cHandles = getConnectionHandles();
	}

	if (applet.getSpBehavior().equals(ECardApplet.WAIT)) {
	    if (!eventOccurred) {
		waitForInput();
	    }
	    cHandles = getConnectionHandles();
	}

	if (applet.getSpBehavior().equals(ECardApplet.CLICK)) {
	    waitForInput();
	    if (selection != null) {
		cHandles = new ArrayList<ConnectionHandleType>(1);
		ConnectionHandleType cHandle = getConnectionHandle(selection);
		// need a slothandle
		Connect c = new Connect();
		c.setContextHandle(cHandle.getContextHandle());
		c.setExclusive(false);
		c.setIFDName(selection);
		c.setSlot(BigInteger.ZERO);
		ConnectResponse cr = this.applet.getClientEnvironment().getIFD().connect(c);
		cHandle.setSlotHandle(cr.getSlotHandle());
		//doesn't work with mtg testserver !? so remove
		cHandle.setRecognitionInfo(null);
		cHandle.setChannelHandle(null);
		cHandles.add(cHandle);
	    } else {
		cHandles = getConnectionHandles();
	    }
	}

	StartPAOS sp = new StartPAOS();
	sp.getConnectionHandle().addAll(cHandles);
	sp.setSessionIdentifier(applet.getSessionID());

	try {
	    // Object result = applet.getPAOS().sendStartPAOS(sp);

	    /*
	     * String redirectUrl = applet.getRedirectURL();
	     * if (redirectUrl != null) {
	     * try {
	     * applet.getAppletContext().showDocument(new URL(redirectUrl), "_top");
	     * } catch (MalformedURLException ex) {
	     * if (_logger.isLoggable(Level.WARNING)) {
	     * _logger.logp(Level.WARNING, this.getClass().getName(), "run()", ex.getMessage(), ex);
	     * }
	     * }
	     * return;
	     * } else {
	     * if (_logger.isLoggable(Level.WARNING)) {
	     * _logger.logp(Level.WARNING, this.getClass().getName(), "run()", "Unknown response type.", result);
	     * }
	     * return;
	     * }
	     */
	    // try {
	    // 	URL url = new URL(redirectURL);
	    // 	if (url.getQuery() != null) {
	    // 	    redirectURL = redirectURL + "&ResultMajor=ok";
	    // 	} else {
	    // 	    redirectURL = redirectURL + "?ResultMajor=ok";
	    // 	}
	    // 	System.out.println("redirecting to: " + redirectURL);
	    // 	ECardApplet.this.getAppletContext().showDocument(new URL(redirectURL), "_blank");
	    // } catch (MalformedURLException ex) {
	    // 	// <editor-fold defaultstate="collapsed" desc="log exception">
	    // 	logger.error(LoggingConstants.THROWING, "Exception", ex);
	    // 	// </editor-fold>
	    // }
	} catch (Exception ex) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error("Exception", ex);
	    // </editor-fold>
	}
    }

    private synchronized void waitForInput() {
	try {
	    thread.wait();
	} catch (InterruptedException ignore) {
	}
    }

    private ConnectionHandleType getConnectionHandle(String ifdName) {
	List<ConnectionHandleType> cHandles = getConnectionHandles();
	for (ConnectionHandleType cHandle : cHandles) {
	    if (ifdName.equals(cHandle.getIFDName())) {
		return cHandle;
	    }
	}
	return null;
    }

    private List<ConnectionHandleType> getConnectionHandles() {
	return ((TinySAL) applet.getClientEnvironment().getSAL()).getConnectionHandles();
    }

    @Override
    public void signalEvent(EventType eventType, Object eventData) {
	if (eventType.equals(EventType.CARD_INSERTED) || eventType.equals(EventType.CARD_RECOGNIZED)) {
	    applet.getClientEnvironment().getEventManager().unregister(this);
	    synchronized (this) {
		eventOccurred = true;
	    }
	    startPAOS(null);
	}
    }

    @Override
    public ClientResponse request(ClientRequest request) {
	if (request instanceof TCTokenRequest) {
	    return handleActivate((TCTokenRequest) request);
	}
	return null;
    }

    private TCTokenResponse handleActivate(TCTokenRequest request) {
	TCTokenResponse response = new TCTokenResponse();

	// TCToken
	TCToken token = request.getTCToken();

	// ContextHandle and SlotHandle
	ConnectionHandleType connectionHandle = null;
	byte[] requestedContextHandle = request.getContextHandle();
	BigInteger requestedSlotIndex = request.getSlotIndex();

	ConnectionHandleType requestedHandle = new ConnectionHandleType();

	requestedHandle.setContextHandle(requestedContextHandle);
	requestedHandle.setIFDName(request.getIFDName());
	requestedHandle.setSlotIndex(requestedSlotIndex);

	Set<CardStateEntry> matchingHandles = applet.getCardStates().getMatchingEntries(requestedHandle);

	if (!matchingHandles.isEmpty()) {
	    connectionHandle = matchingHandles.toArray(new CardStateEntry[]{})[0].handleCopy();
	}

	if (connectionHandle == null) {
	    // <editor-fold defaultstate="collapsed" desc="log error">
	    logger.error("Warning", "Given ConnectionHandle is invalied.");
	    // </editor-fold>
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "Given ConnectionHandle is invalied."));
	    return response;
	}

	try {
	    // Perform a CardApplicationPath and CardApplicationConnect to connect to the card application
	    CardApplicationPath cardApplicationPath = new CardApplicationPath();
	    cardApplicationPath.setCardAppPathRequest(connectionHandle);
	    CardApplicationPathResponse cardApplicationPathResponse = applet.getClientEnvironment().getSAL().cardApplicationPath(cardApplicationPath);

	    try {
		// Check CardApplicationPathResponse
		WSHelper.checkResult(cardApplicationPathResponse);
	    } catch (WSHelper.WSException ex) {
		// <editor-fold defaultstate="collapsed" desc="log exception">
		//	    logger.error(LoggingConstants.THROWING, "Exception", ex);
		// </editor-fold>
		response.setResult(ex.getResult());
		return response;
	    }

	    CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	    cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0));
	    CardApplicationConnectResponse cardApplicationConnectResponse = applet.getClientEnvironment().getSAL().cardApplicationConnect(cardApplicationConnect);
	    // Update ConnectionHandle. It now includes a SlotHandle.
	    connectionHandle = cardApplicationConnectResponse.getConnectionHandle();

	    try {
		// Check CardApplicationConnectResponse
		WSHelper.checkResult(cardApplicationConnectResponse);
	    } catch (WSHelper.WSException ex) {
		// <editor-fold defaultstate="collapsed" desc="log exception">
		//	    logger.error(LoggingConstants.THROWING, "Exception", ex);
		// </editor-fold>
		response.setResult(ex.getResult());
		return response;
	    }

	    String sessionIdentifier = token.getSessionIdentifier();
	    URL serverAddress = token.getServerAddress();

	    // FIXME: Wie weit ist das NPA abhängig.
	    if (token.getPathSecurityParameters() != null && token.getPathSecurityParameters().getPSK() != null) {
		serverAddress = new URL(serverAddress + "/?sessionid=" + sessionIdentifier);
	    }

	    TlsClientSocketFactory tlsClientFactory = null;

	    // Collect parameters for PSK based TLS
	    //TODO Change to support different protocols
	    if (token.getPathSecurityProtocol().equals("urn:ietf:rfc:4279")
		    || token.getPathSecurityProtocol().equals("urn:ietf:rfc:5487")) {
		byte[] psk = token.getPathSecurityParameters().getPSK();
		byte[] sessionBytes = sessionIdentifier.getBytes();

		// Set up TLS connection
		PSKTlsClientImpl tlsClient = new PSKTlsClientImpl(sessionBytes, psk, serverAddress.getHost());
		tlsClientFactory = new TlsClientSocketFactory(tlsClient);
	    }

	    // Set up PAOS connection
	    PAOS p = new PAOS(serverAddress, applet.getClientEnvironment().getDispatcher(), tlsClientFactory);

	    // Send StartPAOS
	    StartPAOS sp = new StartPAOS();
	    sp.getConnectionHandle().add(connectionHandle);
	    sp.setSessionIdentifier(sessionIdentifier);

	    ExecutorService service = Executors.newCachedThreadPool();

	    // FIXME: remove background thread as soon as possible
	    service.submit(new TestRunnable(p, sp));

	    response.setRefreshAddress(token.getRefreshAddress());

	} catch (Exception ex) {
	    if (ex instanceof RuntimeException) {
		throw (RuntimeException) ex;
	    }
	    logger.error("Exception", ex);
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, ex.getMessage()));
	}

	response.setResult(WSHelper.makeResultOK());
	return response;
    }

    private class TestRunnable implements Runnable {

	final PAOS p;
	final StartPAOS sp;

	public TestRunnable(PAOS p, StartPAOS sp) {
	    this.p = p;
	    this.sp = sp;
	}

	@Override
	public void run() {
	    try {
		this.p.sendStartPAOS(sp);
	    } catch (Exception e) {
		logger.error(e.getMessage(), e);
	    }
	}

    }

}
