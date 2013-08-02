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

package org.openecard.control.module.status;

import iso.std.iso_iec._24727.tech.schema.CardApplicationType;
import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDInfoType;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.openecard.addon.EventHandler;
import org.openecard.common.Version;
import org.openecard.common.interfaces.ProtocolInfo;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.recognition.CardRecognition;
import org.openecard.ws.schema.Status;
import org.openecard.ws.schema.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles the generic part of status requests.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class GenericStatusHandler {

    private static final Logger logger = LoggerFactory.getLogger(GenericStatusHandler.class);

    private CardStateMap cardStates;
    private EventHandler eventHandler;
    private ProtocolInfo protocols;
    private CardRecognition rec;

    /**
     * Create a new GenericStatusHandler.
     *
     * @param cardStates CardStateMap of the client for querying all ConnectionHandles
     * @param eventHandler for adding eventQueues
     */
    public GenericStatusHandler(CardStateMap cardStates, EventHandler eventHandler, ProtocolInfo protocols, CardRecognition rec) {
	this.cardStates = cardStates;
	this.eventHandler = eventHandler;
	this.protocols = protocols;
	this.rec = rec;
    }

    /**
     * Handles a Status-Request by returning a status including list of all known ConnectionHandles (including
     * unrecognized cards).
     *
     * @param statusRequest Status Request containing an optional session identifier
     * @return Status including list of all known ConnectionHandles
     */
    public Status handleRequest(StatusRequest statusRequest) {
	String sessionIdentifier = statusRequest.getSessionIdentifier();
	Status status = new Status();
	ConnectionHandleType handle = new ConnectionHandleType();

	// fill in static values
	// user agent
	StatusType.UserAgent ua = new StatusType.UserAgent();
	ua.setName("Open eCard App");
	ua.setVersionMajor(BigInteger.valueOf(Version.getMajor()));
	ua.setVersionMinor(BigInteger.valueOf(Version.getMinor()));
	ua.setVersionSubminor(BigInteger.valueOf(Version.getPatch()));
	status.setUserAgent(ua);
	// API versions
	StatusType.SupportedAPIVersions apiVersion = new StatusType.SupportedAPIVersions();
	apiVersion.setName("http://www.bsi.bund.de/ecard/api");
	apiVersion.setVersionMajor(BigInteger.ONE);
	apiVersion.setVersionMinor(BigInteger.ONE);
	status.getSupportedAPIVersions().add(apiVersion);
	// TODO: supported cards
	List<CardInfoType> cifs = rec.getCardInfos();
	List<StatusType.SupportedCards> supportedCards = getSupportedCards(protocols.protocols(), cifs);
	status.getSupportedCards().addAll(supportedCards);
	// supported DID protocols
	status.getSupportedDIDProtocols().addAll(protocols.protocols());
	// TODO: additional features

	if (sessionIdentifier != null) {
	    ChannelHandleType channelHandle = new ChannelHandleType();
	    channelHandle.setSessionIdentifier(sessionIdentifier);
	    handle.setChannelHandle(channelHandle);
	    eventHandler.addQueue(sessionIdentifier);
	}

	Set<CardStateEntry> entries = this.cardStates.getMatchingEntries(handle);

	for (CardStateEntry entry : entries) {
	    status.getConnectionHandle().add(entry.handleCopy());
	}

	return status;
    }

    /**
     *
     * @param requestURI
     *            Status request URI
     * @return StatusRequest containing an optional session identifier
     * @throws UnsupportedEncodingException
     * @throws MalformedURLException
     *             if mandatory parameters or values are missing
     */
    public StatusRequest parseStatusRequestURI(URI requestURI) throws UnsupportedEncodingException,
	    MalformedURLException {
	StatusRequest statusRequest = new StatusRequest();

	if (requestURI.getQuery() == null) {
	    return statusRequest;
	}

	String[] query = requestURI.getQuery().split("&");

	for (String q : query) {
	    String name = q.substring(0, q.indexOf("="));
	    String value = q.substring(q.indexOf("=") + 1, q.length());

	    if (name.startsWith("session")) {
		if (!value.isEmpty()) {
		    value = URLDecoder.decode(value, "UTF-8");
		    statusRequest.setSessionIdentifier(value);
		} else {
		    throw new MalformedURLException("Value for session parameter is missing.");
		}
	    } else {
		logger.debug("Unknown query element: {}", name);
	    }
	}
	return statusRequest;
    }

    private static List<StatusType.SupportedCards> getSupportedCards(List<String> protocols, List<CardInfoType> cifs) {
	List<StatusType.SupportedCards> result = new ArrayList<StatusType.SupportedCards>();

	for (CardInfoType cif : cifs) {
	    StatusType.SupportedCards supportedCard = new StatusType.SupportedCards();
	    result.add(supportedCard);
	    String name = cif.getCardType().getObjectIdentifier();
	    supportedCard.setCardType(name);

	    for (CardApplicationType app : cif.getApplicationCapabilities().getCardApplication()) {
		for (DIDInfoType did : app.getDIDInfo()) {
		    String proto = did.getDifferentialIdentity().getDIDProtocol();
		    if (protocols.contains(proto)) {
			supportedCard.getDIDProtocols().add(proto);
		    }
		}
	    }
	}

	return result;
    }

}
