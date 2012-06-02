/**
 * **************************************************************************
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
 **************************************************************************
 */
package org.openecard.client.richclient;

import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import java.net.BindException;
import java.net.URL;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.logging.LoggingConstants;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.sal.state.SALStateCallback;
import org.openecard.client.common.util.ValueGenerators;
import org.openecard.client.connector.Connector;
import org.openecard.client.connector.ConnectorListener;
import org.openecard.client.connector.messages.StatusRequest;
import org.openecard.client.connector.messages.StatusResponse;
import org.openecard.client.connector.messages.TCTokenRequest;
import org.openecard.client.connector.messages.TCTokenResponse;
import org.openecard.client.connector.messages.common.ClientRequest;
import org.openecard.client.connector.messages.common.ClientResponse;
import org.openecard.client.connector.tctoken.TCToken;
import org.openecard.client.event.EventManager;
import org.openecard.client.gui.swing.SwingDialogWrapper;
import org.openecard.client.gui.swing.SwingUserConsent;
import org.openecard.client.ifd.protocol.pace.PACEProtocolFactory;
import org.openecard.client.ifd.scio.IFD;
import org.openecard.client.management.TinyManagement;
import org.openecard.client.recognition.CardRecognition;
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
 */
public final class RichClient implements ConnectorListener {

    private static final Logger logger = LoggerFactory.getLogger(RichClient.class.getName());
    private static RichClient client;
    // Client environment
    private ClientEnv env = new ClientEnv();
    // Interface Device Layer (IFD)
    private IFD ifd;
    // Service Access Layer (SAL)
    private TinySAL sal;
    private CardRecognition recognition;
    private CardStateMap cardStates;
    // ContextHandle determines a specific IFD layer context
    private byte[] contextHandle;

    public static void main(String args[]) {
	try {
	    RichClient.getInstance();
	} catch (Exception e) {
	    logger.warn(e.getMessage());
	}
    }

    public static RichClient getInstance() throws Exception {
	if (client == null) {
	    client = new RichClient();
	    try {
		Connector connector = Connector.getInstance();
		connector.addConnectorListener(client);
	    } catch (BindException e) {
		throw new Exception("Client connector is running.");
	    }
	}
	return client;
    }

    private RichClient() throws Exception {

	setup();
    }

    @Override
    public ClientResponse request(ClientRequest request) {
	// <editor-fold defaultstate="collapsed" desc="log request">
	logger.debug(LoggingConstants.FINER, "Client request: {}", request.getClass());
	// </editor-fold>
	if (request instanceof TCTokenRequest) {
	    return handleActivate((TCTokenRequest) request);
	} else if (request instanceof StatusRequest) {
	    return handleStatus((StatusRequest) request);
	}
	return null;
    }

    private StatusResponse handleStatus(StatusRequest statusRequest) {
	StatusResponse response = new StatusResponse();

	return response;
    }

    /**
     * Activate the client.
     *
     * @param request ActivationApplicationRequest
     * @return ActivationApplicationResponse
     */
    private TCTokenResponse handleActivate(TCTokenRequest request) {
	TCTokenResponse response = new TCTokenResponse();

	TCToken token = request.getTCToken();


	//FIXME ConnectionHandle kommt nachher vom ActivationApplicationRequest
//	for (ConnectionHandleType connectionHandle : sal.getConnectionHandles()) {
//	    if(connectionHandle.)
//	}
//
//	ConnectionHandleType connectionHandle = request.getConnectionHandleID();
//	ConnectionHandleType connectionHandle = request.getConnectionHandleID();
	ConnectionHandleType connectionHandle = sal.getConnectionHandles().get(0);

	try {
	    // Perform a CardApplicationPath and CardApplicationConnect to connect to the card application
	    CardApplicationPath cardApplicationPath = new CardApplicationPath();
	    cardApplicationPath.setCardAppPathRequest(connectionHandle);
	    CardApplicationPathResponse cardApplicationPathResponse = sal.cardApplicationPath(cardApplicationPath);

	    try {
		// Check CardApplicationPathResponse
		WSHelper.checkResult(cardApplicationPathResponse);
	    } catch (WSException ex) {
		// <editor-fold defaultstate="collapsed" desc="log exception">
//	    logger.error(LoggingConstants.THROWING, "Exception", ex);
		// </editor-fold>
		response.setErrorMessage(ex.getMessage());
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
		// <editor-fold defaultstate="collapsed" desc="log exception">
//	    logger.error(LoggingConstants.THROWING, "Exception", ex);
		// </editor-fold>
		response.setErrorMessage(ex.getMessage());
		return response;
	    }

	    // Collect parameters for PSK based TLS
	    //TODO Change to support different protocols
	    byte[] psk = token.getPathSecurityParameter().getPSK();
	    String sessionIdentifier = token.getSessionIdentifier();
	    URL serverAddress = token.getServerAddress();
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

	    response.setRefreshAddress(token.getRefreshAddress());

	} catch (Throwable w) {
	    logger.error(LoggingConstants.THROWING, "Exception", w);
	    response.setErrorMessage(w.getMessage());
	}

	return response;
    }

    public void setup() throws Exception {
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
	EventManager em = new EventManager(recognition, env, contextHandle, ValueGenerators.generateSessionID());
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

	// Initialize the EventManager
	em.initialize();
    }
}
