/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.client.control.module.tctoken;

import generated.TCTokenType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Set;
import org.openecard.bouncycastle.crypto.tls.TlsClient;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.state.CardStateEntry;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.control.module.tctoken.gui.InsertCardUserConsent;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.transport.paos.PAOS;
import org.openecard.client.transport.tls.PSKTlsClientImpl;
import org.openecard.client.transport.tls.TlsClientSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class GenericTCTokenHandler {

    private static final Logger logger = LoggerFactory.getLogger(GenericTCTokenHandler.class);
    final CardStateMap cardStates;
    private final Dispatcher dispatcher;
    private final UserConsent gui;
    private CardRecognition reg;

    public GenericTCTokenHandler(CardStateMap cardStates, Dispatcher dispatcher, UserConsent gui, CardRecognition reg){
	this.cardStates = cardStates;
	this.dispatcher = dispatcher;
	this.gui = gui;
	this.reg = reg;
    }

    public TCTokenRequest parseTCTokenRequestURI(URI requestURI) throws UnsupportedEncodingException, MalformedURLException, TCTokenException {
   	TCTokenRequest tcTokenRequest = new TCTokenRequest();
   	String query[] = requestURI.getQuery().split("&");

   	for (String q : query) {
   	    String name = q.substring(0, q.indexOf("="));
   	    String value = q.substring(q.indexOf("=") + 1, q.length());

   	    if (name.startsWith("tcTokenURL")) {
   		if (!value.isEmpty()) {
   		    value = URLDecoder.decode(value, "UTF-8");
   		    TCTokenType token = TCTokenFactory.generateTCToken(new URL(value));
   		    tcTokenRequest.setTCToken(token);
   		} else {
   		    throw new IllegalArgumentException("Malformed TCTokenURL");
   		}

   	    } else if (name.startsWith("ifdName")) {
   		if (!value.isEmpty()) {
   		    value = URLDecoder.decode(value, "UTF-8");
   		    tcTokenRequest.setIFDName(value);
   		} else {
   		    throw new IllegalArgumentException("Malformed IFDName");
   		}

   	    } else if (name.startsWith("contextHandle")) {
   		if (!value.isEmpty()) {
   		    tcTokenRequest.setContextHandle(value);
   		} else {
   		    throw new IllegalArgumentException("Malformed ContextHandle");
   		}

   	    } else if (name.startsWith("slotIndex")) {
   		if (!value.isEmpty()) {
   		    tcTokenRequest.setSlotIndex(value);
   		} else {
   		    throw new IllegalArgumentException("Malformed SlotIndex");
   		}
   	    } else if (name.startsWith("cardType")) {
   		if (!value.isEmpty()) {
   		    tcTokenRequest.setCardType(value);
   		} else {
   		    throw new IllegalArgumentException("Malformed CardType");
   		}
   	    } else {
   		logger.debug("Unknown query element: {}", name);
   	    }
   	}
   	return tcTokenRequest;
    }

    /**
     * Activate the client, but assume to use the given card type and no handle information is given upfront.
     * 
     * @param request
     * @return
     */
    private TCTokenResponse handleCardTypeActivate(TCTokenRequest request) {
	TCTokenType token = request.getTCToken();

	// get handle to first card of specifified type
	ConnectionHandleType connectionHandle = getFirstHandle(request.getCardType());
	if (connectionHandle == null) {
	    TCTokenResponse response = new TCTokenResponse();
	    String msg = "No ConnectionHandle with card type '" + request.getCardType() + "' available.";
	    logger.error(msg);
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, msg));
	    return response;
	}
	return doPAOS(token, connectionHandle);
    }

    /**
     * Get the first found handle of the given card type.
     * 
     * @param type
     *            the card type to get the first handle for
     * @return Handle describing the given card type or null if none is present.
     */
    private ConnectionHandleType getFirstHandle(String type) {
	try {
	    ConnectionHandleType conHandle = new ConnectionHandleType();
	    ConnectionHandleType.RecognitionInfo rec = new ConnectionHandleType.RecognitionInfo();
	    rec.setCardType(type);
	    conHandle.setRecognitionInfo(rec);
	    Set<CardStateEntry> entries;
	    entries = cardStates.getMatchingEntries(conHandle);
	    if (entries.isEmpty()) {

		InsertCardUserConsent uc = new InsertCardUserConsent(gui, reg, conHandle, cardStates);
		return uc.show();
	    } else
		return entries.iterator().next().handleCopy();
	} catch (RuntimeException e) {
	    e.printStackTrace();
	    throw e;
	}
    }

    private TCTokenResponse doPAOS(TCTokenType token, ConnectionHandleType connectionHandle) {
	try {
	    // Perform a CardApplicationPath and CardApplicationConnect to connect to the card application
	    CardApplicationPath cardApplicationPath = new CardApplicationPath();
	    cardApplicationPath.setCardAppPathRequest(connectionHandle);
	    CardApplicationPathResponse cardApplicationPathResponse = (CardApplicationPathResponse) dispatcher
		    .deliver(cardApplicationPath);

	    try {
		// Check CardApplicationPathResponse
		WSHelper.checkResult(cardApplicationPathResponse);
	    } catch (WSException ex) {
		TCTokenResponse response = new TCTokenResponse();
		response.setResult(ex.getResult());
		return response;
	    }

	    CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	    cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet()
		    .getCardApplicationPathResult().get(0));
	    CardApplicationConnectResponse cardApplicationConnectResponse = (CardApplicationConnectResponse) dispatcher
		    .deliver(cardApplicationConnect);
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

	    String sessionIdentifier = token.getSessionIdentifier();
	    URL serverAddress = new URL(token.getServerAddress());

	    // Set up TLS connection
	    String secProto = token.getPathSecurityProtocol();
	    TlsClientSocketFactory tlsClientFactory = null;
	    // TODO: Change to support different protocols
	    if (secProto.equals("urn:ietf:rfc:4279") || secProto.equals("urn:ietf:rfc:5487")) {
		byte[] psk = token.getPathSecurityParameters().getPSK();
		TlsClient tlsClient = new PSKTlsClientImpl(sessionIdentifier.getBytes(), psk, serverAddress.getHost());
		tlsClientFactory = new TlsClientSocketFactory(tlsClient);
	    }

	    // Set up PAOS connection
	    PAOS p = new PAOS(serverAddress, dispatcher, tlsClientFactory);

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
	    logger.error(w.getMessage(), w);
	    response.setResult(w.getResult());
	    return response;
	} catch (Throwable w) {
	    TCTokenResponse response = new TCTokenResponse();
	    logger.error(w.getMessage(), w);
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, w.getMessage()));
	    return response;
	}
    }

    /**
     * Activate the client.
     * 
     * @param request
     *            ActivationApplicationRequest
     * @return ActivationApplicationResponse
     */
    public TCTokenResponse handleActivate(TCTokenRequest request) {

	// use dumb activation without explicitly specifying the card and terminal
	// see TR-03112-7 v 1.1.2 (2012-02-28) sec. 3.2
	if (request.getContextHandle() == null || request.getIFDName() == null || request.getSlotIndex() == null) {
	    return handleCardTypeActivate(request);
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
	    connectionHandle = matchingHandles.toArray(new CardStateEntry[] {})[0].handleCopy();
	}

	if (connectionHandle == null) {
	    String msg = "Given ConnectionHandle is invalid.";
	    logger.error(msg);
	    response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, msg));
	    return response;
	}

	return doPAOS(token, connectionHandle);
    }

}
