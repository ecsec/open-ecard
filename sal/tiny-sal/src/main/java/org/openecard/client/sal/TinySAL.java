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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.ECardException;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.interfaces.Environment;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.Protocol;
import org.openecard.client.common.sal.ProtocolFactory;
import org.openecard.client.common.sal.state.CardStateEntry;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.CardCommands;
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
    private ConcurrentSkipListMap<String, ConnectionHandleType> cHandles;
    private boolean legacyMode;
    private ProtocolFactories protocolFactories = new ProtocolFactories();
    private UserConsent userConsent;
    private CardStateMap states;


    public TinySAL(Environment env, CardStateMap states) {
	this.env = env;
	this.states = states;
	sessionId = ValueGenerators.generateSessionID();
	legacyMode = false;
	cHandles = new ConcurrentSkipListMap<String, ConnectionHandleType>();
    }

    @Deprecated
    public TinySAL(Environment env, CardStateMap states, String sessionId) {
	this.env = env;
	this.states = states;
	this.sessionId = sessionId;
	legacyMode = true;
	cHandles = new ConcurrentSkipListMap<String, ConnectionHandleType>();
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

	ChannelHandleType channelHandle = cardApplicationPath.getCardAppPathRequest().getChannelHandle();
	byte[] contextHandle = cardApplicationPath.getCardAppPathRequest().getContextHandle();
	String ifdName = cardApplicationPath.getCardAppPathRequest().getIFDName();
	BigInteger slotIndex = cardApplicationPath.getCardAppPathRequest().getSlotIndex();
	byte[] cardApplication = cardApplicationPath.getCardAppPathRequest().getCardApplication();

	CardApplicationPathResponse res = new CardApplicationPathResponse();
	Result result = new Result();
	result.setResultMajor(org.openecard.client.common.ECardConstants.Major.OK);
	res.setResult(result);
	CardAppPathResultSet cardAppPathResultSet =  new CardAppPathResultSet();
	
	
	
	if(channelHandle!=null){
	    return WSHelper.makeResponse(CardApplicationPathResponse.class, WSHelper.makeResultUnknownError("Remote frameworks are not yet supported"));
	}

	EstablishContextResponse ecr = this.env.getIFD().establishContext(new EstablishContext());
	if(!ByteUtils.compare(contextHandle, ecr.getContextHandle())){
	    return WSHelper.makeResponse(CardApplicationPathResponse.class, WSHelper.makeResultUnknownError("No IFD with the specified ContextHandle available"));
	}

	if(ifdName!=null){ //Search only this IFD 
	    Connect c = new Connect();
	    if(slotIndex==null){ //Search on all slots, but as SMIO creates a new IFD for every Slot there wont be more than the "0"-Slot
		c.setContextHandle(contextHandle);
		c.setIFDName(ifdName);
		c.setSlot(new BigInteger("0"));
	    } else { //Search only on specific slot
		c.setContextHandle(contextHandle);
		c.setIFDName(ifdName);
		c.setSlot(slotIndex);
	    }
	    
	    ConnectResponse cr = this.env.getIFD().connect(c);
	    if(!cr.getResult().getResultMajor().equals(ECardConstants.Major.OK)){
		 CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
		    cardApplicationPathType.setContextHandle(contextHandle);
		    cardApplicationPathType.setIFDName(ifdName);
		    cardApplicationPathType.setSlotIndex(new BigInteger("0"));
		    cardAppPathResultSet.getCardApplicationPathResult().add(cardApplicationPathType);
	    }

	    if(cardApplication!=null){
		   Transmit t = new Transmit();
		   t.setSlotHandle(cr.getSlotHandle());
		   InputAPDUInfoType iait = new InputAPDUInfoType();
		   iait.setInputAPDU(CardCommands.Select.application(cardApplication));
		   t.getInputAPDUInfo().add(iait);
		   TransmitResponse tr = this.env.getIFD().transmit(t);
		   System.out.println(tr.getResult().getResultMajor());
		   if(ByteUtils.compare(new byte[]{(byte) 0x90,0x00}, tr.getOutputAPDU().get(0))){
		       CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
		       cardApplicationPathType.setContextHandle(contextHandle);
		       cardApplicationPathType.setIFDName(ifdName);
		       cardApplicationPathType.setSlotIndex(new BigInteger("0"));
		       cardApplicationPathType.setCardApplication(cardApplication);
		       cardAppPathResultSet.getCardApplicationPathResult().add(cardApplicationPathType);    
		   } 
		} else {
		//TODO list all applications ..
		}
	} else { //Search on all IFDs

	    ListIFDs listIFDs = new ListIFDs();
	    listIFDs.setContextHandle(contextHandle);
	    ListIFDsResponse listIFDsResponse = this.env.getIFD().listIFDs(listIFDs);

	    for(String s : listIFDsResponse.getIFDName()){
		Connect c = new Connect();
		c.setContextHandle(contextHandle);
		c.setIFDName(ifdName);
		c.setSlot(new BigInteger("0"));
		ConnectResponse cr = this.env.getIFD().connect(c);
		if(!cr.getResult().getResultMajor().equals(ECardConstants.Major.OK)){ //empty
		    CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
		    cardApplicationPathType.setContextHandle(contextHandle);
		    cardApplicationPathType.setIFDName(s);
		    cardApplicationPathType.setSlotIndex(new BigInteger("0"));
		    cardAppPathResultSet.getCardApplicationPathResult().add(cardApplicationPathType);
		    continue;
		}
		
		if(cardApplication!=null){
		    Transmit t = new Transmit();
			   InputAPDUInfoType iait = new InputAPDUInfoType();
			   iait.setInputAPDU(CardCommands.Select.application(cardApplication));
			   t.getInputAPDUInfo().add(iait);
			   TransmitResponse tr = this.env.getIFD().transmit(t);
			   if(ByteUtils.compare(new byte[]{(byte) 0x90,0x00}, tr.getOutputAPDU().get(0))){
			       CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
			       cardApplicationPathType.setContextHandle(contextHandle);
			       cardApplicationPathType.setIFDName(ifdName);
			       cardApplicationPathType.setSlotIndex(new BigInteger("0"));
			       cardApplicationPathType.setCardApplication(cardApplication);
			       cardAppPathResultSet.getCardApplicationPathResult().add(cardApplicationPathType);    
			   } 
		} else {
		//TODO list all applications ..
		}
	    }
	}


	

	//CardAppPathResultSet bla =  new CardAppPathResultSet();

	//bla.getCardApplicationPathResult().add(arg0)

	res.setCardAppPathResultSet(cardAppPathResultSet);



	//	   env.setIFD(ifd);
	//	        EstablishContext ecRequest = new EstablishContext();
	//	        EstablishContextResponse ecResponse = ifd.establishContext(ecRequest);
	//
	//	        if (ecResponse.getResult().getResultMajor().equals(ECardConstants.Major.OK)) {
	//	            if (ecResponse.getContextHandle() != null) {
	//	                ctx = ecResponse.getContextHandle();
	//	                initialized = true;
	//	            }
	//	        } else {
	//	            System.err.println("NO CONTEXT");
	//	        }
	//	        if (recognizeCard) {
	//	            try {
	//	                recognition = new CardRecognition(ifd, ctx);
	//	            } catch (Exception ex) {
	//	                initialized = false;
	//	                System.err.println("NO initialized " + ex.getMessage());
	//	            }
	//	        } else {
	//	            recognition = null;
	//	            System.err.println("NO recognition");
	//	        }
	////	        EventManager em = new EventManager(recognition, env, ctx, null);
	////	        em.initialize();
	////	        env.setEventManager(em);
	//
	//
	////	        ClientEventCallBack callBack = new ClientEventCallBack();
	////	        em.registerAllEvents(callBack);
	//









	//		CardApplicationPath ::= SEQUENCE {
	//		    pathSecurity PathSecurityType OPTIONAL,
	//		    -- The pathSecurity element specifies the protection
	//		    -- between the local dispatcher and the remote dispatcher
	//		    -- which is located at channelHandle.protocolTerminationPoint
	//		    channelHandle ChannelHandleType OPTIONAL,
	//		    contextHandle ContextHandleType OPTIONAL,
	//		    iFDName UTF8String OPTIONAL,
	//		    slotIndex INTEGER OPTIONAL,
	//		    cardApplication ApplicationIdentifier
	//		    }


	//
	//	        ListIFDs ifds = new ListIFDs();
	//	        ifds.setContextHandle(ctx);
	//	        ListIFDsResponse res3 = ifd.listIFDs(ifds);
	//
	//	        System.out.println("ListIfds: " + res3.getResult().getResultMajor());
	//	        System.out.println("TEST: " + res3.getResult().getResultMajor());
	//	        List<String> ifdList = res3.getIFDName();
	//	        System.out.println("size: " + ifdList.size());
	//	        for (String i : ifdList) {
	//	            System.out.println("ifd:" + i);
	//	        }
	//
	//
	//	        path.setSlotIndex(BigInteger.ZERO);
	//	        path.setIFDName(ifdList.get(0));
	//
	//
	//
	//	        System.out.println("CTX: " + ByteUtils.toHexString(ctx));
	//
	//	        set.getCardApplicationPathResult().add(path);
	//	        res.setCardAppPathResultSet(set);
	//	        return res;

	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "cardApplicationPath(CardApplicationPath cardApplicationPath)", res);
	} // </editor-fold>

return res;
	//return WSHelper.makeResponse(CardApplicationPathResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
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
