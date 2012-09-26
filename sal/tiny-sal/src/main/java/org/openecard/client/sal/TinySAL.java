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

import iso.std.iso_iec._24727.tech.schema.*;
import iso.std.iso_iec._24727.tech.schema.CardApplicationListResponse.CardApplicationNameList;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse.CardAppPathResultSet;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.smartcardio.ResponseAPDU;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.ECardException;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.interfaces.Environment;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.Protocol;
import org.openecard.client.common.sal.ProtocolFactory;
import org.openecard.client.common.sal.anytype.CryptoMarkerType;
import org.openecard.client.common.sal.state.CardStateEntry;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.sal.state.cif.CardApplicationWrapper;
import org.openecard.client.common.sal.state.cif.CardInfoWrapper;
import org.openecard.client.common.tlv.iso7816.FCP;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.CardCommands;
import org.openecard.client.gui.UserConsent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Simon Potzernheim <potzernheim@hs-coburg.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TinySAL implements org.openecard.ws.SAL {

    private static final Logger logger = LoggerFactory.getLogger(TinySAL.class);
    
    private Environment env;
    private ProtocolFactories protocolFactories = new ProtocolFactories();
    private UserConsent userConsent;
    private CardStateMap states;

    public TinySAL(Environment env, CardStateMap states) {
	this.env = env;
	this.states = states;
    }

    public void setGUI(UserConsent uc) {
	this.userConsent = uc;
    }

    /**
     * Get list of all currently known handles, even for unrecognized cards.
     *
     * @return
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

    public boolean addProtocol(String proto, ProtocolFactory factory) {
	return protocolFactories.add(proto, factory);
    }

    private Protocol getProtocol(ConnectionHandleType handle, String protoUri) throws UnknownProtocolException, UnknownConnectionHandle {
	CardStateEntry entry = states.getEntry(handle);
	if (entry == null) {
	    throw new UnknownConnectionHandle(handle);
	} else {
	    Protocol proto = entry.getProtocol(protoUri);
	    if (proto == null) {
		if (protocolFactories.contains(protoUri)) {
		    proto = protocolFactories.get(protoUri).createInstance(env.getDispatcher(), this.userConsent);
		    entry.setProtocol(protoUri, proto);
		} else {
		    throw new UnknownProtocolException("The protocol URI '" + protoUri + "' is not registered in this SAL component.");
		}
	    }
	    proto.getInternalData().put("cardState", entry);
	    return proto;
	}
    }

    public void removeFinishedProtocol(ConnectionHandleType handle, String protoUri, Protocol proto) throws UnknownConnectionHandle {
	if (proto.isFinished()) {
	    CardStateEntry entry = states.getEntry(handle);
	    if (entry == null) {
		throw new UnknownConnectionHandle(handle);
	    } else {
		entry.removeProtocol(protoUri);
	    }
	}
    }

    @Override
    public InitializeResponse initialize(Initialize parameters) {
        return WSHelper.makeResponse(InitializeResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public TerminateResponse terminate(Terminate parameters) {
	return WSHelper.makeResponse(TerminateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * [TR-03112-4] The CardApplicationPath function determines a path between
     * the client application and a card application.
     */
    @Override
    public CardApplicationPathResponse cardApplicationPath(CardApplicationPath cardApplicationPath) {
	// get card handles (not terminals)
	CardApplicationPathType path = cardApplicationPath.getCardAppPathRequest();
	// check existence of required parameters
	if (path == null) {
	    return WSHelper.makeResponse(CardApplicationPathResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "CardAppPathRequest is null"));
	}

	Set<CardStateEntry> entries = states.getMatchingEntries(path);

	// copy entries to result set
	CardAppPathResultSet resultSet = new CardAppPathResultSet();
	List<CardApplicationPathType> resultPaths = resultSet.getCardApplicationPathResult();
	for (CardStateEntry entry : entries) {
	    CardApplicationPathType pathCopy = entry.pathCopy();
	    if (path.getCardApplication() != null) {
		pathCopy.setCardApplication(path.getCardApplication());
	    } else {
		pathCopy.setCardApplication(entry.getImplicitlySelectedApplicationIdentifier());
	    }
	    resultPaths.add(pathCopy);
	}

	// i don't see how the errors in the spec map to what could have gone wrong here
	CardApplicationPathResponse res = WSHelper.makeResponse(CardApplicationPathResponse.class, WSHelper.makeResultOK());
	res.setCardAppPathResultSet(resultSet);
	return res;
    }

    /**
     * [TR-03112-4] The CardApplicationConnect function establishes an
     * unauthenticated connection between the client application and the card
     * application. <br/>
     * <br/>
     * After invocation: A connection to the card application has been
     * established. This means that a corresponding SlotHandle has been created
     * with Connect (also refer to [TR-03112-6]) and the card application was
     * selected.
     */
    @Override
    public CardApplicationConnectResponse cardApplicationConnect(CardApplicationConnect cardApplicationConnect) {
	CardApplicationConnectResponse cardApplicationConnectResponse = new CardApplicationConnectResponse();
	// check existence of required parameters
	if (cardApplicationConnect.getCardApplicationPath() == null) {
	    return WSHelper.makeResponse(CardApplicationConnectResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "CardApplicationPath is null"));
	}

	byte[] cardApplication = cardApplicationConnect.getCardApplicationPath().getCardApplication();
	Set<CardStateEntry> cardStateEntrySet = states.getMatchingEntries(cardApplicationConnect.getCardApplicationPath());

	if (cardStateEntrySet.isEmpty()) {
	    return WSHelper.makeResponse(CardApplicationConnectResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "The ConnectionHandle is invalid."));
	}

	// [TR-03112-4] If the provided path fragments are valid for more than
	// one card application the eCard-API-Framework SHALL return any of the
	// possible choices.

	// we always return the first one matching
	CardStateEntry cardStateEntry = cardStateEntrySet.iterator().next();
	CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();

	if (!cardStateEntry.checkApplicationSecurityCondition(cardApplication, ConnectionServiceActionName.CARD_APPLICATION_CONNECT)) {
	    return WSHelper.makeResponse(CardApplicationConnectResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.SECURITY_CONDITINON_NOT_SATISFIED, null));
	}
	try {
	    // Connect to card
	    Connect connect = new Connect();
	    CardApplicationPathType cardApplicationPath = cardStateEntry.pathCopy();
	    connect.setContextHandle(cardApplicationPath.getContextHandle());
	    connect.setIFDName(cardApplicationPath.getIFDName());
	    connect.setSlot(cardApplicationPath.getSlotIndex());
	    ConnectResponse connectResponse = (ConnectResponse) env.getDispatcher().deliver(connect);
	    WSHelper.checkResult(connectResponse);

	    // Select application
	    // nPA Hack
	    // TODO: Differ between MF, EF, DF and AID.
	    if (Arrays.equals(cardApplication, new byte[]{0x3F, 0x00})) {
		transmitSingleAPDU(CardCommands.Select.MF(), connectResponse.getSlotHandle());
	    } else {
		transmitSingleAPDU(CardCommands.Select.application(cardApplication), connectResponse.getSlotHandle());
	    }
	    //FIXME
	    cardStateEntry.setCurrentCardApplication(cardApplication);
	    cardStateEntry.setSlotHandle(connectResponse.getSlotHandle());
	    states.addEntry(cardStateEntry);

	    cardApplicationConnectResponse.setConnectionHandle(cardStateEntry.handleCopy());
	    cardApplicationConnectResponse.getConnectionHandle().setCardApplication(cardApplication);
	    cardApplicationConnectResponse.setResult(WSHelper.makeResultOK());

	    return cardApplicationConnectResponse;
	} catch (Exception e) {
	    e.printStackTrace();
	    logger.warn(e.getMessage(), e);
	    return WSHelper.makeResponse(CardApplicationConnectResponse.class, WSHelper.makeResult(e));
	}
    }

    /**
     * [TR-03112-4] The CardApplicationDisconnect function terminates the
     * connection to a card application.<br/>
     * <br/>
     * After invocation: The logical connection to the card application was
     * terminated. Disconnect was invoked in particular (also refer to
     * [TR-03112-6]), whereby the SlotHandle as part of the ConnectionHandle has
     * lost its validity.
     */
    @Override
    public CardApplicationDisconnectResponse cardApplicationDisconnect(CardApplicationDisconnect cardApplicationDisconnect) {
	ConnectionHandleType connectionHandle = cardApplicationDisconnect.getConnectionHandle();
	byte[] slotHandle = connectionHandle != null ? connectionHandle.getSlotHandle() : null;

	// check existence of required parameters
	if (slotHandle == null) {
	    return WSHelper.makeResponse(CardApplicationDisconnectResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "ConnectionHandle is null"));
	}
	// CardApplicationDisconnect only operates on slotHandle
	connectionHandle = new ConnectionHandleType();
	connectionHandle.setSlotHandle(slotHandle);

	CardStateEntry cardStateEntry = states.getEntry(connectionHandle);
	if (cardStateEntry == null) {
	    return WSHelper.makeResponse(CardApplicationDisconnectResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "The ConnectionHandle is invalid."));
	}

	CardApplicationDisconnectResponse cardApplicationDisconnectResponse;
	try {
	    Disconnect disconnect = new Disconnect();
	    disconnect.setSlotHandle(connectionHandle.getSlotHandle());
	    DisconnectResponse disconnectResponse = (DisconnectResponse) env.getDispatcher().deliver(disconnect);
	    cardApplicationDisconnectResponse = new CardApplicationDisconnectResponse();
	    cardApplicationDisconnectResponse.setResult(disconnectResponse.getResult());
	    WSHelper.checkResult(disconnectResponse);
	    // remove entries associated with this handle
	    states.removeEntry(connectionHandle);
	} catch (Exception e) {
	    cardApplicationDisconnectResponse = WSHelper.makeResponse(CardApplicationDisconnectResponse.class, WSHelper.makeResult(e));
	}

	return cardApplicationDisconnectResponse;
    }

    @Override
    public CardApplicationStartSessionResponse cardApplicationStartSession(CardApplicationStartSession parameters) {
	return WSHelper.makeResponse(CardApplicationStartSessionResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public CardApplicationEndSessionResponse cardApplicationEndSession(CardApplicationEndSession parameters) {
	return WSHelper.makeResponse(CardApplicationEndSessionResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * [TR-03112-4] The CardApplicationList function returns a list of the
     * available card applications on an eCard.
     */
    @Override
    public CardApplicationListResponse cardApplicationList(CardApplicationList cardApplicationList) {
	ConnectionHandleType connectionHandle = cardApplicationList.getConnectionHandle();

	// check existence of required parameters
	if (connectionHandle == null) {
	    return WSHelper.makeResponse(CardApplicationListResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "ConnectionHandle is null"));
	}
	CardStateEntry cardStateEntry = states.getEntry(connectionHandle);

	if (cardStateEntry == null) {
	    return WSHelper.makeResponse(CardApplicationListResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "The ConnectionHandle is invalid."));
	}
	CardInfoWrapper cardInfoWrapper = cardStateEntry.getInfo();
	if (!cardStateEntry.checkApplicationSecurityCondition(cardStateEntry.getImplicitlySelectedApplicationIdentifier(), CardApplicationServiceActionName.CARD_APPLICATION_LIST)) {
	    return WSHelper.makeResponse(CardApplicationListResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.SECURITY_CONDITINON_NOT_SATISFIED, null));
	}

	CardApplicationListResponse cardApplicationListResponse = new CardApplicationListResponse();
	CardApplicationNameList cardApplicationNameList = new CardApplicationNameList();
	cardApplicationNameList.getCardApplicationName().addAll(cardInfoWrapper.getCardApplicationNameList());
	cardApplicationListResponse.setCardApplicationNameList(cardApplicationNameList);
	cardApplicationListResponse.setResult(WSHelper.makeResultOK());

	return cardApplicationListResponse;
    }

    @Override
    public CardApplicationCreateResponse cardApplicationCreate(CardApplicationCreate parameters) {
	return WSHelper.makeResponse(CardApplicationCreateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public CardApplicationDeleteResponse cardApplicationDelete(CardApplicationDelete parameters) {
	return WSHelper.makeResponse(CardApplicationDeleteResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public CardApplicationServiceListResponse cardApplicationServiceList(CardApplicationServiceList parameters) {
	return WSHelper.makeResponse(CardApplicationServiceListResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public CardApplicationServiceCreateResponse cardApplicationServiceCreate(CardApplicationServiceCreate parameters) {
	return WSHelper.makeResponse(CardApplicationServiceCreateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public CardApplicationServiceLoadResponse cardApplicationServiceLoad(CardApplicationServiceLoad parameters) {
	return WSHelper.makeResponse(CardApplicationServiceLoadResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public CardApplicationServiceDeleteResponse cardApplicationServiceDelete(CardApplicationServiceDelete parameters) {
	return WSHelper.makeResponse(CardApplicationServiceDeleteResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public CardApplicationServiceDescribeResponse cardApplicationServiceDescribe(CardApplicationServiceDescribe parameters) {
	return WSHelper.makeResponse(CardApplicationServiceDescribeResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public ExecuteActionResponse executeAction(ExecuteAction parameters) {
	return WSHelper.makeResponse(ExecuteActionResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * The DataSetList function returns the list of the data sets in the card
     * application addressed with the ConnectionHandle.
     */
    @Override
    public DataSetListResponse dataSetList(DataSetList dataSetList) {
	ConnectionHandleType connectionHandle = dataSetList.getConnectionHandle();
	if (connectionHandle == null) {
	    return WSHelper.makeResponse(DataSetListResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "The ConnectionHandle is null."));
	}
	CardStateEntry cardStateEntry = states.getEntry(connectionHandle);

	if (cardStateEntry == null) {
	    return WSHelper.makeResponse(DataSetListResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "The ConnectionHandle is invalid."));
	}

	if (!cardStateEntry.checkApplicationSecurityCondition(connectionHandle.getCardApplication(), NamedDataServiceActionName.DATA_SET_LIST)) {
	    return WSHelper.makeResponse(DataSetListResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.SECURITY_CONDITINON_NOT_SATISFIED, null));
	}

	DataSetNameListType dataSetNameList = cardStateEntry.getInfo().getDataSetNameList(connectionHandle.getCardApplication());
	DataSetListResponse dataSetListResponse = new DataSetListResponse();
	dataSetListResponse.setDataSetNameList(dataSetNameList);
	dataSetListResponse.setResult(WSHelper.makeResultOK());

	return dataSetListResponse;
    }

    @Override
    public DataSetCreateResponse dataSetCreate(DataSetCreate parameters) {
	return WSHelper.makeResponse(DataSetCreateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * [TR-03112-4] The DataSetSelect function selects a data set in a card
     * application.
     */
    @Override
    public DataSetSelectResponse dataSetSelect(DataSetSelect dataSetSelect) {
	ConnectionHandleType connectionHandle = dataSetSelect.getConnectionHandle();

	if (connectionHandle == null) {
	    return WSHelper.makeResponse(DataSetSelectResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "The ConnectionHandle is null."));
	}

	String dataSetName = dataSetSelect.getDataSetName();

	if (dataSetName == null) {
	    return WSHelper.makeResponse(DataSetSelectResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "The dataSetName is null."));
	}

	CardStateEntry cardStateEntry = states.getEntry(connectionHandle);

	if (cardStateEntry == null) {
	    return WSHelper.makeResponse(DataSetSelectResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "The ConnectionHandle is invalid."));
	}

	DataSetInfoType dataSetInfo = cardStateEntry.getInfo().getDataSet(dataSetName, connectionHandle.getCardApplication());

	if (dataSetInfo == null) {
	    return WSHelper.makeResponse(DataSetSelectResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.NAMED_ENTITY_NOT_FOUND, "The dataset " + dataSetName + "could not be found."));
	}

	byte[] fileIdentifier = dataSetInfo.getDataSetPath().getEfIdOrPath();
	if (!cardStateEntry.checkDataSetSecurityCondition(connectionHandle.getCardApplication(), dataSetName, NamedDataServiceActionName.DATA_SET_SELECT)) {
	    return WSHelper.makeResponse(DataSetSelectResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.SECURITY_CONDITINON_NOT_SATISFIED, null));
	}
	try {
	    this.transmitSingleAPDU(CardCommands.Select.EF(fileIdentifier), connectionHandle.getSlotHandle());
	} catch (Exception e) {
	    logger.warn(e.getMessage(), e);
	    return WSHelper.makeResponse(DataSetSelectResponse.class, WSHelper.makeResult(e));
	}
	DataSetSelectResponse dataSetSelectResponse = new DataSetSelectResponse();
	dataSetSelectResponse.setResult(WSHelper.makeResultOK());

	return dataSetSelectResponse;
    }

    @Override
    public DataSetDeleteResponse dataSetDelete(DataSetDelete parameters) {
	return WSHelper.makeResponse(DataSetDeleteResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public DSIListResponse dsiList(DSIList parameters) {
	return WSHelper.makeResponse(DSIListResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public DSICreateResponse dsiCreate(DSICreate parameters) {
	return WSHelper.makeResponse(DSICreateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public DSIDeleteResponse dsiDelete(DSIDelete parameters) {
	return WSHelper.makeResponse(DSIDeleteResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public DSIWriteResponse dsiWrite(DSIWrite parameters) {
	return WSHelper.makeResponse(DSIWriteResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * [TR-03112-4] The DSIRead function reads out the content of a specific DSI
     * (Data Structure for Interoperability).
     */
    @Override
    public DSIReadResponse dsiRead(DSIRead dsiRead) {
	try {
	    String dsiName = dsiRead.getDSIName();
	    if (dsiName == null) {
		return WSHelper.makeResponse(DSIReadResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "dsiName is null."));
	    }
	    if (dsiRead.getConnectionHandle() == null) {
		return WSHelper.makeResponse(DSIReadResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "The ConnectionHandle is null."));
	    }
	    byte[] slotHandle = dsiRead.getConnectionHandle().getSlotHandle();
	    CardStateEntry cardStateEntry = states.getEntry(dsiRead.getConnectionHandle());
	    if (cardStateEntry == null) {
		return WSHelper.makeResponse(DSIReadResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "The ConnectionHandle is invalid."));
	    }

	    DataSetInfoType dataSetInfo = cardStateEntry.getInfo().getDataSet(dsiName, dsiRead.getConnectionHandle().getCardApplication());
	    if (dataSetInfo == null) {
		return WSHelper.makeResponse(DSIReadResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.NAMED_ENTITY_NOT_FOUND, "The dsi " + dsiName + "could not be found."));
	    }
	    byte[] fileIdentifier = dataSetInfo.getDataSetPath().getEfIdOrPath();

	    if (!cardStateEntry.checkDataSetSecurityCondition(dsiRead.getConnectionHandle().getCardApplication(), dsiName, NamedDataServiceActionName.DSI_READ)) {
		return WSHelper.makeResponse(DSIReadResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.SECURITY_CONDITINON_NOT_SATISFIED, null));
	    }

	    // read length of the DSI
	    ResponseAPDU rapdu = this.transmitSingleAPDU(CardCommands.Select.EF_FCP(fileIdentifier), slotHandle);
	    FCP fcp = new FCP(rapdu.getData());
	    long length = fcp.getNumBytes();

	    // select the dsi
	    this.transmitSingleAPDU(CardCommands.Select.EF(fileIdentifier), slotHandle);

	    // actually read the contents of the DSI
	    ByteArrayOutputStream fileContent = new ByteArrayOutputStream((int) length);
	    for (short offset = 0; length > 0; offset += 255, length -= 255) {
		byte[] apdu = CardCommands.Read.binary(offset, (short) ((length >= 255) ? 255 : length));
		rapdu = this.transmitSingleAPDU(apdu, slotHandle);
		fileContent.write(rapdu.getData());
	    }

	    DSIReadResponse dsiReadResponse = new DSIReadResponse();
	    dsiReadResponse.setDSIContent(fileContent.toByteArray());
	    dsiReadResponse.setResult(WSHelper.makeResultOK());

	    return dsiReadResponse;
	} catch (Exception e) {
	    logger.warn(e.getMessage(), e);
	    return WSHelper.makeResponse(DSIReadResponse.class, WSHelper.makeResult(e));
	}
    }

    @Override
    public EncipherResponse encipher(Encipher encipher) {
	try {
	    String didName = encipher.getDIDName();
	    if (didName == null) {
		return WSHelper.makeResponse(EncipherResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "didName is null."));
	    }
	    byte[] plainText = encipher.getPlainText();
	    if (plainText == null) {
		return WSHelper.makeResponse(EncipherResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "plainText is null."));
	    }
	    ConnectionHandleType connectionHandle = encipher.getConnectionHandle();
	    if (connectionHandle == null) {
		return WSHelper.makeResponse(EncipherResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "connectionHandle is null."));
	    }

	    CardStateEntry cardStateEntry = states.getEntry(connectionHandle);
	    if (cardStateEntry == null) {
		return WSHelper.makeResponse(EncipherResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "The ConnectionHandle is invalid."));
	    }

	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, connectionHandle.getCardApplication());
	    if (didStructure == null) {
		return WSHelper.makeResponse(EncipherResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.NAMED_ENTITY_NOT_FOUND, "The did " + didName + "could not be found."));
	    }
	    String protoUri = didStructure.getDIDMarker().getProtocol();

	    Protocol proto = getProtocol(connectionHandle, protoUri);
	    if (proto.hasNextStep(FunctionType.Encipher)) {
		EncipherResponse resp = proto.encipher(encipher);
		removeFinishedProtocol(connectionHandle, protoUri, proto);
		return resp;
	    } else {
		throw new UnknownProtocolException("No protocol step available for Encipher in protocol " + proto.toString() + ".");
	    }
	} catch (ECardException ex) {
	    logger.warn(ex.getMessage(), ex);
	    Result res = WSHelper.makeResult(ex);
	    EncipherResponse resp = WSHelper.makeResponse(EncipherResponse.class, res);
	    return resp;

	} catch (RuntimeException ex) {
	    logger.warn(ex.getMessage(), ex);
	    Result res = WSHelper.makeResultUnknownError(ex.getMessage());
	    EncipherResponse resp = WSHelper.makeResponse(EncipherResponse.class, res);
	    return resp;
	}
    }

    @Override
    public DecipherResponse decipher(Decipher decipher) {
	try {
	    String didName = decipher.getDIDName();
	    ConnectionHandleType connectionHandle = decipher.getConnectionHandle();
	    if (connectionHandle == null) {
		return WSHelper.makeResponse(DecipherResponse.class,
			WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "connectionHandle is null."));
	    }

	    CardStateEntry cardStateEntry = states.getEntry(connectionHandle);
	    if (cardStateEntry == null) {
		return WSHelper.makeResponse(DecipherResponse.class, WSHelper.makeResultError(
			ECardConstants.Minor.App.INCORRECT_PARM, "The ConnectionHandle is invalid."));
	    }
	    if (didName == null) {
		return WSHelper.makeResponse(DecipherResponse.class,
			WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "didName is null."));
	    }
	    byte[] message = decipher.getCipherText();
	    if (message == null) {
		return WSHelper.makeResponse(DecipherResponse.class,
			WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "message is null."));
	    }

	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName,
		    connectionHandle.getCardApplication());
	    if (didStructure == null) {
		return WSHelper.makeResponse(
			DecipherResponse.class,
			WSHelper.makeResultError(ECardConstants.Minor.SAL.NAMED_ENTITY_NOT_FOUND, "The did " + didName
				+ "could not be found."));
	    }
	    String protoUri = didStructure.getDIDMarker().getProtocol();

	    Protocol proto = getProtocol(connectionHandle, protoUri);
	    if (proto.hasNextStep(FunctionType.Decipher)) {
		DecipherResponse resp = proto.decipher(decipher);
		removeFinishedProtocol(connectionHandle, protoUri, proto);
		return resp;
	    } else {
		throw new UnknownProtocolException("No protocol step available for sign in protocol "
			+ proto.toString() + ".");
	    }
	} catch (ECardException ex) {
	    logger.warn(ex.getMessage(), ex);
	    Result res = WSHelper.makeResult(ex);
	    DecipherResponse resp = WSHelper.makeResponse(DecipherResponse.class, res);
	    return resp;

	} catch (RuntimeException ex) {
	    logger.warn(ex.getMessage(), ex);
	    Result res = WSHelper.makeResultUnknownError(ex.getMessage());
	    DecipherResponse resp = WSHelper.makeResponse(DecipherResponse.class, res);
	    return resp;
	}
    }

    @Override
    public GetRandomResponse getRandom(GetRandom parameters) {
	return WSHelper.makeResponse(GetRandomResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public HashResponse hash(Hash parameters) {
	return WSHelper.makeResponse(HashResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public SignResponse sign(Sign sign) {
	try {
	    String didName = sign.getDIDName();
	    ConnectionHandleType connectionHandle = sign.getConnectionHandle();
	    if (connectionHandle == null) {
		return WSHelper.makeResponse(SignResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "connectionHandle is null."));
	    }

	    CardStateEntry cardStateEntry = states.getEntry(connectionHandle);
	    if (cardStateEntry == null) {
		return WSHelper.makeResponse(SignResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "The ConnectionHandle is invalid."));
	    }
	    if (didName == null) {
		return WSHelper.makeResponse(SignResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "didName is null."));
	    }
	    byte[] message = sign.getMessage();
	    if (message == null) {
		return WSHelper.makeResponse(SignResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "message is null."));
	    }

	    DIDStructureType didStructure = cardStateEntry.getDIDStructure(didName, connectionHandle.getCardApplication());
	    if (didStructure == null) {
		return WSHelper.makeResponse(SignResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.NAMED_ENTITY_NOT_FOUND, "The did " + didName + "could not be found."));
	    }
	    String protoUri = didStructure.getDIDMarker().getProtocol();

	    Protocol proto = getProtocol(connectionHandle, protoUri);
	    if (proto.hasNextStep(FunctionType.Sign)) {
		SignResponse resp = proto.sign(sign);
		removeFinishedProtocol(connectionHandle, protoUri, proto);
		return resp;
	    } else {
		throw new UnknownProtocolException("No protocol step available for sign in protocol " + proto.toString() + ".");
	    }
	} catch (ECardException ex) {
	    logger.warn(ex.getMessage(), ex);
	    Result res = WSHelper.makeResult(ex);
	    SignResponse resp = WSHelper.makeResponse(SignResponse.class, res);
	    return resp;

	} catch (RuntimeException ex) {
	    logger.warn(ex.getMessage(), ex);
	    Result res = WSHelper.makeResultUnknownError(ex.getMessage());
	    SignResponse resp = WSHelper.makeResponse(SignResponse.class, res);
	    return resp;
	}
    }

    @Override
    public VerifySignatureResponse verifySignature(VerifySignature parameters) {
	return WSHelper.makeResponse(VerifySignatureResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public VerifyCertificateResponse verifyCertificate(VerifyCertificate parameters) {
	return WSHelper.makeResponse(VerifyCertificateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    /**
     * [TR-03112-4] The DIDList function returns a list of the existing DIDs in
     * the card application addressed by the ConnectionHandle or the
     * ApplicationIdentifier-element within the Filter.
     */
    @Override
    public DIDListResponse didList(DIDList didList) {
	try {
	    ConnectionHandleType connectionHandle = didList.getConnectionHandle();
	    if (connectionHandle == null) {
		return WSHelper.makeResponse(DIDListResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "connectionHandle is null."));
	    }
	    CardStateEntry cardStateEntry = states.getEntry(connectionHandle);
	    if (cardStateEntry == null) {
		return WSHelper.makeResponse(DIDListResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "The ConnectionHandle is invalid."));
	    }

	    DIDQualifierType didQualifier = didList.getFilter();
	    String objectIdentifier = null;
	    List<DIDInfoType> didInfos = null;
	    byte[] applicationIdentifier = null;
	    String applicationFunction = null;
	    if (didQualifier != null) {
		/*
		 * [TR-03112-4] Allows specifying a protocol OID (cf.
		 * [TR-03112-7]) such that only DIDs which support a given
		 * protocol are listed.
		 */
		objectIdentifier = didQualifier.getObjectIdentifier();
		/*
		 * [TR-03112-4] Allows filtering for DIDs, which support a
		 * specific cryptographic operation. The bit string is coded as
		 * the SupportedOperations-element in [ISO7816-15].
		 */
		applicationFunction = didQualifier.getApplicationFunction();
		/*
		 * [TR-03112-4] Allows specifying an application identifier. If
		 * this element is present all DIDs within the specified card
		 * application are returned no matter which card application is
		 * currently selected.
		 */
		applicationIdentifier = didQualifier.getApplicationIdentifier();
	    }
	    CardApplicationWrapper cardApplication;
	    if (applicationIdentifier != null) {
		cardApplication = cardStateEntry.getInfo().getCardApplication(applicationIdentifier);
		if (cardApplication != null) {
		    didInfos = cardApplication.getDIDInfoList();
		} else {
		    Result r = WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "No card application with identifier "
			    + ByteUtils.toHexString(applicationIdentifier) + " available");
		    return WSHelper.makeResponse(DIDListResponse.class, r);
		}
	    } else {
		cardApplication = cardStateEntry.getCurrentCardApplication();
		didInfos = cardApplication.getDIDInfoList();
	    }

	    if (!cardStateEntry.checkApplicationSecurityCondition(cardApplication.getApplicationIdentifier(), DifferentialIdentityServiceActionName.DID_LIST)) {
		return WSHelper.makeResponse(DIDListResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.SECURITY_CONDITINON_NOT_SATISFIED, null));
	    }

	    // filter according to specified objectIdentifier
	    if (objectIdentifier != null) {
		Iterator<DIDInfoType> it = didInfos.iterator();
		while (it.hasNext()) {
		    DIDInfoType next = it.next();
		    if (!next.getDifferentialIdentity().getDIDProtocol().equals(objectIdentifier)) {
			it.remove();
		    }
		}
	    }

	    // filter according to specified applicationFunction
	    if (applicationFunction != null) {
		Iterator<DIDInfoType> it = didInfos.iterator();
		while (it.hasNext()) {
		    DIDInfoType next = it.next();
		    if (next.getDifferentialIdentity().getDIDMarker().getCryptoMarker() == null) {
			it.remove();
		    } else {
			CryptoMarkerType cryptoMarker = new CryptoMarkerType(next.getDifferentialIdentity().getDIDMarker().getCryptoMarker());
			if (!cryptoMarker.getAlgorithmInfo().getSupportedOperations().contains(applicationFunction)) {
			    it.remove();
			}
		    }
		}
	    }

	    DIDListResponse didListResponse = new DIDListResponse();
	    didListResponse.setResult(WSHelper.makeResultOK());
	    DIDNameListType didNameList = new DIDNameListType();
	    for (DIDInfoType didInfo : didInfos) {
		didNameList.getDIDName().add(didInfo.getDifferentialIdentity().getDIDName());
	    }
	    didListResponse.setDIDNameList(didNameList);

            return didListResponse;
	} catch (Exception e) {
	    logger.warn(e.getMessage(), e);
	    return WSHelper.makeResponse(DIDListResponse.class, WSHelper.makeResult(e));
	}
    }

    @Override
    public DIDCreateResponse didCreate(DIDCreate parameters) {
	return WSHelper.makeResponse(DIDCreateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public DIDGetResponse didGet(DIDGet didGet) {
	String didName = didGet.getDIDName();
	ConnectionHandleType connectionHandle = didGet.getConnectionHandle();
	if (didName == null) {
	    return WSHelper.makeResponse(DIDGetResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "didName is null."));
	}
	if (connectionHandle == null) {
	    return WSHelper.makeResponse(DIDGetResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "connectionHandle is null."));
	}

	CardStateEntry cardStateEntry = states.getEntry(connectionHandle);
	if (cardStateEntry == null) {
	    return WSHelper.makeResponse(DIDGetResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "The ConnectionHandle is invalid."));
	}
	DIDStructureType didStructure = null;
	if (didGet.getDIDScope() != null && didGet.getDIDScope().equals(DIDScopeType.GLOBAL)) {
	    didStructure = cardStateEntry.getDIDStructure(didName, cardStateEntry.getImplicitlySelectedApplicationIdentifier());
	} else {
	    didStructure = cardStateEntry.getDIDStructure(didName, connectionHandle.getCardApplication());
	}
	if (didStructure == null) {
	    return WSHelper.makeResponse(DIDGetResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.NAMED_ENTITY_NOT_FOUND, "The did " + didName + " could not be found."));
	}
	String protoUri = didStructure.getDIDMarker().getProtocol();
	DIDGetResponse resp;

	try {
	    Protocol proto = getProtocol(connectionHandle, protoUri);
	    if (proto.hasNextStep(FunctionType.DIDGet)) {
		resp = proto.didGet(didGet);
		removeFinishedProtocol(connectionHandle, protoUri, proto);
	    } else {
		throw new UnknownProtocolException("No protocol step available for DIDGet in protocol " + proto.toString() + ".");
	    }
	} catch (ECardException ex) {
	    logger.warn(ex.getMessage(), ex);
	    Result res = WSHelper.makeResult(ex);
	    resp = WSHelper.makeResponse(DIDGetResponse.class, res);

	} catch (RuntimeException ex) {
	    logger.warn(ex.getMessage(), ex);
	    Result res = WSHelper.makeResultUnknownError(ex.getMessage());
	    resp = WSHelper.makeResponse(DIDGetResponse.class, res);

	}

	return resp;
    }

    @Override
    public DIDUpdateResponse didUpdate(DIDUpdate parameters) {
	return WSHelper.makeResponse(DIDUpdateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public DIDDeleteResponse didDelete(DIDDelete parameters) {
	return WSHelper.makeResponse(DIDDeleteResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public DIDAuthenticateResponse didAuthenticate(DIDAuthenticate didAuthenticate) {
	if (didAuthenticate.getAuthenticationProtocolData() == null) {
	    return WSHelper.makeResponse(DIDAuthenticateResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "AuthenticationProtocolData is null."));
	}
	String protoUri = didAuthenticate.getAuthenticationProtocolData().getProtocol();
	//FIXME workaround for missing protoUri from eID-Servers
	if (protoUri == null) {
	    logger.warn("ProtocolURI was null");
	    protoUri = ECardConstants.Protocol.EAC;
	} else if (protoUri.equals("urn:oid:1.0.24727.3.0.0.7.2")) {
	    logger.warn("ProtocolURI was urn:oid:1.0.24727.3.0.0.7.2");
	    protoUri = ECardConstants.Protocol.EAC;
	}

	String didName = didAuthenticate.getDIDName();

	ConnectionHandleType connectionHandle = didAuthenticate.getConnectionHandle();
	if (didName == null) {
	    return WSHelper.makeResponse(DIDAuthenticateResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "didName is null."));
	}
	if (connectionHandle == null) {
	    return WSHelper.makeResponse(DIDAuthenticateResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "connectionHandle is null."));
	}
	DIDAuthenticateResponse resp = null;
	try {
	    Protocol proto = getProtocol(connectionHandle, protoUri);
	    if (proto.hasNextStep(FunctionType.DIDAuthenticate)) {
		resp = proto.didAuthenticate(didAuthenticate);
		removeFinishedProtocol(connectionHandle, protoUri, proto);
	    } else {
		throw new UnknownProtocolException("No protocol step available for DIDAuthenticate in protocol " + proto.toString() + ".");
	    }
	} catch (ECardException ex) {
	    logger.warn(ex.getMessage(), ex);
	    Result res = WSHelper.makeResult(ex);
	    resp = WSHelper.makeResponse(DIDAuthenticateResponse.class, res);
	} catch (RuntimeException ex) {
	    logger.warn(ex.getMessage(), ex);
	    Result res = WSHelper.makeResultUnknownError(ex.getMessage());
	    resp = WSHelper.makeResponse(DIDAuthenticateResponse.class, res);
	}

	return resp;
    }

    /**
     * [TR-03112-4] The ACLList function returns the access control list for the stated
     * target object (card application, data set, DID).
     */
    @Override
    public ACLListResponse aclList(ACLList aclList) {
	try {
	    ConnectionHandleType connectionHandle = aclList.getConnectionHandle();
	    if (connectionHandle == null) {
		return WSHelper.makeResponse(ACLListResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "The ConnectionHandle is null."));
	    }
	    CardStateEntry cardStateEntry = states.getEntry(connectionHandle);
	    if (cardStateEntry == null) {
		return WSHelper.makeResponse(ACLListResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "The ConnectionHandle is invalid."));
	    }

	    String dataSetName = aclList.getTargetName().getDataSetName();
	    byte[] cardApplicationIdentifier = aclList.getTargetName().getCardApplicationName();
	    String didName = aclList.getTargetName().getDIDName();
	    ACLListResponse aclListResponse = new ACLListResponse();
	    if (dataSetName == null && didName == null && cardApplicationIdentifier == null) {
		return WSHelper.makeResponse(ACLListResponse.class, WSHelper.makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, "TargetName is missing."));
	    }
	    if (dataSetName != null) {
		DataSetInfoType dataSetInfo = cardStateEntry.getInfo().getDataSet(dataSetName, connectionHandle.getCardApplication());
		if (dataSetInfo == null) {
		    return WSHelper.makeResponse(ACLListResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.NAMED_ENTITY_NOT_FOUND, "The dataSet" + dataSetName + "could not be found."));
		}
		/*
		 * if(!cardStateEntry.checkSecurityCondition(dataSetInfo, AuthorizationServiceActionName.ACL_LIST)){
		 * return WSHelper.makeResponse(ACLListResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.SECURITY_CONDITINON_NOT_SATISFIED, null));
		 * }
		 */
		aclListResponse.setTargetACL(cardStateEntry.getInfo().getDataSet(dataSetName, connectionHandle.getCardApplication()).getDataSetACL());
	    } else if (didName != null) {
		DIDInfoType didInfo = cardStateEntry.getInfo().getDIDInfo(didName, connectionHandle.getCardApplication());
		if (didInfo == null) {
		    return WSHelper.makeResponse(ACLListResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.NAMED_ENTITY_NOT_FOUND, "The did" + didName + "could not be found."));
		}
		/*
		 * if(!cardInfoWrapper.checkSecurityCondition(didName, didScope, AuthorizationServiceActionName.ACL_LIST)){
		 * return WSHelper.makeResponse(ACLListResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.SECURITY_CONDITINON_NOT_SATISFIED, null));
		 * }
		 */
		aclListResponse.setTargetACL(cardStateEntry.getInfo().getDIDInfo(didName, connectionHandle.getCardApplication()).getDIDACL());
	    } else if (cardApplicationIdentifier != null) {
		CardApplicationWrapper cardApplication = cardStateEntry.getInfo().getCardApplication(cardApplicationIdentifier);
		if (cardApplication == null) {
		    return WSHelper.makeResponse(ACLListResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.NAMED_ENTITY_NOT_FOUND, "The cardapplication with identifier " + ByteUtils.toHexString(cardApplicationIdentifier) + "could not be found."));
		}
		if (!cardStateEntry.checkApplicationSecurityCondition(cardApplicationIdentifier, AuthorizationServiceActionName.ACL_LIST)) {
		    return WSHelper.makeResponse(ACLListResponse.class, WSHelper.makeResultError(ECardConstants.Minor.SAL.SECURITY_CONDITINON_NOT_SATISFIED, null));
		}
		aclListResponse.setTargetACL(cardStateEntry.getInfo().getCardApplication(cardApplicationIdentifier).getCardApplicationACL());
	    }

	    aclListResponse.setResult(WSHelper.makeResultOK());

	    return aclListResponse;
	} catch (Exception e) {
	    logger.warn(e.getMessage(), e);
	    return WSHelper.makeResponse(ACLListResponse.class, WSHelper.makeResult(e));
	}
    }

    @Override
    public ACLModifyResponse aclModify(ACLModify parameters) {
	return WSHelper.makeResponse(ACLModifyResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    private ResponseAPDU transmitSingleAPDU(byte[] apdu, byte[] slotHandle) throws WSException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	ArrayList<byte[]> responses = new ArrayList<byte[]>() {

	    {
		add(new byte[]{(byte) 0x90, (byte) 0x00});
		add(new byte[]{(byte) 0x63, (byte) 0xC3});
	    }
	};

	Transmit t = CardCommands.makeTransmit(slotHandle, apdu, responses);
	TransmitResponse tr = (TransmitResponse) WSHelper.checkResult((TransmitResponse) env.getDispatcher().deliver(t));
	return new ResponseAPDU(tr.getOutputAPDU().get(0));
    }
}
