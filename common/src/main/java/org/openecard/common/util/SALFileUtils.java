/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDList;
import iso.std.iso_iec._24727.tech.schema.DIDListResponse;
import iso.std.iso_iec._24727.tech.schema.DataSetList;
import iso.std.iso_iec._24727.tech.schema.DataSetListResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.apdu.exception.APDUException;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;


/**
 * Utility class for easier selection of DataSets and Applications.
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class SALFileUtils {

    /**
     * The method connects a CardApplication by a DataSet name.
     * 
     * @param dataSetName Name of the DataSet which should be contained in the application to connect.
     * @param dispatcher Dispatcher for message delivery.
     * @param connectionHandle ConnectionHandle which identifies the card and terminal.
     * @throws DispatcherException
     * @throws InvocationTargetException
     * @throws org.openecard.common.WSHelper.WSException
     * @throws APDUException
     * @return 
     */
    public static ConnectionHandleType selectApplicationByDataSetName(String dataSetName, Dispatcher dispatcher,
	    ConnectionHandleType connectionHandle) throws DispatcherException, InvocationTargetException, WSException,
	    APDUException {
	CardApplicationPath appPathReq = new CardApplicationPath();
	CardApplicationPathType pType = new CardApplicationPathType();
	pType.setIFDName(connectionHandle.getIFDName());
	appPathReq.setCardAppPathRequest(pType);
	CardApplicationPathResponse appPathResp = (CardApplicationPathResponse) dispatcher.deliver(appPathReq);
	WSHelper.checkResult(appPathResp);

	connectionHandle.setCardApplication(
		appPathResp.getCardAppPathResultSet().getCardApplicationPathResult().get(0).getCardApplication());
	connectionHandle.setChannelHandle(
		appPathResp.getCardAppPathResultSet().getCardApplicationPathResult().get(0).getChannelHandle());
	connectionHandle.setContextHandle(
		appPathResp.getCardAppPathResultSet().getCardApplicationPathResult().get(0).getContextHandle());
	connectionHandle.setIFDName(
		appPathResp.getCardAppPathResultSet().getCardApplicationPathResult().get(0).getIFDName());
	connectionHandle.setSlotIndex(
		appPathResp.getCardAppPathResultSet().getCardApplicationPathResult().get(0).getSlotIndex());

	CardApplicationList cardApps = new CardApplicationList();
	cardApps.setConnectionHandle(connectionHandle);
	CardApplicationListResponse cardAppsResp = (CardApplicationListResponse) dispatcher.deliver(cardApps);
	WSHelper.checkResult(cardAppsResp);

	ConnectionHandleType handle2 = HandlerUtils.copyHandle(connectionHandle);
	appPathReq = new CardApplicationPath();
	pType = new CardApplicationPathType();
	pType.setIFDName(handle2.getIFDName());
	pType.setChannelHandle(handle2.getChannelHandle());
	pType.setContextHandle(handle2.getContextHandle());
	pType.setSlotIndex(handle2.getSlotIndex());
	appPathReq.setCardAppPathRequest(pType);
	appPathResp = (CardApplicationPathResponse) dispatcher.deliver(appPathReq);
	WSHelper.checkResult(appPathResp);

	connectionHandle.setCardApplication(
		appPathResp.getCardAppPathResultSet().getCardApplicationPathResult().get(0).getCardApplication());
	connectionHandle.setChannelHandle(
		appPathResp.getCardAppPathResultSet().getCardApplicationPathResult().get(0).getChannelHandle());
	connectionHandle.setContextHandle(
		appPathResp.getCardAppPathResultSet().getCardApplicationPathResult().get(0).getContextHandle());
	connectionHandle.setIFDName(
		appPathResp.getCardAppPathResultSet().getCardApplicationPathResult().get(0).getIFDName());
	connectionHandle.setSlotIndex(
		appPathResp.getCardAppPathResultSet().getCardApplicationPathResult().get(0).getSlotIndex());

	List<byte[]> cardApplications = cardAppsResp.getCardApplicationNameList().getCardApplicationName();
	for (byte[] app : cardApplications) {
	    DataSetList dataSetListReq = new DataSetList();
	    connectionHandle.setCardApplication(app);
	    dataSetListReq.setConnectionHandle(connectionHandle);
	    DataSetListResponse dataSetListResp = (DataSetListResponse) dispatcher.deliver(dataSetListReq);
	    WSHelper.checkResult(dataSetListResp);
	    
	    if (dataSetListResp.getDataSetNameList().getDataSetName().contains(dataSetName)) {
		connectionHandle = selectApplication(app, dispatcher, handle2);
		break;
	    }
	}

	return connectionHandle;
    }
    
    /**
     * The method connects a CardApplication by a DIDName.
     * 
     * @param didName Name of the DID which is contained in the application to connect.
     * @param dispatcher Dispatcher for message delivery.
     * @param connectionHandle ConnectionHandle which identifies Card and Terminal.
     * @throws DispatcherException
     * @throws InvocationTargetException
     * @throws org.openecard.common.WSHelper.WSException
     * @throws APDUException
     */
    public static void selectApplicationByDIDName(String didName, Dispatcher dispatcher,
	    ConnectionHandleType connectionHandle) throws DispatcherException, InvocationTargetException, WSException, 
	    APDUException {
	CardApplicationList cardApps = new CardApplicationList();
	cardApps.setConnectionHandle(connectionHandle);
	CardApplicationListResponse cardAppsResp = (CardApplicationListResponse) dispatcher.deliver(cardApps);
	WSHelper.checkResult(cardAppsResp);

	List<byte[]> cardApplications = cardAppsResp.getCardApplicationNameList().getCardApplicationName();
	for (byte[] app : cardApplications) {
	    DIDList didListReq = new DIDList();
	    connectionHandle.setCardApplication(app);
	    didListReq.setConnectionHandle(connectionHandle);
	    DIDListResponse didListResp = (DIDListResponse) dispatcher.deliver(didListReq);
	    WSHelper.checkResult(didListResp);

	    if (didListResp.getDIDNameList().getDIDName().contains(didName)) {
		selectApplication(app, dispatcher, connectionHandle);
		break;
	    }
	}
    }

    public static ConnectionHandleType selectApplication(byte[] applicationIdentifier, Dispatcher dispatcher, ConnectionHandleType handle)
	    throws DispatcherException, InvocationTargetException, WSException {
	CardApplicationConnect appConnectReq = new CardApplicationConnect();
	CardApplicationPathType path = new CardApplicationPathType();
	path.setCardApplication(applicationIdentifier);
	appConnectReq.setCardApplicationPath(path);
	CardApplicationConnectResponse appConnectResp = (CardApplicationConnectResponse) dispatcher.deliver(appConnectReq);
	WSHelper.checkResult(appConnectResp);
	return appConnectResp.getConnectionHandle();
    }

}
