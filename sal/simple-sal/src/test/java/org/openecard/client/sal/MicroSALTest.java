package org.openecard.client.sal;

import org.junit.Ignore;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.enums.EventType;
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
import iso.std.iso_iec._24727.tech.schema.Sign;
import iso.std.iso_iec._24727.tech.schema.SignResponse;
import iso.std.iso_iec._24727.tech.schema.Terminate;
import iso.std.iso_iec._24727.tech.schema.TerminateResponse;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificate;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificateResponse;
import iso.std.iso_iec._24727.tech.schema.VerifySignature;
import iso.std.iso_iec._24727.tech.schema.VerifySignatureResponse;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class MicroSALTest {
    
    public MicroSALTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getConnectionHandles method, of class MicroSAL.
     */
    @Test
    public void testGetConnectionHandles() {
        System.out.println("getConnectionHandles");
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        List<ConnectionHandleType> cHandles = instance.getConnectionHandles();
        assertTrue(cHandles.isEmpty());
        String[] readers = {"Reader 1", "Reader 2"};
        ConnectionHandleType cHandle1 = new ConnectionHandleType();
        cHandle1.setIFDName(readers[0]);
        ConnectionHandleType cHandle2 = new ConnectionHandleType();
        cHandle2.setIFDName(readers[1]);
        // add connection handles to microSAL
        instance.signalEvent(EventType.TERMINAL_ADDED, cHandle1);
        instance.signalEvent(EventType.TERMINAL_ADDED, cHandle2);
        cHandles = instance.getConnectionHandles();
        assertTrue(cHandles.size() == 2);
        for (int i = 0; i < cHandles.size(); i++) {
            assertEquals(cHandles.get(i).getIFDName(), readers[i]);
        }
        // remove one connection handle from microSAL
        instance.signalEvent(EventType.TERMINAL_REMOVED, cHandle1);
        cHandles = instance.getConnectionHandles();
        assertTrue(cHandles.size() == 1);
        assertEquals(cHandles.get(0).getIFDName(), readers[1]);
    }

    /**
     * Test of initialize method, of class MicroSAL.
     */
    @Ignore
    @Test
    public void testInitialize() {
        // TODO: make this test work
        System.out.println("initialize");
        Initialize parameters = new Initialize();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        InitializeResponse result = instance.initialize(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of terminate method, of class MicroSAL.
     */
    @Test
    public void testTerminate() {
        System.out.println("terminate");
        Terminate parameters = new Terminate();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        TerminateResponse result = instance.terminate(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationPath method, of class MicroSAL.
     */
    @Test
    public void testCardApplicationPath() {
        System.out.println("cardApplicationPath");
        CardApplicationPath parameters = new CardApplicationPath();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        CardApplicationPathResponse result = instance.cardApplicationPath(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationConnect method, of class MicroSAL.
     */
    @Test
    public void testCardApplicationConnect() {
        System.out.println("cardApplicationConnect");
        CardApplicationConnect parameters = new CardApplicationConnect();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        CardApplicationConnectResponse result = instance.cardApplicationConnect(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationDisconnect method, of class MicroSAL.
     */
    @Test
    public void testCardApplicationDisconnect() {
        System.out.println("cardApplicationDisconnect");
        CardApplicationDisconnect parameters = new CardApplicationDisconnect();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        CardApplicationDisconnectResponse result = instance.cardApplicationDisconnect(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationStartSession method, of class MicroSAL.
     */
    @Test
    public void testCardApplicationStartSession() {
        System.out.println("cardApplicationStartSession");
        CardApplicationStartSession parameters = new CardApplicationStartSession();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        CardApplicationStartSessionResponse result = instance.cardApplicationStartSession(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationEndSession method, of class MicroSAL.
     */
    @Test
    public void testCardApplicationEndSession() {
        System.out.println("cardApplicationEndSession");
        CardApplicationEndSession parameters = new CardApplicationEndSession();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        CardApplicationEndSessionResponse result = instance.cardApplicationEndSession(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationList method, of class MicroSAL.
     */
    @Test
    public void testCardApplicationList() {
        System.out.println("cardApplicationList");
        CardApplicationList parameters = new CardApplicationList();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        CardApplicationListResponse result = instance.cardApplicationList(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationCreate method, of class MicroSAL.
     */
    @Test
    public void testCardApplicationCreate() {
        System.out.println("cardApplicationCreate");
        CardApplicationCreate parameters = new CardApplicationCreate();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        CardApplicationCreateResponse result = instance.cardApplicationCreate(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationDelete method, of class MicroSAL.
     */
    @Test
    public void testCardApplicationDelete() {
        System.out.println("cardApplicationDelete");
        CardApplicationDelete parameters = new CardApplicationDelete();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        CardApplicationDeleteResponse result = instance.cardApplicationDelete(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationServiceList method, of class MicroSAL.
     */
    @Test
    public void testCardApplicationServiceList() {
        System.out.println("cardApplicationServiceList");
        CardApplicationServiceList parameters = new CardApplicationServiceList();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        CardApplicationServiceListResponse result = instance.cardApplicationServiceList(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationServiceCreate method, of class MicroSAL.
     */
    @Test
    public void testCardApplicationServiceCreate() {
        System.out.println("cardApplicationServiceCreate");
        CardApplicationServiceCreate parameters = new CardApplicationServiceCreate();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        CardApplicationServiceCreateResponse result = instance.cardApplicationServiceCreate(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationServiceLoad method, of class MicroSAL.
     */
    @Test
    public void testCardApplicationServiceLoad() {
        System.out.println("cardApplicationServiceLoad");
        CardApplicationServiceLoad parameters = new CardApplicationServiceLoad();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        CardApplicationServiceLoadResponse result = instance.cardApplicationServiceLoad(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationServiceDelete method, of class MicroSAL.
     */
    @Test
    public void testCardApplicationServiceDelete() {
        System.out.println("cardApplicationServiceDelete");
        CardApplicationServiceDelete parameters = new CardApplicationServiceDelete();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        CardApplicationServiceDeleteResponse result = instance.cardApplicationServiceDelete(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationServiceDescribe method, of class MicroSAL.
     */
    @Test
    public void testCardApplicationServiceDescribe() {
        System.out.println("cardApplicationServiceDescribe");
        CardApplicationServiceDescribe parameters = new CardApplicationServiceDescribe();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        CardApplicationServiceDescribeResponse result = instance.cardApplicationServiceDescribe(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of executeAction method, of class MicroSAL.
     */
    @Test
    public void testExecuteAction() {
        System.out.println("executeAction");
        ExecuteAction parameters = new ExecuteAction();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        ExecuteActionResponse result = instance.executeAction(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dataSetList method, of class MicroSAL.
     */
    @Test
    public void testDataSetList() {
        System.out.println("dataSetList");
        DataSetList parameters = new DataSetList();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        DataSetListResponse result = instance.dataSetList(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dataSetCreate method, of class MicroSAL.
     */
    @Test
    public void testDataSetCreate() {
        System.out.println("dataSetCreate");
        DataSetCreate parameters = new DataSetCreate();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        DataSetCreateResponse result = instance.dataSetCreate(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dataSetSelect method, of class MicroSAL.
     */
    @Test
    public void testDataSetSelect() {
        System.out.println("dataSetSelect");
        DataSetSelect parameters = new DataSetSelect();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        DataSetSelectResponse result = instance.dataSetSelect(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dataSetDelete method, of class MicroSAL.
     */
    @Test
    public void testDataSetDelete() {
        System.out.println("dataSetDelete");
        DataSetDelete parameters = new DataSetDelete();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        DataSetDeleteResponse result = instance.dataSetDelete(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dsiList method, of class MicroSAL.
     */
    @Test
    public void testDsiList() {
        System.out.println("dsiList");
        DSIList parameters = new DSIList();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        DSIListResponse result = instance.dsiList(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dsiCreate method, of class MicroSAL.
     */
    @Test
    public void testDsiCreate() {
        System.out.println("dsiCreate");
        DSICreate parameters = new DSICreate();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        DSICreateResponse result = instance.dsiCreate(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dsiDelete method, of class MicroSAL.
     */
    @Test
    public void testDsiDelete() {
        System.out.println("dsiDelete");
        DSIDelete parameters = new DSIDelete();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        DSIDeleteResponse result = instance.dsiDelete(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dsiWrite method, of class MicroSAL.
     */
    @Test
    public void testDsiWrite() {
        System.out.println("dsiWrite");
        DSIWrite parameters = new DSIWrite();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        DSIWriteResponse result = instance.dsiWrite(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dsiRead method, of class MicroSAL.
     */
    @Test
    public void testDsiRead() {
        System.out.println("dsiRead");
        DSIRead parameters = new DSIRead();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        DSIReadResponse result = instance.dsiRead(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of encipher method, of class MicroSAL.
     */
    @Test
    public void testEncipher() {
        System.out.println("encipher");
        Encipher parameters = new Encipher();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        EncipherResponse result = instance.encipher(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of decipher method, of class MicroSAL.
     */
    @Test
    public void testDecipher() {
        System.out.println("decipher");
        Decipher parameters = new Decipher();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        DecipherResponse result = instance.decipher(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of getRandom method, of class MicroSAL.
     */
    @Test
    public void testGetRandom() {
        System.out.println("getRandom");
        GetRandom parameters = new GetRandom();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        GetRandomResponse result = instance.getRandom(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of hash method, of class MicroSAL.
     */
    @Test
    public void testHash() {
        System.out.println("hash");
        Hash parameters = new Hash();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        HashResponse result = instance.hash(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of sign method, of class MicroSAL.
     */
    @Test
    public void testSign() {
        System.out.println("sign");
        Sign parameters = new Sign();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        SignResponse result = instance.sign(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of verifySignature method, of class MicroSAL.
     */
    @Test
    public void testVerifySignature() {
        System.out.println("verifySignature");
        VerifySignature parameters = new VerifySignature();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        VerifySignatureResponse result = instance.verifySignature(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of verifyCertificate method, of class MicroSAL.
     */
    @Test
    public void testVerifyCertificate() {
        System.out.println("verifyCertificate");
        VerifyCertificate parameters = new VerifyCertificate();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        VerifyCertificateResponse result = instance.verifyCertificate(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of didList method, of class MicroSAL.
     */
    @Test
    public void testDidList() {
        System.out.println("didList");
        DIDList parameters = new DIDList();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        DIDListResponse result = instance.didList(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of didCreate method, of class MicroSAL.
     */
    @Test
    public void testDidCreate() {
        System.out.println("didCreate");
        DIDCreate parameters = new DIDCreate();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        DIDCreateResponse result = instance.didCreate(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of didGet method, of class MicroSAL.
     */
    @Test
    public void testDidGet() {
        System.out.println("didGet");
        DIDGet parameters = new DIDGet();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        DIDGetResponse result = instance.didGet(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of didUpdate method, of class MicroSAL.
     */
    @Test
    public void testDidUpdate() {
        System.out.println("didUpdate");
        DIDUpdate parameters = new DIDUpdate();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        DIDUpdateResponse result = instance.didUpdate(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of didDelete method, of class MicroSAL.
     */
    @Test
    public void testDidDelete() {
        System.out.println("didDelete");
        DIDDelete parameters = new DIDDelete();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        DIDDeleteResponse result = instance.didDelete(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of didAuthenticate method, of class MicroSAL.
     */
    @Test
    public void testDidAuthenticate() {
        System.out.println("didAuthenticate");
        DIDAuthenticate parameters = new DIDAuthenticate();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        DIDAuthenticateResponse result = instance.didAuthenticate(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of aclList method, of class MicroSAL.
     */
    @Test
    public void testAclList() {
        System.out.println("aclList");
        ACLList parameters = new ACLList();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        ACLListResponse result = instance.aclList(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of aclModify method, of class MicroSAL.
     */
    @Test
    public void testAclModify() {
        System.out.println("aclModify");
        ACLModify parameters = new ACLModify();
        EnvStub env = new EnvStub();
        MicroSAL instance = new MicroSAL(env);
        ACLModifyResponse result = instance.aclModify(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of singalEvent method, of class MicroSAL.
     */
    @Test
    public void testSingalEvent() {
        System.out.println("singalEvent");
        testGetConnectionHandles();
    }
}
