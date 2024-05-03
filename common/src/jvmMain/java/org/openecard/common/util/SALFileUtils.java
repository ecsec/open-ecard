/****************************************************************************
 * Copyright (C) 2014-2016 ecsec GmbH.
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

package org.openecard.common.util;

import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationList;
import iso.std.iso_iec._24727.tech.schema.CardApplicationListResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDList;
import iso.std.iso_iec._24727.tech.schema.DIDListResponse;
import iso.std.iso_iec._24727.tech.schema.DataSetList;
import iso.std.iso_iec._24727.tech.schema.DataSetListResponse;
import java.util.List;
import javax.annotation.Nonnull;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.interfaces.Dispatcher;


/**
 * Utility class for easier selection of DataSets and Applications.
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
public class SALFileUtils {

    private final Dispatcher dispatcher;

    /**
     * Creates a SALFileUtils instance with the given dispatcher instance.
     *
     * @param dispatcher The dispatcher which will be used to deliver the eCard messages.
     */
    public SALFileUtils(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    /**
     * The method connects the given card to the CardApplication containing the requested DataSet.
     *
     * @param dataSetName Name of the DataSet which should be contained in the application to connect.
     * @param handle ConnectionHandle which identifies the card and terminal.
     * @return The handle describing the new state of the card.
     * @throws WSException Thrown in case any of the requested eCard API methods returned an error, or no application of
     *   the specified card contains the requested data set.
     */
    @Nonnull
    public ConnectionHandleType selectAppByDataSet(@Nonnull String dataSetName, @Nonnull ConnectionHandleType handle)
	    throws WSException {
	// copy handle so that the given handle is not damaged
	handle = HandlerUtils.copyHandle(handle);

	// get all card applications
	CardApplicationList cardApps = new CardApplicationList();
	cardApps.setConnectionHandle(handle);
	CardApplicationListResponse cardAppsResp = (CardApplicationListResponse) dispatcher.safeDeliver(cardApps);
	WSHelper.checkResult(cardAppsResp);
	List<byte[]> cardApplications = cardAppsResp.getCardApplicationNameList().getCardApplicationName();

	// check if our data set is in any of the applications
	for (byte[] app : cardApplications) {
	    DataSetList dataSetListReq = new DataSetList();
	    handle.setCardApplication(app);
	    dataSetListReq.setConnectionHandle(handle);
	    DataSetListResponse dataSetListResp = (DataSetListResponse) dispatcher.safeDeliver(dataSetListReq);
	    WSHelper.checkResult(dataSetListResp);

	    if (dataSetListResp.getDataSetNameList().getDataSetName().contains(dataSetName)) {
		handle = selectApplication(app, handle);
		return handle;
	    }
	}

	// data set not found
	String msg = "Failed to find the requested data set (%s) in any of the applications of the specified card.";
	msg = String.format(msg, dataSetName);
	Result r = WSHelper.makeResultError(ECardConstants.Minor.SAL.FILE_NOT_FOUND, msg);
	throw WSHelper.createException(r);
    }

    /**
     * The method connects the given card to the CardApplication containing the requested DID Name.
     *
     * @param didName Name of the DID which is contained in the application to connect.
     * @param handle ConnectionHandle which identifies Card and Terminal.
     * @return The handle describing the new state of the card.
     * @throws WSException Thrown in case any of the requested eCard API methods returned an error, or no application of
     *   the specified card contains the requested DID name.
     */
    @Nonnull
    public ConnectionHandleType selectAppByDID(@Nonnull String didName, @Nonnull ConnectionHandleType handle)
	    throws WSException {
	// copy handle so that the given handle is not damaged
	handle = HandlerUtils.copyHandle(handle);

	// get all card applications
	CardApplicationList cardApps = new CardApplicationList();
	cardApps.setConnectionHandle(handle);
	CardApplicationListResponse cardAppsResp = (CardApplicationListResponse) dispatcher.safeDeliver(cardApps);
	WSHelper.checkResult(cardAppsResp);
	List<byte[]> cardApplications = cardAppsResp.getCardApplicationNameList().getCardApplicationName();

	// check if our data set is in any of the applications
	for (byte[] app : cardApplications) {
	    DIDList didListReq = new DIDList();
	    handle.setCardApplication(app);
	    didListReq.setConnectionHandle(handle);
	    DIDListResponse didListResp = (DIDListResponse) dispatcher.safeDeliver(didListReq);
	    WSHelper.checkResult(didListResp);

	    if (didListResp.getDIDNameList().getDIDName().contains(didName)) {
		handle = selectApplication(app, handle);
		return handle;
	    }
	}

	// data set not found
	String msg = "Failed to find the requested DID (%s) in any of the applications of the specified card.";
	msg = String.format(msg, didName);
	Result r = WSHelper.makeResultError(ECardConstants.Minor.SAL.FILE_NOT_FOUND, msg);
	throw WSHelper.createException(r);
    }

    /**
     * Performs a CardApplicationConnect SAL call for the given handle.
     * The path part of the handle is used as a basis to connect the card.
     *
     * @param appId
     * @param handle
     * @return The handle of the card after performing the connect.
     * @throws org.openecard.common.WSHelper.WSException
     */
    public ConnectionHandleType selectApplication(@Nonnull byte[] appId, @Nonnull ConnectionHandleType handle)
	    throws WSException {
	CardApplicationConnect appConnectReq = new CardApplicationConnect();
	// copy path part of the handle and use it to identify the card
	CardApplicationPathType path = HandlerUtils.copyPath(handle);
	path.setCardApplication(appId);
	appConnectReq.setCardApplicationPath(path);
	CardApplicationConnectResponse resp = (CardApplicationConnectResponse) dispatcher.safeDeliver(appConnectReq);
	WSHelper.checkResult(resp);
	return resp.getConnectionHandle();
    }

}
