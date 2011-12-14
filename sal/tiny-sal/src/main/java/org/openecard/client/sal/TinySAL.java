package org.openecard.client.sal;

import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.Environment;
import org.openecard.client.common.interfaces.EventCallback;
import org.openecard.client.common.util.ValueGenerators;
import org.openecard.client.common.WSHelper;
import iso.std.iso_iec._24727.tech.schema.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;


/**
 *
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class TinySAL implements org.openecard.ws.SAL, EventCallback {

    private Environment env;
    private String sessionId;
    
    private ConcurrentSkipListMap<String, ConnectionHandleType> cHandles;
    private boolean legacyMode;    

    public TinySAL(Environment env) {
        this.env = env;
        sessionId = ValueGenerators.generateSessionID();
        legacyMode = false;
        cHandles = new ConcurrentSkipListMap<String, ConnectionHandleType>();
    }

    public TinySAL(Environment env, String sessionId) {
        this.env = env;
        this.sessionId = sessionId;
        legacyMode = true;
        cHandles = new ConcurrentSkipListMap<String, ConnectionHandleType>();
    }
    
    public List<ConnectionHandleType> getConnectionHandles() {
        ArrayList<ConnectionHandleType> list = new ArrayList<ConnectionHandleType>(cHandles.values());
        return list;
    }
    
    public String getSessionId() {
        return sessionId;
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
    public DIDAuthenticateResponse didAuthenticate(DIDAuthenticate parameters) {
        return WSHelper.makeResponse(DIDAuthenticateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
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
        
            switch(eventType) {
                case TERMINAL_REMOVED:
                    cHandles.remove(cHandle.getIFDName());
                    break;
                default:
                    cHandles.put(cHandle.getIFDName(), cHandle);
            }
        }
        
    }
    
}
