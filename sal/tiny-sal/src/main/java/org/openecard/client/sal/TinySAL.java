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

package org.openecard.client.sal;

import iso.std.iso_iec._24727.tech.schema.ACLList;
import iso.std.iso_iec._24727.tech.schema.ACLListResponse;
import iso.std.iso_iec._24727.tech.schema.ACLModify;
import iso.std.iso_iec._24727.tech.schema.ACLModifyResponse;
import iso.std.iso_iec._24727.tech.schema.AuthorizationServiceActionName;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationCreate;
import iso.std.iso_iec._24727.tech.schema.CardApplicationCreateResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDelete;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDeleteResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationEndSession;
import iso.std.iso_iec._24727.tech.schema.CardApplicationEndSessionResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationList;
import iso.std.iso_iec._24727.tech.schema.CardApplicationListResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationListResponse.CardApplicationNameList;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse.CardAppPathResultSet;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceActionName;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceCreate;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceCreateResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceDelete;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceDeleteResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceDescribe;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceDescribeResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceList;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceListResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceLoad;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceLoadResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationStartSession;
import iso.std.iso_iec._24727.tech.schema.CardApplicationStartSessionResponse;
import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionServiceActionName;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.DIDCreate;
import iso.std.iso_iec._24727.tech.schema.DIDCreateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDDelete;
import iso.std.iso_iec._24727.tech.schema.DIDDeleteResponse;
import iso.std.iso_iec._24727.tech.schema.DIDGet;
import iso.std.iso_iec._24727.tech.schema.DIDGetResponse;
import iso.std.iso_iec._24727.tech.schema.DIDInfoType;
import iso.std.iso_iec._24727.tech.schema.DIDList;
import iso.std.iso_iec._24727.tech.schema.DIDListResponse;
import iso.std.iso_iec._24727.tech.schema.DIDNameListType;
import iso.std.iso_iec._24727.tech.schema.DIDQualifierType;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DIDStructureType;
import iso.std.iso_iec._24727.tech.schema.DIDUpdate;
import iso.std.iso_iec._24727.tech.schema.DIDUpdateResponse;
import iso.std.iso_iec._24727.tech.schema.DSICreate;
import iso.std.iso_iec._24727.tech.schema.DSICreateResponse;
import iso.std.iso_iec._24727.tech.schema.DSIDelete;
import iso.std.iso_iec._24727.tech.schema.DSIDeleteResponse;
import iso.std.iso_iec._24727.tech.schema.DSIList;
import iso.std.iso_iec._24727.tech.schema.DSIListResponse;
import iso.std.iso_iec._24727.tech.schema.DSIRead;
import iso.std.iso_iec._24727.tech.schema.DSIReadResponse;
import iso.std.iso_iec._24727.tech.schema.DSIWrite;
import iso.std.iso_iec._24727.tech.schema.DSIWriteResponse;
import iso.std.iso_iec._24727.tech.schema.DataSetCreate;
import iso.std.iso_iec._24727.tech.schema.DataSetCreateResponse;
import iso.std.iso_iec._24727.tech.schema.DataSetDelete;
import iso.std.iso_iec._24727.tech.schema.DataSetDeleteResponse;
import iso.std.iso_iec._24727.tech.schema.DataSetInfoType;
import iso.std.iso_iec._24727.tech.schema.DataSetList;
import iso.std.iso_iec._24727.tech.schema.DataSetListResponse;
import iso.std.iso_iec._24727.tech.schema.DataSetNameListType;
import iso.std.iso_iec._24727.tech.schema.DataSetSelect;
import iso.std.iso_iec._24727.tech.schema.DataSetSelectResponse;
import iso.std.iso_iec._24727.tech.schema.Decipher;
import iso.std.iso_iec._24727.tech.schema.DecipherResponse;
import iso.std.iso_iec._24727.tech.schema.DifferentialIdentityServiceActionName;
import iso.std.iso_iec._24727.tech.schema.Disconnect;
import iso.std.iso_iec._24727.tech.schema.DisconnectResponse;
import iso.std.iso_iec._24727.tech.schema.Encipher;
import iso.std.iso_iec._24727.tech.schema.EncipherResponse;
import iso.std.iso_iec._24727.tech.schema.ExecuteAction;
import iso.std.iso_iec._24727.tech.schema.ExecuteActionResponse;
import iso.std.iso_iec._24727.tech.schema.GetRandom;
import iso.std.iso_iec._24727.tech.schema.GetRandomResponse;
import iso.std.iso_iec._24727.tech.schema.Hash;
import iso.std.iso_iec._24727.tech.schema.HashResponse;
import iso.std.iso_iec._24727.tech.schema.Initialize;
import iso.std.iso_iec._24727.tech.schema.InitializeResponse;
import iso.std.iso_iec._24727.tech.schema.NamedDataServiceActionName;
import iso.std.iso_iec._24727.tech.schema.Sign;
import iso.std.iso_iec._24727.tech.schema.SignResponse;
import iso.std.iso_iec._24727.tech.schema.TargetNameType;
import iso.std.iso_iec._24727.tech.schema.Terminate;
import iso.std.iso_iec._24727.tech.schema.TerminateResponse;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificate;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificateResponse;
import iso.std.iso_iec._24727.tech.schema.VerifySignature;
import iso.std.iso_iec._24727.tech.schema.VerifySignatureResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.ECardException;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.apdu.Select;
import org.openecard.client.common.apdu.common.CardCommandAPDU;
import org.openecard.client.common.apdu.utils.CardUtils;
import org.openecard.client.common.interfaces.Environment;
import org.openecard.client.common.sal.Assert;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.Protocol;
import org.openecard.client.common.sal.ProtocolFactory;
import org.openecard.client.common.sal.anytype.CryptoMarkerType;
import org.openecard.client.common.sal.exception.IncorrectParameterException;
import org.openecard.client.common.sal.exception.UnknownConnectionHandleException;
import org.openecard.client.common.sal.exception.UnknownProtocolException;
import org.openecard.client.common.sal.state.CardStateEntry;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.sal.state.cif.CardApplicationWrapper;
import org.openecard.client.common.sal.state.cif.CardInfoWrapper;
import org.openecard.client.common.sal.util.SALUtils;
import org.openecard.client.gui.UserConsent;
import org.openecard.ws.SAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a Service Access Layer (SAL).
 * 
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Simon Potzernheim <potzernheim@hs-coburg.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TinySAL implements SAL {

    private static final Logger logger = LoggerFactory.getLogger(TinySAL.class);
    private final Environment env;
    private final CardStateMap states;
    private ProtocolFactories protocolFactories = new ProtocolFactories();
    private UserConsent userConsent;

    /**
     * Creates a new TinySAL.
     *
     * @param env Environment
     * @param states CardStateMap
     */
    public TinySAL(Environment env, CardStateMap states) {
	this.env = env;
	this.states = states;
    }

    /**
     * The Initialize function is executed when the ISO24727-3-Interface is invoked for the first time.
     * The interface is initialised with this function.
     * See BSI-TR-03112-4, version 1.1.2, section 3.1.1.
     *
     * @param initialize Initialize
     * @return InitializeResponse
     */
    @Override
    public InitializeResponse initialize(Initialize initialize) {
	return WSHelper.makeResponse(InitializeResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The Terminate function is executed when the ISO24727-3-Interface is terminated.
     * This function closes all established connections and open sessions.
     * See BSI-TR-03112-4, version 1.1.2, section 3.1.2.
     *
     * @param terminate Terminate
     * @return TerminateResponse
     */
    @Override
    public TerminateResponse terminate(Terminate terminate) {
	return WSHelper.makeResponse(TerminateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The CardApplicationPath function determines a path between the client application and a card application.
     * See BSI-TR-03112-4, version 1.1.2, section 3.1.3.
     *
     * @param cardApplicationPath CardApplicationPath
     * @return CardApplicationPathResponse
     */
    @Override
    public CardApplicationPathResponse cardApplicationPath(CardApplicationPath cardApplicationPath) {
	CardApplicationPathResponse response = WSHelper.makeResponse(CardApplicationPathResponse.class, WSHelper.makeResultOK());

	try {
	    CardApplicationPathType cardAppPath = cardApplicationPath.getCardAppPathRequest();
	    Assert.assertIncorrectParameter(cardAppPath, "The parameter CardAppPathRequest is empty.");

	    Set<CardStateEntry> entries = states.getMatchingEntries(cardAppPath);

	    // Copy entries to result set
	    CardAppPathResultSet resultSet = new CardAppPathResultSet();
	    List<CardApplicationPathType> resultPaths = resultSet.getCardApplicationPathResult();
	    for (CardStateEntry entry : entries) {
		CardApplicationPathType pathCopy = entry.pathCopy();
		if (cardAppPath.getCardApplication() != null) {
		    pathCopy.setCardApplication(cardAppPath.getCardApplication());
		} else {
		    pathCopy.setCardApplication(entry.getImplicitlySelectedApplicationIdentifier());
		}
		resultPaths.add(pathCopy);
	    }

	    response.setCardAppPathResultSet(resultSet);
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The CardApplicationConnect function establishes an unauthenticated connection between the client
     * application and the card application.
     * See BSI-TR-03112-4, version 1.1.2, section 3.2.1.
     *
     * @param cardApplicationConnect CardApplicationConnect
     * @return CardApplicationConnectResponse
     */
    @Override
    public CardApplicationConnectResponse cardApplicationConnect(CardApplicationConnect cardApplicationConnect) {
	CardApplicationConnectResponse response = WSHelper.makeResponse(CardApplicationConnectResponse.class, WSHelper.makeResultOK());

	try {
	    CardApplicationPathType cardAppPath = cardApplicationConnect.getCardApplicationPath();
	    Assert.assertIncorrectParameter(cardAppPath, "The parameter CardAppPathRequest is empty.");

	    Set<CardStateEntry> cardStateEntrySet = states.getMatchingEntries(cardAppPath);
	    Assert.assertIncorrectParameter(cardStateEntrySet, "The given ConnectionHandle is invalid.");

	    /*
	     * [TR-03112-4] If the provided path fragments are valid for more than one card application
	     * the eCard-API-Framework SHALL return any of the possible choices.
	     */
	    CardStateEntry cardStateEntry = cardStateEntrySet.iterator().next();
	    byte[] applicationID = cardAppPath.getCardApplication();
	    Assert.securityConditionApplication(cardStateEntry, applicationID, ConnectionServiceActionName.CARD_APPLICATION_CONNECT);

	    // Connect to the card
	    CardApplicationPathType cardApplicationPath = cardStateEntry.pathCopy();
	    Connect connect = new Connect();
	    connect.setContextHandle(cardApplicationPath.getContextHandle());
	    connect.setIFDName(cardApplicationPath.getIFDName());
	    connect.setSlot(cardApplicationPath.getSlotIndex());

	    ConnectResponse connectResponse = (ConnectResponse) env.getDispatcher().deliver(connect);
	    WSHelper.checkResult(connectResponse);

	    // Select the card application
	    CardCommandAPDU select;
	    if (Arrays.equals(applicationID, Select.MasterFile.MF_FID)) {
		select = new Select.MasterFile();
	    } else {
		select = new Select.Application(applicationID);
	    }
	    select.transmit(env.getDispatcher(), connectResponse.getSlotHandle());

	    cardStateEntry.setCurrentCardApplication(applicationID);
	    cardStateEntry.setSlotHandle(connectResponse.getSlotHandle());
	    states.addEntry(cardStateEntry);

	    response.setConnectionHandle(cardStateEntry.handleCopy());
	    response.getConnectionHandle().setCardApplication(applicationID);
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The CardApplicationDisconnect function terminates the connection to a card application.
     * See BSI-TR-03112-4, version 1.1.2, section 3.2.2.
     *
     * @param cardApplicationDisconnect CardApplicationDisconnect
     * @return CardApplicationDisconnectResponse
     */
    @Override
    public CardApplicationDisconnectResponse cardApplicationDisconnect(CardApplicationDisconnect cardApplicationDisconnect) {
	CardApplicationDisconnectResponse response = WSHelper.makeResponse(CardApplicationDisconnectResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(cardApplicationDisconnect);
	    byte[] slotHandle = connectionHandle.getSlotHandle();

	    // check existence of required parameters
	    if (slotHandle == null) {
		return WSHelper.makeResponse(CardApplicationDisconnectResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "ConnectionHandle is null"));
	    }
	    // CardApplicationDisconnect only operates on slotHandle
	    // cardStateMap must only have this one param
	    connectionHandle = new ConnectionHandleType();
	    connectionHandle.setSlotHandle(slotHandle);

	    Disconnect disconnect = new Disconnect();
	    disconnect.setSlotHandle(connectionHandle.getSlotHandle());
	    DisconnectResponse disconnectResponse = (DisconnectResponse) env.getDispatcher().deliver(disconnect);

	    // remove entries associated with this handle
	    states.removeEntry(connectionHandle);

	    response.setResult(disconnectResponse.getResult());
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * This CardApplicationStartSession function starts a session between the client application and the card application.
     * See BSI-TR-03112-4, version 1.1.2, section 3.2.3.
     *
     * @param cardApplicationStartSession CardApplicationStartSession
     * @return CardApplicationStartSessionResponse
     */
    @Override
    public CardApplicationStartSessionResponse cardApplicationStartSession(CardApplicationStartSession cardApplicationStartSession) {
	return WSHelper.makeResponse(CardApplicationStartSessionResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The CardApplicationEndSession function closes the session between the client application and the card application.
     * See BSI-TR-03112-4, version 1.1.2, section 3.2.4.
     *
     * @param cardApplicationEndSession CardApplicationEndSession
     * @return CardApplicationEndSessionResponse
     */
    @Override
    public CardApplicationEndSessionResponse cardApplicationEndSession(CardApplicationEndSession cardApplicationEndSession) {
	return WSHelper.makeResponse(CardApplicationEndSessionResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The CardApplicationList function returns a list of the available card applications on an eCard.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.1.
     *
     * @param cardApplicationList CardApplicationList
     * @return CardApplicationListResponse
     */
    @Override
    public CardApplicationListResponse cardApplicationList(CardApplicationList cardApplicationList) {
	CardApplicationListResponse response = WSHelper.makeResponse(CardApplicationListResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(cardApplicationList);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] cardApplicationID = connectionHandle.getCardApplication();

	    Assert.securityConditionApplication(cardStateEntry, cardApplicationID, CardApplicationServiceActionName.CARD_APPLICATION_LIST);

	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    CardApplicationNameList cardApplicationNameList = new CardApplicationNameList();
	    cardApplicationNameList.getCardApplicationName().addAll(cardInfoWrapper.getCardApplicationNameList());

	    response.setCardApplicationNameList(cardApplicationNameList);
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * A new card application is created on an eCard with the CardApplicationCreate function.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.2.
     *
     * @param cardApplicationCreate CardApplicationCreate
     * @return CardApplicationCreateResponse
     */
    @Override
    public CardApplicationCreateResponse cardApplicationCreate(CardApplicationCreate cardApplicationCreate) {
	return WSHelper.makeResponse(CardApplicationCreateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The CardApplicationDelete function deletes a card application as well as all corresponding
     * data sets, DSIs, DIDs and services.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.3.
     *
     * @param cardApplicationDelete CardApplicationDelete
     * @return CardApplicationDeleteResponse
     */
    @Override
    public CardApplicationDeleteResponse cardApplicationDelete(CardApplicationDelete cardApplicationDelete) {
	return WSHelper.makeResponse(CardApplicationDeleteResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The CardApplicationServiceList function returns a list of all avail-able services of a card application.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.4.
     *
     * @param cardApplicationServiceList CardApplicationServiceList
     * @return CardApplicationServiceListResponse
     */
    @Override
    public CardApplicationServiceListResponse cardApplicationServiceList(CardApplicationServiceList cardApplicationServiceList) {
	return WSHelper.makeResponse(CardApplicationServiceListResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The CardApplicationServiceCreate function creates a new service in the card application.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.5.
     *
     * @param cardApplicationServiceCreate CardApplicationServiceCreate
     * @return CardApplicationServiceCreateResponse
     */
    @Override
    public CardApplicationServiceCreateResponse cardApplicationServiceCreate(CardApplicationServiceCreate cardApplicationServiceCreate) {
	return WSHelper.makeResponse(CardApplicationServiceCreateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * Code for a specific card application service was loaded into the card application with the aid
     * of the CardApplicationServiceLoad function.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.6.
     *
     * @param cardApplicationServiceLoad CardApplicationServiceLoad
     * @return CardApplicationServiceLoadResponse
     */
    @Override
    public CardApplicationServiceLoadResponse cardApplicationServiceLoad(CardApplicationServiceLoad cardApplicationServiceLoad) {
	return WSHelper.makeResponse(CardApplicationServiceLoadResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The CardApplicationServiceDelete function deletes a card application service in a card application.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.7.
     *
     * @param cardApplicationServiceDelete CardApplicationServiceDelete
     * @return CardApplicationServiceDeleteResponse
     */
    @Override
    public CardApplicationServiceDeleteResponse cardApplicationServiceDelete(CardApplicationServiceDelete cardApplicationServiceDelete) {
	return WSHelper.makeResponse(CardApplicationServiceDeleteResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The CardApplicationServiceDescribe function can be used to request an URI, an URL or a detailed description
     * of the selected card application service.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.8.
     *
     * @param cardApplicationServiceDescribe CardApplicationServiceDescribe
     * @return CardApplicationServiceDescribeResponse
     */
    @Override
    public CardApplicationServiceDescribeResponse cardApplicationServiceDescribe(CardApplicationServiceDescribe cardApplicationServiceDescribe) {
	return WSHelper.makeResponse(CardApplicationServiceDescribeResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The ExecuteAction function permits use of additional card application services by the client application
     * which are not explicitly specified in [ISO24727-3] but which can be implemented by the eCard with additional code.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.9.
     *
     * @param excuteAction ExecuteAction
     * @return ExecuteActionResponse
     */
    @Override
    public ExecuteActionResponse executeAction(ExecuteAction excuteAction) {
	return WSHelper.makeResponse(ExecuteActionResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The DataSetList function returns the list of the data sets in the card application addressed with the ConnectionHandle.
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.1.
     *
     * @param dataSetList DataSetList
     * @return DataSetListResponse
     */
    @Override
    public DataSetListResponse dataSetList(DataSetList dataSetList) {
	DataSetListResponse response = WSHelper.makeResponse(DataSetListResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(dataSetList);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] cardApplicationID = connectionHandle.getCardApplication();

	    Assert.securityConditionApplication(cardStateEntry, cardApplicationID, NamedDataServiceActionName.DATA_SET_LIST);

	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    DataSetNameListType dataSetNameList = cardInfoWrapper.getDataSetNameList(cardApplicationID);

	    response.setDataSetNameList(dataSetNameList);
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The DataSetCreate function creates a new data set in the card application addressed with the
     * ConnectionHandle (or otherwise in a previously selected data set if this is implemented as a DF).
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.2.
     *
     * @param dataSetCreate DataSetCreate
     * @return DataSetCreateResponse
     */
    @Override
    public DataSetCreateResponse dataSetCreate(DataSetCreate dataSetCreate) {
	return WSHelper.makeResponse(DataSetCreateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The DataSetSelect function selects a data set in a card application.
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.3.
     *
     * @param dataSetSelect DataSetSelect
     * @return DataSetSelectResponse
     */
    @Override
    public DataSetSelectResponse dataSetSelect(DataSetSelect dataSetSelect) {
	DataSetSelectResponse response = WSHelper.makeResponse(DataSetSelectResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(dataSetSelect);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);

	    String dataSetName = dataSetSelect.getDataSetName();
	    Assert.assertIncorrectParameter(dataSetName, "The parameter DataSetName is empty.");

	    byte[] applicationID = connectionHandle.getCardApplication();
	    Assert.securityConditionDataSet(cardStateEntry, applicationID, dataSetName, NamedDataServiceActionName.DATA_SET_SELECT);

	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    DataSetInfoType dataSetInfo = cardInfoWrapper.getDataSet(dataSetName, applicationID);
	    Assert.assertNamedEntityNotFound(dataSetInfo, "The given DataSet cannot be found.");

	    byte[] fileID = dataSetInfo.getDataSetPath().getEfIdOrPath();
	    byte[] slotHandle = connectionHandle.getSlotHandle();
	    CardCommandAPDU selectEF = new Select.File(fileID);
	    selectEF.transmit(env.getDispatcher(), slotHandle);
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The DataSetDelete function deletes a data set of a card application on an eCard.
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.4.
     *
     * @param dataSetDelete DataSetDelete
     * @return DataSetDeleteResponse
     */
    @Override
    public DataSetDeleteResponse dataSetDelete(DataSetDelete dataSetDelete) {
	return WSHelper.makeResponse(DataSetDeleteResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The function DSIList supplies the list of the DSI (Data Structure for Interoperability) which exist in the selected data set.
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.5.
     *
     * @param didList DSIList
     * @return DSIListResponse
     */
    @Override
    public DSIListResponse dsiList(DSIList didList) {
	return WSHelper.makeResponse(DSIListResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The DSICreate function creates a DSI (Data Structure for Interoperability) in the currently selected data set.
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.6.
     *
     * @param didCreate DSICreate
     * @return DSICreateResponse
     */
    @Override
    public DSICreateResponse dsiCreate(DSICreate didCreate) {
	return WSHelper.makeResponse(DSICreateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The DSIDelete function deletes a DSI (Data Structure for Interoperability) in the currently selected data set.
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.7.
     *
     * @param didDelete DSIDelete
     * @return DSIDeleteResponse
     */
    @Override
    public DSIDeleteResponse dsiDelete(DSIDelete didDelete) {
	return WSHelper.makeResponse(DSIDeleteResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The DSIWrite function changes the content of a DSI (Data Structure for Interoperability).
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.8.
     *
     * @param didWrite DSIWrite
     * @return DSIWriteResponse
     */
    @Override
    public DSIWriteResponse dsiWrite(DSIWrite didWrite) {
	return WSHelper.makeResponse(DSIWriteResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The DSIRead function reads out the content of a specific DSI (Data Structure for Interoperability).
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.9.
     *
     * @param dsiRead DSIRead
     * @return DSIReadResponse
     */
    @Override
    public DSIReadResponse dsiRead(DSIRead dsiRead) {
	DSIReadResponse response = WSHelper.makeResponse(DSIReadResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(dsiRead);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);

	    String dsiName = dsiRead.getDSIName();
	    Assert.assertIncorrectParameter(dsiName, "The parameter DSIName is empty.");

	    byte[] applicationID = connectionHandle.getCardApplication();
	    Assert.securityConditionDataSet(cardStateEntry, applicationID, dsiName, NamedDataServiceActionName.DSI_READ);

	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    DataSetInfoType dataSetInfo = cardInfoWrapper.getDataSet(dsiName, applicationID);
	    Assert.assertNamedEntityNotFound(dataSetInfo, "The given DSIName cannot be found.");

	    byte[] fileID = dataSetInfo.getDataSetPath().getEfIdOrPath();
	    byte[] slotHandle = connectionHandle.getSlotHandle();
	    byte[] fileContent = CardUtils.readFile(env.getDispatcher(), slotHandle, fileID);

	    response.setDSIContent(fileContent);
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The Encipher function encrypts a transmitted plain text. The detailed behaviour of this function depends on
     * the protocol of the DID.
     * See BSI-TR-03112-4, version 1.1.2, section 3.5.1.
     *
     * @param encipher Encipher
     * @return EncipherResponse
     */
    @Override
    public EncipherResponse encipher(Encipher encipher) {
	EncipherResponse response = WSHelper.makeResponse(EncipherResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(encipher);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    String didName = SALUtils.getDIDName(encipher);

	    byte[] plainText = encipher.getPlainText();
	    Assert.assertIncorrectParameter(plainText, "The parameter PlainText is empty.");

	    byte[] applicationID = connectionHandle.getCardApplication();
	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, applicationID);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    Protocol protocol = getProtocol(connectionHandle, protocolURI);
	    if (protocol.hasNextStep(FunctionType.Encipher)) {
		response = protocol.encipher(encipher);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new UnknownProtocolException("Encipher", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The Decipher function decrypts a given cipher text. The detailed behaviour of this function depends on
     * the protocol of the DID.
     * See BSI-TR-03112-4, version 1.1.2, section 3.5.2.
     *
     * @param decipher Decipher
     * @return DecipherResponse
     */
    @Override
    public DecipherResponse decipher(Decipher decipher) {
	DecipherResponse response = WSHelper.makeResponse(DecipherResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(decipher);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    String didName = SALUtils.getDIDName(decipher);

	    byte[] cipherText = decipher.getCipherText();
	    Assert.assertIncorrectParameter(cipherText, "The parameter CipherText is empty.");

	    byte[] applicationID = connectionHandle.getCardApplication();
	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, applicationID);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    Protocol protocol = getProtocol(connectionHandle, protocolURI);
	    if (protocol.hasNextStep(FunctionType.Decipher)) {
		response = protocol.decipher(decipher);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new UnknownProtocolException("Decipher", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The GetRandom function returns a random number which is suitable for authentication with the DID addressed with DIDName.
     * See BSI-TR-03112-4, version 1.1.2, section 3.5.3.
     *
     * @param getRandom GetRandom
     * @return GetRandomResponse
     */
    @Override
    public GetRandomResponse getRandom(GetRandom getRandom) {
	return WSHelper.makeResponse(GetRandomResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The Hash function calculates the hash value of a transmitted message.
     * See BSI-TR-03112-4, version 1.1.2, section 3.5.4.
     *
     * @param hash Hash
     * @return HashResponse
     */
    @Override
    public HashResponse hash(Hash hash) {
	return WSHelper.makeResponse(HashResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The Sign function signs a transmitted message.
     * See BSI-TR-03112-4, version 1.1.2, section 3.5.5.
     *
     * @param sign Sign
     * @return SignResponse
     */
    @Override
    public SignResponse sign(Sign sign) {
	SignResponse response = WSHelper.makeResponse(SignResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(sign);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    String didName = SALUtils.getDIDName(sign);

	    byte[] message = sign.getMessage();
	    Assert.assertIncorrectParameter(message, "The parameter Message is empty.");

	    byte[] applicationID = connectionHandle.getCardApplication();
	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, applicationID);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    Protocol protocol = getProtocol(connectionHandle, protocolURI);
	    if (protocol.hasNextStep(FunctionType.Sign)) {
		response = protocol.sign(sign);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new UnknownProtocolException("Sign", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The VerifySignature function verifies a digital signature.
     * See BSI-TR-03112-4, version 1.1.2, section 3.5.6.
     *
     * @param verifySignature VerifySignature
     * @return VerifySignatureResponse
     */
    @Override
    public VerifySignatureResponse verifySignature(VerifySignature verifySignature) {
	return WSHelper.makeResponse(VerifySignatureResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The VerifyCertificate function validates a given certificate.
     * See BSI-TR-03112-4, version 1.1.2, section 3.5.7.
     *
     * @param verifyCretificate VerifyCertificate
     * @return VerifyCertificateResponse
     */
    @Override
    public VerifyCertificateResponse verifyCertificate(VerifyCertificate verifyCretificate) {
	return WSHelper.makeResponse(VerifyCertificateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The DIDList function returns a list of the existing DIDs in the card application addressed by the
     * ConnectionHandle or the ApplicationIdentifier element within the Filter.
     * See BSI-TR-03112-4, version 1.1.2, section 3.6.1.
     *
     * @param didList DIDList
     * @return DIDListResponse
     */
    @Override
    public DIDListResponse didList(DIDList didList) {
	DIDListResponse response = WSHelper.makeResponse(DIDListResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(didList);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);

	    byte[] applicationID = connectionHandle.getCardApplication();
	    Assert.securityConditionApplication(cardStateEntry, applicationID, DifferentialIdentityServiceActionName.DID_LIST);

	    byte[] applicationIDFilter = null;
	    String objectIDFilter = null;
	    String applicationFunctionFilter = null;

	    DIDQualifierType didQualifier = didList.getFilter();
	    if (didQualifier != null) {
		applicationIDFilter = didQualifier.getApplicationIdentifier();
		objectIDFilter = didQualifier.getObjectIdentifier();
		applicationFunctionFilter = didQualifier.getApplicationFunction();
	    }

	    /*
	     * Filter by ApplicationIdentifier.
	     * [TR-03112-4] Allows specifying an application identifier. If this element is present all
	     * DIDs within the specified card application are returned no matter which card application
	     * is currently selected.
	     */
	    CardApplicationWrapper cardApplication;
	    if (applicationIDFilter != null) {
		cardApplication = cardStateEntry.getInfo().getCardApplication(applicationIDFilter);
		Assert.assertIncorrectParameter(cardApplication, "The given CardApplication cannot be found.");
	    } else {
		cardApplication = cardStateEntry.getCurrentCardApplication();
	    }

	    List<DIDInfoType> didInfos = cardApplication.getDIDInfoList();

	    /*
	     * Filter by ObjectIdentifier.
	     * [TR-03112-4] Allows specifying a protocol OID (cf. [TR-03112-7]) such that only DIDs
	     * which support a given protocol are listed.
	     */
	    if (objectIDFilter != null) {
		Iterator<DIDInfoType> it = didInfos.iterator();
		while (it.hasNext()) {
		    DIDInfoType next = it.next();
		    if (!next.getDifferentialIdentity().getDIDProtocol().equals(objectIDFilter)) {
			it.remove();
		    }
		}
	    }

	    /*
	     * Filter by ApplicationFunction.
	     * [TR-03112-4] Allows filtering for DIDs, which support a specific cryptographic operation.
	     * The bit string is coded as the SupportedOperations-element in [ISO7816-15].
	     */
	    if (applicationFunctionFilter != null) {
		Iterator<DIDInfoType> it = didInfos.iterator();
		while (it.hasNext()) {
		    DIDInfoType next = it.next();
		    if (next.getDifferentialIdentity().getDIDMarker().getCryptoMarker() == null) {
			it.remove();
		    } else {
			CryptoMarkerType cryptoMarker = new CryptoMarkerType(next.getDifferentialIdentity().getDIDMarker().getCryptoMarker());
			if (!cryptoMarker.getAlgorithmInfo().getSupportedOperations().contains(applicationFunctionFilter)) {
			    it.remove();
			}
		    }
		}
	    }

	    DIDNameListType didNameList = new DIDNameListType();
	    for (DIDInfoType didInfo : didInfos) {
		didNameList.getDIDName().add(didInfo.getDifferentialIdentity().getDIDName());
	    }

	    response.setDIDNameList(didNameList);
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The DIDCreate function creates a new differential identity in the card application addressed with ConnectionHandle.
     * See BSI-TR-03112-4, version 1.1.2, section 3.6.2.
     *
     * @param didCreate DIDCreate
     * @return DIDCreateResponse
     */
    @Override
    public DIDCreateResponse didCreate(DIDCreate didCreate) {
	return WSHelper.makeResponse(DIDCreateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The public information for a DID is read with the DIDGet function.
     * See BSI-TR-03112-4, version 1.1.2, section 3.6.3.
     *
     * @param didGet DIDGet
     * @return DIDGetResponse
     */
    @Override
    public DIDGetResponse didGet(DIDGet didGet) {
	DIDGetResponse response = WSHelper.makeResponse(DIDGetResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(didGet);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    String didName = SALUtils.getDIDName(didGet);

	    DIDStructureType didStructure;
	    if (didGet.getDIDScope() != null && didGet.getDIDScope().equals(DIDScopeType.GLOBAL)) {
		didStructure = cardStateEntry.getDIDStructure(didName, cardStateEntry.getImplicitlySelectedApplicationIdentifier());
	    } else {
		didStructure = cardStateEntry.getDIDStructure(didName, connectionHandle.getCardApplication());
	    }

	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    Protocol protocol = getProtocol(connectionHandle, protocolURI);
	    if (protocol.hasNextStep(FunctionType.DIDGet)) {
		response = protocol.didGet(didGet);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new UnknownProtocolException("DIDGet", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The DIDUpdate function creates a new key (marker) for the DID addressed with DIDName.
     * See BSI-TR-03112-4, version 1.1.2, section 3.6.4.
     *
     * @param didUpdate DIDUpdate
     * @return DIDUpdateResponse
     */
    @Override
    public DIDUpdateResponse didUpdate(DIDUpdate didUpdate) {
	return WSHelper.makeResponse(DIDUpdateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The DIDDelete function deletes the DID addressed with DIDName.
     * See BSI-TR-03112-4, version 1.1.2, section 3.6.5.
     *
     * @param didDelete DIDDelete
     * @return DIDDeleteResponse
     */
    @Override
    public DIDDeleteResponse didDelete(DIDDelete didDelete) {
	return WSHelper.makeResponse(DIDDeleteResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The DIDAuthenticate function can be used to execute an authentication protocol using a DID addressed by DIDName.
     * See BSI-TR-03112-4, version 1.1.2, section 3.6.6.
     *
     * @param didAuthenticate DIDAuthenticate
     * @return DIDAuthenticateResponse
     */
    @Override
    public DIDAuthenticateResponse didAuthenticate(DIDAuthenticate didAuthenticate) {
	DIDAuthenticateResponse response = WSHelper.makeResponse(DIDAuthenticateResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(didAuthenticate);

	    DIDAuthenticationDataType didAuthenticationData = didAuthenticate.getAuthenticationProtocolData();
	    Assert.assertIncorrectParameter(didAuthenticationData, "The parameter AuthenticationProtocolData is empty.");

	    String protocolURI = didAuthenticate.getAuthenticationProtocolData().getProtocol();
	    //FIXME workaround for missing protoUri from eID-Servers
	    if (protocolURI == null) {
		logger.warn("ProtocolURI was null");
		protocolURI = ECardConstants.Protocol.EAC;
	    } else if (protocolURI.equals("urn:oid:1.0.24727.3.0.0.7.2")) {
		logger.warn("ProtocolURI was urn:oid:1.0.24727.3.0.0.7.2");
		protocolURI = ECardConstants.Protocol.EAC;
	    }

	    Protocol protocol = getProtocol(connectionHandle, protocolURI);
	    if (protocol.hasNextStep(FunctionType.DIDAuthenticate)) {
		response = protocol.didAuthenticate(didAuthenticate);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new UnknownProtocolException("DIDAuthenticate", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The ACLList function returns the access control list for the stated target object (card application, data set, DID).
     * See BSI-TR-03112-4, version 1.1.2, section 3.7.1.
     *
     * @param aclList ACLList
     * @return ACLListResponse
     */
    @Override
    public ACLListResponse aclList(ACLList aclList) {
	ACLListResponse response = WSHelper.makeResponse(ACLListResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(aclList);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);

	    TargetNameType targetName = aclList.getTargetName();
	    Assert.assertIncorrectParameter(targetName, "The parameter TargetName is empty.");

	    String dataSetName = targetName.getDataSetName();
	    String didName = SALUtils.getDIDName(targetName);
	    byte[] cardApplicationID = targetName.getCardApplicationName();

	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    byte[] applicationIdentifier = connectionHandle.getCardApplication();

	    if (dataSetName != null) {
		DataSetInfoType dataSetInfo = cardInfoWrapper.getDataSet(dataSetName, applicationIdentifier);
		Assert.assertNamedEntityNotFound(dataSetInfo, "The given DataSet cannot be found.");
		response.setTargetACL(cardInfoWrapper.getDataSet(dataSetName, applicationIdentifier).getDataSetACL());
	    } else if (didName != null) {
		DIDInfoType didInfo = cardInfoWrapper.getDIDInfo(didName, applicationIdentifier);
		Assert.assertNamedEntityNotFound(didInfo, "The given DIDInfo cannot be found.");
		//TODO Check security condition ?
		response.setTargetACL(cardInfoWrapper.getDIDInfo(didName, applicationIdentifier).getDIDACL());
	    } else if (cardApplicationID != null) {
		CardApplicationWrapper cardApplication = cardInfoWrapper.getCardApplication(cardApplicationID);
		Assert.assertNamedEntityNotFound(cardApplication, "The given CardApplication cannot be found.");
		Assert.securityConditionApplication(cardStateEntry, cardApplicationID, AuthorizationServiceActionName.ACL_LIST);

		response.setTargetACL(cardInfoWrapper.getCardApplication(cardApplicationID).getCardApplicationACL());
	    } else {
		throw new IncorrectParameterException("The given TargetName is invalid.");
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * An access rule in the access control list is modified with the ACLModify function.
     * See BSI-TR-03112-4, version 1.1.2, section 3.7.2.
     *
     * @param aclModify ACLModify
     * @return ACLModifyResponse
     */
    @Override
    public ACLModifyResponse aclModify(ACLModify aclModify) {
	return WSHelper.makeResponse(ACLModifyResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * Sets the GUI.
     *
     * @param uc User consent
     */
    public void setGUI(UserConsent uc) {
	this.userConsent = uc;
    }

    /**
     * Returns a list of ConnectionHandles.
     *
     * @return List of ConnectionHandles
     */
    public List<ConnectionHandleType> getConnectionHandles() {
	ConnectionHandleType handle = new ConnectionHandleType();
	Set<CardStateEntry> entries = states.getMatchingEntries(handle);
	ArrayList<ConnectionHandleType> result = new ArrayList<ConnectionHandleType>(entries.size());

	for (CardStateEntry entry : entries) {
	    result.add(entry.handleCopy());
	}

	return result;
    }

    /**
     * Adds a protocol to the SAL instance.
     *
     * @param protocolURI Protocol URI
     * @param factory Protocol factory
     * @return True if the protocol is added, otherwise false
     */
    public boolean addProtocol(String protocolURI, ProtocolFactory factory) {
	return protocolFactories.add(protocolURI, factory);
    }

    /**
     * Removes a finished protocol from the SAL instance.
     *
     * @param handle Connection Handle
     * @param protocolURI Protocol URI
     * @param protocol Protocol
     * @throws UnknownConnectionHandleException
     */
    public void removeFinishedProtocol(ConnectionHandleType handle, String protocolURI, Protocol protocol)
	    throws UnknownConnectionHandleException {
	if (protocol.isFinished()) {
	    CardStateEntry entry = SALUtils.getCardStateEntry(states, handle);
	    entry.removeProtocol(protocolURI);
	}
    }

    private Protocol getProtocol(ConnectionHandleType handle, String protocolURI)
	    throws UnknownProtocolException, UnknownConnectionHandleException {
	CardStateEntry entry = SALUtils.getCardStateEntry(states, handle);
	Protocol protocol = entry.getProtocol(protocolURI);
	if (protocol == null) {
	    if (protocolFactories.contains(protocolURI)) {
		protocol = protocolFactories.get(protocolURI).createInstance(env.getDispatcher(), this.userConsent);
		entry.setProtocol(protocolURI, protocol);
	    } else {
		throw new UnknownProtocolException("The protocol URI '" + protocolURI + "' is not registered in this SAL component.");
	    }
	}
	protocol.getInternalData().put("cardState", entry);

	return protocol;
    }
    
}
