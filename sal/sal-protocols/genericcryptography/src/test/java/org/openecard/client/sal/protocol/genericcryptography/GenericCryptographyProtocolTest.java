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

package org.openecard.client.sal.protocol.genericcryptography;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import iso.std.iso_iec._24727.tech.schema.*;
import java.math.BigInteger;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.bouncycastle.util.encoders.Hex;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.sal.state.SALStateCallback;
import org.openecard.client.gui.swing.SwingUserConsent;
import org.openecard.client.ifd.scio.IFD;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.sal.protocol.genericryptography.GenericCryptoProtocolFactory;
import org.openecard.client.sal.protocol.pincompare.PinCompareProtocolFactory;
import org.openecard.client.transport.dispatcher.MessageDispatcher;
import static org.testng.Assert.assertEquals;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class GenericCryptographyProtocolTest {

    @BeforeClass
    public static void disable() {
	throw new SkipException("Test completely disabled.");
    }

    private static ClientEnv env;
    private static TinySAL instance;
    private static CardStateMap states;
    byte[] cardApplication = Hex.decode("A000000167455349474E");
    byte[] cardApplication_ROOT = Hex.decode("D2760001448000");
    
    @BeforeClass
    public static void setUp() throws Exception {
	env = new ClientEnv();
        Dispatcher d = new MessageDispatcher(env);
        env.setDispatcher(d);
	IFD ifd = new IFD();
	ifd.setGUI(new SwingUserConsent(new SwingDialogWrapper()));
	env.setIFD(ifd);
	states = new CardStateMap();

	EstablishContextResponse ecr = env.getIFD().establishContext(new EstablishContext());
	CardRecognition cr = new CardRecognition(ifd, ecr.getContextHandle());
	ListIFDs listIFDs = new ListIFDs();

	listIFDs.setContextHandle(ecr.getContextHandle());
	ListIFDsResponse listIFDsResponse = ifd.listIFDs(listIFDs);
	RecognitionInfo recognitionInfo = cr.recognizeCard(listIFDsResponse.getIFDName().get(0), new BigInteger("0"));
	SALStateCallback salCallback = new SALStateCallback(cr, states);
	Connect c = new Connect();
	c.setContextHandle(ecr.getContextHandle());
	c.setIFDName(listIFDsResponse.getIFDName().get(0));
	c.setSlot(new BigInteger("0"));
	ConnectResponse connectResponse = env.getIFD().connect(c);

	ConnectionHandleType connectionHandleType = new ConnectionHandleType();
	connectionHandleType.setContextHandle(ecr.getContextHandle());
	connectionHandleType.setRecognitionInfo(recognitionInfo);
	connectionHandleType.setIFDName(listIFDsResponse.getIFDName().get(0));
	connectionHandleType.setSlotIndex(new BigInteger("0"));
	connectionHandleType.setSlotHandle(connectResponse.getSlotHandle());
	salCallback.signalEvent(EventType.CARD_RECOGNIZED, connectionHandleType);
	instance = new TinySAL(env, states);
	instance.addProtocol(ECardConstants.Protocol.GENERIC_CRYPTO, new GenericCryptoProtocolFactory());
	instance.addProtocol(ECardConstants.Protocol.PIN_COMPARE, new PinCompareProtocolFactory());
    }

    @Test
    public void testDIDGet() throws ParserConfigurationException {
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(cardApplication);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);

	CardApplicationConnect parameters = new CardApplicationConnect();
	parameters.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0));
	CardApplicationConnectResponse result = instance.cardApplicationConnect(parameters);
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());
	DIDList didList = new DIDList();
	didList.setConnectionHandle(result.getConnectionHandle());
	DIDQualifierType didQualifier = new DIDQualifierType();
	didQualifier.setApplicationIdentifier(cardApplication);
	didQualifier.setObjectIdentifier(ECardConstants.Protocol.GENERIC_CRYPTO);
	didQualifier.setApplicationFunction("Compute-signature");
	didList.setFilter(didQualifier);
	DIDListResponse didListResponse = instance.didList(didList);

	DIDGet didGet = new DIDGet();
	didGet.setConnectionHandle(result.getConnectionHandle());
	didGet.setDIDName(didListResponse.getDIDNameList().getDIDName().get(0));
	didGet.setDIDScope(DIDScopeType.LOCAL);
	DIDGetResponse didGetResponse = instance.didGet(didGet);

	assertEquals(ECardConstants.Major.OK, didGetResponse.getResult().getResultMajor());
	org.openecard.client.common.sal.anytype.CryptoMarkerType cryptoMarker = new org.openecard.client.common.sal.anytype.CryptoMarkerType((CryptoMarkerType) didGetResponse.getDIDStructure().getDIDMarker());
    }

    @Test
    public void testSign() throws Exception {
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(cardApplication);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);
	WSHelper.checkResult(cardApplicationPathResponse);
	CardApplicationConnect parameters = new CardApplicationConnect();
	parameters.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0));
	CardApplicationConnectResponse result = instance.cardApplicationConnect(parameters);
	WSHelper.checkResult(result);

	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());
	DIDList didList = new DIDList();
	didList.setConnectionHandle(result.getConnectionHandle());
	DIDQualifierType didQualifier = new DIDQualifierType();
	didQualifier.setApplicationIdentifier(cardApplication);
	didQualifier.setObjectIdentifier(ECardConstants.Protocol.GENERIC_CRYPTO);
	didQualifier.setApplicationFunction("Compute-signature");
	didList.setFilter(didQualifier);
	DIDListResponse didListResponse = instance.didList(didList);
	WSHelper.checkResult(didListResponse);

	DIDAuthenticate didAthenticate = new DIDAuthenticate();
	didAthenticate.setDIDName("PIN.home");
	PinCompareDIDAuthenticateInputType didAuthenticationData = new PinCompareDIDAuthenticateInputType();
	didAthenticate.setAuthenticationProtocolData(didAuthenticationData);
	didAthenticate.setConnectionHandle(result.getConnectionHandle());
	didAthenticate.getConnectionHandle().setCardApplication(cardApplication_ROOT);
	didAuthenticationData.setProtocol(ECardConstants.Protocol.PIN_COMPARE);
	didAthenticate.setAuthenticationProtocolData(didAuthenticationData);
	DIDAuthenticateResponse didAuthenticateResult = instance.didAuthenticate(didAthenticate);
	WSHelper.checkResult(didAuthenticateResult);

	assertEquals(didAuthenticateResult.getAuthenticationProtocolData().getProtocol(), ECardConstants.Protocol.PIN_COMPARE);
	assertEquals(didAuthenticateResult.getAuthenticationProtocolData().getAny().size(), 0);
	assertEquals(ECardConstants.Major.OK, didAuthenticateResult.getResult().getResultMajor());

	Sign sign = new Sign();
	sign.setMessage(new byte[] { 0x0, 0x0, 0x0 });
	sign.setConnectionHandle(result.getConnectionHandle());
	sign.getConnectionHandle().setCardApplication(cardApplication);
	sign.setDIDName(didListResponse.getDIDNameList().getDIDName().get(0));
	sign.setDIDScope(DIDScopeType.LOCAL);
	SignResponse signResponse = instance.sign(sign);
	System.out.println(signResponse.getResult().getResultMinor());
	assertEquals(ECardConstants.Major.OK, signResponse.getResult().getResultMajor());
	WSHelper.checkResult(signResponse);
    }

    public void testDIDCreate() {
	// TODO
    }

    public void testDIDUpdate() {
	// TODO
    }

    public void testEncipher() {
	// TODO
    }

    public void testDecipher() {
	// TODO
    }

    public void testGetRandom() {
	// TODO
    }

    public void testHash() {
	// TODO
    }

    public void testVerifySignature() {
	// TODO
    }

    public void testVerifyCertificate() {
	// TODO
    }

    public void testDIDAuthenticate() {
	// TODO
    }

    public void testCardApplicationStartSession() {
	// TODO expected result /resultminor/sal#inappropriateProtocolForAction
    }

}
