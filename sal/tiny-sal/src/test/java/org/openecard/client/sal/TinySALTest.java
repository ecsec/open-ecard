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

import java.math.BigInteger;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.openecard.bouncycastle.util.encoders.Hex;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.ifd.scio.IFD;

import static org.junit.Assert.*;


/**
 *
 * @author Johannes.Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class TinySALTest {
    
    private ClientEnv env;
    private TinySAL instance;

    @Before
    public void setUp() {
        env = new ClientEnv();
        env.setIFD(new IFD());
        instance = new TinySAL(env, "");
    }


    /**
     * Test of getConnectionHandles method, of class TinySAL.
     */
    @Test
    public void testGetConnectionHandles() {
        System.out.println("getConnectionHandles");
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
     * Test of initialize method, of class TinySAL.
     */
    @Ignore
    @Test
    public void testInitialize() {
        // TODO: make this test work
        System.out.println("initialize");
        Initialize parameters = new Initialize();
        InitializeResponse result = instance.initialize(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of terminate method, of class TinySAL.
     */
    @Test
    public void testTerminate() {
        System.out.println("terminate");
        Terminate parameters = new Terminate();
        TerminateResponse result = instance.terminate(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationPath method, of class TinySAL.
     */
    @Ignore
    @Test
    public void testCardApplicationPath() {
        System.out.println("cardApplicationPath");
        CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(Hex.decode("A000000167455349474E")); //DF.ESign
	EstablishContextResponse ecr = this.env.getIFD().establishContext(new EstablishContext());

	cardApplicationPathType.setContextHandle(ecr.getContextHandle());

	ListIFDs listIFDs = new ListIFDs();
	listIFDs.setContextHandle(ecr.getContextHandle());
	ListIFDsResponse listIFDsResponse = this.env.getIFD().listIFDs(listIFDs);
	for(String s : listIFDsResponse.getIFDName()) {
	    System.out.println(s);
	}
	cardApplicationPathType.setIFDName("REINER SCT cyberJack RFID standard USB 52");
	cardApplicationPathType.setSlotIndex(new BigInteger("0"));

        cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
        CardApplicationPathResponse cardApplicationPathResponse = this.instance.cardApplicationPath(cardApplicationPath);
    }

    /**
     * Test of cardApplicationConnect method, of class TinySAL.
     */
    @Test
    public void testCardApplicationConnect() {
        System.out.println("cardApplicationConnect");
        CardApplicationConnect parameters = new CardApplicationConnect();
        CardApplicationConnectResponse result = instance.cardApplicationConnect(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationDisconnect method, of class TinySAL.
     */
    @Ignore
    @Test
    public void testCardApplicationDisconnect() {
        System.out.println("cardApplicationDisconnect");
	CardApplicationDisconnect parameters = new CardApplicationDisconnect();
	CardApplicationDisconnectResponse result = instance.cardApplicationDisconnect(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationStartSession method, of class TinySAL.
     */
    @Test
    public void testCardApplicationStartSession() {
        System.out.println("cardApplicationStartSession");
        CardApplicationStartSession parameters = new CardApplicationStartSession();
        CardApplicationStartSessionResponse result = instance.cardApplicationStartSession(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationEndSession method, of class TinySAL.
     */
    @Test
    public void testCardApplicationEndSession() {
        System.out.println("cardApplicationEndSession");
        CardApplicationEndSession parameters = new CardApplicationEndSession();
        CardApplicationEndSessionResponse result = instance.cardApplicationEndSession(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationList method, of class TinySAL.
     */
    @Test
    public void testCardApplicationList() {
        System.out.println("cardApplicationList");
        CardApplicationList parameters = new CardApplicationList();
        CardApplicationListResponse result = instance.cardApplicationList(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationCreate method, of class TinySAL.
     */
    @Test
    public void testCardApplicationCreate() {
        System.out.println("cardApplicationCreate");
        CardApplicationCreate parameters = new CardApplicationCreate();
        CardApplicationCreateResponse result = instance.cardApplicationCreate(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationDelete method, of class TinySAL.
     */
    @Test
    public void testCardApplicationDelete() {
        System.out.println("cardApplicationDelete");
        CardApplicationDelete parameters = new CardApplicationDelete();
        CardApplicationDeleteResponse result = instance.cardApplicationDelete(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationServiceList method, of class TinySAL.
     */
    @Test
    public void testCardApplicationServiceList() {
        System.out.println("cardApplicationServiceList");
        CardApplicationServiceList parameters = new CardApplicationServiceList();
        CardApplicationServiceListResponse result = instance.cardApplicationServiceList(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationServiceCreate method, of class TinySAL.
     */
    @Test
    public void testCardApplicationServiceCreate() {
        System.out.println("cardApplicationServiceCreate");
        CardApplicationServiceCreate parameters = new CardApplicationServiceCreate();
        CardApplicationServiceCreateResponse result = instance.cardApplicationServiceCreate(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationServiceLoad method, of class TinySAL.
     */
    @Test
    public void testCardApplicationServiceLoad() {
        System.out.println("cardApplicationServiceLoad");
        CardApplicationServiceLoad parameters = new CardApplicationServiceLoad();
        CardApplicationServiceLoadResponse result = instance.cardApplicationServiceLoad(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationServiceDelete method, of class TinySAL.
     */
    @Test
    public void testCardApplicationServiceDelete() {
        System.out.println("cardApplicationServiceDelete");
        CardApplicationServiceDelete parameters = new CardApplicationServiceDelete();
        CardApplicationServiceDeleteResponse result = instance.cardApplicationServiceDelete(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationServiceDescribe method, of class TinySAL.
     */
    @Test
    public void testCardApplicationServiceDescribe() {
        System.out.println("cardApplicationServiceDescribe");
        CardApplicationServiceDescribe parameters = new CardApplicationServiceDescribe();
        CardApplicationServiceDescribeResponse result = instance.cardApplicationServiceDescribe(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of executeAction method, of class TinySAL.
     */
    @Test
    public void testExecuteAction() {
        System.out.println("executeAction");
        ExecuteAction parameters = new ExecuteAction();
        ExecuteActionResponse result = instance.executeAction(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dataSetList method, of class TinySAL.
     */
    @Test
    public void testDataSetList() {
        System.out.println("dataSetList");
        DataSetList parameters = new DataSetList();
        DataSetListResponse result = instance.dataSetList(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dataSetCreate method, of class TinySAL.
     */
    @Test
    public void testDataSetCreate() {
        System.out.println("dataSetCreate");
        DataSetCreate parameters = new DataSetCreate();
        DataSetCreateResponse result = instance.dataSetCreate(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dataSetSelect method, of class TinySAL.
     */
    @Test
    public void testDataSetSelect() {
        System.out.println("dataSetSelect");
        DataSetSelect parameters = new DataSetSelect();
        DataSetSelectResponse result = instance.dataSetSelect(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dataSetDelete method, of class TinySAL.
     */
    @Test
    public void testDataSetDelete() {
        System.out.println("dataSetDelete");
        DataSetDelete parameters = new DataSetDelete();
        DataSetDeleteResponse result = instance.dataSetDelete(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dsiList method, of class TinySAL.
     */
    @Test
    public void testDsiList() {
        System.out.println("dsiList");
        DSIList parameters = new DSIList();
        DSIListResponse result = instance.dsiList(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dsiCreate method, of class TinySAL.
     */
    @Test
    public void testDsiCreate() {
        System.out.println("dsiCreate");
        DSICreate parameters = new DSICreate();
        DSICreateResponse result = instance.dsiCreate(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dsiDelete method, of class TinySAL.
     */
    @Test
    public void testDsiDelete() {
        System.out.println("dsiDelete");
        DSIDelete parameters = new DSIDelete();
        DSIDeleteResponse result = instance.dsiDelete(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dsiWrite method, of class TinySAL.
     */
    @Test
    public void testDsiWrite() {
        System.out.println("dsiWrite");
        DSIWrite parameters = new DSIWrite();
        DSIWriteResponse result = instance.dsiWrite(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dsiRead method, of class TinySAL.
     */
    @Test
    public void testDsiRead() {
        System.out.println("dsiRead");
        DSIRead parameters = new DSIRead();
        DSIReadResponse result = instance.dsiRead(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of encipher method, of class TinySAL.
     */
    @Test
    public void testEncipher() {
        System.out.println("encipher");
        Encipher parameters = new Encipher();
        EncipherResponse result = instance.encipher(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of decipher method, of class TinySAL.
     */
    @Test
    public void testDecipher() {
        System.out.println("decipher");
        Decipher parameters = new Decipher();
        DecipherResponse result = instance.decipher(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of getRandom method, of class TinySAL.
     */
    @Test
    public void testGetRandom() {
        System.out.println("getRandom");
        GetRandom parameters = new GetRandom();
        GetRandomResponse result = instance.getRandom(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of hash method, of class TinySAL.
     */
    @Test
    public void testHash() {
        System.out.println("hash");
        Hash parameters = new Hash();
        HashResponse result = instance.hash(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of sign method, of class TinySAL.
     */
    @Test
    public void testSign() {
        System.out.println("sign");
        Sign parameters = new Sign();
        SignResponse result = instance.sign(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of verifySignature method, of class TinySAL.
     */
    @Test
    public void testVerifySignature() {
        System.out.println("verifySignature");
        VerifySignature parameters = new VerifySignature();
        VerifySignatureResponse result = instance.verifySignature(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of verifyCertificate method, of class TinySAL.
     */
    @Test
    public void testVerifyCertificate() {
        System.out.println("verifyCertificate");
        VerifyCertificate parameters = new VerifyCertificate();
        VerifyCertificateResponse result = instance.verifyCertificate(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of didList method, of class TinySAL.
     */
    @Test
    public void testDidList() {
        System.out.println("didList");
        DIDList parameters = new DIDList();
        DIDListResponse result = instance.didList(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of didCreate method, of class TinySAL.
     */
    @Test
    public void testDidCreate() {
        System.out.println("didCreate");
        DIDCreate parameters = new DIDCreate();
        DIDCreateResponse result = instance.didCreate(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of didGet method, of class TinySAL.
     */
    @Test
    public void testDidGet() {
        System.out.println("didGet");
        DIDGet parameters = new DIDGet();
        DIDGetResponse result = instance.didGet(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of didUpdate method, of class TinySAL.
     */
    @Test
    public void testDidUpdate() {
        System.out.println("didUpdate");
        DIDUpdate parameters = new DIDUpdate();
        DIDUpdateResponse result = instance.didUpdate(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of didDelete method, of class TinySAL.
     */
    @Test
    public void testDidDelete() {
        System.out.println("didDelete");
        DIDDelete parameters = new DIDDelete();
        DIDDeleteResponse result = instance.didDelete(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of didAuthenticate method, of class TinySAL.
     */
    @Ignore
    @Test
    public void testDidAuthenticate() {
        System.out.println("didAuthenticate");
        DIDAuthenticate parameters = new DIDAuthenticate();
        DIDAuthenticateResponse result = instance.didAuthenticate(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of aclList method, of class TinySAL.
     */
    @Test
    public void testAclList() {
        System.out.println("aclList");
        ACLList parameters = new ACLList();
        ACLListResponse result = instance.aclList(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of aclModify method, of class TinySAL.
     */
    @Test
    public void testAclModify() {
        System.out.println("aclModify");
        ACLModify parameters = new ACLModify();
        ACLModifyResponse result = instance.aclModify(parameters);
        assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of singalEvent method, of class TinySAL.
     */
    @Test
    public void testSingalEvent() {
        System.out.println("singalEvent");
        // same as getconnectionhandles, so call this one instead
        testGetConnectionHandles();
    }

}
