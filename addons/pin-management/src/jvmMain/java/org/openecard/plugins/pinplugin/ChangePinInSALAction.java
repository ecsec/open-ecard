/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.plugins.pinplugin;

import iso.std.iso_iec._24727.tech.schema.ActionType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDGet;
import iso.std.iso_iec._24727.tech.schema.DIDGetResponse;
import iso.std.iso_iec._24727.tech.schema.DIDList;
import iso.std.iso_iec._24727.tech.schema.DIDListResponse;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.DIDUpdate;
import iso.std.iso_iec._24727.tech.schema.DIDUpdateDataType;
import iso.std.iso_iec._24727.tech.schema.PinCompareDIDUpdateDataType;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.addon.ActionInitializationException;
import org.openecard.addon.Context;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class ChangePinInSALAction extends AbstractPINAction {

    private static final Logger LOG = LoggerFactory.getLogger(ChangePinInSALAction.class);

    @Override
    public void execute() {
	List<ConnectionHandleType> connectedCards = null;
	try {
	    connectedCards = connectCards();

	    if (connectedCards.isEmpty()) {
		// TODO: show no card inserted dialog
	    }

	    for (ConnectionHandleType nextCard : connectedCards) {
		// pick first card, find pin DID and call didupdate
		String didName = null;
		try {
		    didName = getPinDid(nextCard);
		} catch (WSException ex) {
		    LOG.info("Skipping card, because it has no PIN DID.");
		    continue;
		}

		DIDUpdate updateReq = new DIDUpdate();
		updateReq.setConnectionHandle(nextCard);
		updateReq.setDIDName(didName);
		DIDUpdateDataType updateData = new PinCompareDIDUpdateDataType();
		updateData.setProtocol("urn:oid:1.3.162.15480.3.0.9");
		updateReq.setDIDUpdateData(updateData);

		dispatcher.safeDeliver(updateReq);
	    }
	} catch (WSException ex) {

	} finally {
	    if (connectedCards != null) {
		for (ConnectionHandleType nextHandle : connectedCards) {
		    CardApplicationDisconnect dr = new CardApplicationDisconnect();
		    dr.setConnectionHandle(nextHandle);
		    dr.setAction(ActionType.RESET);
		    dispatcher.safeDeliver(dr);
		}
	    }
	}
    }

    @Override
    public void init(Context aCtx) throws ActionInitializationException {
	dispatcher = aCtx.getDispatcher();
	this.gui = aCtx.getUserConsent();
	this.recognition = aCtx.getRecognition();
	this.evDispatcher = aCtx.getEventDispatcher();
	this.salStateView = aCtx.getSalStateView();
    }

    @Override
    public void destroy(boolean force) {
	//ignore
    }

    private List<ConnectionHandleType> connectCards() throws WSHelper.WSException {
	// get all cards in the system
	CardApplicationPath pathReq = new CardApplicationPath();
	CardApplicationPathType pathType = new CardApplicationPathType();
	pathReq.setCardAppPathRequest(pathType);

	CardApplicationPathResponse pathRes = (CardApplicationPathResponse) dispatcher.safeDeliver(pathReq);
	WSHelper.checkResult(pathRes);

	// connect every card in the set
	ArrayList<ConnectionHandleType> connectedCards = new ArrayList<>();
	for (CardApplicationPathType path : pathRes.getCardAppPathResultSet().getCardApplicationPathResult()) {
	    try {
		CardApplicationConnect conReq = new CardApplicationConnect();
		conReq.setCardApplicationPath(path);
		conReq.setExclusiveUse(false);

		CardApplicationConnectResponse conRes = (CardApplicationConnectResponse) dispatcher.safeDeliver(conReq);
		WSHelper.checkResult(conRes);
		connectedCards.add(conRes.getConnectionHandle());
	    } catch (WSHelper.WSException ex) {
		LOG.error("Failed to connect card, skipping this entry.", ex);
	    }
	}

	return connectedCards;
    }

    @Nonnull
    private String getPinDid(ConnectionHandleType handle) throws WSException {
	// get all DIDs
	DIDList listReq = new DIDList();
	listReq.setConnectionHandle(handle);
	DIDListResponse listRes = (DIDListResponse) dispatcher.safeDeliver(listReq);
	WSHelper.checkResult(listRes);

	// find pin did
	for (String didName : listRes.getDIDNameList().getDIDName()) {
	    DIDGet getReq = new DIDGet();
	    getReq.setConnectionHandle(handle);
	    getReq.setDIDName(didName);
	    DIDGetResponse getRes = (DIDGetResponse) dispatcher.safeDeliver(getReq);
	    // don't check result, just see if we have a response
	    DIDStructureType struct = getRes.getDIDStructure();
	    if (struct != null) {
		if ("urn:oid:1.3.162.15480.3.0.9".equals(struct.getDIDMarker().getProtocol())) {
		    return didName;
		}
	    }
	}

	Result r = WSHelper.makeResultError(ECardConstants.Minor.SAL.INAPPROPRIATE_PROTOCOL_FOR_ACTION, "No PIN DID found.");
	throw WSHelper.createException(r);
    }

}
