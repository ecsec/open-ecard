/****************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
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
import iso.std.iso_iec._24727.tech.schema.CardApplicationSelect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationSelectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceActionName;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceCreate;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceCreateResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceDelete;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceDeleteResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceDescribe;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceDescribeResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceList;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceListResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceListResponse.CardApplicationServiceNameList;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceLoad;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceLoadResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationServiceType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationStartSession;
import iso.std.iso_iec._24727.tech.schema.CardApplicationStartSessionResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationType;
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
import iso.std.iso_iec._24727.tech.schema.DIDUpdateDataType;
import iso.std.iso_iec._24727.tech.schema.DIDUpdateResponse;
import iso.std.iso_iec._24727.tech.schema.DSICreate;
import iso.std.iso_iec._24727.tech.schema.DSICreateResponse;
import iso.std.iso_iec._24727.tech.schema.DSIDelete;
import iso.std.iso_iec._24727.tech.schema.DSIDeleteResponse;
import iso.std.iso_iec._24727.tech.schema.DSIList;
import iso.std.iso_iec._24727.tech.schema.DSIListResponse;
import iso.std.iso_iec._24727.tech.schema.DSINameListType;
import iso.std.iso_iec._24727.tech.schema.DSIRead;
import iso.std.iso_iec._24727.tech.schema.DSIReadResponse;
import iso.std.iso_iec._24727.tech.schema.DSIType;
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
import iso.std.iso_iec._24727.tech.schema.PathType;
import iso.std.iso_iec._24727.tech.schema.PathType.TagRef;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.addon.AddonManager;
import org.openecard.addon.AddonNotFoundException;
import org.openecard.addon.AddonSelector;
import org.openecard.addon.HighestVersionSelector;
import org.openecard.addon.sal.FunctionType;
import org.openecard.addon.sal.SALProtocol;
import org.openecard.common.ECardConstants;
import org.openecard.common.ECardException;
import org.openecard.common.ThreadTerminateException;
import org.openecard.common.WSHelper;
import org.openecard.common.apdu.DeleteFile;
import org.openecard.common.apdu.EraseBinary;
import org.openecard.common.apdu.EraseRecord;
import org.openecard.common.apdu.GetData;
import org.openecard.common.apdu.GetResponse;
import org.openecard.common.apdu.ReadBinary;
import org.openecard.common.apdu.ReadRecord;
import org.openecard.common.apdu.Select;
import org.openecard.common.apdu.UpdateBinary;
import org.openecard.common.apdu.UpdateRecord;
import org.openecard.common.apdu.WriteBinary;
import org.openecard.common.apdu.WriteRecord;
import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.apdu.common.CardResponseAPDU;
import org.openecard.common.apdu.common.TrailerConstants;
import org.openecard.common.apdu.utils.CardUtils;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.interfaces.InvocationTargetExceptionUnchecked;
import org.openecard.common.interfaces.Publish;
import org.openecard.common.sal.Assert;
import org.openecard.common.sal.exception.InappropriateProtocolForActionException;
import org.openecard.common.sal.exception.IncorrectParameterException;
import org.openecard.common.sal.exception.NameExistsException;
import org.openecard.common.sal.exception.NamedEntityNotFoundException;
import org.openecard.common.sal.exception.PrerequisitesNotSatisfiedException;
import org.openecard.common.sal.exception.SecurityConditionNotSatisfiedException;
import org.openecard.common.sal.exception.UnknownConnectionHandleException;
import org.openecard.common.sal.exception.UnknownProtocolException;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.common.sal.state.cif.CardApplicationWrapper;
import org.openecard.common.sal.state.cif.CardInfoWrapper;
import org.openecard.common.sal.util.SALUtils;
import org.openecard.common.tlv.TLV;
import org.openecard.common.tlv.TLVException;
import org.openecard.common.tlv.iso7816.DataElements;
import org.openecard.common.tlv.iso7816.FCP;
import org.openecard.common.util.ByteUtils;
import org.openecard.crypto.common.sal.did.CryptoMarkerType;
import org.openecard.gui.UserConsent;
import org.openecard.ws.SAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a Service Access Layer (SAL).
 *
 * @author Johannes Schmölz
 * @author Dirk Petrautzki
 * @author Simon Potzernheim
 * @author Tobias Wich
 * @author Moritz Horsch
 * @author Antonio de la Piedra
 * @author Hans-Martin Haase
 */
public class TinySAL implements SAL {

    private static final Logger LOG = LoggerFactory.getLogger(TinySAL.class);
    private static final byte[] MF = new byte[] {(byte) 0x3F, (byte) 0x00};

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
	states.setProtocolSelector(protocolSelector);
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
	InitializeResponse res = WSHelper.makeResponse(InitializeResponse.class, WSHelper.makeResultOK());
	return res;
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
	TerminateResponse res = WSHelper.makeResponse(TerminateResponse.class, WSHelper.makeResultOK());
	return res;
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
	CardApplicationPathResponse response = WSHelper.makeResponse(CardApplicationPathResponse.class,
		WSHelper.makeResultOK());

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
		    if (entry.getImplicitlySelectedApplicationIdentifier() != null) {
			pathCopy.setCardApplication(entry.getImplicitlySelectedApplicationIdentifier());
		    } else {
			LOG.warn("No CardApplication and ImplicitlySelectedApplication available using MF now.");
			pathCopy.setCardApplication(MF);
		    }
		}
		resultPaths.add(pathCopy);
	    }

	    response.setCardAppPathResultSet(resultSet);
	} catch (IncorrectParameterException e) {
	    response.setResult(e.getResult());
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
	CardApplicationConnectResponse response = WSHelper.makeResponse(CardApplicationConnectResponse.class,
		WSHelper.makeResultOK());

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
		if (cardStateEntry.getImplicitlySelectedApplicationIdentifier() != null) {
		    applicationID = cardStateEntry.getImplicitlySelectedApplicationIdentifier();
		} else {
		    applicationID = MF;
		}
	    }
	    Assert.securityConditionApplication(cardStateEntry, applicationID,
		    ConnectionServiceActionName.CARD_APPLICATION_CONNECT);

	    // Connect to the card
	    ConnectionHandleType handle = cardStateEntry.handleCopy();
	    cardStateEntry = cardStateEntry.derive(handle);
	    Connect connect = new Connect();
	    connect.setContextHandle(handle.getContextHandle());
	    connect.setIFDName(handle.getIFDName());
	    connect.setSlot(handle.getSlotIndex());

	    ConnectResponse connectResponse = (ConnectResponse) env.getDispatcher().safeDeliver(connect);
	    WSHelper.checkResult(connectResponse);

	    // Select the card application
	    CardCommandAPDU select;
	    // TODO: proper determination of path, file and app id
	    if (applicationID.length == 2) {
		select = new Select.File(applicationID);
		List<byte[]> responses = new ArrayList<>();
		responses.add(TrailerConstants.Success.OK());
		responses.add(TrailerConstants.Error.WRONG_P1_P2());
		CardResponseAPDU resp = select.transmit(env.getDispatcher(), connectResponse.getSlotHandle(), responses);

		if (Arrays.equals(resp.getTrailer(), TrailerConstants.Error.WRONG_P1_P2())) {
		    select = new Select.AbsolutePath(applicationID);
		    select.transmit(env.getDispatcher(), connectResponse.getSlotHandle());
		}

	    } else {
		select = new Select.Application(applicationID);
		select.transmit(env.getDispatcher(), connectResponse.getSlotHandle());
	    }

	    cardStateEntry.setCurrentCardApplication(applicationID);
	    cardStateEntry.setSlotHandle(connectResponse.getSlotHandle());
	    // reset the ef FCP
	    cardStateEntry.unsetFCPOfSelectedEF();
	    states.addEntry(cardStateEntry);

	    response.setConnectionHandle(cardStateEntry.handleCopy());
	    response.getConnectionHandle().setCardApplication(applicationID);
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	}

	return response;
    }

    @Override
    public CardApplicationSelectResponse cardApplicationSelect(CardApplicationSelect request) {
	CardApplicationSelectResponse response = WSHelper.makeResponse(CardApplicationSelectResponse.class,
		WSHelper.makeResultOK());

	try {
	    byte[] slotHandle = request.getSlotHandle();
	    ConnectionHandleType connectionHandle = SALUtils.createConnectionHandle(slotHandle);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] reqApplicationID = request.getCardApplication();

	    Assert.assertIncorrectParameter(reqApplicationID, "The parameter CardApplication is empty.");

	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    CardApplicationWrapper appInfo = cardInfoWrapper.getCardApplication(reqApplicationID);
	    Assert.assertNamedEntityNotFound(appInfo, "The given Application cannot be found.");

	    Assert.securityConditionApplication(cardStateEntry, reqApplicationID,
		    ConnectionServiceActionName.CARD_APPLICATION_CONNECT);

	    // check if the currently selected application is already what the caller wants
	    byte[] curApplicationID = cardStateEntry.getCurrentCardApplication().getApplicationIdentifier();
	    if (! ByteUtils.compare(reqApplicationID, curApplicationID)) {
		// Select the card application
		CardCommandAPDU select;
		// TODO: proper determination of path, file and app id
		if (reqApplicationID.length == 2) {
		    select = new Select.File(reqApplicationID);
		    List<byte[]> responses = new ArrayList<>();
		    responses.add(TrailerConstants.Success.OK());
		    responses.add(TrailerConstants.Error.WRONG_P1_P2());
		    CardResponseAPDU resp = select.transmit(env.getDispatcher(), slotHandle, responses);

		    if (Arrays.equals(resp.getTrailer(), TrailerConstants.Error.WRONG_P1_P2())) {
			select = new Select.AbsolutePath(reqApplicationID);
			select.transmit(env.getDispatcher(), slotHandle);
		    }

		} else {
		    select = new Select.Application(reqApplicationID);
		    select.transmit(env.getDispatcher(), slotHandle);
		}

		cardStateEntry.setCurrentCardApplication(reqApplicationID);
		// reset the ef FCP
		cardStateEntry.unsetFCPOfSelectedEF();
	    }

	    response.setConnectionHandle(cardStateEntry.handleCopy());
	} catch (ECardException e) {
	    response.setResult(e.getResult());
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
	CardApplicationDisconnectResponse response = WSHelper.makeResponse(CardApplicationDisconnectResponse.class,
		WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    byte[] slotHandle = connectionHandle.getSlotHandle();

	    // check existence of required parameters
	    if (slotHandle == null) {
		return WSHelper.makeResponse(CardApplicationDisconnectResponse.class,
			WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "ConnectionHandle is null"));
	    }

	    Disconnect disconnect = new Disconnect();
	    disconnect.setSlotHandle(slotHandle);
	    if (request.getAction() != null) {
		disconnect.setAction(request.getAction());
	    }
	    
	    DisconnectResponse disconnectResponse = (DisconnectResponse) env.getDispatcher().safeDeliver(disconnect);

	    // remove entries associated with this handle
	    states.removeSlotHandleEntry(slotHandle);

	    response.setResult(disconnectResponse.getResult());
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
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
    @Publish
    @Override
    public CardApplicationStartSessionResponse cardApplicationStartSession(CardApplicationStartSession request) {
	CardApplicationStartSessionResponse response = WSHelper.makeResponse(CardApplicationStartSessionResponse.class,
		WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] cardApplicationID = connectionHandle.getCardApplication();
	    
	    String didName = SALUtils.getDIDName(request);
	    Assert.assertIncorrectParameter(didName, "The parameter didName is empty.");

	    DIDAuthenticationDataType didAuthenticationProtocolData = request.getAuthenticationProtocolData();
	    Assert.assertIncorrectParameter(didAuthenticationProtocolData,
		    "The parameter didAuthenticationProtocolData is empty.");

	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, cardApplicationID);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");
	    Assert.securityConditionApplication(cardStateEntry, cardApplicationID,
		    ConnectionServiceActionName.CARD_APPLICATION_START_SESSION);

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, request.getDIDScope(), protocolURI);
	    if (protocol.hasNextStep(FunctionType.CardApplicationStartSession)) {
		response = protocol.cardApplicationStartSession(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("CardApplicationStartSession", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
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
    @Publish
    @Override
    public CardApplicationEndSessionResponse cardApplicationEndSession(CardApplicationEndSession request) {
	CardApplicationEndSessionResponse response = WSHelper.makeResponse(CardApplicationEndSessionResponse.class,
		WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] cardApplicationID = connectionHandle.getCardApplication();
	    String didName = SALUtils.getDIDName(request);    
	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, cardApplicationID);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    Assert.securityConditionApplication(cardStateEntry, cardApplicationID,
		    ConnectionServiceActionName.CARD_APPLICATION_END_SESSION);

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, null, protocolURI);
	    if (protocol.hasNextStep(FunctionType.CardApplicationEndSession)) {
		response = protocol.cardApplicationEndSession(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("CardApplicationEndSession", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
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
    @Publish
    @Override
    public CardApplicationListResponse cardApplicationList(CardApplicationList request) {
	CardApplicationListResponse response = WSHelper.makeResponse(CardApplicationListResponse.class,
		WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);

	    /*
		TR-03112-4 section 3.3.2 states that the alpha application have to be connected with
		CardApplicationConnect.
		In case of using CardInfo file descriptions this is not necessary because we just work on a file.
	    */
	    // byte[] cardApplicationID = connectionHandle.getCardApplication();
	    // Assert.securityConditionApplication(cardStateEntry, cardApplicationID,
	    //	    CardApplicationServiceActionName.CARD_APPLICATION_LIST);

	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    CardApplicationNameList cardApplicationNameList = new CardApplicationNameList();
	    cardApplicationNameList.getCardApplicationName().addAll(cardInfoWrapper.getCardApplicationNameList());

	    response.setCardApplicationNameList(cardApplicationNameList);
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
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
	return WSHelper.makeResponse(CardApplicationCreateResponse.class,
		WSHelper.makeResultUnknownError("Not supported yet."));
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
	CardApplicationDeleteResponse response = WSHelper.makeResponse(CardApplicationDeleteResponse.class,
		WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);
	    byte[] cardApplicationName = request.getCardApplicationName();
	    Assert.assertIncorrectParameter(cardApplicationName, "The parameter CardApplicationName is empty.");
	    Assert.securityConditionApplication(cardStateEntry, connectionHandle.getCardApplication(),
		    CardApplicationServiceActionName.CARD_APPLICATION_DELETE);
	    // TODO: determine how the deletion have to be performed. A card don't have to support the Deletion by
	    // application identifier. Necessary attributes should be available in the ATR or EF.ATR.
	    DeleteFile delFile = new DeleteFile.Application(cardApplicationName);
	    delFile.transmit(env.getDispatcher(), connectionHandle.getSlotHandle());
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The CardApplicationServiceList function returns a list of all available services of a card application.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.4.
     *
     * @param request CardApplicationServiceList
     * @return CardApplicationServiceListResponse
     */
    @Publish
    @Override
    public CardApplicationServiceListResponse cardApplicationServiceList(CardApplicationServiceList request) {
	CardApplicationServiceListResponse response = WSHelper.makeResponse(CardApplicationServiceListResponse.class,
		WSHelper.makeResultOK());

	 try {  
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] cardApplicationID = connectionHandle.getCardApplication();

	    //Assert.securityConditionApplication(cardStateEntry, cardApplicationID,
	    //	    CardApplicationServiceActionName.CARD_APPLICATION_SERVICE_LIST);

	    CardApplicationServiceNameList cardApplicationServiceNameList = new CardApplicationServiceNameList();
                            
	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    Iterator<CardApplicationType> it = cardInfoWrapper.getApplicationCapabilities().getCardApplication().iterator();

            while (it.hasNext()) {
                CardApplicationType next = it.next();
            
                byte[] appName = next.getApplicationIdentifier();
                
                if (Arrays.equals(appName, cardApplicationID)) {
                    Iterator<CardApplicationServiceType> itt = next.getCardApplicationServiceInfo().iterator();

                    while (itt.hasNext()) {
                        CardApplicationServiceType nextt = itt.next();
                        cardApplicationServiceNameList.getCardApplicationServiceName().add(nextt.getCardApplicationServiceName());
                    }
                }
            }
            
            response.setCardApplicationServiceNameList(cardApplicationServiceNameList);

	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
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
	return WSHelper.makeResponse(CardApplicationServiceCreateResponse.class,
		WSHelper.makeResultUnknownError("Not supported yet."));
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
	return WSHelper.makeResponse(CardApplicationServiceLoadResponse.class,
		WSHelper.makeResultUnknownError("Not supported yet."));
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
	return WSHelper.makeResponse(CardApplicationServiceDeleteResponse.class,
		WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The CardApplicationServiceDescribe function can be used to request an URI, an URL or a detailed description
     * of the selected card application service.
     * See BSI-TR-03112-4, version 1.1.2, section 3.3.8.
     *
     * @param request CardApplicationServiceDescribe
     * @return CardApplicationServiceDescribeResponse
     */
    @Publish
    @Override
    public CardApplicationServiceDescribeResponse cardApplicationServiceDescribe(CardApplicationServiceDescribe request) {
	 CardApplicationServiceDescribeResponse response =
		 WSHelper.makeResponse(CardApplicationServiceDescribeResponse.class, WSHelper.makeResultOK());

	 try {  
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] cardApplicationID = connectionHandle.getCardApplication();
            
	    String cardApplicationServiceName = request.getCardApplicationServiceName();
	    Assert.assertIncorrectParameter(cardApplicationServiceName,
		    "The parameter CardApplicationServiceName is empty.");

	    //Assert.securityConditionApplication(cardStateEntry, cardApplicationID,
	    //	    CardApplicationServiceActionName.CARD_APPLICATION_SERVICE_DESCRIBE);
                            
	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();

	    Iterator<CardApplicationType> it = cardInfoWrapper.getApplicationCapabilities().getCardApplication().iterator();

            while (it.hasNext()) {
                CardApplicationType next = it.next();
                byte[] appName = next.getApplicationIdentifier();
                
                if (Arrays.equals(appName, cardApplicationID)){
                    
                    Iterator<CardApplicationServiceType> itt = next.getCardApplicationServiceInfo().iterator();
                    
                    while (itt.hasNext()) {
                        CardApplicationServiceType nextt = itt.next();
                        if (nextt.getCardApplicationServiceName().equals(cardApplicationServiceName)) {
                            response.setServiceDescription(nextt.getCardApplicationServiceDescription());
			    return response;
                        }    
                    }
                }
            }
         } catch (ECardException e) {
	    response.setResult(e.getResult());
	 } catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
	    response.setResult(WSHelper.makeResult(e));
	 }

	return response;
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
     * The DataSetList function returns the list of the data sets in the card application addressed with the
     * ConnectionHandle.
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.1.
     *
     * @param request DataSetList
     * @return DataSetListResponse
     */
    @Publish
    @Override
    public DataSetListResponse dataSetList(DataSetList request) {
	DataSetListResponse response = WSHelper.makeResponse(DataSetListResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);
	    byte[] cardApplicationID = connectionHandle.getCardApplication();

	    Assert.securityConditionApplication(cardStateEntry, cardApplicationID,
		    NamedDataServiceActionName.DATA_SET_LIST);

	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    DataSetNameListType dataSetNameList = cardInfoWrapper.getDataSetNameList(cardApplicationID);

	    response.setDataSetNameList(dataSetNameList);
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
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
    @Publish
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

	    Assert.securityConditionDataSet(cardStateEntry, applicationID, dataSetName,
		    NamedDataServiceActionName.DATA_SET_SELECT);

	    byte[] fileID = dataSetInfo.getDataSetPath().getEfIdOrPath();
	    byte[] slotHandle = connectionHandle.getSlotHandle();
	    CardResponseAPDU result = CardUtils.selectFileWithOptions(env.getDispatcher(), slotHandle, fileID,
		    null, CardUtils.FCP_RESPONSE_DATA);

	    FCP fcp = null;
	    if (result != null && result.getData().length > 0) {
		try {
		    fcp = new FCP(result.getData());
		} catch (TLVException ex) {
		    LOG.warn("Invalid FCP received.");
		}
	    }
	    if (fcp == null) {
		LOG.info("Using fake FCP.");
		fcp = new FCP(createFakeFCP(Arrays.copyOfRange(fileID, fileID.length - 2, fileID.length)));
	    }
	    cardStateEntry.setFCPOfSelectedEF(fcp);
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
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
	DataSetDeleteResponse response = WSHelper.makeResponse(DataSetDeleteResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] cardApplicationID = connectionHandle.getCardApplication();
	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
            
            String dataSetName = request.getDataSetName();
            Assert.assertIncorrectParameter(dataSetName, "The parameter DataSetName is empty.");
	    Assert.securityConditionDataSet(cardStateEntry, cardApplicationID, dataSetName,
		    NamedDataServiceActionName.DATA_SET_DELETE);

	    DataSetInfoType dataSet = cardInfoWrapper.getDataSet(dataSetName, cardApplicationID);
	    if (dataSet == null) {
		throw new NamedEntityNotFoundException("The data set " + dataSetName + " does not exist.");
	    }

	    byte[] path = dataSet.getDataSetPath().getEfIdOrPath();
	    int len = path.length;
	    byte[] fid = new byte[] {path[len - 2], path[len - 1]};
	    DeleteFile delFile = new DeleteFile.ChildFile(fid);
	    delFile.transmit(env.getDispatcher(), connectionHandle.getSlotHandle());
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The function DSIList supplies the list of the DSI (Data Structure for Interoperability) which exist in the
     * selected data set.
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.5. <br>
     * <br>
     * Prerequisites: <br>
     * - a connection to a card application has been established <br>
     * - a data set has been selected <br>
     *
     * @param request DSIList
     * @return DSIListResponse
     */
    @Publish
    @Override
    public DSIListResponse dsiList(DSIList request) {
        DSIListResponse response = WSHelper.makeResponse(DSIListResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);
	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    byte[] cardApplicationID = connectionHandle.getCardApplication();

	    if (cardStateEntry.getFCPOfSelectedEF() == null) {
		throw new PrerequisitesNotSatisfiedException("No EF selected.");
	    }

            DataSetInfoType dataSet = cardInfoWrapper.getDataSetByFid(
		    cardStateEntry.getFCPOfSelectedEF().getFileIdentifiers().get(0));
            Assert.securityConditionDataSet(cardStateEntry, cardApplicationID, dataSet.getDataSetName(),
		    NamedDataServiceActionName.DSI_LIST);

            DSINameListType dsiNameList = new DSINameListType();
	    for (DSIType dsi : dataSet.getDSI()) {
                dsiNameList.getDSIName().add(dsi.getDSIName());
            }

            response.setDSINameList(dsiNameList);
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The DSICreate function creates a DSI (Data Structure for Interoperability) in the currently selected data set.
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.6.
     * <br>
     * <br>
     * Preconditions: <br>
     * - Connection to a card application established via CardApplicationConnect <br>
     * - A data set has been selected with DataSetSelect <br>
     * - The DSI does not exist in the data set. <br>
     *
     * @param request DSICreate
     * @return DSICreateResponse
     */
    @Override
    public DSICreateResponse dsiCreate(DSICreate request) {
	DSICreateResponse response = WSHelper.makeResponse(DSICreateResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    byte[] cardApplicationID = connectionHandle.getCardApplication();

            byte[] dsiContent = request.getDSIContent();
	    Assert.assertIncorrectParameter(dsiContent, "The parameter DSIContent is empty.");

            String dsiName = request.getDSIName();
	    Assert.assertIncorrectParameter(dsiName, "The parameter DSIName is empty.");

	    DSIType dsi = cardInfoWrapper.getDSIbyName(dsiName);
	    if (dsi != null) {
		throw new NameExistsException("There is already an DSI with the name " + dsiName + " in the current EF.");
	    }

	    byte[] slotHandle = connectionHandle.getSlotHandle();

	    if (cardStateEntry.getFCPOfSelectedEF() == null) {
		throw new PrerequisitesNotSatisfiedException("No data set for writing selected.");
	    } else {
		DataSetInfoType dataSet = cardInfoWrapper.getDataSetByFid(
			cardStateEntry.getFCPOfSelectedEF().getFileIdentifiers().get(0));
		Assert.securityConditionDataSet(cardStateEntry, cardApplicationID, dataSet.getDataSetName(),
			NamedDataServiceActionName.DSI_CREATE);
		DataElements dElements = cardStateEntry.getFCPOfSelectedEF().getDataElements();

		if (dElements.isTransparent()) {
		    WriteBinary writeBin = new WriteBinary(WriteBinary.INS_WRITE_BINARY_DATA, (byte) 0x00, (byte) 0x00,
			    dsiContent);
		    writeBin.transmit(env.getDispatcher(), slotHandle);
		} else if (dElements.isCyclic()) {
		    WriteRecord writeRec = new WriteRecord((byte) 0x00, WriteRecord.WRITE_PREVIOUS, dsiContent);
		    writeRec.transmit(env.getDispatcher(), slotHandle);
		} else {
		    WriteRecord writeRec = new WriteRecord((byte) 0x00, WriteRecord.WRITE_LAST, dsiContent);
		    writeRec.transmit(env.getDispatcher(), slotHandle);
		}
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;

    }

    /**
     * The DSIDelete function deletes a DSI (Data Structure for Interoperability) in the currently selected data set.
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.7.
     *
     * @param request DSIDelete
     * @return DSIDeleteResponse
     */
    //TODO: rewiew function and add @Publish annotation
    @Override
    public DSIDeleteResponse dsiDelete(DSIDelete request) {
	DSIDeleteResponse response = WSHelper.makeResponse(DSIDeleteResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    String dsiName = request.getDSIName();
	    Assert.assertIncorrectParameter(dsiName, "The parameter DSIName is empty.");

	    if (cardStateEntry.getFCPOfSelectedEF() == null) {
		String msg = "No DataSet selected for deleting the DSI " + request.getDSIName();
		throw new PrerequisitesNotSatisfiedException(msg);
	    }

	    DataSetInfoType dataSet = cardInfoWrapper.getDataSetByDsiName(request.getDSIName());
	    byte[] fidOrPath = dataSet.getDataSetPath().getEfIdOrPath();
	    byte[] dataSetFid = new byte[] {fidOrPath[fidOrPath.length - 2], fidOrPath[fidOrPath.length - 1]};
	    if (! Arrays.equals(dataSetFid, cardStateEntry.getFCPOfSelectedEF().getFileIdentifiers().get(0))) {
		String msg = "The wrong DataSet for the deletion of DSI " + request.getDSIName() + " is selected.";
		throw new PrerequisitesNotSatisfiedException(msg);
	    }
	    
	    DataSetInfoType dSet =
		    cardInfoWrapper.getDataSetByFid(cardStateEntry.getFCPOfSelectedEF().getFileIdentifiers().get(0));
	    Assert.securityConditionDataSet(cardStateEntry, connectionHandle.getCardApplication(), dSet.getDataSetName(),
		    NamedDataServiceActionName.DSI_DELETE);
	    DSIType dsi = cardInfoWrapper.getDSIbyName(dsiName);

	    // We have to define some allowed answers because if the file has an write operation counter we wont get an
	    // 9000 response.
	    ArrayList<byte[]> responses = new ArrayList<byte[]>() {
		{
		    add(new byte[] {(byte) 0x90, (byte) 0x00});
		    add(new byte[] {(byte) 0x63, (byte) 0xC1});
		    add(new byte[] {(byte) 0x63, (byte) 0xC2});
		    add(new byte[] {(byte) 0x63, (byte) 0xC3});
		    add(new byte[] {(byte) 0x63, (byte) 0xC4});
		    add(new byte[] {(byte) 0x63, (byte) 0xC5});
		    add(new byte[] {(byte) 0x63, (byte) 0xC6});
		    add(new byte[] {(byte) 0x63, (byte) 0xC7});
		    add(new byte[] {(byte) 0x63, (byte) 0xC8});
		    add(new byte[] {(byte) 0x63, (byte) 0xC9});
		    add(new byte[] {(byte) 0x63, (byte) 0xCA});
		    add(new byte[] {(byte) 0x63, (byte) 0xCB});
		    add(new byte[] {(byte) 0x63, (byte) 0xCC});
		    add(new byte[] {(byte) 0x63, (byte) 0xCD});
		    add(new byte[] {(byte) 0x63, (byte) 0xCE});
		    add(new byte[] {(byte) 0x63, (byte) 0xCF});
		}
	    };

	    if (cardStateEntry.getFCPOfSelectedEF().getDataElements().isLinear()) {
		EraseRecord rmRecord = new EraseRecord(dsi.getDSIPath().getIndex()[0], EraseRecord.ERASE_JUST_P1);
		rmRecord.transmit(env.getDispatcher(), connectionHandle.getSlotHandle(), responses);
	    } else {
		// NOTE: Erase binary allows to erase only everything after the offset or everything in front of the offset.
		// currently erasing everything after the offset is used.
		EraseBinary rmBinary = new EraseBinary((byte) 0x00, (byte) 0x00, dsi.getDSIPath().getIndex());
		rmBinary.transmit(env.getDispatcher(), connectionHandle.getSlotHandle(), responses);
	    }
	} catch (ECardException e) {
	    LOG.error(e.getMessage(), e);
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The DSIWrite function changes the content of a DSI (Data Structure for Interoperability).
     * See BSI-TR-03112-4, version 1.1.2, section 3.4.8.
     * For clarification this method updates an existing DSI and does not create a new one.
     *
     * The precondition for this method is that a connection to a card application was established and a data set was
     * selected. Furthermore the DSI exists already.
     *
     * @param request DSIWrite
     * @return DSIWriteResponse
     */
    @Publish
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
	    DataSetInfoType dataSetInfo = cardInfoWrapper.getDataSetByDsiName(dsiName);
	    DSIType dsi = cardInfoWrapper.getDSIbyName(dsiName);
	    Assert.assertNamedEntityNotFound(dataSetInfo, "The given DSIName cannot be found.");
	    Assert.securityConditionDataSet(cardStateEntry, applicationID, dsiName, NamedDataServiceActionName.DSI_WRITE);

	    if (cardStateEntry.getFCPOfSelectedEF() == null) {
		throw new PrerequisitesNotSatisfiedException("No EF with DSI selected.");
	    }

	    if (! Arrays.equals(dataSetInfo.getDataSetPath().getEfIdOrPath(), 
		    cardStateEntry.getFCPOfSelectedEF().getFileIdentifiers().get(0))) {
		String msg = "The currently selected data set does not contain the DSI to be updated.";
		throw new PrerequisitesNotSatisfiedException(msg);
	    }

	    byte[] slotHandle = connectionHandle.getSlotHandle();
	    if (cardStateEntry.getFCPOfSelectedEF().getDataElements().isTransparent()) {
		// currently assuming that the index encodes the offset
		byte[] index = dsi.getDSIPath().getIndex();
		UpdateBinary updateBin = new UpdateBinary(index[0], index[1], updateData);
		updateBin.transmit(env.getDispatcher(), slotHandle);
	    } else {
		// currently assuming that the index encodes the record number
		byte index = dsi.getDSIPath().getIndex()[0];
		UpdateRecord updateRec = new UpdateRecord(index, updateData);
		updateRec.transmit(env.getDispatcher(), slotHandle);
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
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
    @Publish
    @Override
    public DSIReadResponse dsiRead(DSIRead request) {
	DSIReadResponse response = WSHelper.makeResponse(DSIReadResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle);
	    byte[] applicationID = cardStateEntry.getCurrentCardApplication().getApplicationIdentifier();
	    String dsiName = request.getDSIName();
	    byte[] slotHandle = connectionHandle.getSlotHandle();
	    Assert.assertIncorrectParameter(dsiName, "The parameter DSIName is empty.");
	    Assert.securityConditionDataSet(cardStateEntry, applicationID, dsiName, NamedDataServiceActionName.DSI_READ);

	    if (cardStateEntry.getFCPOfSelectedEF() == null) {
		throw new PrerequisitesNotSatisfiedException("No DataSet to read selected.");
	    }

	    CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	    DataSetInfoType dataSetInfo = cardInfoWrapper.getDataSetByDsiName(dsiName);

	    if (dataSetInfo == null) {
		// there is no data set which contains the given dsi name so the name should be an data set name
		dataSetInfo = cardInfoWrapper.getDataSetByName(dsiName);

		if (dataSetInfo != null) {
		    if (!cardStateEntry.getFCPOfSelectedEF().getFileIdentifiers().isEmpty()) {

			byte[] path = dataSetInfo.getDataSetPath().getEfIdOrPath();
			byte[] fid = Arrays.copyOfRange(path, path.length - 2, path.length);
			if (!Arrays.equals(fid, cardStateEntry.getFCPOfSelectedEF().getFileIdentifiers().get(0))) {
			    String msg = "Wrong DataSet for reading the DSI " + dsiName + " is selected.";
			    throw new PrerequisitesNotSatisfiedException(msg);
			}
		    }

		    byte[] fileContent = CardUtils.readFile(cardStateEntry.getFCPOfSelectedEF(), env.getDispatcher(),
			    slotHandle);
		    response.setDSIContent(fileContent);
		} else {
		    String msg = "The given DSIName does not related to any know DSI or DataSet.";
		    throw new IncorrectParameterException(msg);
		}
	    } else {
		// There exists a data set with the given dsi name
		// check whether the correct file is selected
		byte[] dataSetPath = dataSetInfo.getDataSetPath().getEfIdOrPath();
		byte[] dataSetFID = new byte[] {dataSetPath[dataSetPath.length - 2], dataSetPath[dataSetPath.length - 1]};

		if (Arrays.equals(dataSetFID, cardStateEntry.getFCPOfSelectedEF().getFileIdentifiers().get(0))) {
		    DSIType dsi = cardInfoWrapper.getDSIbyName(dsiName);
		    PathType dsiPath = dsi.getDSIPath();

		    if (dsiPath.getTagRef() != null) {
			TagRef tagReference = dsiPath.getTagRef();
			byte[] tag = tagReference.getTag();
			GetData getDataRequest;
			if (tag.length == 2) {
			    getDataRequest = new GetData(GetData.INS_DATA, tag[0], tag[1]);
			    CardResponseAPDU cardResponse = getDataRequest.transmit(env.getDispatcher(), slotHandle,
				    Collections.EMPTY_LIST);
			    byte[] responseData = cardResponse.getData();

			    while (cardResponse.getTrailer()[0] == (byte) 0x61) {
				GetResponse allData = new GetResponse();
				cardResponse = allData.transmit(env.getDispatcher(), slotHandle, Collections.EMPTY_LIST);
				responseData = ByteUtils.concatenate(responseData, cardResponse.getData());
			    }
			    
			    response.setDSIContent(responseData);
			} else if (tag.length == 1) {
			    // how to determine Simple- or BER-TLV in this case correctly?
			    // Now try Simple-TLV first and if it fail try BER-TLV
			    getDataRequest = new GetData(GetData.INS_DATA, GetData.SIMPLE_TLV, tag[0]);
			    CardResponseAPDU cardResponse = getDataRequest.transmit(env.getDispatcher(), slotHandle,
				    Collections.EMPTY_LIST);
			    byte[] responseData = cardResponse.getData();

			    // just an assumption
			    if (Arrays.equals(cardResponse.getTrailer(), new byte[] {(byte) 0x6A, (byte) 0x88})) {
				getDataRequest = new GetData(GetData.INS_DATA, GetData.BER_TLV_ONE_BYTE, tag[0]);
				cardResponse = getDataRequest.transmit(env.getDispatcher(), slotHandle,
					Collections.EMPTY_LIST);
				responseData = cardResponse.getData();
			    }

			    while (cardResponse.getTrailer()[0] == (byte) 0x61) {
				GetResponse allData = new GetResponse();
				cardResponse = allData.transmit(env.getDispatcher(), slotHandle, Collections.EMPTY_LIST);
				responseData = ByteUtils.concatenate(responseData, cardResponse.getData());
			    }

			    response.setDSIContent(responseData);
			}
		    } else if (dsiPath.getIndex() != null) {
			byte[] index = dsiPath.getIndex();
			byte[] length = dsiPath.getLength();

			List<byte[]> allowedResponse = new ArrayList<>();
			allowedResponse.add(new byte[]{(byte) 0x90, (byte) 0x00});
			allowedResponse.add(new byte[]{(byte) 0x62, (byte) 0x82});
			
			if (cardStateEntry.getFCPOfSelectedEF().getDataElements().isLinear()) {
			    // in this case we use the index as record number and the length as length of record
			    ReadRecord readRecord = new ReadRecord(index[0]);
			    // NOTE: For record based files TR-0312-4 states to ignore the length field in case of records    
			    CardResponseAPDU cardResponse = readRecord.transmit(env.getDispatcher(), slotHandle,
				    allowedResponse);
			    response.setDSIContent(cardResponse.getData());
			} else {
			    // in this case we use index as offset and length as the expected length
			    ReadBinary readBinary = new ReadBinary(ByteUtils.toShort(index), ByteUtils.toShort(length));
			    CardResponseAPDU cardResponse = readBinary.transmit(env.getDispatcher(), slotHandle,
				    allowedResponse);
			    response.setDSIContent(cardResponse.getData());
			}
		    } else {
			String msg = "The currently selected data set does not contain the DSI with the name " + dsiName;
			throw new PrerequisitesNotSatisfiedException(msg);
		    }
		}
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
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
    @Publish
    @Override
    public EncipherResponse encipher(Encipher request) {
	EncipherResponse response = WSHelper.makeResponse(EncipherResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);
	    byte[] applicationID = cardStateEntry.getCurrentCardApplication().getApplicationIdentifier();
	    String didName = SALUtils.getDIDName(request);

	    byte[] plainText = request.getPlainText();
	    Assert.assertIncorrectParameter(plainText, "The parameter PlainText is empty.");

	    DIDScopeType didScope = request.getDIDScope();
	    if (didScope == null) {
		didScope = DIDScopeType.LOCAL;
	    }

	    if (didScope.equals(DIDScopeType.LOCAL)) {
		byte[] necessaryCardApp = cardStateEntry.getInfo().getApplicationIdByDidName(didName, didScope);
		if (! Arrays.equals(necessaryCardApp, applicationID)) {
		    throw new SecurityConditionNotSatisfiedException("Wrong application selected.");
		}
	    }

	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, didScope);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, request.getDIDScope(), protocolURI);
	    if (protocol.hasNextStep(FunctionType.Encipher)) {
		response = protocol.encipher(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("Encipher", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
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
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);
	    byte[] applicationID = cardStateEntry.getCurrentCardApplication().getApplicationIdentifier();
	    String didName = SALUtils.getDIDName(request);
	    byte[] cipherText = request.getCipherText();
	    Assert.assertIncorrectParameter(cipherText, "The parameter CipherText is empty.");

	    DIDScopeType didScope = request.getDIDScope();
	    if (didScope == null) {
		didScope = DIDScopeType.LOCAL;
	    }

	    if (didScope.equals(DIDScopeType.LOCAL)) {
		byte[] necessaryCardApp = cardStateEntry.getInfo().getApplicationIdByDidName(didName, didScope);
		if (! Arrays.equals(necessaryCardApp, applicationID)) {
		    throw new SecurityConditionNotSatisfiedException("Wrong application selected.");
		}
	    }

	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, didScope);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");
	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, request.getDIDScope(), protocolURI);
	    
	    if (protocol.hasNextStep(FunctionType.Decipher)) {
		response = protocol.decipher(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("Decipher", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
	    response.setResult(WSHelper.makeResult(e));
	}

	return response;
    }

    /**
     * The GetRandom function returns a random number which is suitable for authentication with the DID addressed with
     * DIDName.
     * See BSI-TR-03112-4, version 1.1.2, section 3.5.3.
     *
     * @param request GetRandom
     * @return GetRandomResponse
     */
    @Publish
    @Override
    public GetRandomResponse getRandom(GetRandom request) {
	GetRandomResponse response = WSHelper.makeResponse(GetRandomResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);
	    byte[] applicationID = cardStateEntry.getCurrentCardApplication().getApplicationIdentifier();
	    String didName = SALUtils.getDIDName(request);

	    DIDScopeType didScope = request.getDIDScope();
	    if (didScope == null) {
		didScope = DIDScopeType.LOCAL;
	    }

	    if (didScope.equals(DIDScopeType.LOCAL)) {
		byte[] necessaryApp = cardStateEntry.getInfo().getApplicationIdByDidName(didName, didScope);
		if (! Arrays.equals(necessaryApp, applicationID)) {
		    throw new SecurityConditionNotSatisfiedException("The wrong application is selected for getRandom()");
		}
	    }

	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, didScope);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, request.getDIDScope(), protocolURI);
	    if (protocol.hasNextStep(FunctionType.GetRandom)) {
		response = protocol.getRandom(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("GetRandom", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
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
    @Publish
    @Override
    public HashResponse hash(Hash request) {
	HashResponse response = WSHelper.makeResponse(HashResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);
	    byte[] applicationID = cardStateEntry.getCurrentCardApplication().getApplicationIdentifier();
	    String didName = SALUtils.getDIDName(request);
	    byte[] message = request.getMessage();
    	    Assert.assertIncorrectParameter(message, "The parameter Message is empty.");

	    DIDScopeType didScope = request.getDIDScope();
	    if (didScope == null) {
		didScope = DIDScopeType.LOCAL;
	    }

	    if (didScope.equals(DIDScopeType.LOCAL)) {
		byte[] necesssaryApp = cardStateEntry.getInfo().getApplicationIdByDidName(didName, didScope);
		if (! Arrays.equals(necesssaryApp, applicationID)) {
		    String msg = "Wrong application for executing Hash with the specified DID " + didName + ".";
		    throw new SecurityConditionNotSatisfiedException(msg);
		}
	    }

	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, didScope);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, request.getDIDScope(), protocolURI);
	    if (protocol.hasNextStep(FunctionType.Hash)) {
		response = protocol.hash(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("Hash", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
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

	CardStateEntry cardStateEntry = null;
	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);
	    byte[] applicationID = cardStateEntry.getCurrentCardApplication().getApplicationIdentifier();
	    String didName = SALUtils.getDIDName(request);
	    byte[] message = request.getMessage();
	    Assert.assertIncorrectParameter(message, "The parameter Message is empty.");
	    DIDScopeType didScope = request.getDIDScope();

	    if (didScope == null) {
		didScope = DIDScopeType.LOCAL;
	    }

	    if (didScope.equals(DIDScopeType.LOCAL)) {
		byte[] necessarySelectedApp = cardStateEntry.getInfo().getApplicationIdByDidName(didName, didScope);
		if (! Arrays.equals(necessarySelectedApp, applicationID)) {
		    String msg = "Wrong application selected for the execution of Sign with the DID " + didName + ".";
		    throw new SecurityConditionNotSatisfiedException(msg);
		}
	    }

	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, didScope);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, request.getDIDScope(), protocolURI);
	    if (protocol.hasNextStep(FunctionType.Sign)) {
		response = protocol.sign(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("Sign", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
	    response.setResult(WSHelper.makeResult(e));
	}

	// TODO: remove when PIN state tracking is implemented
	setPinNotAuth(cardStateEntry);

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
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);
	    byte[] applicationID = cardStateEntry.getCurrentCardApplication().getApplicationIdentifier();
	    String didName = SALUtils.getDIDName(request);
	    byte[] signature = request.getSignature();
	    Assert.assertIncorrectParameter(signature, "The parameter Signature is empty.");
	    DIDScopeType didScope = request.getDIDScope();

	    if (didScope == null) {
		didScope = DIDScopeType.LOCAL;
	    }

	    if (didScope.equals(DIDScopeType.LOCAL)) {
		byte[] necessarySelectedApp = cardStateEntry.getInfo().getApplicationIdByDidName(didName, didScope);
		if (! Arrays.equals(necessarySelectedApp, applicationID)) {
		    String msg = "Wrong application selected for the execution of VerifySignature with the DID " +
			    didName + ".";
		    throw new SecurityConditionNotSatisfiedException(msg);
		}
	    }

	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, didScope);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, request.getDIDScope(), protocolURI);
	    if (protocol.hasNextStep(FunctionType.VerifySignature)) {
		response = protocol.verifySignature(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("VerifySignature", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
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
	VerifyCertificateResponse response = WSHelper.makeResponse(VerifyCertificateResponse.class,
		WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);
	    byte[] applicationID = cardStateEntry.getCurrentCardApplication().getApplicationIdentifier();
	    String didName = SALUtils.getDIDName(request);

	    byte[] certificate = request.getCertificate();
	    Assert.assertIncorrectParameter(certificate, "The parameter Certificate is empty.");

	    String certificateType = request.getCertificateType();
	    Assert.assertIncorrectParameter(certificateType, "The parameter CertificateType is empty.");

	    String rootCert = request.getRootCert();
	    Assert.assertIncorrectParameter(rootCert, "The parameter RootCert is empty.");

	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, applicationID);
	    Assert.assertNamedEntityNotFound(didStructure, "The given DIDName cannot be found.");

	    DIDScopeType didScope = request.getDIDScope();
	    if (didScope == null) {
		didScope = DIDScopeType.LOCAL;
	    }

	    if (didScope.equals(DIDScopeType.LOCAL)) {
		byte[] necessarySelectedApp = cardStateEntry.getInfo().getApplicationIdByDidName(didName, didScope);
		if (! Arrays.equals(necessarySelectedApp, applicationID)) {
		    String msg = "Wrong application selected for the execution of VerifyCertificate with the DID " +
			    didName + ".";
		    throw new SecurityConditionNotSatisfiedException(msg);
		}
	    }

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, request.getDIDScope(), protocolURI);
	    if (protocol.hasNextStep(FunctionType.VerifyCertificate)) {
		response = protocol.verifyCertificate(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("VerifyCertificate", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
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
    @Publish
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

	    List<DIDInfoType> didInfos = new ArrayList<>(cardApplication.getDIDInfoList());

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
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
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
	return WSHelper.makeResponse(DIDCreateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The public information for a DID is read with the DIDGet function.
     * See BSI-TR-03112-4, version 1.1.2, section 3.6.3.
     *
     * @param request DIDGet
     * @return DIDGetResponse
     */
    @Publish
    @Override
    public DIDGetResponse didGet(DIDGet request) {
	DIDGetResponse response = WSHelper.makeResponse(DIDGetResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    // handle must be requested without application, as it is irrelevant for this call
	    CardStateEntry cardStateEntry = SALUtils.getCardStateEntry(states, connectionHandle, false);
	    String didName = SALUtils.getDIDName(request);

	    DIDStructureType didStructure = SALUtils.getDIDStructure(request, didName, cardStateEntry, connectionHandle);
	    response.setDIDStructure(didStructure);
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
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
    // TODO: discuss whether we should publish this @Publish
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

            Assert.securityConditionDID(cardStateEntry, cardApplicationID, didName,
		    DifferentialIdentityServiceActionName.DID_UPDATE);

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, null, protocolURI);
	    if (protocol.hasNextStep(FunctionType.DIDUpdate)) {
		response = protocol.didUpdate(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("DIDUpdate", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
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

            Assert.securityConditionDID(cardStateEntry, cardApplicationID, didName,
		    DifferentialIdentityServiceActionName.DID_DELETE);

	    String protocolURI = didStructure.getDIDMarker().getProtocol();
	    SALProtocol protocol = getProtocol(connectionHandle, null, protocolURI);
	    if (protocol.hasNextStep(FunctionType.DIDDelete)) {
		response = protocol.didDelete(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("DIDDelete", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
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
    @Publish
    @Override
    public DIDAuthenticateResponse didAuthenticate(DIDAuthenticate request) {
	DIDAuthenticateResponse response = WSHelper.makeResponse(DIDAuthenticateResponse.class, WSHelper.makeResultOK());

	try {
	    ConnectionHandleType connectionHandle = SALUtils.getConnectionHandle(request);
	    DIDAuthenticationDataType didAuthenticationData = request.getAuthenticationProtocolData();
	    Assert.assertIncorrectParameter(didAuthenticationData, "The parameter AuthenticationProtocolData is empty.");

	    String protocolURI = didAuthenticationData.getProtocol();
	    // FIXME: workaround for missing protocol URI from eID-Servers
	    if (protocolURI == null) {
		LOG.warn("ProtocolURI was null");
		protocolURI = ECardConstants.Protocol.EAC_GENERIC;
	    } else if (protocolURI.equals("urn:oid:1.0.24727.3.0.0.7.2")) {
		LOG.warn("ProtocolURI was urn:oid:1.0.24727.3.0.0.7.2");
		protocolURI = ECardConstants.Protocol.EAC_GENERIC;
	    }
	    didAuthenticationData.setProtocol(protocolURI);

	    SALProtocol protocol = getProtocol(connectionHandle, request.getDIDScope(), protocolURI);
	    if (protocol.hasNextStep(FunctionType.DIDAuthenticate)) {
		response = protocol.didAuthenticate(request);
		removeFinishedProtocol(connectionHandle, protocolURI, protocol);
	    } else {
		throw new InappropriateProtocolForActionException("DIDAuthenticate", protocol.toString());
	    }
	} catch (ECardException e) {
	    response.setResult(e.getResult());
	} catch (Exception e) {
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
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
    @Publish
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
	    LOG.error(e.getMessage(), e);
	    throwThreadKillException(e);
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
     * Removes a finished protocol from the SAL instance.
     *
     * @param handle Connection Handle
     * @param protocolURI Protocol URI
     * @param protocol Protocol
     * @throws UnknownConnectionHandleException
     */
    private void removeFinishedProtocol(ConnectionHandleType handle, String protocolURI, SALProtocol protocol)
	    throws UnknownConnectionHandleException {
	if (protocol.isFinished()) {
	    try {
		CardStateEntry entry = SALUtils.getCardStateEntry(states, handle, false);
		entry.removeProtocol(protocolURI);
	    } finally {
		protocolSelector.returnSALProtocol(protocol, false);
	    }
	}
    }

    private SALProtocol getProtocol(@Nonnull ConnectionHandleType handle, @Nullable DIDScopeType scope,
	    @Nonnull String protocolURI) throws UnknownProtocolException, UnknownConnectionHandleException {
	CardStateEntry entry = SALUtils.getCardStateEntry(states, handle, scope != DIDScopeType.GLOBAL);
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

    private byte[] createFakeFCP(byte[] fid) {
	try {
	    TLV fcp = new TLV();
	    fcp.setTagNumWithClass((byte) 0x62);
	    TLV fileID = new TLV();
	    fileID.setTagNumWithClass((byte) 0x83);
	    fileID.setValue(fid);
	    fcp.setChild(fileID);
	    return fcp.toBER();
	} catch (TLVException ex) {
	    LOG.error(null, ex);
	    return null;
	}
    }

    // TODO: remove function when state tracking is implemented
    private void setPinNotAuth(@Nullable CardStateEntry cardStateEntry) {
	if (cardStateEntry != null) {
	    LOG.info("Unauthenticate Card PIN (state=false).");

	    // This method only works in a a very limited way. All PIN DIDs get status unauth here.
	    for (DIDInfoType didInfo : Collections.unmodifiableCollection(cardStateEntry.getAuthenticatedDIDs())) {
		if ("urn:oid:1.3.162.15480.3.0.9".equals(didInfo.getDifferentialIdentity().getDIDProtocol())) {
		    cardStateEntry.removeAuthenticated(didInfo);
		}
	    }
	}
    }

    private void throwThreadKillException(Exception ex) {
	Throwable cause;
	if (ex instanceof InvocationTargetExceptionUnchecked) {
	    cause = ex.getCause();
	} else {
	    cause = ex;
	}

	if (cause instanceof ThreadTerminateException) {
	    throw (ThreadTerminateException) cause;
	} else if (cause instanceof InterruptedException) {
	    throw new ThreadTerminateException("Thread running inside SAL interrupted.", cause);
	} else if (cause instanceof RuntimeException) {
	    throw (RuntimeException) ex;
	}
    }

}
