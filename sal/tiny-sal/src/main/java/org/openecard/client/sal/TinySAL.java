/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard Client.
 *
 * GNU General Public License Usage
 *
 * Open eCard Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Open eCard Client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Other Usage
 *
 * Alternatively, this file may be used in accordance with the terms and
 * conditions contained in a signed written agreement between you and ecsec.
 *
 ****************************************************************************/

package org.openecard.client.sal;

import iso.std.iso_iec._24727.tech.schema.*;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse.CardAppPathResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.ECardException;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.interfaces.Environment;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.Protocol;
import org.openecard.client.common.sal.ProtocolFactory;
import org.openecard.client.common.sal.state.CardStateEntry;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.util.ValueGenerators;
import org.openecard.client.gui.UserConsent;


/**
 * 
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Simon Potzernheim <potzernheim@hs-coburg.de>
 * @author Tobias WIch <tobias.wich@ecsec.de>
 */
public class TinySAL implements org.openecard.ws.SAL {

    private static final Logger _logger = LogManager.getLogger(TinySAL.class.getName());

    private Environment env;
    private String sessionId;
    private boolean legacyMode;
    private ProtocolFactories protocolFactories = new ProtocolFactories();
    private UserConsent userConsent;
    private CardStateMap states;


    public TinySAL(Environment env, CardStateMap states) {
	this.env = env;
	this.states = states;
	sessionId = ValueGenerators.generateSessionID();
	legacyMode = false;
    }

    @Deprecated
    public TinySAL(Environment env, CardStateMap states, String sessionId) {
	this.env = env;
	this.states = states;
	this.sessionId = sessionId;
	legacyMode = true;
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

    @Deprecated
    public String getSessionId() {
	return sessionId;
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
		    proto = protocolFactories.get(protoUri).createInstance(this, env.getIFD(), userConsent);
		    entry.setProtocol(protoUri, proto);
		} else {
		    throw new UnknownProtocolException("The protocol URI '" + protoUri + "' is not registered in this SAL component.");
		}
	    }
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
	if (env != null) {
	    env.getEventManager().initialize();

	    return WSHelper.makeResponse(InitializeResponse.class, WSHelper.makeResultOK());
	}
	return WSHelper.makeResponse(InitializeResponse.class, WSHelper.makeResultUnknownError("Initialization of SAL failed."));
    }

    @Override
    public TerminateResponse terminate(Terminate parameters) {
	return WSHelper.makeResponse(TerminateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public CardApplicationPathResponse cardApplicationPath(CardApplicationPath cardApplicationPath) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "cardApplicationPath(CardApplicationPath cardApplicationPath)");
	} // </editor-fold>
	// get card handles (not terminals)
	CardApplicationPathType path = cardApplicationPath.getCardAppPathRequest();
	Set<CardStateEntry> entries = states.getMatchingEntries(path);

	// copy entries to result set
	CardAppPathResultSet resultSet = new CardAppPathResultSet();
	List<CardApplicationPathType> resultPaths = resultSet.getCardApplicationPathResult();
	for (CardStateEntry entry : entries) {
	    resultPaths.add(entry.pathCopy());
	}

	// i don't see how the errors in the spec map to what could have gone wrong here
	CardApplicationPathResponse res = WSHelper.makeResponse(CardApplicationPathResponse.class, WSHelper.makeResultOK());
	res.setCardAppPathResultSet(resultSet);
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "cardApplicationPath(CardApplicationPath cardApplicationPath)", res);
	} // </editor-fold>
	return res;
    }

    @Override
    public CardApplicationConnectResponse cardApplicationConnect(CardApplicationConnect cardApplicationConnect) {
	/*  Result result = new Result();
	        CardApplicationConnectResponse response = new CardApplicationConnectResponse();
//	        cac.setExclusiveUse(false);
//	        cac.setOutput(new OutputInfoType());

	        Connect con = new Connect();
	        con.setContextHandle(cardApplicationConnect.getCardApplicationPath().getContextHandle());
	        con.setIFDName(cardApplicationConnect.getCardApplicationPath().getIFDName());
	        con.setSlot(cardApplicationConnect.getCardApplicationPath().getSlotIndex());
	        cardApplicationConnect.getCardApplicationPath().get

	        ConnectResponse res2 = this.env.getIFD().connect(con);

	        if (res2.getResult().getResultMajor().contains("error")) {
	            response.setResult(res2.getResult());
	            return response;
	        }


	        System.out.println("ConnectResponse: " + res2.getResult().getResultMajor());


	        ConnectionHandleType handle = new ConnectionHandleType();
	        handle.setSlotHandle(res2.getSlotHandle());
	        System.out.println("SAL: Connected SlotHandle:" + res2.getSlotHandle());
	        ConnectionHandleType.RecognitionInfo info;
	        try {
	            System.out.println("IFD: " + cardApplicationConnect.getCardApplicationPath().getIFDName());
	            System.out.println("SLOTINDEX: " + cardApplicationConnect.getCardApplicationPath().getSlotIndex());
	            info = recognition.recognizeCard(cardApplicationConnect.getCardApplicationPath().getIFDName(), cardApplicationConnect.getCardApplicationPath().getSlotIndex());
	        } catch (RecognitionException ex) {
	            Logger.getLogger(SALTlsCredentialsTest.class.getName()).log(Level.SEVERE, null, ex);
	            throw new UnsupportedOperationException("Scheiss ...." + ex.toString() + " " + ex.getLocalizedMessage() + " " + ex.getMessage()
	                    + cardApplicationConnect.getCardApplicationPath().getIFDName() + cardApplicationConnect.getCardApplicationPath().getSlotIndex());
	        }
	        handle.setRecognitionInfo(info);
	        response.setConnectionHandle(handle);

	        result.setResultMajor(org.openecard.client.common.ECardConstants.Major.OK);
	        response.setResult(result);
	        return response;
	 */
	return WSHelper.makeResponse(CardApplicationConnectResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public CardApplicationDisconnectResponse cardApplicationDisconnect(CardApplicationDisconnect cardApplicationDisconnect) {
//	// <editor-fold defaultstate="collapsed" desc="log trace">
//	if (_logger.isLoggable(Level.FINER)) {
//	    _logger.entering(this.getClass().getName(), "cardApplicationDisconnect(CardApplicationDisconnect cardApplicationDisconnect)");
//	} // </editor-fold>
//	Disconnect disconnect = new Disconnect();
//	disconnect.setSlotHandle(cardApplicationDisconnect.getConnectionHandle().getSlotHandle());
//	System.out.println("SAL: Disconnecting SlotHandle:" + cardApplicationDisconnect.getConnectionHandle().getSlotHandle());
//
//	DisconnectResponse res = this.env.getIFD().disconnect(disconnect);
//
//	CardApplicationDisconnectResponse response = new CardApplicationDisconnectResponse();
//	response.setResult(res.getResult());
//	// <editor-fold defaultstate="collapsed" desc="log trace">
//	if (_logger.isLoggable(Level.FINER)) {
//	    _logger.exiting(this.getClass().getName(), "cardApplicationDisconnect(CardApplicationDisconnect cardApplicationDisconnect)", response);
//	} // </editor-fold>
//	return response;
//

	return WSHelper.makeResponse(CardApplicationDisconnectResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public CardApplicationStartSessionResponse cardApplicationStartSession(CardApplicationStartSession parameters) {
	return WSHelper.makeResponse(CardApplicationStartSessionResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public CardApplicationEndSessionResponse cardApplicationEndSession(CardApplicationEndSession parameters) {
	return WSHelper.makeResponse(CardApplicationEndSessionResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public CardApplicationListResponse cardApplicationList(CardApplicationList parameters) {
	return WSHelper.makeResponse(CardApplicationListResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
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

    @Override
    public DataSetListResponse dataSetList(DataSetList parameters) {
	return WSHelper.makeResponse(DataSetListResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public DataSetCreateResponse dataSetCreate(DataSetCreate parameters) {
	return WSHelper.makeResponse(DataSetCreateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public DataSetSelectResponse dataSetSelect(DataSetSelect parameters) {
	return WSHelper.makeResponse(DataSetSelectResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
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

    @Override
    public DSIReadResponse dsiRead(DSIRead parameters) {
	return WSHelper.makeResponse(DSIReadResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public EncipherResponse encipher(Encipher parameters) {
	return WSHelper.makeResponse(EncipherResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public DecipherResponse decipher(Decipher parameters) {
	return WSHelper.makeResponse(DecipherResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
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
    public SignResponse sign(Sign parameters) {
	return WSHelper.makeResponse(SignResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public VerifySignatureResponse verifySignature(VerifySignature parameters) {
	return WSHelper.makeResponse(VerifySignatureResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public VerifyCertificateResponse verifyCertificate(VerifyCertificate parameters) {
	return WSHelper.makeResponse(VerifyCertificateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public DIDListResponse didList(DIDList parameters) {
	return WSHelper.makeResponse(DIDListResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public DIDCreateResponse didCreate(DIDCreate parameters) {
	return WSHelper.makeResponse(DIDCreateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public DIDGetResponse didGet(DIDGet parameters) {
	return WSHelper.makeResponse(DIDGetResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
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
	String protoUri = didAuthenticate.getAuthenticationProtocolData().getProtocol();
	ConnectionHandleType handle = didAuthenticate.getConnectionHandle();

	try {
	    Protocol proto = getProtocol(handle, protoUri);
	    if (proto.hasNextStep(FunctionType.DIDAuthenticate)) {
		DIDAuthenticateResponse resp = proto.didAuthenticate(didAuthenticate);
		removeFinishedProtocol(handle, protoUri, proto);
		return resp;
	    } else {
		throw new UnknownProtocolException("No protocol step available for DIDAuthenticate in protocol " + proto.toString() + ".");
	    }
	} catch (ECardException ex) {
	    // TODO: log exception
	    Result res = WSHelper.makeResult(ex);
	    DIDAuthenticateResponse resp = WSHelper.makeResponse(DIDAuthenticateResponse.class, res);
	    return resp;

	} catch (RuntimeException ex) {
	    // TODO: log exception
	    Result res = WSHelper.makeResultUnknownError(ex.getMessage());
	    DIDAuthenticateResponse resp = WSHelper.makeResponse(DIDAuthenticateResponse.class, res);
	    return resp;
	}
    }


    @Override
    public ACLListResponse aclList(ACLList parameters) {
	return WSHelper.makeResponse(ACLListResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public ACLModifyResponse aclModify(ACLModify parameters) {
	return WSHelper.makeResponse(ACLModifyResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

}
