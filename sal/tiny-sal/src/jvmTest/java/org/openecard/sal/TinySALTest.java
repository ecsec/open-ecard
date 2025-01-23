/****************************************************************************
 * Copyright (C) 2012-2019 HS Coburg.
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
import iso.std.iso_iec._24727.tech.schema.CardInfoType;
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
import iso.std.iso_iec._24727.tech.schema.PathType;
import iso.std.iso_iec._24727.tech.schema.Sign;
import iso.std.iso_iec._24727.tech.schema.SignResponse;
import iso.std.iso_iec._24727.tech.schema.TargetNameType;
import iso.std.iso_iec._24727.tech.schema.Terminate;
import iso.std.iso_iec._24727.tech.schema.TerminateResponse;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificate;
import iso.std.iso_iec._24727.tech.schema.VerifyCertificateResponse;
import iso.std.iso_iec._24727.tech.schema.VerifySignature;
import iso.std.iso_iec._24727.tech.schema.VerifySignatureResponse;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Iterator;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.bouncycastle.util.encoders.Hex;
import org.openecard.common.ClientEnv;
import org.openecard.common.ECardConstants;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.CIFProvider;
import org.openecard.common.util.ByteUtils;
import org.openecard.ifd.scio.IFD;
import org.openecard.recognition.CardRecognitionImpl;
import org.openecard.transport.dispatcher.MessageDispatcher;
import org.testng.Assert;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;


/**
 *
 * @author Johannes Schmölz
 * @author Dirk Petrautzki
 */
public class TinySALTest {

    private static final boolean TESTS_ENABLED = false;

    private ClientEnv env;
    private TinySAL instance;
    private byte[] contextHandle = null;
    byte[] appIdentifier_ESIGN = Hex.decode("A000000167455349474E");
    byte[] appIdentifier_ROOT = Hex.decode("D2760001448000");

    @BeforeMethod()
    public void setUp() throws Exception {
	env = new ClientEnv();
	Dispatcher dispatcher = new MessageDispatcher(env);
	env.setDispatcher(dispatcher);
	IFD ifd = new IFD();
	ifd.setEnvironment(env);
	env.setIfd(ifd);

	EstablishContextResponse ecr = env.getIfd().establishContext(new EstablishContext());
	final CardRecognitionImpl cr = new CardRecognitionImpl(env);
	ListIFDs listIFDs = new ListIFDs();
	contextHandle = ecr.getContextHandle();
	listIFDs.setContextHandle(ecr.getContextHandle());
	ListIFDsResponse listIFDsResponse = ifd.listIFDs(listIFDs);
	RecognitionInfo recognitionInfo = cr.recognizeCard(contextHandle, listIFDsResponse.getIFDName().get(0), BigInteger.ZERO);
	CIFProvider cp = new CIFProvider() {
	    @Override
	    public CardInfoType getCardInfo(ConnectionHandleType type, String cardType) {
		return cr.getCardInfo(cardType);
	    }
	    @Override
	    public boolean needsRecognition(byte[] atr) {
		return true;
	    }

            @Override
            public CardInfoType getCardInfo(String cardType) throws RuntimeException {
                return cr.getCardInfo(cardType);
            }

            @Override
            public InputStream getCardImage(String cardType) {
                return null;
            }
	};
	env.setCifProvider(cp);
	// TODO: fix test
//	SALStateCallback salCallback = new SALStateCallback(env, states);
//
//	ConnectionHandleType connectionHandleType = new ConnectionHandleType();
//	connectionHandleType.setContextHandle(ecr.getContextHandle());
//	connectionHandleType.setRecognitionInfo(recognitionInfo);
//	connectionHandleType.setIFDName(listIFDsResponse.getIFDName().get(0));
//	connectionHandleType.setSlotIndex(new BigInteger("0"));
//
//	salCallback.signalEvent(EventType.CARD_RECOGNIZED, new IfdEventObject(connectionHandleType));
//	instance = new TinySAL(env, states);
//	env.setSAL(instance);
    }

    /**
     * Test of initialize method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testInitialize() {
	System.out.println("initialize");
	Initialize parameters = new Initialize();
	InitializeResponse result = instance.initialize(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of terminate method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testTerminate() {
	System.out.println("terminate");
	Terminate parameters = new Terminate();
	TerminateResponse result = instance.terminate(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationPath method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
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
	cardApplicationPath.setCardAppPathRequest(null);
	cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);
	assertEquals(cardApplicationPathResponse.getResult().getResultMajor(), ECardConstants.Major.ERROR);
	assertEquals(cardApplicationPathResponse.getResult().getResultMinor(), ECardConstants.Minor.App.INCORRECT_PARM);
    }

    /**
     * Test of cardApplicationConnect method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
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
    @Test(enabled = TESTS_ENABLED)
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
	instance.cardApplicationDisconnect(cardApplicationDisconnect);
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
	CardApplicationDisconnectResponse cardApplicationDisconnectResponse = instance.cardApplicationDisconnect(cardApplicationDisconnect);
	assertEquals(ECardConstants.Major.ERROR, cardApplicationDisconnectResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, cardApplicationDisconnectResponse.getResult().getResultMinor());

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
    @Test(enabled = TESTS_ENABLED)
    public void testCardApplicationStartSession() {
	System.out.println("cardApplicationStartSession");
	CardApplicationStartSession parameters = new CardApplicationStartSession();
	CardApplicationStartSessionResponse result = instance.cardApplicationStartSession(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationEndSession method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testCardApplicationEndSession() {
	System.out.println("cardApplicationEndSession");
	CardApplicationEndSession parameters = new CardApplicationEndSession();
	CardApplicationEndSessionResponse result = instance.cardApplicationEndSession(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationList method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
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
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, cardApplicationListResponse.getResult().getResultMinor());

	// test nullpointer
	cardApplicationList = new CardApplicationList();
	cardApplicationList.setConnectionHandle(null);
	cardApplicationListResponse = instance.cardApplicationList(cardApplicationList);
	assertEquals(ECardConstants.Major.ERROR, cardApplicationListResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, cardApplicationListResponse.getResult().getResultMinor());
    }

    // TODO: make it work again according to redesign
//    /**
//     * Test of cardApplicationCreate method, of class TinySAL.
//     */
//    @Test(enabled = TESTS_ENABLED)
//    public void testCardApplicationCreate() {
//	System.out.println("cardApplicationCreate");
//
//	Set<CardStateEntry> cHandles = states.getMatchingEntries(new ConnectionHandleType());
//	byte[] appName = {(byte)0x74, (byte)0x65, (byte)0x73, (byte)0x74};
//
//	CardApplicationCreate parameters = new CardApplicationCreate();
//	parameters.setConnectionHandle(cHandles.iterator().next().handleCopy());
//	parameters.setCardApplicationName(appName);
//
//	AccessControlListType cardApplicationACL = new AccessControlListType();
//	parameters.setCardApplicationACL(cardApplicationACL);
//
//	CardApplicationCreateResponse result = instance.cardApplicationCreate(parameters);
//	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());
//
//	// get path to esign
//	CardApplicationPath cardApplicationPath = new CardApplicationPath();
//	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
//	cardApplicationPathType.setCardApplication(appIdentifier_ESIGN);
//	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
//	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);
//
//	// connect to esign
//	CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
//	cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0));
//	CardApplicationConnectResponse resultConnect = instance.cardApplicationConnect(cardApplicationConnect);
//	assertEquals(ECardConstants.Major.OK, resultConnect.getResult().getResultMajor());
//
//	CardApplicationList cardApplicationList = new CardApplicationList();
//	cardApplicationList.setConnectionHandle(cHandles.iterator().next().handleCopy());
//	CardApplicationListResponse cardApplicationListResponse = instance.cardApplicationList(cardApplicationList);
//
//	Iterator<byte[]> it = cardApplicationListResponse.getCardApplicationNameList().getCardApplicationName().iterator();
//	boolean appFound = false;
//
//	try {
//	    while (it.hasNext()) {
//		byte[] val = it.next();
//
//		if (Arrays.equals(val, appName)) {
//		    appFound = true;
//		}
//	    }
//
//	    assertTrue(appFound);
//
//	} catch (Exception e) {
//	    assertTrue(appFound);
//	    System.out.println(e);
//	}
//    }
//
//    /**
//     * Test of cardApplicationDelete method, of class TinySAL.
//     */
//    @Test(enabled = TESTS_ENABLED)
//    public void testCardApplicationDelete() {
//	System.out.println("cardApplicationDelete");
//
//	Set<CardStateEntry> cHandles = states.getMatchingEntries(new ConnectionHandleType());
//	byte[] appName = {(byte)0x74, (byte)0x65, (byte)0x73, (byte)0x74};
//
//	CardApplicationDelete parameters = new CardApplicationDelete();
//	parameters.setConnectionHandle(cHandles.iterator().next().handleCopy());
//	parameters.setCardApplicationName(appName);
//
//	CardApplicationDeleteResponse result = instance.cardApplicationDelete(parameters);
//	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());
//
//	// get path to esign
//	CardApplicationPath cardApplicationPath = new CardApplicationPath();
//	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
//	cardApplicationPathType.setCardApplication(appIdentifier_ESIGN);
//	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
//	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);
//
//	// connect to esign
//	CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
//	cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0));
//	CardApplicationConnectResponse resultConnect = instance.cardApplicationConnect(cardApplicationConnect);
//	assertEquals(ECardConstants.Major.OK, resultConnect.getResult().getResultMajor());
//
//	CardApplicationList cardApplicationList = new CardApplicationList();
//	cardApplicationList.setConnectionHandle(cHandles.iterator().next().handleCopy());
//	CardApplicationListResponse cardApplicationListResponse = instance.cardApplicationList(cardApplicationList);
//
//	Iterator<byte[]> it = cardApplicationListResponse.getCardApplicationNameList().getCardApplicationName().iterator();
//	boolean appFound = false;
//
//	try {
//	    while (it.hasNext()) {
//		byte[] val = it.next();
//
//		if (Arrays.equals(val, appName)) {
//		    appFound = true;
//		}
//	    }
//
//	    assertTrue(!appFound);
//
//	} catch (Exception e) {
//	    assertTrue(!appFound);
//	    System.out.println(e);
//	}
//    }

    /**
     * Test of cardApplicationServiceList method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
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
	assertTrue(cardApplicationServiceNameList.getCardApplicationServiceName().isEmpty());
    }

    /**
     * Test of cardApplicationServiceCreate method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testCardApplicationServiceCreate() {
	System.out.println("cardApplicationServiceCreate");
	CardApplicationServiceCreate parameters = new CardApplicationServiceCreate();
	CardApplicationServiceCreateResponse result = instance.cardApplicationServiceCreate(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationServiceLoad method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testCardApplicationServiceLoad() {
	System.out.println("cardApplicationServiceLoad");
	CardApplicationServiceLoad parameters = new CardApplicationServiceLoad();
	CardApplicationServiceLoadResponse result = instance.cardApplicationServiceLoad(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationServiceDelete method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testCardApplicationServiceDelete() {
	System.out.println("cardApplicationServiceDelete");
	CardApplicationServiceDelete parameters = new CardApplicationServiceDelete();
	CardApplicationServiceDeleteResponse result = instance.cardApplicationServiceDelete(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of cardApplicationServiceDescribe method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testCardApplicationServiceDescribe() {
	System.out.println("cardApplicationServiceDescribe");
	CardApplicationServiceDescribe parameters = new CardApplicationServiceDescribe();

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

	CardApplicationServiceDescribeResponse resultServiceDescribe = instance.cardApplicationServiceDescribe(parameters);
	assertEquals(ECardConstants.Major.OK, resultServiceDescribe.getResult().getResultMajor());
    }

    /**
     * Test of executeAction method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testExecuteAction() {
	System.out.println("executeAction");
	ExecuteAction parameters = new ExecuteAction();
	ExecuteActionResponse result = instance.executeAction(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dataSetList method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
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
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, dataSetListResponse.getResult().getResultMinor());

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
    @Test(enabled = TESTS_ENABLED)
    public void testDataSetCreate() {
	System.out.println("dataSetCreate");

	DataSetCreate parameters = new DataSetCreate();

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

	AccessControlListType accessControlList = new AccessControlListType();

	parameters.setConnectionHandle(result.getConnectionHandle());

	String dataSetName = "DataSetTest";
	parameters.setDataSetName(dataSetName);
	parameters.setDataSetACL(accessControlList);

	DataSetCreateResponse resultDataSetCreate = instance.dataSetCreate(parameters);
	assertEquals(ECardConstants.Major.OK, resultDataSetCreate.getResult().getResultMajor());

	// list datasets of esign
	DataSetList dataSetList = new DataSetList();
	dataSetList.setConnectionHandle(result.getConnectionHandle());
	DataSetListResponse dataSetListResponse = instance.dataSetList(dataSetList);

	Iterator<String> it = dataSetListResponse.getDataSetNameList().getDataSetName().iterator();
	boolean appFound = false;

	while (it.hasNext()) {
	    String val = it.next();

	    if (val.equals(dataSetName)) {
		appFound = true;
	    }
	}

	assertTrue(appFound);
	assertEquals(ECardConstants.Major.OK, dataSetListResponse.getResult().getResultMajor());
    }

    /**
     * Test of dataSetSelect method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
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
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, dataSetSelectResponse.getResult().getResultMinor());
    }

    /**
     * Test of dataSetDelete method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testDataSetDelete() {
	System.out.println("dataSetDelete");

	DataSetDelete parameters = new DataSetDelete();

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

	parameters.setConnectionHandle(result.getConnectionHandle());

	String dataSetName = "DataSetTest";
	parameters.setDataSetName(dataSetName);

	DataSetDeleteResponse resultDataSetDelete = instance.dataSetDelete(parameters);
	assertEquals(ECardConstants.Major.OK, resultDataSetDelete.getResult().getResultMajor());

	// list datasets of esign
	DataSetList dataSetList = new DataSetList();
	dataSetList.setConnectionHandle(result.getConnectionHandle());
	DataSetListResponse dataSetListResponse = instance.dataSetList(dataSetList);

	Iterator<String> it = dataSetListResponse.getDataSetNameList().getDataSetName().iterator();
	boolean appFound = false;

	while (it.hasNext()) {
	    String val = it.next();

	    if (val.equals(dataSetName)) {
		appFound = true;
	    }
	}

	assertTrue(!appFound);
	assertEquals(ECardConstants.Major.OK, dataSetListResponse.getResult().getResultMajor());
    }

    /**
     * Test of dsiList method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testDsiList() {
	System.out.println("dsiList");

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

	Assert.assertTrue(dataSetListResponse.getDataSetNameList().getDataSetName().size() > 0);
	assertEquals(ECardConstants.Major.OK, dataSetListResponse.getResult().getResultMajor());

	String dataSetName = dataSetListResponse.getDataSetNameList().getDataSetName().get(0);

	DSIList parameters = new DSIList();
	parameters.setConnectionHandle(result.getConnectionHandle());

	DSIListResponse resultDSIList = instance.dsiList(parameters);
	assertEquals(ECardConstants.Major.OK, resultDSIList.getResult().getResultMajor());
	assertTrue(resultDSIList.getDSINameList().getDSIName().isEmpty());
    }

    /**
     * Test of dsiCreate method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testDsiCreate() {
	System.out.println("dsiCreate");

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

	Assert.assertTrue(dataSetListResponse.getDataSetNameList().getDataSetName().size() > 0);
	assertEquals(ECardConstants.Major.OK, dataSetListResponse.getResult().getResultMajor());

	String dataSetName = dataSetListResponse.getDataSetNameList().getDataSetName().get(0);
	byte[] dsiContent = {(byte)0x74, (byte)0x65, (byte)0x73, (byte)0x74};
	String dsiName = "DsiTest";
	PathType dsiPath = new PathType();
	byte[] dsiEF = {(byte)0x03, (byte)0x00};
	dsiPath.setEfIdOrPath(dsiEF);

	DSICreate parameters = new DSICreate();
	parameters.setConnectionHandle(result.getConnectionHandle());
	parameters.setDSIContent(dsiContent);
	parameters.setDSIName(dsiName);

	DSICreateResponse resultDSICreate = instance.dsiCreate(parameters);
	assertEquals(ECardConstants.Major.OK, resultDSICreate.getResult().getResultMajor());

	// list DSIs of DataSetName

	DSIList parametersDSI = new DSIList();
	parametersDSI.setConnectionHandle(result.getConnectionHandle());

	DSIListResponse resultDSIList = instance.dsiList(parametersDSI);
	assertEquals(ECardConstants.Major.OK, resultDSIList.getResult().getResultMajor());

	// try to find new DSI

	Iterator<String> it = resultDSIList.getDSINameList().getDSIName().iterator();
	boolean dsiFound = false;

	while (it.hasNext()) {
	    String val = it.next();

	    if (val.equals(dsiName)) {
		dsiFound = true;
	    }
	}

	assertTrue(dsiFound);
    }

    /**
     * Test of dsiDelete method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testDsiDelete() {
	System.out.println("dsiDelete");

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

	Assert.assertTrue(dataSetListResponse.getDataSetNameList().getDataSetName().size() > 0);
	assertEquals(ECardConstants.Major.OK, dataSetListResponse.getResult().getResultMajor());

	String dataSetName = dataSetListResponse.getDataSetNameList().getDataSetName().get(0);
	String dsiName = "dsiTest";

	DSIDelete parameters = new DSIDelete();
	parameters.setConnectionHandle(result.getConnectionHandle());
	parameters.setDSIName(dsiName);

	DSIDeleteResponse resultDSIDelete = instance.dsiDelete(parameters);
	assertEquals(ECardConstants.Major.OK, resultDSIDelete.getResult().getResultMajor());

	// try to find dsiName under dataSetName

	DSIList parametersDSI = new DSIList();
	parametersDSI.setConnectionHandle(result.getConnectionHandle());

	DSIListResponse resultDSIList = instance.dsiList(parametersDSI);
	assertEquals(ECardConstants.Major.OK, resultDSIList.getResult().getResultMajor());

	// try to find new DSI

	Iterator<String> it = resultDSIList.getDSINameList().getDSIName().iterator();
	boolean dsiFound = false;

	while (it.hasNext()) {
	    String val = it.next();

	    if (val.equals(dsiName)) {
		dsiFound = true;
	    }
	}

	assertTrue(!dsiFound);
    }

    /**
     * Test of dsiWrite method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testDsiWrite() {
	System.out.println("dsiWrite");
	DSIWrite parameters = new DSIWrite();
	DSIWriteResponse result = instance.dsiWrite(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of dsiRead method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
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
	assertEquals(ECardConstants.Minor.SAL.SECURITY_CONDITION_NOT_SATISFIED, dsiReadResponse.getResult().getResultMinor());

	// test invalid connectionhandle
	dsiRead = new DSIRead();
	dsiRead.setConnectionHandle(result.getConnectionHandle());
	dsiRead.getConnectionHandle().setIFDName("invalid");
	dsiRead.setDSIName(null);
	dsiReadResponse = instance.dsiRead(dsiRead);
	assertEquals(ECardConstants.Major.ERROR, dsiReadResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, dsiReadResponse.getResult().getResultMinor());
    }

    /**
     * Test of encipher method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testEncipher() {
	System.out.println("encipher");
	Encipher parameters = new Encipher();
	EncipherResponse result = instance.encipher(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of decipher method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testDecipher() {
	System.out.println("decipher");
	Decipher parameters = new Decipher();
	DecipherResponse result = instance.decipher(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of getRandom method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testGetRandom() {
	System.out.println("getRandom");
	GetRandom parameters = new GetRandom();
	GetRandomResponse result = instance.getRandom(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of hash method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testHash() {
	System.out.println("hash");
	Hash parameters = new Hash();
	HashResponse result = instance.hash(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of sign method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testSign() {
	System.out.println("sign");
	Sign parameters = new Sign();
	SignResponse result = instance.sign(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of verifySignature method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testVerifySignature() {
	System.out.println("verifySignature");
	VerifySignature parameters = new VerifySignature();
	VerifySignatureResponse result = instance.verifySignature(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of verifyCertificate method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testVerifyCertificate() {
	System.out.println("verifyCertificate");
	VerifyCertificate parameters = new VerifyCertificate();
	VerifyCertificateResponse result = instance.verifyCertificate(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of didList method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
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
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, didListResponse.getResult().getResultMinor());
    }

    /**
     * Test of didCreate method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testDidCreate() {
	System.out.println("didCreate");
	DIDCreate parameters = new DIDCreate();
	DIDCreateResponse result = instance.didCreate(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of didGet method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testDidGet() {
	System.out.println("didGet");

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

	String didName = didListResponse.getDIDNameList().getDIDName().get(0);

	DIDGet parameters = new DIDGet();
	parameters.setDIDName(didName);
	parameters.setConnectionHandle(result.getConnectionHandle());

	DIDGetResponse resultDIDGet = instance.didGet(parameters);
	assertEquals(ECardConstants.Major.OK, resultDIDGet.getResult().getResultMajor());
	assertTrue(resultDIDGet.getDIDStructure() != null);
    }

    /**
     * Test of didUpdate method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testDidUpdate() {
	System.out.println("didUpdate");
	DIDUpdate parameters = new DIDUpdate();
	DIDUpdateResponse result = instance.didUpdate(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of didDelete method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
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
    @Test(enabled = TESTS_ENABLED)
    public void testDidAuthenticate() throws ParserConfigurationException {
	System.out.println("didAuthenticate");
	DIDAuthenticate parameters = new DIDAuthenticate();
	DIDAuthenticateResponse result = instance.didAuthenticate(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

    /**
     * Test of aclList method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
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
	assertEquals(ECardConstants.Minor.App.INCORRECT_PARM, aclListResponse.getResult().getResultMinor());
    }

    /**
     * Test of aclModify method, of class TinySAL.
     */
    @Test(enabled = TESTS_ENABLED)
    public void testAclModify() {
	System.out.println("aclModify");
	ACLModify parameters = new ACLModify();
	ACLModifyResponse result = instance.aclModify(parameters);
	assertEquals(ECardConstants.Major.ERROR, result.getResult().getResultMajor());
    }

}
