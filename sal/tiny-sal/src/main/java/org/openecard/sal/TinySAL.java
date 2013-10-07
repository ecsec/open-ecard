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

package org.openecard.sal;

import iso.std.iso_iec._24727.tech.schema.ACLList;
import iso.std.iso_iec._24727.tech.schema.ACLListResponse;
import iso.std.iso_iec._24727.tech.schema.ACLModify;
import iso.std.iso_iec._24727.tech.schema.ACLModifyResponse;
import iso.std.iso_iec._24727.tech.schema.AlgorithmInfoType;
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.openecard.addon.AddonManager;
import org.openecard.addon.AddonNotFoundException;
import org.openecard.addon.AddonSelector;
import org.openecard.addon.HighestVersionSelector;
import org.openecard.addon.sal.FunctionType;
import org.openecard.addon.sal.SALProtocol;
import org.openecard.common.ECardConstants;
import org.openecard.common.ECardException;
import org.openecard.common.WSHelper;
import org.openecard.common.apdu.Select;
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.apdu.utils.CardUtils;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.sal.Assert;
import org.openecard.common.sal.anytype.CryptoMarkerType;
import org.openecard.common.sal.exception.InappropriateProtocolForActionException;
import org.openecard.common.sal.exception.IncorrectParameterException;
import org.openecard.common.sal.exception.UnknownConnectionHandleException;
import org.openecard.common.sal.exception.UnknownProtocolException;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.common.sal.state.cif.CardApplicationWrapper;
import org.openecard.common.sal.state.cif.CardInfoWrapper;
import org.openecard.common.sal.util.SALUtils;
import org.openecard.common.tlv.iso7816.FCP;
import org.openecard.gui.UserConsent;
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
 * @author Antonio de la Piedra <a.delapiedra@cs.ru.nl>
 */
public class TinySAL implements SAL {

    private static final Logger logger = LoggerFactory.getLogger(TinySAL.class);

    private final Environment env;
    private final CardStateMap states;
    private AddonSelector protocolSelector;
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

    public void setAddonManager(AddonManager manager) {
	protocolSelector = new AddonSelector(manager);
	protocolSelector.setStrategy(new HighestVersionSelector());
    }

    /**
     * The Initialize function is executed when the ISO24727-3-Interface is invoked for the first time.
     * The interface is initialised with this function.
     * See BSI-TR-03112-4, version 1.1.2, section 3.1.1.
     *
     * @param request Initialize
     * @return InitializeResponse
     */
    @Override
    public InitializeResponse initialize(Initialize request) {
	return WSHelper.makeResponse(InitializeResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The Terminate function is executed when the ISO24727-3-Interface is terminated.
     * This function closes all established connections and open sessions.
     * See BSI-TR-03112-4, version 1.1.2, section 3.1.2.
     *
     * @param request Terminate
     * @return TerminateResponse
     */
    @Override
    public TerminateResponse terminate(Terminate request) {
	return WSHelper.makeResponse(TerminateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The CardApplicationPath function determines a path between the client application and a card application.
     * See BSI-TR-03112-4, version 1.1.2, section 3.1.3.
     *
     * @param request CardApplicationPath
     * @return CardApplicationPathResponse
     */
    @Override
    public CardApplicationPathResponse cardApplicationPath(CardApplicationPath request) {
	CardApplicationPathResponse response = WSHelper.makeResponse(CardApplicationPathResponse.class, WSHelper.makeResultOK());

	try {
	    CardApplicationPathType cardAppPath = request.getCardAppPathRequest();
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
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The CardApplicationConnect function establishes an unauthenticated connection between the client
     * application and the card application.
     * See BSI-TR-03112-4, version 1.1.2, section 3.2.1.
     *
     * @param request CardApplicationConnect
     * @return CardApplicationConnectResponse
     */
    @Override
    public CardApplicationConnectResponse cardApplicationConnect(CardApplicationConnect request) {
	CardApplicationConnectResponse response = WSHelper.makeResponse(CardApplicationConnectResponse.class, WSHelper.makeResultOK());

	try {
	    CardApplicationPathType cardAppPath = request.getCardApplicationPath();
	    Assert.assertIncorrectParameter(cardAppPath, "The parameter CardAppPathRequest is empty.");

	    Set<CardStateEntry> cardStateEntrySet = states.getMatchingEntries(cardAppPath, false);
	    Assert.assertIncorrectParameter(cardStateEntrySet, "The given ConnectionHandle is invalid.");

	    /*
	     * [TR-03112-4] If the provided path fragments are valid for more than one card application
	     * the eCard-API-Framework SHALL return any of the possible choices.
	     */
	    CardStateEntry cardStateEntry = cardStateEntrySet.iterator().next();
	    byte[] applicationID = cardAppPath.getCardApplication();
	    if (applicationID == null) {
		applicationID = cardStateEntry.getImplicitlySelectedApplicationIdentifier();
	    }
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
	    // TODO: proper determination of path, file and app id
	    if (applicationID.length == 2) {
		select = new Select.File(applicationID);
	    } else {
		select = new Select.Application(applicationID);
	    }
	    select.transmit(env.getDispatcher(), connectResponse.getSlotHandle());

	    cardStateEntry.setCurrentCardApplication(applicationID);
	    cardStateEntry.setSlotHandle(connectResponse.getSlotHandle());
	    // reset the ef FCP
	    cardStateEntry.unsetFCPOfSelectedEF();
	    states.addEntry(cardStateEntry);

	    response.setConnectionHandle(cardStateEntry.handleCopy());
	    response.getConnectionHandle().setCardApplication(applicationID);
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The CardApplicationDisconnect function terminates the connection to a card application.
     * See BSI-TR-03112-4, version 1.1.2, section 3.2.2.
     *
     * @param request CardApplicationDisconnect
     * @return CardApplicationDisconnectResponse
     */
    @Override
    public CardApplicationDisconnectResponse cardApplicationDisconnect(CardApplicationDisconnect request) {
	CardApplicationDisconnectResponse response = WSHelper.makeResponse(CardApplicationDisconnectResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    byte[] slotHandle = connectionHandle.getSlotHandle();

	    // check existence of required parameters
	    if (slotHandle == null) {
		return WSHelper.makeResponse(CardApplicationDisconnectResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "ConnectionHandle is null"));
	    }

	    Disconnect disconnect = new Disconnect();
	    disconnect.setSlotHandle(slotHandle);
	    DisconnectResponse disconnectResponse = (DisconnectResponse) env.getDispatcher().deliver(disconnect);

	    // remove entries associated with this handle
	    states.removeSlotHandleEntry(slotHandle);

	    response.setResult(disconnectResponse.getResult());
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * This CardApplicationStartSession function starts a session between the client application and the card application.
     * See BSI-TR-03112-4, version 1.1.2, section 3.2.3.
     *
     * @param request CardApplicationStartSession
     * @return CardApplicationStartSessionResponse
     */
    @Override
    public CardApplicationStartSessionResponse cardApplicationStartSession(CardApplicationStartSession request) {
	CardApplicationStartSessionResponse response = WSHelper.makeResponse(CardApplicationStartSessionResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] cardApplicationID = connectionHandle.getCardApplication();
	    
	    String didName = SALUtils.getDIDName(request);
	    Assert.assertIncorrectParameter(didName, "The parameter didName is empty.");

	    DIDAuthenticationDataType didAuthenticationProtocolData = request.getAuthenticationProtocolData();
	    Assert.assertIncorrectParameter(didAuthenticationProtocolData, "The parameter didAuthenticationProtocolData is empty.");
	    
	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, cardApplicationID);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    DIDScopeType didScope = request.getDIDScope();
	    Assert.assertIncorrectParameter(didScope, "The parameter DIDScope is empty.");

	    ConnectionHandleType samConnectionHandle = request.getSAMConnectionHandle();
	    Assert.assertIncorrectParameter(samConnectionHandle, "The parameter SAMConnectionHandle is empty.");

	    Assert.securityConditionApplication(cardStateEntry, cardApplicationID, ConnectionServiceActionName.CARD_APPLICATION_START_SESSION);

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, protocolURI);
	    if (protocol.hasNextStep(FunctionType.CardApplicationStartSession)) {
		response = protocol.cardApplicationStartSession(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("CardApplicationStartSession", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The CardApplicationEndSession function closes the session between the client application and the card application.
     * See BSI-TR-03112-4, version 1.1.2, section 3.2.4.
     *
     * @param request CardApplicationEndSession
     * @return CardApplicationEndSessionResponse
     */
    @Override
    public CardApplicationEndSessionResponse cardApplicationEndSession(CardApplicationEndSession request) {
	CardApplicationEndSessionResponse response = WSHelper.makeResponse(CardApplicationEndSessionResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] cardApplicationID = connectionHandle.getCardApplication();
	    
	    /* DIDName is required for accesing the DID structure. However,
	       this parameter does not appears at ISO-24727-3.xsd. I've added it. */
	    
	    String didName = SALUtils.getDIDName(request);
	    Assert.assertIncorrectParameter(didName, "The parameter didName is empty.");
	    
	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, cardApplicationID);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    Assert.securityConditionApplication(cardStateEntry, cardApplicationID, ConnectionServiceActionName.CARD_APPLICATION_END_SESSION);

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, protocolURI);
	    if (protocol.hasNextStep(FunctionType.CardApplicationEndSession)) {
		response = protocol.cardApplicationEndSession(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("CardApplicationEndSession", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The CardApplicationList function returns a list of the available card applications on an eCard.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.1.
     *
     * @param request CardApplicationList
     * @return CardApplicationListResponse
     */
    @Override
    public CardApplicationListResponse cardApplicationList(CardApplicationList request) {
	CardApplicationListResponse response = WSHelper.makeResponse(CardApplicationListResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
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
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * A new card application is created on an eCard with the CardApplicationCreate function.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.2.
     *
     * @param request CardApplicationCreate
     * @return CardApplicationCreateResponse
     */
    @Override
    public CardApplicationCreateResponse cardApplicationCreate(CardApplicationCreate request) {
	CardApplicationCreateResponse response = WSHelper.makeResponse(CardApplicationCreateResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = states.getEntry(connectionHandle, false);
	    
	    SALUtils.getCardStateEntry(states, connectionHandle);
	    	    
	    byte[] cardApplicationName = request.getCardApplicationName();
	    Assert.assertIncorrectParameter(cardApplicationName, "The parameter CardApplicationName is empty.");

	    AccessControlListType cardApplicationACL = request.getCardApplicationACL();
	    Assert.assertIncorrectParameter(cardApplicationACL, "The parameter CardApplicationACL is empty.");
	    
	    CardApplicationType cardApplicationType = new CardApplicationType();
	    cardApplicationType.setApplicationIdentifier(cardApplicationName);
	    cardApplicationType.setCardApplicationACL(cardApplicationACL);
	    
	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    cardInfoWrapper.getApplicationCapabilities().getCardApplication().add(cardApplicationType);

	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The CardApplicationDelete function deletes a card application as well as all corresponding
     * data sets, DSIs, DIDs and services.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.3.
     *
     * @param request CardApplicationDelete
     * @return CardApplicationDeleteResponse
     */
    @Override
    public CardApplicationDeleteResponse cardApplicationDelete(CardApplicationDelete request) {
	return WSHelper.makeResponse(CardApplicationDeleteResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The CardApplicationServiceList function returns a list of all avail-able services of a card application.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.4.
     *
     * @param request CardApplicationServiceList
     * @return CardApplicationServiceListResponse
     */
    @Override
    public CardApplicationServiceListResponse cardApplicationServiceList(CardApplicationServiceList request) {
	return WSHelper.makeResponse(CardApplicationServiceListResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The CardApplicationServiceCreate function creates a new service in the card application.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.5.
     *
     * @param request CardApplicationServiceCreate
     * @return CardApplicationServiceCreateResponse
     */
    @Override
    public CardApplicationServiceCreateResponse cardApplicationServiceCreate(CardApplicationServiceCreate request) {
	return WSHelper.makeResponse(CardApplicationServiceCreateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * Code for a specific card application service was loaded into the card application with the aid
     * of the CardApplicationServiceLoad function.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.6.
     *
     * @param request CardApplicationServiceLoad
     * @return CardApplicationServiceLoadResponse
     */
    @Override
    public CardApplicationServiceLoadResponse cardApplicationServiceLoad(CardApplicationServiceLoad request) {
	return WSHelper.makeResponse(CardApplicationServiceLoadResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The CardApplicationServiceDelete function deletes a card application service in a card application.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.7.
     *
     * @param request CardApplicationServiceDelete
     * @return CardApplicationServiceDeleteResponse
     */
    @Override
    public CardApplicationServiceDeleteResponse cardApplicationServiceDelete(CardApplicationServiceDelete request) {
	return WSHelper.makeResponse(CardApplicationServiceDeleteResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The CardApplicationServiceDescribe function can be used to request an URI, an URL or a detailed description
     * of the selected card application service.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.8.
     *
     * @param request CardApplicationServiceDescribe
     * @return CardApplicationServiceDescribeResponse
     */
    @Override
    public CardApplicationServiceDescribeResponse cardApplicationServiceDescribe(CardApplicationServiceDescribe request) {
	return WSHelper.makeResponse(CardApplicationServiceDescribeResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The ExecuteAction function permits use of additional card application services by the client application
     * which are not explicitly specified in [ISO24727-3] but which can be implemented by the eCard with additional code.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.9.
     *
     * @param request ExecuteAction
     * @return ExecuteActionResponse
     */
    @Override
    public ExecuteActionResponse executeAction(ExecuteAction request) {
	return WSHelper.makeResponse(ExecuteActionResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The DataSetList function returns the list of the data sets in the card application addressed with the ConnectionHandle.
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.1.
     *
     * @param request DataSetList
     * @return DataSetListResponse
     */
    @Override
    public DataSetListResponse dataSetList(DataSetList request) {
	DataSetListResponse response = WSHelper.makeResponse(DataSetListResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] cardApplicationID = connectionHandle.getCardApplication();

	    Assert.securityConditionApplication(cardStateEntry, cardApplicationID, NamedDataServiceActionName.DATA_SET_LIST);

	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    DataSetNameListType dataSetNameList = cardInfoWrapper.getDataSetNameList(cardApplicationID);

	    response.setDataSetNameList(dataSetNameList);
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The DataSetCreate function creates a new data set in the card application addressed with the
     * ConnectionHandle (or otherwise in a previously selected data set if this is implemented as a DF).
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.2.
     *
     * @param request DataSetCreate
     * @return DataSetCreateResponse
     */
    @Override
    public DataSetCreateResponse dataSetCreate(DataSetCreate request) {
	return WSHelper.makeResponse(DataSetCreateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The DataSetSelect function selects a data set in a card application.
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.3.
     *
     * @param request DataSetSelect
     * @return DataSetSelectResponse
     */
    @Override
    public DataSetSelectResponse dataSetSelect(DataSetSelect request) {
	DataSetSelectResponse response = WSHelper.makeResponse(DataSetSelectResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] applicationID = connectionHandle.getCardApplication();
	    String dataSetName = request.getDataSetName();

	    Assert.assertIncorrectParameter(dataSetName, "The parameter DataSetName is empty.");

	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    DataSetInfoType dataSetInfo = cardInfoWrapper.getDataSet(dataSetName, applicationID);
	    Assert.assertNamedEntityNotFound(dataSetInfo, "The given DataSet cannot be found.");

	    Assert.securityConditionDataSet(cardStateEntry, applicationID, dataSetName, NamedDataServiceActionName.DATA_SET_SELECT);

	    byte[] fileID = dataSetInfo.getDataSetPath().getEfIdOrPath();
	    byte[] slotHandle = connectionHandle.getSlotHandle();
	    CardResponseAPDU result = CardUtils.selectFileWithOptions(env.getDispatcher(), slotHandle, fileID,
		    null, CardUtils.FCP_RESPONSE_DATA);

	    if (result != null) {
		cardStateEntry.setFCPOfSelectedEF(new FCP(result.getData()));
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The DataSetDelete function deletes a data set of a card application on an eCard.
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.4.
     *
     * @param request DataSetDelete
     * @return DataSetDeleteResponse
     */
    @Override
    public DataSetDeleteResponse dataSetDelete(DataSetDelete request) {
	return WSHelper.makeResponse(DataSetDeleteResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The function DSIList supplies the list of the DSI (Data Structure for Interoperability) which exist in the selected data set.
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.5.
     *
     * @param request DSIList
     * @return DSIListResponse
     */
    @Override
    public DSIListResponse dsiList(DSIList request) {
	return WSHelper.makeResponse(DSIListResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The DSICreate function creates a DSI (Data Structure for Interoperability) in the currently selected data set.
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.6.
     *
     * @param request DSICreate
     * @return DSICreateResponse
     */
    @Override
    public DSICreateResponse dsiCreate(DSICreate request) {
	return WSHelper.makeResponse(DSICreateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The DSIDelete function deletes a DSI (Data Structure for Interoperability) in the currently selected data set.
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.7.
     *
     * @param request DSIDelete
     * @return DSIDeleteResponse
     */
    @Override
    public DSIDeleteResponse dsiDelete(DSIDelete request) {
	return WSHelper.makeResponse(DSIDeleteResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The DSIWrite function changes the content of a DSI (Data Structure for Interoperability).
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.8.
     *
     * @param request DSIWrite
     * @return DSIWriteResponse
     */
    @Override
    public DSIWriteResponse dsiWrite(DSIWrite request) {
	DSIWriteResponse response = WSHelper.makeResponse(DSIWriteResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] applicationID = connectionHandle.getCardApplication();
	    String dsiName = request.getDSIName();
	    byte[] updateData = request.getDSIContent();

	    Assert.assertIncorrectParameter(dsiName, "The parameter DSIName is empty.");
	    Assert.assertIncorrectParameter(updateData, "The parameter DSIContent is empty.");

	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    DataSetInfoType dataSetInfo = cardInfoWrapper.getDataSet(dsiName, applicationID);
	    Assert.assertNamedEntityNotFound(dataSetInfo, "The given DSIName cannot be found.");

	    Assert.securityConditionDataSet(cardStateEntry, applicationID, dsiName, NamedDataServiceActionName.DSI_WRITE);

	    byte[] fileID = dataSetInfo.getDataSetPath().getEfIdOrPath();
	    byte[] slotHandle = connectionHandle.getSlotHandle();
	    CardUtils.writeFile(env.getDispatcher(), slotHandle, fileID, updateData);
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The DSIRead function reads out the content of a specific DSI (Data Structure for Interoperability).
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.9.
     *
     * @param request DSIRead
     * @return DSIReadResponse
     */
    @Override
    public DSIReadResponse dsiRead(DSIRead request) {
	DSIReadResponse response = WSHelper.makeResponse(DSIReadResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] applicationID = connectionHandle.getCardApplication();
	    String dsiName = request.getDSIName();

	    Assert.assertIncorrectParameter(dsiName, "The parameter DSIName is empty.");

	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    DataSetInfoType dataSetInfo = cardInfoWrapper.getDataSet(dsiName, applicationID);
	    Assert.assertNamedEntityNotFound(dataSetInfo, "The given DSIName cannot be found.");

	    Assert.securityConditionDataSet(cardStateEntry, applicationID, dsiName, NamedDataServiceActionName.DSI_READ);

	    byte[] slotHandle = connectionHandle.getSlotHandle();
	    // throws a null pointer if no ef is selected
	    byte[] fileContent = CardUtils.readFile(cardStateEntry.getFCPOfSelectedEF(), env.getDispatcher(), slotHandle);

	    response.setDSIContent(fileContent);
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The Encipher function encrypts a transmitted plain text. The detailed behaviour of this function depends on
     * the protocol of the DID.
     * See BSI-TR-03112-4, version 1.1.2, section 3.5.1.
     *
     * @param request Encipher
     * @return EncipherResponse
     */
    @Override
    public EncipherResponse encipher(Encipher request) {
	EncipherResponse response = WSHelper.makeResponse(EncipherResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] applicationID = connectionHandle.getCardApplication();
	    String didName = SALUtils.getDIDName(request);

	    byte[] plainText = request.getPlainText();
	    Assert.assertIncorrectParameter(plainText, "The parameter PlainText is empty.");

	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, applicationID);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, protocolURI);
	    if (protocol.hasNextStep(FunctionType.Encipher)) {
		response = protocol.encipher(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("Encipher", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The Decipher function decrypts a given cipher text. The detailed behaviour of this function depends on
     * the protocol of the DID.
     * See BSI-TR-03112-4, version 1.1.2, section 3.5.2.
     *
     * @param request Decipher
     * @return DecipherResponse
     */
    @Override
    public DecipherResponse decipher(Decipher request) {
	DecipherResponse response = WSHelper.makeResponse(DecipherResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] applicationID = connectionHandle.getCardApplication();
	    String didName = SALUtils.getDIDName(request);

	    byte[] cipherText = request.getCipherText();
	    Assert.assertIncorrectParameter(cipherText, "The parameter CipherText is empty.");

	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, applicationID);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, protocolURI);
	    if (protocol.hasNextStep(FunctionType.Decipher)) {
		response = protocol.decipher(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("Decipher", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The GetRandom function returns a random number which is suitable for authentication with the DID addressed with DIDName.
     * See BSI-TR-03112-4, version 1.1.2, section 3.5.3.
     *
     * @param request GetRandom
     * @return GetRandomResponse
     */
    @Override
    public GetRandomResponse getRandom(GetRandom request) {
	GetRandomResponse response = WSHelper.makeResponse(GetRandomResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] applicationID = connectionHandle.getCardApplication();
	    String didName = SALUtils.getDIDName(request);

	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, applicationID);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, protocolURI);
	    if (protocol.hasNextStep(FunctionType.GetRandom)) {
		response = protocol.getRandom(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("GetRandom", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The Hash function calculates the hash value of a transmitted message.
     * See BSI-TR-03112-4, version 1.1.2, section 3.5.4.
     *
     * @param request Hash
     * @return HashResponse
     */
    @Override
    public HashResponse hash(Hash request) {
	HashResponse response = WSHelper.makeResponse(HashResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] applicationID = connectionHandle.getCardApplication();
	    String didName = SALUtils.getDIDName(request);

	    byte[] message = request.getMessage();
    	    Assert.assertIncorrectParameter(message, "The parameter Message is empty.");
    	    
	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, applicationID);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, protocolURI);
	    if (protocol.hasNextStep(FunctionType.Hash)) {
		response = protocol.hash(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("Hash", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The Sign function signs a transmitted message.
     * See BSI-TR-03112-4, version 1.1.2, section 3.5.5.
     *
     * @param request Sign
     * @return SignResponse
     */
    @Override
    public SignResponse sign(Sign request) {
	SignResponse response = WSHelper.makeResponse(SignResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] applicationID = connectionHandle.getCardApplication();
	    String didName = SALUtils.getDIDName(request);

	    byte[] message = request.getMessage();
	    Assert.assertIncorrectParameter(message, "The parameter Message is empty.");

	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, applicationID);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, protocolURI);
	    if (protocol.hasNextStep(FunctionType.Sign)) {
		response = protocol.sign(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("Sign", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The VerifySignature function verifies a digital signature.
     * See BSI-TR-03112-4, version 1.1.2, section 3.5.6.
     *
     * @param request VerifySignature
     * @return VerifySignatureResponse
     */
    @Override
    public VerifySignatureResponse verifySignature(VerifySignature request) {
	VerifySignatureResponse response = WSHelper.makeResponse(VerifySignatureResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] applicationID = connectionHandle.getCardApplication();
	    String didName = SALUtils.getDIDName(request);

	    byte[] signature = request.getSignature();
	    Assert.assertIncorrectParameter(signature, "The parameter Signature is empty.");

	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, applicationID);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, protocolURI);
	    if (protocol.hasNextStep(FunctionType.VerifySignature)) {
		response = protocol.verifySignature(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("VerifySignature", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The VerifyCertificate function validates a given certificate.
     * See BSI-TR-03112-4, version 1.1.2, section 3.5.7.
     *
     * @param request VerifyCertificate
     * @return VerifyCertificateResponse
     */
    @Override
    public VerifyCertificateResponse verifyCertificate(VerifyCertificate request) {
	VerifyCertificateResponse response = WSHelper.makeResponse(VerifyCertificateResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] applicationID = connectionHandle.getCardApplication();
	    String didName = SALUtils.getDIDName(request);

	    byte[] certificate = request.getCertificate();
	    Assert.assertIncorrectParameter(certificate, "The parameter Certificate is empty.");

	    String certificateType = request.getCertificateType();
	    Assert.assertIncorrectParameter(certificateType, "The parameter CertificateType is empty.");

	    String rootCert = request.getRootCert();
	    Assert.assertIncorrectParameter(rootCert, "The parameter RootCert is empty.");

	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, applicationID);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, protocolURI);
	    if (protocol.hasNextStep(FunctionType.VerifyCertificate)) {
		response = protocol.verifyCertificate(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("VerifyCertificate", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The DIDList function returns a list of the existing DIDs in the card application addressed by the
     * ConnectionHandle or the ApplicationIdentifier element within the Filter.
     * See BSI-TR-03112-4, version 1.1.2, section 3.6.1.
     *
     * @param request DIDList
     * @return DIDListResponse
     */
    @Override
    public DIDListResponse didList(DIDList request) {
	DIDListResponse response = WSHelper.makeResponse(DIDListResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    byte[] appId = connectionHandle.getCardApplication();
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);

	    Assert.securityConditionApplication(cardStateEntry, appId, DifferentialIdentityServiceActionName.DID_LIST);

	    byte[] applicationIDFilter = null;
	    String objectIDFilter = null;
	    String applicationFunctionFilter = null;

	    DIDQualifierType didQualifier = request.getFilter();
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

	    List<DIDInfoType> didInfos = new ArrayList<DIDInfoType>(cardApplication.getDIDInfoList());

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
			iso.std.iso_iec._24727.tech.schema.CryptoMarkerType rawMarker;
			rawMarker = next.getDifferentialIdentity().getDIDMarker().getCryptoMarker();
			CryptoMarkerType cryptoMarker = new CryptoMarkerType(rawMarker);
			AlgorithmInfoType algInfo = cryptoMarker.getAlgorithmInfo();
			if (! algInfo.getSupportedOperations().contains(applicationFunctionFilter)) {
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
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The DIDCreate function creates a new differential identity in the card application addressed with ConnectionHandle.
     * See BSI-TR-03112-4, version 1.1.2, section 3.6.2.
     *
     * @param request DIDCreate
     * @return DIDCreateResponse
     */
    @Override
    public DIDCreateResponse didCreate(DIDCreate request) {
	DIDCreateResponse response = WSHelper.makeResponse(DIDCreateResponse.class, WSHelper.makeResultOK());

	try {
            ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
            byte[] cardApplicationID = connectionHandle.getCardApplication();
            CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);
                                        
	    String didName = request.getDIDName();
	    Assert.assertIncorrectParameter(didName, "The parameter DIDName is empty.");

	    DIDCreateDataType didCreateData = request.getDIDCreateData();
	    Assert.assertIncorrectParameter(didCreateData, "The parameter DIDCreateData is empty.");

	    AccessControlListType DidAcl = request.getDIDACL();
	    Assert.assertIncorrectParameter(DidAcl, "The parameter DIDCreateData is empty.");
	    
	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, cardApplicationID);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

            Assert.securityConditionDID(cardStateEntry, cardApplicationID, didName, DifferentialIdentityServiceActionName.DID_CREATE);

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, protocolURI);
	    if (protocol.hasNextStep(FunctionType.DIDCreate)) {
		response = protocol.didCreate(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("DIDCreate", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The public information for a DID is read with the DIDGet function.
     * See BSI-TR-03112-4, version 1.1.2, section 3.6.3.
     *
     * @param request DIDGet
     * @return DIDGetResponse
     */
    @Override
    public DIDGetResponse didGet(DIDGet request) {
	DIDGetResponse response = WSHelper.makeResponse(DIDGetResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
            byte[] cardApplicationID = connectionHandle.getCardApplication();

	    // handle must be requested without application, as it is irrelevant for this call
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);

	    String didName = SALUtils.getDIDName(request);
	    Assert.assertIncorrectParameter(didName, "The parameter DIDName is empty.");

	    DIDScopeType didScope = request.getDIDScope();
	    Assert.assertIncorrectParameter(didScope, "The parameter DIDScope is empty.");

            Assert.securityConditionDID(cardStateEntry, cardApplicationID, didName, DifferentialIdentityServiceActionName.DID_GET);
	    
	    DIDStructureType didStructure = SALUtils.getDIDStructure(request, didName, cardStateEntry, connectionHandle);
	    response.setDIDStructure(didStructure);
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The DIDUpdate function creates a new key (marker) for the DID addressed with DIDName.
     * See BSI-TR-03112-4, version 1.1.2, section 3.6.4.
     *
     * @param request DIDUpdate
     * @return DIDUpdateResponse
     */
    @Override
    public DIDUpdateResponse didUpdate(DIDUpdate request) {
	DIDUpdateResponse response = WSHelper.makeResponse(DIDUpdateResponse.class, WSHelper.makeResultOK());

	try {
            ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
            byte[] cardApplicationID = connectionHandle.getCardApplication();
            CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);
                                        
	    String didName = request.getDIDName();
	    Assert.assertIncorrectParameter(didName, "The parameter DIDName is empty.");

	    DIDUpdateDataType didUpdateData = request.getDIDUpdateData();
	    Assert.assertIncorrectParameter(didUpdateData, "The parameter DIDUpdateData is empty.");
	    
	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, cardApplicationID);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

            Assert.securityConditionDID(cardStateEntry, cardApplicationID, didName, DifferentialIdentityServiceActionName.DID_UPDATE);

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, protocolURI);
	    if (protocol.hasNextStep(FunctionType.DIDUpdate)) {
		response = protocol.didUpdate(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("DIDUpdate", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}
	
	return response;
    }

    /**
     * The DIDDelete function deletes the DID addressed with DIDName.
     * See BSI-TR-03112-4, version 1.1.2, section 3.6.5.
     *
     * @param request DIDDelete
     * @return DIDDeleteResponse
     */
    @Override
    public DIDDeleteResponse didDelete(DIDDelete request) {
	DIDDeleteResponse response = WSHelper.makeResponse(DIDDeleteResponse.class, WSHelper.makeResultOK());

	try {
            ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
            byte[] cardApplicationID = connectionHandle.getCardApplication();
            CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);
                                        
	    String didName = request.getDIDName();
	    Assert.assertIncorrectParameter(didName, "The parameter DIDName is empty.");
	    
	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, cardApplicationID);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

            Assert.securityConditionDID(cardStateEntry, cardApplicationID, didName, DifferentialIdentityServiceActionName.DID_DELETE);

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, protocolURI);
	    if (protocol.hasNextStep(FunctionType.DIDDelete)) {
		response = protocol.didDelete(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("DIDDelete", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;

    }

    /**
     * The DIDAuthenticate function can be used to execute an authentication protocol using a DID addressed by DIDName.
     * See BSI-TR-03112-4, version 1.1.2, section 3.6.6.
     *
     * @param request DIDAuthenticate
     * @return DIDAuthenticateResponse
     */
    @Override
    public DIDAuthenticateResponse didAuthenticate(DIDAuthenticate request) {
	DIDAuthenticateResponse response = WSHelper.makeResponse(DIDAuthenticateResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
            byte[] cardApplicationID = connectionHandle.getCardApplication();
            CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);

	    DIDAuthenticationDataType didAuthenticationData = request.getAuthenticationProtocolData();
	    Assert.assertIncorrectParameter(didAuthenticationData, "The parameter AuthenticationProtocolData is empty.");

	    String protocolURI = didAuthenticationData.getProtocol();
	    // FIXME: workaround for missing protocol URI from eID-Servers
	    if (protocolURI == null) {
		logger.warn("ProtocolURI was null");
		protocolURI = ECardConstants.Protocol.EAC_GENERIC;
	    } else if (protocolURI.equals("urn:oid:1.0.24727.3.0.0.7.2")) {
		logger.warn("ProtocolURI was urn:oid:1.0.24727.3.0.0.7.2");
		protocolURI = ECardConstants.Protocol.EAC_GENERIC;
	    }

	    didAuthenticationData.setProtocol(protocolURI);

            String didName = SALUtils.getDIDName(request);
	    Assert.assertIncorrectParameter(didName, "The parameter didName is empty.");
	    
	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, cardApplicationID);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    DIDScopeType didScope = request.getDIDScope();
	    Assert.assertIncorrectParameter(didScope, "The parameter DIDScope is empty.");

	    ConnectionHandleType samConnectionHandle = request.getSAMConnectionHandle();
	    Assert.assertIncorrectParameter(samConnectionHandle, "The parameter SAMConnectionHandle is empty.");

            Assert.securityConditionDID(cardStateEntry, cardApplicationID, didName, DifferentialIdentityServiceActionName.DID_AUTHENTICATE);

	    SALProtocol protocol = getProtocol(connectionHandle, protocolURI);
	    if (protocol.hasNextStep(FunctionType.DIDAuthenticate)) {
		response = protocol.didAuthenticate(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("DIDAuthenticate", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;

    }

    /**
     * The ACLList function returns the access control list for the stated target object (card application, data set, DID).
     * See BSI-TR-03112-4, version 1.1.2, section 3.7.1.
     *
     * @param request ACLList
     * @return ACLListResponse
     */
    @Override
    public ACLListResponse aclList(ACLList request) {
	ACLListResponse response = WSHelper.makeResponse(ACLListResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);

	    TargetNameType targetName = request.getTargetName();
	    Assert.assertIncorrectParameter(targetName, "The parameter TargetName is empty.");

	    // get the target values, according to the schema only one must exist, we pick the first existing ;-)
	    byte[] targetAppId = targetName.getCardApplicationName();
	    String targetDataSet = targetName.getDataSetName();
	    String targetDid = targetName.getDIDName();

	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    byte[] handleAppId = connectionHandle.getCardApplication();

	    if (targetDataSet != null) {
		DataSetInfoType dataSetInfo = cardInfoWrapper.getDataSet(targetDataSet, handleAppId);
		Assert.assertNamedEntityNotFound(dataSetInfo, "The given DataSet cannot be found.");
		response.setTargetACL(cardInfoWrapper.getDataSet(targetDataSet, handleAppId).getDataSetACL());
	    } else if (targetDid != null) {
		DIDInfoType didInfo = cardInfoWrapper.getDIDInfo(targetDid, handleAppId);
		Assert.assertNamedEntityNotFound(didInfo, "The given DIDInfo cannot be found.");
		//TODO Check security condition ?
		response.setTargetACL(cardInfoWrapper.getDIDInfo(targetDid, handleAppId).getDIDACL());
	    } else if (targetAppId != null) {
		CardApplicationWrapper cardApplication = cardInfoWrapper.getCardApplication(targetAppId);
		Assert.assertNamedEntityNotFound(cardApplication, "The given CardApplication cannot be found.");
		Assert.securityConditionApplication(cardStateEntry, targetAppId, AuthorizationServiceActionName.ACL_LIST);

		response.setTargetACL(cardInfoWrapper.getCardApplication(targetAppId).getCardApplicationACL());
	    } else {
		throw new IncorrectParameterException("The given TargetName is invalid.");
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * An access rule in the access control list is modified with the ACLModify function.
     * See BSI-TR-03112-4, version 1.1.2, section 3.7.2.
     *
     * @param request ACLModify
     * @return ACLModifyResponse
     */
    @Override
    public ACLModifyResponse aclModify(ACLModify request) {
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
     * Removes a finished protocol from the SAL instance.
     *
     * @param handle Connection Handle
     * @param protocolURI Protocol URI
     * @param protocol Protocol
     * @throws UnknownConnectionHandleException
     */
    public void removeFinishedProtocol(ConnectionHandleType handle, String protocolURI, SALProtocol protocol)
	    throws UnknownConnectionHandleException {
	if (protocol.isFinished()) {
	    CardStateEntry entry = SALUtils.getCardStateEntry(states, handle);
	    entry.removeProtocol(protocolURI);
	}
    }

    private SALProtocol getProtocol(ConnectionHandleType handle, String protocolURI)
	    throws UnknownProtocolException, UnknownConnectionHandleException {
	CardStateEntry entry = SALUtils.getCardStateEntry(states, handle);
	SALProtocol protocol = entry.getProtocol(protocolURI);
	if (protocol == null) {
	    try {
		protocol = protocolSelector.getSALProtocol(protocolURI);
		entry.setProtocol(protocolURI, protocol);
	    } catch (AddonNotFoundException ex) {
		throw new UnknownProtocolException("The protocol URI '" + protocolURI + "' is not available.");
	    }
	}
	protocol.getInternalData().put("cardState", entry);

	return protocol;
    }

}
