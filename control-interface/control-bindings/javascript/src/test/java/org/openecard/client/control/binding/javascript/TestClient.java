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
package org.openecard.client.control.binding.javascript;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.sal.state.CardStateEntry;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.sal.state.SALStateCallback;
import org.openecard.client.common.util.ValueGenerators;
import org.openecard.client.control.client.ClientRequest;
import org.openecard.client.control.client.ClientResponse;
import org.openecard.client.control.client.ControlListener;
import org.openecard.client.control.module.status.StatusRequest;
import org.openecard.client.control.module.status.StatusResponse;
import org.openecard.client.control.module.tctoken.TCToken;
import org.openecard.client.control.module.tctoken.TCTokenRequest;
import org.openecard.client.control.module.tctoken.TCTokenResponse;
import org.openecard.client.event.EventManager;
import org.openecard.client.ifd.scio.IFD;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.transport.dispatcher.MessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a TestClient to test the control interface.
 * 
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class TestClient implements ControlListener {

    private static final Logger logger = LoggerFactory.getLogger(TestClient.class);
    // Service Access Layer (SAL)
    private TinySAL sal;
    // card states
    private CardStateMap cardStates;

    public TestClient() {
	try {
	    setup();
	} catch (Exception e) {
	    logger.error("Exception", e);
	}
    }

    private void setup() throws Exception {
	// Set up client environment
	ClientEnv env = new ClientEnv();

	// Set up the IFD
	IFD ifd = new IFD();
	env.setIFD(ifd);

	// Set up the Dispatcher
	MessageDispatcher dispatcher = new MessageDispatcher(env);
	env.setDispatcher(dispatcher);
	ifd.setDispatcher(dispatcher);

	// Perform an EstablishContext to get a ContextHandle
	EstablishContext establishContext = new EstablishContext();
	EstablishContextResponse establishContextResponse = ifd.establishContext(establishContext);

	byte[] contextHandle = ifd.establishContext(establishContext).getContextHandle();

	CardRecognition recognition = new CardRecognition(ifd, contextHandle);

	// Set up EventManager
	EventManager em = new EventManager(recognition, env, contextHandle);
	env.setEventManager(em);

	// Set up SALStateCallback
	cardStates = new CardStateMap();
	SALStateCallback salCallback = new SALStateCallback(recognition, cardStates);
	em.registerAllEvents(salCallback);

	// Set up SAL
	sal = new TinySAL(env, cardStates);
	env.setSAL(sal);

	// Initialize the EventManager
	em.initialize();
    }

    @Override
    public ClientResponse request(ClientRequest request) {
	logger.debug("Client request: {}", request.getClass());

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

    private TCTokenResponse handleActivate(TCTokenRequest request) {

	TCTokenResponse response = new TCTokenResponse();
	try {
	    // TCToken
	    TCToken token = request.getTCToken();

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
		logger.error("Warning", "Given ConnectionHandle is invalied.");
		response.setResult(WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "Given ConnectionHandle is invalid."));
		return response;
	    }

	    response.setRefreshAddress(new URL("http://www.openecard.org"));
	} catch (Exception e) {
	    logger.error("Exception", e);
	    response.setResult(WSHelper.makeResultUnknownError(e.getMessage()));
	}

	return response;
    }

}
