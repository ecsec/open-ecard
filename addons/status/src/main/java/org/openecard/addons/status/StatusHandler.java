/****************************************************************************
 * Copyright (C) 2012-2019 HS Coburg.
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

package org.openecard.addons.status;

import iso.std.iso_iec._24727.tech.schema.CardApplicationType;
import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDInfoType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import org.openecard.addon.AddonManager;
import org.openecard.addon.AddonRegistry;
import org.openecard.addon.Context;
import org.openecard.addon.EventHandler;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.addon.manifest.ProtocolPluginSpecification;
import org.openecard.addon.sal.SalStateView;
import org.openecard.common.ECardConstants;
import org.openecard.common.AppVersion;
import org.openecard.common.interfaces.CardRecognition;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.schema.Status;
import org.openecard.ws.schema.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles the status request.
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
public class StatusHandler {

    private static final Logger LOG = LoggerFactory.getLogger(StatusHandler.class);

    private final Dispatcher dispatcher;
    private final EventHandler eventHandler;
    private final List<String> protocols;
    private final CardRecognition rec;
    private final SalStateView salStateView;


    public StatusHandler(Context ctx) {
	dispatcher = ctx.getDispatcher();
	eventHandler = ctx.getEventHandler();
	protocols = getProtocolInfo(ctx.getManager());
	this.salStateView = ctx.getSalStateView();
	rec = ctx.getRecognition();
    }

    /**
     * Handles a Status-Request by returning a status message describing the capabilities if the App.
     *
     * @param statusRequest Status Request possibly containing a session identifier for event registration.
     * @return Status message.
     * @throws WSMarshallerException
     */
    public BindingResult handleRequest(StatusRequest statusRequest) throws WSMarshallerException {
	Status status = new Status();

	// user agent
	StatusType.UserAgent ua = new StatusType.UserAgent();
	ua.setName(AppVersion.getName());
	ua.setVersionMajor(BigInteger.valueOf(AppVersion.getMajor()));
	ua.setVersionMinor(BigInteger.valueOf(AppVersion.getMinor()));
	ua.setVersionSubminor(BigInteger.valueOf(AppVersion.getPatch()));
	status.setUserAgent(ua);

	// API versions
	StatusType.SupportedAPIVersions apiVersion = new StatusType.SupportedAPIVersions();
	apiVersion.setName("http://www.bsi.bund.de/ecard/api");
	apiVersion.setVersionMajor(ECardConstants.ECARD_API_VERSION_MAJOR);
	apiVersion.setVersionMinor(ECardConstants.ECARD_API_VERSION_MINOR);
	apiVersion.setVersionSubminor(ECardConstants.ECARD_API_VERSION_SUBMINOR);
	status.getSupportedAPIVersions().add(apiVersion);

	// supported cards
	List<CardInfoType> cifs = rec.getCardInfos();
	List<StatusType.SupportedCards> supportedCards = getSupportedCards(protocols, cifs);
	status.getSupportedCards().addAll(supportedCards);

	// supported DID protocols
	status.getSupportedDIDProtocols().addAll(protocols);

	// TODO: additional features

	// add available cards
	status.getConnectionHandle().addAll(getCardHandles());

	// register session for wait for change
	if (statusRequest.hasSessionIdentifier()) {
	    String sessionIdentifier = statusRequest.getSessionIdentifier();
	    eventHandler.addQueue(sessionIdentifier);
	}

	return new StatusResponseBodyFactory().createStatusResponse(status);
    }

    @Nonnull
    private static List<StatusType.SupportedCards> getSupportedCards(List<String> protocols, List<CardInfoType> cifs) {
	List<StatusType.SupportedCards> result = new ArrayList<>();

	for (CardInfoType cif : cifs) {
	    StatusType.SupportedCards supportedCard = new StatusType.SupportedCards();
	    result.add(supportedCard);
	    String name = cif.getCardType().getObjectIdentifier();
	    supportedCard.setCardType(name);

	    for (CardApplicationType app : cif.getApplicationCapabilities().getCardApplication()) {
		for (DIDInfoType did : app.getDIDInfo()) {
		    String proto = did.getDifferentialIdentity().getDIDProtocol();
		    // add protocol to list only if it is supported by the application and not yet added
		    if (protocols.contains(proto) && ! supportedCard.getDIDProtocols().contains(proto)) {
			supportedCard.getDIDProtocols().add(proto);
		    }
		}
	    }
	}

	return result;
    }

    @Nonnull
    private List<String> getProtocolInfo(AddonManager manager) {
	TreeSet<String> result = new TreeSet<>();

	// check all sal protocols in the
	AddonRegistry registry = manager.getRegistry();
	Set<AddonSpecification> addons = registry.listAddons();
	for (AddonSpecification addon : addons) {
	    for (ProtocolPluginSpecification proto : addon.getSalActions()) {
		result.add(proto.getUri());
	    }
	}

	return new ArrayList<>(result);
    }

    @Nonnull
    private List<ConnectionHandleType> getCardHandles() {
	// TODO: reimplement according to redesign.
	// TODO: verify done
//	ConnectionHandleType handle = new ConnectionHandleType();
//	Set<CardStateEntry> entries = cardStates.getMatchingEntries(handle, false);
//
//	ArrayList<ConnectionHandleType> result = new ArrayList<>(entries.size());
//	for (CardStateEntry entry : entries) {
//	    result.add(entry.handleCopy());
//	}
//
//	return result;
	return this.salStateView.listCardHandles();
    }

}
