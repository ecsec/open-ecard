/*
 * Copyright 2012 Johannes Schmoelz ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.sal;

import iso.std.iso_iec._24727.tech.schema.ACLList;
import iso.std.iso_iec._24727.tech.schema.ACLListResponse;
import iso.std.iso_iec._24727.tech.schema.ACLModify;
import iso.std.iso_iec._24727.tech.schema.ACLModifyResponse;
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
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse.CardAppPathResultSet;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
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
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDCreate;
import iso.std.iso_iec._24727.tech.schema.DIDCreateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDDelete;
import iso.std.iso_iec._24727.tech.schema.DIDDeleteResponse;
import iso.std.iso_iec._24727.tech.schema.DIDGet;
import iso.std.iso_iec._24727.tech.schema.DIDGetResponse;
import iso.std.iso_iec._24727.tech.schema.DIDList;
import iso.std.iso_iec._24727.tech.schema.DIDListResponse;
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
import iso.std.iso_iec._24727.tech.schema.DataSetList;
import iso.std.iso_iec._24727.tech.schema.DataSetListResponse;
import iso.std.iso_iec._24727.tech.schema.DataSetSelect;
import iso.std.iso_iec._24727.tech.schema.DataSetSelectResponse;
import iso.std.iso_iec._24727.tech.schema.Decipher;
import iso.std.iso_iec._24727.tech.schema.DecipherResponse;
import iso.std.iso_iec._24727.tech.schema.Disconnect;
import iso.std.iso_iec._24727.tech.schema.DisconnectResponse;
import iso.std.iso_iec._24727.tech.schema.Encipher;
import iso.std.iso_iec._24727.tech.schema.EncipherResponse;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.ExecuteAction;
import iso.std.iso_iec._24727.tech.schema.ExecuteActionResponse;
import iso.std.iso_iec._24727.tech.schema.GetRandom;
import iso.std.iso_iec._24727.tech.schema.GetRandomResponse;
import iso.std.iso_iec._24727.tech.schema.Hash;
import iso.std.iso_iec._24727.tech.schema.HashResponse;
import iso.std.iso_iec._24727.tech.schema.Initialize;
import iso.std.iso_iec._24727.tech.schema.InitializeResponse;
import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import iso.std.iso_iec._24727.tech.schema.PathSecurityType;
import iso.std.iso_iec._24727.tech.schema.Sign;
import iso.std.iso_iec._24727.tech.schema.SignResponse;
import iso.std.iso_iec._24727.tech.schema.Terminate;
import iso.std.iso_iec._24727.tech.schema.TerminateResponse;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificate;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificateResponse;
import iso.std.iso_iec._24727.tech.schema.VerifySignature;
import iso.std.iso_iec._24727.tech.schema.VerifySignatureResponse;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import oasis.names.tc.dss._1_0.core.schema.Result;

import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.Environment;
import org.openecard.client.common.interfaces.EventCallback;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.Protocol;
import org.openecard.client.common.sal.ProtocolFactory;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.CardCommands;
import org.openecard.client.common.util.SelfCleaningMap;
import org.openecard.client.common.util.ValueGenerators;
import org.openecard.client.gui.UserConsent;



/**
 * 
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Simon Potzernheim <potzernheim@hs-coburg.de>
 */
public class TinySAL implements org.openecard.ws.SAL, EventCallback {

    private Environment env;
    private String sessionId;
    private static final Logger _logger = LogManager.getLogger(TinySAL.class.getName());
    private ConcurrentSkipListMap<String, ConnectionHandleType> cHandles;
    private boolean legacyMode;
    private ProtocolFactories protocolFactories = new ProtocolFactories();
    private UserConsent userConsent;

    private Map<String,Protocol> sessionProtocol;


    public TinySAL(Environment env) {
	this.env = env;
	sessionId = ValueGenerators.generateSessionID();
	legacyMode = false;
	cHandles = new ConcurrentSkipListMap<String, ConnectionHandleType>();
	try {
	    sessionProtocol = new SelfCleaningMap<String, Protocol>(ConcurrentSkipListMap.class, 5);
	} catch (Exception ex) {
	    // TODO: log exception
	    // fallback to non cleaning version
	    sessionProtocol = new ConcurrentSkipListMap<String, Protocol>();
	}
    }

    @Deprecated
    public TinySAL(Environment env, String sessionId) {
	this.env = env;
	this.sessionId = sessionId;
	legacyMode = true;
	cHandles = new ConcurrentSkipListMap<String, ConnectionHandleType>();
	try {
	    sessionProtocol = new SelfCleaningMap<String, Protocol>(ConcurrentSkipListMap.class, 5);
	} catch (Exception ex) {
	    // TODO: log exception
	    // fallback to non cleaning version
	    sessionProtocol = new ConcurrentSkipListMap<String, Protocol>();
	}
    }


    public void setGUI(UserConsent uc) {
	this.userConsent = uc;
    }

    public List<ConnectionHandleType> getConnectionHandles() {
	ArrayList<ConnectionHandleType> list = new ArrayList<ConnectionHandleType>(cHandles.values());
	return list;
    }

    public String getSessionId() {
	return sessionId;
    }


    public boolean addProtocol(String proto, ProtocolFactory factory) {	
	return protocolFactories.add(proto, factory); 		
    }


    private Protocol getProtocol(String session, String protoUri) throws UnknownProtocolException {
	Protocol proto = sessionProtocol.get(session);
	if (proto == null) {
	    if (protocolFactories.contains(protoUri)) {
		proto = protocolFactories.get(protoUri).createInstance(this, env.getIFD(), userConsent);
		sessionProtocol.put(session, proto);
	    } else {
		throw new UnknownProtocolException("The protocol URI '" + protoUri + "' is not registered in this SAL component.");
	    }
	}
	return proto;
    }

    public void removeFinishedProtocol(String session, Protocol proto) {
	if (proto.isFinished()) {
	    sessionProtocol.remove(session);
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
	String session = didAuthenticate.getConnectionHandle().getChannelHandle().getSessionIdentifier();

	try {
	    Protocol proto = getProtocol(session, protoUri);
	    if (proto.hasNextStep(FunctionType.DIDAuthenticate)) {
		DIDAuthenticateResponse resp = proto.didAuthenticate(didAuthenticate);
		removeFinishedProtocol(session, proto);
		return resp;
	    } else {
		Result res = WSHelper.makeResultUnknownError("No protocol step available for DIDAuthenticate in protocol " + proto.toString() + ".");
		DIDAuthenticateResponse resp = WSHelper.makeResponse(DIDAuthenticateResponse.class, res);
		return resp;
	    }
	} catch (UnknownProtocolException ex) {
	    // TODO: log exception
	    Result res = WSHelper.makeResult(ex);
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

    @Override
    public void signalEvent(EventType eventType, Object eventData) {
	if (eventData instanceof ConnectionHandleType) {
	    ConnectionHandleType cHandle = (ConnectionHandleType) eventData;

	    switch (eventType) {
	    case TERMINAL_REMOVED:
		cHandles.remove(cHandle.getIFDName());
		break;
	    default:
		cHandles.put(cHandle.getIFDName(), cHandle);
	    }
	}

    }

}
