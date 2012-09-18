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
package org.openecard.client.richclient;

import generated.TCTokenType;
import iso.std.iso_iec._24727.tech.schema.*;
import java.math.BigInteger;
import java.net.BindException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.I18n;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.sal.state.CardStateEntry;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.sal.state.SALStateCallback;
import org.openecard.client.control.ControlInterface;
import org.openecard.client.control.binding.http.HTTPBinding;
import org.openecard.client.control.client.ClientRequest;
import org.openecard.client.control.client.ClientResponse;
import org.openecard.client.control.client.ControlListener;
import org.openecard.client.control.module.status.StatusRequest;
import org.openecard.client.control.module.status.StatusResponse;
import org.openecard.client.control.module.tctoken.TCTokenRequest;
import org.openecard.client.control.module.tctoken.TCTokenResponse;
import org.openecard.client.event.EventManager;
import org.openecard.client.gui.swing.SwingDialogWrapper;
import org.openecard.client.gui.swing.SwingUserConsent;
import org.openecard.client.gui.swing.common.GUIDefaults;
import org.openecard.client.ifd.protocol.pace.PACEProtocolFactory;
import org.openecard.client.ifd.scio.IFD;
import org.openecard.client.management.TinyManagement;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.richclient.gui.AppTray;
import org.openecard.client.richclient.gui.MessageDialog;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.sal.protocol.eac.EACProtocolFactory;
import org.openecard.client.transport.dispatcher.MessageDispatcher;
import org.openecard.client.transport.paos.PAOS;
import org.openecard.client.transport.tls.PSKTlsClientImpl;
import org.openecard.client.transport.tls.TlsClientSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public final class RichClient implements ControlListener {

    private static final Logger _logger = LoggerFactory.getLogger(RichClient.class.getName());
    private static final I18n lang = I18n.getTranslation("gui");
    // Rich client
    private static RichClient client;
    // Tray icon
    private AppTray tray;
    // Control interface
    private ControlInterface control;
    // Client environment
    private ClientEnv env = new ClientEnv();
    // Interface Device Layer (IFD)
    private IFD ifd;
    // Service Access Layer (SAL)
    private TinySAL sal;
    // Event manager
    private EventManager em;
    // Card recognition
    private CardRecognition recognition;
    // card states
    private CardStateMap cardStates;
    // ContextHandle determines a specific IFD layer context
    private byte[] contextHandle;

    public static void main(String args[]) {
	RichClient.getInstance();
    }

    public static RichClient getInstance() {
	if (client == null) {
	    client = new RichClient();
	    client.setup();
	}
	return client;
    }

    @Override
    public ClientResponse request(ClientRequest request) {
	_logger.debug("Client request: {}", request.getClass());

	if (request instanceof TCTokenRequest) {
	    return handleActivate((TCTokenRequest) request);
	} else if (request instanceof StatusRequest) {
	    return handleStatus((StatusRequest) request);
	}
	return null;
    }

    private StatusResponse handleStatus(StatusRequest statusRequest) {
	StatusResponse response = new StatusResponse();

	List<ConnectionHandleType> connectionHandles = sal.getConnectionHandles();
	if (connectionHandles.isEmpty()) {
	    response.setResult(WSHelper.makeResultUnknownError("TBD"));
	    return response;
	}

	response.setConnectionHandles(connectionHandles);

	return response;
    }

    /**
     * Activate the client.
     *
     * @param request ActivationApplicationRequest
     * @return ActivationApplicationResponse
     */
    private TCTokenResponse handleActivate(TCTokenRequest request) {
	// use dumb activation without explicitly specifying the card and terminal
	// see TR-03112-7 v 1.1.2 (2012-02-28) sec. 3.2
	if (request.getContextHandle() == null || request.getIFDName() == null || request.getSlotIndex() == null) {
	    return handleAusweisappActivate(request);
	}

	TCTokenResponse response = new TCTokenResponse();
	// TCToken
	TCTokenType token = request.getTCToken();

	// ContextHandle, IFDName and SlotIndex
	ConnectionHandleType connectionHandle = null;
	byte[] requestedContextHandle = request.getContextHandle();
	String ifdName = request.getIFDName();
	BigInteger requestedSlotIndex = request.getSlotIndex();

	ConnectionHandleType requestedHandle = new ConnectionHandleType();
	requestedHandle.setContextHandle(requestedContextHandle);
	requestedHandle.setIFDName(ifdName);
	requestedHandle.setSlotIndex(requestedSlotIndex);

	Set<CardStateEntry> matchingHandles = cardStates.getMatchingEntries(requestedHandle);

	if (!matchingHandles.isEmpty()) {
	    connectionHandle = matchingHandles.toArray(new CardStateEntry[]{})[0].handleCopy();
	}

	if (connectionHandle == null) {
	    _logger.error("Warning", "Given ConnectionHandle is invalied.");
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "Given ConnectionHandle is invalid."));
	    return response;
	}

	return doPAOS(token, connectionHandle);
    }

    /**
     * Activate the client, but assume a german eID card is used and no handle information is given upfront.
     *
     * @param request
     * @return
     */
    private TCTokenResponse handleAusweisappActivate(TCTokenRequest request) {
	TCTokenType token = request.getTCToken();

	// get handle to nPA
	ConnectionHandleType connectionHandle = getFirstnPAHandle();
	if (connectionHandle == null) {
	    TCTokenResponse response = new TCTokenResponse();
	    String msg = "No ConnectionHandle with a german eID card available.";
	    _logger.error(msg);
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, msg));
	    return response;
	}

	return doPAOS(token, connectionHandle);
    }

    /**
     * Get the first found handle of an nPA.
     *
     * @return Handle describing the nPA or null if none is present.
     */
    private ConnectionHandleType getFirstnPAHandle() {
	ConnectionHandleType conHandle = new ConnectionHandleType();
	ConnectionHandleType.RecognitionInfo rec = new ConnectionHandleType.RecognitionInfo();
	rec.setCardType("http://bsi.bund.de/cif/npa.xml");
	conHandle.setRecognitionInfo(rec);

	// TODO: wait for nPA a while when none is present

	Set<CardStateEntry> entries = cardStates.getMatchingEntries(conHandle);
	if (entries.isEmpty()) {
	    return null;
	} else {
	    return entries.iterator().next().handleCopy();
	}
    }

    private TCTokenResponse doPAOS(TCTokenType token, ConnectionHandleType connectionHandle) {
	try {
	    // Perform a CardApplicationPath and CardApplicationConnect to connect to the card application
	    CardApplicationPath cardApplicationPath = new CardApplicationPath();
	    cardApplicationPath.setCardAppPathRequest(connectionHandle);
	    CardApplicationPathResponse cardApplicationPathResponse = sal.cardApplicationPath(cardApplicationPath);

	    try {
		// Check CardApplicationPathResponse
		WSHelper.checkResult(cardApplicationPathResponse);
	    } catch (WSException ex) {
		TCTokenResponse response = new TCTokenResponse();
		response.setResult(ex.getResult());
		return response;
	    }

	    CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	    cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0));
	    CardApplicationConnectResponse cardApplicationConnectResponse = sal.cardApplicationConnect(cardApplicationConnect);
	    // Update ConnectionHandle. It now includes a SlotHandle.
	    connectionHandle = cardApplicationConnectResponse.getConnectionHandle();

	    try {
		// Check CardApplicationConnectResponse
		WSHelper.checkResult(cardApplicationConnectResponse);
	    } catch (WSException ex) {
		TCTokenResponse response = new TCTokenResponse();
		response.setResult(ex.getResult());
		return response;
	    }

	    // Collect parameters for PSK based TLS
	    //TODO Change to support different protocols
	    byte[] psk = token.getPathSecurityParameters().getPSK();
	    String sessionIdentifier = token.getSessionIdentifier();
	    URL serverAddress = new URL(token.getServerAddress());
	    URL endpoint = new URL(serverAddress + "/?sessionid=" + sessionIdentifier);

	    // Set up TLS connection
	    PSKTlsClientImpl tlsClient = new PSKTlsClientImpl(sessionIdentifier.getBytes(), psk, serverAddress.getHost());
	    TlsClientSocketFactory tlsClientFactory = new TlsClientSocketFactory(tlsClient);

	    // Set up PAOS connection
	    PAOS p = new PAOS(endpoint, env.getDispatcher(), tlsClientFactory);

	    // Send StartPAOS
	    StartPAOS sp = new StartPAOS();
	    sp.getConnectionHandle().add(connectionHandle);
	    sp.setSessionIdentifier(sessionIdentifier);
	    p.sendStartPAOS(sp);

	    TCTokenResponse response = new TCTokenResponse();
	    response.setRefreshAddress(new URL(token.getRefreshAddress()));
	    response.setResult(WSHelper.makeResultOK());
	    return response;

	} catch (WSException w) {
	    TCTokenResponse response = new TCTokenResponse();
	    _logger.error(w.getMessage(), w);
	    response.setResult(w.getResult());
	    return response;
	} catch (Throwable w) {
	    TCTokenResponse response = new TCTokenResponse();
	    _logger.error(w.getMessage(), w);
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, w.getMessage()));
	    return response;
	}
    }

    public void setup() {
	GUIDefaults.initialize();

	MessageDialog dialog = new MessageDialog();
	dialog.setHeadline(lang.translationForKey("client.startup.failed.headline"));

	try {
	    // Start up control interface
	    try {
		HTTPBinding binding = new HTTPBinding(HTTPBinding.DEFAULT_PORT);
		control = new ControlInterface(binding);
		control.getListeners().addControlListener(this);
		control.start();
	    } catch (BindException e) {
		dialog.setMessage(lang.translationForKey("client.startup.failed.portinuse"));
		throw e;
	    }

	    tray = new AppTray(this);
	    tray.beginSetup();

	    // Set up client environment
	    env = new ClientEnv();

	    // Set up Management
	    TinyManagement management = new TinyManagement(env);
	    env.setManagement(management);

	    // Set up the IFD
	    ifd = new IFD();
	    ifd.addProtocol(ECardConstants.Protocol.PACE, new PACEProtocolFactory());
	    env.setIFD(ifd);

	    // Set up the Dispatcher
	    MessageDispatcher dispatcher = new MessageDispatcher(env);
	    env.setDispatcher(dispatcher);
	    ifd.setDispatcher(dispatcher);

	    // Perform an EstablishContext to get a ContextHandle
	    EstablishContext establishContext = new EstablishContext();
	    EstablishContextResponse establishContextResponse = ifd.establishContext(establishContext);

	    if (establishContextResponse.getResult().getResultMajor().equals(ECardConstants.Major.OK)) {
		if (establishContextResponse.getContextHandle() != null) {
		    contextHandle = ifd.establishContext(establishContext).getContextHandle();
		} else {
		    //TODO
		}
	    } else {
		// TODO
	    }

	    // Set up CardRecognition
	    recognition = new CardRecognition(ifd, contextHandle);

	    // Set up EventManager
	    em = new EventManager(recognition, env, contextHandle);
	    env.setEventManager(em);

	    // Set up SALStateCallback
	    cardStates = new CardStateMap();
	    SALStateCallback salCallback = new SALStateCallback(recognition, cardStates);
	    em.registerAllEvents(salCallback);

	    // Set up SAL
	    sal = new TinySAL(env, cardStates);
	    sal.addProtocol(ECardConstants.Protocol.EAC, new EACProtocolFactory());
	    env.setSAL(sal);

	    // Set up GUI
	    SwingUserConsent gui = new SwingUserConsent(new SwingDialogWrapper());
	    sal.setGUI(gui);
	    ifd.setGUI(gui);

	    tray.endSetup(recognition);
	    em.registerAllEvents(tray.status());

	    // Initialize the EventManager
	    em.initialize();
	} catch (Exception e) {
	    _logger.error("Exception", e);

	    if (dialog.getMessage() == null || dialog.getMessage().isEmpty()) {
		// Add exception message if no custom message is set
		dialog.setMessage(e.getMessage());
	    }

	    // Show dialog to the user and shut down the client
	    JOptionPane.showMessageDialog(null, dialog, "Open eCard App", JOptionPane.PLAIN_MESSAGE);
	    teardown();
	}
    }

    public void teardown() {
	try {
	    // shutdwon event manager
	    em.terminate();

	    // shutdown SAL
	    Terminate terminate = new Terminate();
	    sal.terminate(terminate);

	    // shutdown IFD
	    ReleaseContext releaseContext = new ReleaseContext();
	    releaseContext.setContextHandle(contextHandle);
	    ifd.releaseContext(releaseContext);
	} catch (Exception ignore) {
	}

	System.exit(0);
    }

}
