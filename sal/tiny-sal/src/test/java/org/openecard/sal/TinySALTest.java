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

import iso.std.iso_iec._24727.tech.schema.AccessControlListType;
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
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
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
import iso.std.iso_iec._24727.tech.schema.CardApplicationStartSession;
import iso.std.iso_iec._24727.tech.schema.CardApplicationStartSessionResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
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
import iso.std.iso_iec._24727.tech.schema.DIDQualifierType;
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
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import iso.std.iso_iec._24727.tech.schema.Sign;
import iso.std.iso_iec._24727.tech.schema.SignResponse;
import iso.std.iso_iec._24727.tech.schema.TargetNameType;
import iso.std.iso_iec._24727.tech.schema.Terminate;
import iso.std.iso_iec._24727.tech.schema.TerminateResponse;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificate;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificateResponse;
import iso.std.iso_iec._24727.tech.schema.VerifySignature;
import iso.std.iso_iec._24727.tech.schema.VerifySignatureResponse;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.bouncycastle.util.encoders.Hex;
import org.openecard.common.ClientEnv;
import org.openecard.common.ECardConstants;
import org.openecard.common.enums.EventType;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.sal.state.CardStateEntry;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.common.sal.state.SALStateCallback;
import org.openecard.common.util.ByteUtils;
import org.openecard.ifd.scio.IFD;
import org.openecard.recognition.CardRecognition;
import org.openecard.transport.dispatcher.MessageDispatcher;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class TinySALTest {

    @BeforeClass
    public static void disable() {
	throw new SkipException("Test completely disabled.");
    }

    private static ClientEnv env;
    private static TinySAL instance;
    private static CardStateMap states;
    private static byte[] contextHandle = null;
    byte[] appIdentifier_ESIGN = Hex.decode("A000000167455349474E");
    byte[] appIdentifier_ROOT = Hex.decode("D2760001448000");

    @BeforeClass
    public static void setUp() throws Exception {
	env = new ClientEnv();
	Dispatcher dispatcher = new MessageDispatcher(env);
	env.setDispatcher(dispatcher);
	IFD ifd = new IFD();
	env.setIFD(ifd);
	states = new CardStateMap();

	EstablishContextResponse ecr = env.getIFD().establishContext(new EstablishContext());
	CardRecognition cr = new CardRecognition(ifd, ecr.getContextHandle());
	ListIFDs listIFDs = new ListIFDs();
	contextHandle = ecr.getContextHandle();
	listIFDs.setContextHandle(ecr.getContextHandle());
	ListIFDsResponse listIFDsResponse = ifd.listIFDs(listIFDs);
	RecognitionInfo recognitionInfo = cr.recognizeCard(listIFDsResponse.getIFDName().get(0), new BigInteger("0"));
	SALStateCallback salCallback = new SALStateCallback(cr, states);

	ConnectionHandleType connectionHandleType = new ConnectionHandleType();
	connectionHandleType.setContextHandle(ecr.getContextHandle());
	connectionHandleType.setRecognitionInfo(recognitionInfo);
	connectionHandleType.setIFDName(listIFDsResponse.getIFDName().get(0));
	connectionHandleType.setSlotIndex(new BigInteger("0"));

	salCallback.signalEvent(EventType.CARD_RECOGNIZED, connectionHandleType);
	instance = new TinySAL(env, states);
	env.setSAL(instance);
    }

    /**
     * Test of getConnectionHandles method, of class TinySAL.
     */
    @Test(enabled=false)
    public void testGetConnectionHandles() {
	System.out.println("getConnectionHandles");
	List<ConnectionHandleType> cHandles = instance.getConnectionHandles();
	assertTrue(cHandles.isEmpty());
	String[] readers = { "Reader 1", "Reader 2" };
	ConnectionHandleType cHandle1 = new ConnectionHandleType();
	cHandle1.setIFDName(readers[0]);
	ConnectionHandleType cHandle2 = new ConnectionHandleType();
	cHandle2.setIFDName(readers[1]);
	// add connection handles to microSAL
	CardStateEntry entry1 = new CardStateEntry(cHandle1, null); // TODO: null works as long as
								    // there is no cif support
	states.addEntry(entry1);
	CardStateEntry entry2 = new CardStateEntry(cHandle2, null);
	states.addEntry(entry2);
	cHandles = instance.getConnectionHandles();
	assertTrue(cHandles.size() == 2);
	for (int i = 0; i < cHandles.size(); i++) {
	    assertEquals(readers[i], cHandles.get(i).getIFDName());
	}
	// remove one connection handle from microSAL
	states.removeEntry(cHandle1);
	cHandles = instance.getConnectionHandles();
	assertTrue(cHandles.size() == 1);
	assertEquals(cHandles.get(0).getIFDName(), readers[1]);
    }

    /**
     * Test of initialize method, of class TinySAL.
     */
    @Test
    public void testInitialize() {
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
    @Test
    public void testCardApplicationPath() {
	System.out.println("cardApplicationPath");
	// test normal case
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(this.appIdentifier_ESIGN);
	cardApplicationPathType.setContextHandle(contextHandle);
	cardApplicationPathType.setSlotIndex(new BigInteger("0"));
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);
	assertTrue(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().size()>0);
	assertEquals(cardApplicationPathResponse.getResult().getResultMajor(), ECardConstants.Major.OK);

	// test return of alpha card application
	cardApplicationPath = new CardApplicationPath();
	cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);
	assertTrue(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().size()>0);
	assertNotNull(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0).getCardApplication());
	assertEquals(cardApplicationPathResponse.getResult().getResultMajor(), ECardConstants.Major.OK);

	// test non existent card application identifier
	cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(Hex.decode("C0CA"));
	cardApplicationPathType.setContextHandle(contextHandle);
	cardApplicationPathType.setSlotIndex(new BigInteger("0"));
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);
	assertEquals(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().size(), 0);
	assertEquals(cardApplicationPathResponse.getResult().getResultMajor(), ECardConstants.Major.OK);

	// test nullpointer
	cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPath.setCardAppPathRequest(null);
	cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);
	assertEquals(cardApplicationPathResponse.getResult().getResultMajor(), ECardConstants.Major.ERROR);
	assertEquals(cardApplicationPathResponse.getResult().getResultMinor(), ECardConstants.Minor.App.INCORRECT_PARM);
    }

    /**
     * Test of cardApplicationConnect method, of class TinySAL.
     */
    @Test
    public void testCardApplicationConnect() {
	System.out.println("cardApplicationConnect");
	// test normal case
	// get esign path
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(appIdentifier_ESIGN);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);
	// connect to esign
	CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0));
	CardApplicationConnectResponse result = instance.cardApplicationConnect(cardApplicationConnect);
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());
	assertEquals(appIdentifier_ESIGN, result.getConnectionHandle().getCardApplication());

	// test non existent card application path
	cardApplicationConnect = new CardApplicationConnect();
	CardApplicationPathType wrongCardApplicationPath = cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0);
	wrongCardApplicationPath.setCardApplication(new byte[] { 0x12, 0x23, 0x34 });
	cardApplicationConnect.setCardApplicationPath(wrongCardApplicationPath);
	result = instance.cardApplicationConnect(cardApplicationConnect);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, result.getResult().getResultMinor());

	// test nullpointer
	cardApplicationConnect = new CardApplicationConnect();
	cardApplicationConnect.setCardApplicationPath(null);
	result = instance.cardApplicationConnect(cardApplicationConnect);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, result.getResult().getResultMinor());
    }


    /**
     * Test of cardApplicationDisconnect method, of class TinySAL.
     */
    @Test
    public void testCardApplicationDisconnect() {
	System.out.println("cardApplicationDisconnect");
	// test normal case
	// get esign path
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(appIdentifier_ESIGN);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);
	// connect to esign
	CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0));
	CardApplicationConnectResponse result = instance.cardApplicationConnect(cardApplicationConnect);
	assertEquals(appIdentifier_ESIGN, result.getConnectionHandle().getCardApplication());
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());
	// disconnect
	CardApplicationDisconnect cardApplicationDisconnect = new CardApplicationDisconnect();
	cardApplicationDisconnect.setConnectionHandle(result.getConnectionHandle());
	CardApplicationDisconnectResponse cardApplicationDisconnectResponse = instance.cardApplicationDisconnect(cardApplicationDisconnect);
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());

	// test invalid connectionhandle
	// connect to esign
	cardApplicationConnect = new CardApplicationConnect();
	cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0));
	result = instance.cardApplicationConnect(cardApplicationConnect);
	assertEquals(appIdentifier_ESIGN, result.getConnectionHandle().getCardApplication());
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());
	// disconnect
	cardApplicationDisconnect = new CardApplicationDisconnect();
	cardApplicationDisconnect.setConnectionHandle(result.getConnectionHandle());
	cardApplicationDisconnect.getConnectionHandle().setSlotHandle(new byte[]{0x0, 0x0, 0x0});
	cardApplicationDisconnectResponse = instance.cardApplicationDisconnect(cardApplicationDisconnect);
	assertEquals(ECardConstants.Major.ERROR, cardApplicationDisconnectResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.SAL.UNKNOWN_HANDLE, cardApplicationDisconnectResponse.getResult().getResultMinor());

	// test nullpointer
	// connect to esign
	cardApplicationConnect = new CardApplicationConnect();
	cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0));
	result = instance.cardApplicationConnect(cardApplicationConnect);
	assertEquals(appIdentifier_ESIGN, result.getConnectionHandle().getCardApplication());
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());
	// disconnect
	cardApplicationDisconnect = new CardApplicationDisconnect();
	cardApplicationDisconnect.setConnectionHandle(null);
	cardApplicationDisconnectResponse = instance.cardApplicationDisconnect(cardApplicationDisconnect);
	assertEquals(ECardConstants.Major.ERROR, cardApplicationDisconnectResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, cardApplicationDisconnectResponse.getResult().getResultMinor());
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
	// get path to root
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(appIdentifier_ROOT);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);

	// connect to root
	CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult()
		.get(0));
	CardApplicationConnectResponse result = instance.cardApplicationConnect(cardApplicationConnect);
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());

	CardApplicationList cardApplicationList = new CardApplicationList();
	cardApplicationList.setConnectionHandle(result.getConnectionHandle());
	CardApplicationListResponse cardApplicationListResponse = instance.cardApplicationList(cardApplicationList);
	System.out.println(cardApplicationListResponse.getResult().getResultMinor());
	assertEquals(ECardConstants.Major.OK, cardApplicationListResponse.getResult().getResultMajor());
	assertTrue(cardApplicationListResponse.getCardApplicationNameList().getCardApplicationName().size() > 0);


	// test non existent connectionhandle
	cardApplicationList = new CardApplicationList();
	cardApplicationList.setConnectionHandle(result.getConnectionHandle());
	cardApplicationList.getConnectionHandle().setIFDName("invalid");
	cardApplicationListResponse = instance.cardApplicationList(cardApplicationList);
	assertEquals(ECardConstants.Major.ERROR, cardApplicationListResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.SAL.UNKNOWN_HANDLE, cardApplicationListResponse.getResult().getResultMinor());

	// test nullpointer
	cardApplicationList = new CardApplicationList();
	cardApplicationList.setConnectionHandle(null);
	cardApplicationListResponse = instance.cardApplicationList(cardApplicationList);
	assertEquals(ECardConstants.Major.ERROR, cardApplicationListResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, cardApplicationListResponse.getResult().getResultMinor());
    }

    /**
     * Test of cardApplicationCreate method, of class TinySAL.
     */
    @Test(enabled=false)    
    public void testCardApplicationCreate() {
	System.out.println("cardApplicationCreate");
	
	List<ConnectionHandleType> cHandles = instance.getConnectionHandles();
	byte[] appName = {(byte)0x74, (byte)0x65, (byte)0x73, (byte)0x74};
	
	CardApplicationCreate parameters = new CardApplicationCreate();
	parameters.setConnectionHandle(cHandles.get(0));
	parameters.setCardApplicationName(appName);

        AccessControlListType cardApplicationACL = new AccessControlListType();
	parameters.setCardApplicationACL(cardApplicationACL);
	
	CardApplicationCreateResponse result = instance.cardApplicationCreate(parameters);
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());

	// get path to esign
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(appIdentifier_ESIGN);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);

	// connect to esign
	CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0));
	CardApplicationConnectResponse resultConnect = instance.cardApplicationConnect(cardApplicationConnect);
	assertEquals(ECardConstants.Major.OK, resultConnect.getResult().getResultMajor());

	CardApplicationList cardApplicationList = new CardApplicationList();
	cardApplicationList.setConnectionHandle(cHandles.get(0));
	CardApplicationListResponse cardApplicationListResponse = instance.cardApplicationList(cardApplicationList);

        Iterator<byte[]> it = cardApplicationListResponse.getCardApplicationNameList().getCardApplicationName().iterator();
        boolean appFound = false;

        try {
            while (it.hasNext()) {
                byte[] val = it.next();

                if (Arrays.equals(val, appName))
                    appFound = true;
            }

            assertTrue(appFound);
    
	} catch (Exception e) {
	    assertTrue(appFound);
	    System.out.println(e);
        } 
    }

    /**
     * Test of cardApplicationDelete method, of class TinySAL.
     */
    @Test(enabled=false)    
    public void testCardApplicationDelete() {
	System.out.println("cardApplicationDelete");

	List<ConnectionHandleType> cHandles = instance.getConnectionHandles();
	byte[] appName = {(byte)0x74, (byte)0x65, (byte)0x73, (byte)0x74};
	
	CardApplicationDelete parameters = new CardApplicationDelete();
	parameters.setConnectionHandle(cHandles.get(0));
	parameters.setCardApplicationName(appName);
    	
	CardApplicationDeleteResponse result = instance.cardApplicationDelete(parameters);
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());

	// get path to esign
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(appIdentifier_ESIGN);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);

	// connect to esign
	CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0));
	CardApplicationConnectResponse resultConnect = instance.cardApplicationConnect(cardApplicationConnect);
	assertEquals(ECardConstants.Major.OK, resultConnect.getResult().getResultMajor());

	CardApplicationList cardApplicationList = new CardApplicationList();
	cardApplicationList.setConnectionHandle(cHandles.get(0));
	CardApplicationListResponse cardApplicationListResponse = instance.cardApplicationList(cardApplicationList);

        Iterator<byte[]> it = cardApplicationListResponse.getCardApplicationNameList().getCardApplicationName().iterator();
        boolean appFound = false;

        try {
            while (it.hasNext()) {
                byte[] val = it.next();

                if (Arrays.equals(val, appName))
                    appFound = true;
            }

            assertTrue(!appFound);
    
	} catch (Exception e) {
	    assertTrue(!appFound);
	    System.out.println(e);
        } 
    }

    /**
     * Test of cardApplicationServiceList method, of class TinySAL.
     */
    @Test(enabled=false)    
    public void testCardApplicationServiceList() {
	System.out.println("cardApplicationServiceList");
	CardApplicationServiceList parameters = new CardApplicationServiceList();
	
        // get path to esign
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(appIdentifier_ESIGN);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);
	
	assertTrue(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().size() > 0);
	assertEquals(cardApplicationPathResponse.getResult().getResultMajor(), ECardConstants.Major.OK);

	// connect to esign
	CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0));
	
	CardApplicationConnectResponse result = instance.cardApplicationConnect(cardApplicationConnect);
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());
	assertEquals(appIdentifier_ESIGN, result.getConnectionHandle().getCardApplication());

	parameters.setConnectionHandle(result.getConnectionHandle());
	
	CardApplicationServiceListResponse resultServiceList = instance.cardApplicationServiceList(parameters);
        CardApplicationServiceNameList cardApplicationServiceNameList = resultServiceList.getCardApplicationServiceNameList();

	assertEquals(ECardConstants.Major.OK, resultServiceList.getResult().getResultMajor());
	assertTrue(cardApplicationServiceNameList.getCardApplicationServiceName().size() == 0); 
    }
    
    /**
     * Test of cardApplicationServiceCreate method, of class TinySAL.
     */
    @Test(enabled=false)    
    public void testCardApplicationServiceCreate() {
	System.out.println("cardApplicationServiceCreate");	
	CardApplicationServiceCreate parameters = new CardApplicationServiceCreate();
	
        // get path to esign
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(appIdentifier_ESIGN);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);
	
	assertTrue(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().size() > 0);
	assertEquals(cardApplicationPathResponse.getResult().getResultMajor(), ECardConstants.Major.OK);

	// connect to esign
	CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0));
	
	CardApplicationConnectResponse result = instance.cardApplicationConnect(cardApplicationConnect);
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());
	assertEquals(appIdentifier_ESIGN, result.getConnectionHandle().getCardApplication());

	parameters.setConnectionHandle(result.getConnectionHandle());
	parameters.setCardApplicationServiceName("testService");
	
	CardApplicationServiceCreateResponse resultServiceCreate = instance.cardApplicationServiceCreate(parameters);
	assertEquals(ECardConstants.Major.OK, resultServiceCreate.getResult().getResultMajor());

	CardApplicationServiceList parametersServiceList = new CardApplicationServiceList();
	parametersServiceList.setConnectionHandle(result.getConnectionHandle());
	
	CardApplicationServiceListResponse resultServiceList = instance.cardApplicationServiceList(parametersServiceList);
        CardApplicationServiceNameList cardApplicationServiceNameList = resultServiceList.getCardApplicationServiceNameList();

	assertEquals(ECardConstants.Major.OK, resultServiceList.getResult().getResultMajor());
	assertTrue(cardApplicationServiceNameList.getCardApplicationServiceName().size() > 0); 
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

	// get path to esign
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(appIdentifier_ESIGN);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);

	// connect to esign
	CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult()
		.get(0));
	CardApplicationConnectResponse result = instance.cardApplicationConnect(cardApplicationConnect);
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());

	// list datasets of esign
	DataSetList dataSetList = new DataSetList();
	dataSetList.setConnectionHandle(result.getConnectionHandle());
	DataSetListResponse dataSetListResponse = instance.dataSetList(dataSetList);
	System.out.println(ByteUtils.toHexString(result.getConnectionHandle().getSlotHandle()));
	Assert.assertTrue(dataSetListResponse.getDataSetNameList().getDataSetName().size() > 0);
	assertEquals(ECardConstants.Major.OK, dataSetListResponse.getResult().getResultMajor());

	// test invalid connectionhandle
	dataSetList = new DataSetList();
	ConnectionHandleType wrongConnectionHandle = result.getConnectionHandle();
	wrongConnectionHandle.setCardApplication(new byte[] { 0x0, 0x0, 0x0 });
	dataSetList.setConnectionHandle(wrongConnectionHandle);
	dataSetListResponse = instance.dataSetList(dataSetList);
	assertEquals(ECardConstants.Major.ERROR, dataSetListResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.SAL.UNKNOWN_HANDLE, dataSetListResponse.getResult().getResultMinor());

	// test null connectionhandle
	dataSetList = new DataSetList();
	dataSetList.setConnectionHandle(null);
	dataSetListResponse = instance.dataSetList(dataSetList);
	assertEquals(ECardConstants.Major.ERROR, dataSetListResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, dataSetListResponse.getResult().getResultMinor());

	// TODO test for unsatisfied security condition
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
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(appIdentifier_ESIGN);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);

	CardApplicationConnect parameters = new CardApplicationConnect();
	parameters.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0));
	CardApplicationConnectResponse result = instance.cardApplicationConnect(parameters);
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());

	// test good case
	DataSetSelect dataSetSelect = new DataSetSelect();
	dataSetSelect.setConnectionHandle(result.getConnectionHandle());
	dataSetSelect.setDataSetName("EF.C.CH.AUT");
	DataSetSelectResponse dataSetSelectResponse = instance.dataSetSelect(dataSetSelect);
	assertEquals(ECardConstants.Major.OK, dataSetSelectResponse.getResult().getResultMajor());

	// test connectionhanle == null
	dataSetSelect = new DataSetSelect();
	dataSetSelect.setConnectionHandle(null);
	dataSetSelect.setDataSetName("EF.C.CH.AUT");
	dataSetSelectResponse = instance.dataSetSelect(dataSetSelect);
	assertEquals(ECardConstants.Major.ERROR, dataSetSelectResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, dataSetSelectResponse.getResult().getResultMinor());
	// test datasetname == null
	dataSetSelect = new DataSetSelect();
	dataSetSelect.setConnectionHandle(result.getConnectionHandle());
	dataSetSelect.setDataSetName(null);
	dataSetSelectResponse = instance.dataSetSelect(dataSetSelect);
	assertEquals(ECardConstants.Major.ERROR, dataSetSelectResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, dataSetSelectResponse.getResult().getResultMinor());
	// test datasetname not found
	dataSetSelect = new DataSetSelect();
	dataSetSelect.setConnectionHandle(result.getConnectionHandle());
	dataSetSelect.setDataSetName("INVALID");
	dataSetSelectResponse = instance.dataSetSelect(dataSetSelect);
	assertEquals(ECardConstants.Major.ERROR, dataSetSelectResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.SAL.NAMED_ENTITY_NOT_FOUND, dataSetSelectResponse.getResult().getResultMinor());
	// test invalid connectionhandle
	dataSetSelect = new DataSetSelect();
	ConnectionHandleType invalidConnectionHandle = result.getConnectionHandle();
	invalidConnectionHandle.setIFDName(("invalid"));
	dataSetSelect.setConnectionHandle(invalidConnectionHandle);
	dataSetSelect.setDataSetName("EF.C.CH.AUT");
	dataSetSelectResponse = instance.dataSetSelect(dataSetSelect);
	assertEquals(ECardConstants.Major.ERROR, dataSetSelectResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.SAL.UNKNOWN_HANDLE, dataSetSelectResponse.getResult().getResultMinor());
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
	// test normal case
	// get esign path
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(appIdentifier_ESIGN);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);
	// connect to esign
	CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0));
	CardApplicationConnectResponse result = instance.cardApplicationConnect(cardApplicationConnect);
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());
	assertEquals(appIdentifier_ESIGN, result.getConnectionHandle().getCardApplication());


	// read EF.C.CH.AUT
	DSIRead dsiRead = new DSIRead();
	dsiRead.setConnectionHandle(result.getConnectionHandle());
	dsiRead.setDSIName("EF.C.CH.AUT");
	DSIReadResponse dsiReadResponse = instance.dsiRead(dsiRead);
	System.out.println(dsiReadResponse.getResult().getResultMinor());
	assertEquals(ECardConstants.Major.OK, dsiReadResponse.getResult().getResultMajor());
	System.out.println(dsiReadResponse.getResult().getResultMinor());
	assertTrue(dsiReadResponse.getDSIContent().length>0);

	// test connectionhandle == null
	dsiRead = new DSIRead();
	dsiRead.setConnectionHandle(null);
	dsiRead.setDSIName("EF.C.CH.AUT");
	dsiReadResponse = instance.dsiRead(dsiRead);
	assertEquals(ECardConstants.Major.ERROR, dsiReadResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, dsiReadResponse.getResult().getResultMinor());

	// test dsiName == null
	dsiRead = new DSIRead();
	dsiRead.setConnectionHandle(result.getConnectionHandle());
	dsiRead.setDSIName(null);
	dsiReadResponse = instance.dsiRead(dsiRead);
	assertEquals(ECardConstants.Major.ERROR, dsiReadResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, dsiReadResponse.getResult().getResultMinor());

	// test dsiName invalid
	dsiRead = new DSIRead();
	dsiRead.setConnectionHandle(result.getConnectionHandle());
	dsiRead.setDSIName("INVALID");
	dsiReadResponse = instance.dsiRead(dsiRead);
	assertEquals(ECardConstants.Major.ERROR, dsiReadResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.SAL.NAMED_ENTITY_NOT_FOUND, dsiReadResponse.getResult().getResultMinor());

	// test security condition not satisfied
	dsiRead = new DSIRead();
	dsiRead.setConnectionHandle(result.getConnectionHandle());
	dsiRead.setDSIName("EF.C.CH.AUTN");
	dsiReadResponse = instance.dsiRead(dsiRead);
	assertEquals(ECardConstants.Major.ERROR, dsiReadResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.SAL.SECURITY_CONDITINON_NOT_SATISFIED, dsiReadResponse.getResult().getResultMinor());

	// test invalid connectionhandle
	dsiRead = new DSIRead();
	dsiRead.setConnectionHandle(result.getConnectionHandle());
	dsiRead.getConnectionHandle().setIFDName("invalid");
	dsiRead.setDSIName(null);
	dsiReadResponse = instance.dsiRead(dsiRead);
	assertEquals(ECardConstants.Major.ERROR, dsiReadResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.SAL.UNKNOWN_HANDLE, dsiReadResponse.getResult().getResultMinor());
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

	// get path to esign
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(appIdentifier_ESIGN);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);

	// connect to esign
	CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult()
		.get(0));
	CardApplicationConnectResponse result = instance.cardApplicationConnect(cardApplicationConnect);
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());

	DIDList didList = new DIDList();
	didList.setConnectionHandle(result.getConnectionHandle());
	DIDQualifierType didQualifier = new DIDQualifierType();
	didQualifier.setApplicationIdentifier(appIdentifier_ESIGN);
	didQualifier.setObjectIdentifier("urn:oid:1.3.162.15480.3.0.25");
	didQualifier.setApplicationFunction("Compute-signature");
	didList.setFilter(didQualifier);
	DIDListResponse didListResponse = instance.didList(didList);

	Assert.assertTrue(didListResponse.getDIDNameList().getDIDName().size() > 0);
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());

	// get path to root
	cardApplicationPath = new CardApplicationPath();
	cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(appIdentifier_ROOT);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);

	// connect to root
	cardApplicationConnect = new CardApplicationConnect();
	cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult()
		.get(0));
	cardApplicationConnect.getCardApplicationPath().setCardApplication(appIdentifier_ROOT);
	result = instance.cardApplicationConnect(cardApplicationConnect);
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());


	didList = new DIDList();
	didList.setConnectionHandle(result.getConnectionHandle());
	didQualifier = new DIDQualifierType();
	didQualifier.setApplicationIdentifier(appIdentifier_ROOT);
	didQualifier.setObjectIdentifier("urn:oid:1.3.162.15480.3.0.25");
	didQualifier.setApplicationFunction("Compute-signature");
	didList.setFilter(didQualifier);
	didListResponse = instance.didList(didList);

	// we expect 0 because of the filter
	Assert.assertEquals(didListResponse.getDIDNameList().getDIDName().size(), 0);
	assertEquals(ECardConstants.Major.OK, didListResponse.getResult().getResultMajor());

	// test null connectionhandle
	didList = new DIDList();
	didList.setConnectionHandle(null);
	didListResponse = instance.didList(didList);
	assertEquals(ECardConstants.Major.ERROR, didListResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, didListResponse.getResult().getResultMinor());

	//test invalid connectionhandle
	didList = new DIDList();
	didList.setConnectionHandle(result.getConnectionHandle());
	didList.getConnectionHandle().setIFDName("invalid");
	didListResponse = instance.didList(didList);
	assertEquals(ECardConstants.Major.ERROR, didListResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.SAL.UNKNOWN_HANDLE, didListResponse.getResult().getResultMinor());

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
     *
     * @throws ParserConfigurationException
     */
    @Test
    public void testDidAuthenticate() throws ParserConfigurationException {
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
	// get path to esign
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(appIdentifier_ESIGN);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);

	// connect to esign
	CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult()
		.get(0));
	CardApplicationConnectResponse result = instance.cardApplicationConnect(cardApplicationConnect);
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());

	ACLList aclList = new ACLList();
	aclList.setConnectionHandle(result.getConnectionHandle());
	TargetNameType targetName = new TargetNameType();
	targetName.setCardApplicationName(appIdentifier_ESIGN);
	aclList.setTargetName(targetName);
	ACLListResponse aclListResponse = instance.aclList(aclList);
	assertEquals(aclListResponse.getResult().getResultMajor(), ECardConstants.Major.OK);
	assertTrue(aclListResponse.getTargetACL().getAccessRule().size()>0);

	// test null connectionhandle
	aclList = new ACLList();
	aclList.setConnectionHandle(null);
	targetName = new TargetNameType();
	targetName.setCardApplicationName(appIdentifier_ESIGN);
	aclList.setTargetName(targetName);
	aclListResponse = instance.aclList(aclList);
	assertEquals(ECardConstants.Major.ERROR, aclListResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, aclListResponse.getResult().getResultMinor());

	// test missing targetname
	aclList = new ACLList();
	aclList.setConnectionHandle(null);
	targetName = new TargetNameType();
	aclList.setTargetName(targetName);
	aclListResponse = instance.aclList(aclList);
	assertEquals(ECardConstants.Major.ERROR, aclListResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, aclListResponse.getResult().getResultMinor());

	//test invalid applicationIdentifier
	aclList = new ACLList();
	aclList.setConnectionHandle(result.getConnectionHandle());
	targetName = new TargetNameType();
	targetName.setCardApplicationName(new byte[]{0x0, 0x0, 0x0});
	aclList.setTargetName(targetName);
	aclListResponse = instance.aclList(aclList);
	assertEquals(ECardConstants.Major.ERROR, aclListResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.SAL.NAMED_ENTITY_NOT_FOUND, aclListResponse.getResult().getResultMinor());

	//test invalid connectionhandle
	aclList = new ACLList();
	aclList.setConnectionHandle(result.getConnectionHandle());
	aclList.getConnectionHandle().setIFDName("invalid");
	targetName = new TargetNameType();
	targetName.setCardApplicationName(appIdentifier_ESIGN);
	aclList.setTargetName(targetName);
	aclListResponse = instance.aclList(aclList);
	assertEquals(ECardConstants.Major.ERROR, aclListResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.SAL.UNKNOWN_HANDLE, aclListResponse.getResult().getResultMinor());
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
    @Test(enabled=false)
    public void testSingalEvent() {
	System.out.println("singalEvent");
	// same as getconnectionhandles, so call this one instead
	testGetConnectionHandles();
    }

}
