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

import iso.std.iso_iec._24727.tech.schema.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.Environment;
import org.openecard.client.common.interfaces.EventCallback;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.Protocol;
import org.openecard.client.common.sal.ProtocolFactory;
import org.openecard.client.common.util.SelfCleaningMap;
import org.openecard.client.common.util.ValueGenerators;
import org.openecard.client.gui.UserConsent;


/**
 * 
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class TinySAL implements org.openecard.ws.SAL, EventCallback {

    private Environment env;
    private String sessionId;

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
    public CardApplicationPathResponse cardApplicationPath(CardApplicationPath parameters) {
	return WSHelper.makeResponse(CardApplicationPathResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public CardApplicationConnectResponse cardApplicationConnect(CardApplicationConnect parameters) {
	return WSHelper.makeResponse(CardApplicationConnectResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public CardApplicationDisconnectResponse cardApplicationDisconnect(CardApplicationDisconnect parameters) {
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
