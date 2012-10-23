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

import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import iso.std.iso_iec._24727.tech.schema.CryptoMarkerType;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDGet;
import iso.std.iso_iec._24727.tech.schema.DIDGetResponse;
import iso.std.iso_iec._24727.tech.schema.DIDList;
import iso.std.iso_iec._24727.tech.schema.DIDListResponse;
import iso.std.iso_iec._24727.tech.schema.DIDQualifierType;
import iso.std.iso_iec._24727.tech.schema.DIDScopeType;
import iso.std.iso_iec._24727.tech.schema.DSIRead;
import iso.std.iso_iec._24727.tech.schema.DSIReadResponse;
import iso.std.iso_iec._24727.tech.schema.Decipher;
import iso.std.iso_iec._24727.tech.schema.DecipherResponse;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import iso.std.iso_iec._24727.tech.schema.PinCompareDIDAuthenticateInputType;
import iso.std.iso_iec._24727.tech.schema.Sign;
import iso.std.iso_iec._24727.tech.schema.SignResponse;
import iso.std.iso_iec._24727.tech.schema.VerifySignature;
import iso.std.iso_iec._24727.tech.schema.VerifySignatureResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.crypto.Cipher;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.state.CardStateMap;
import org.openecard.client.common.sal.state.SALStateCallback;
import org.openecard.client.common.util.StringUtils;
import org.openecard.client.gui.swing.SwingDialogWrapper;
import org.openecard.client.gui.swing.SwingUserConsent;
import org.openecard.client.ifd.scio.IFD;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.sal.protocol.pincompare.PinCompareProtocolFactory;
import org.openecard.client.transport.dispatcher.MessageDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


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
    byte[] cardApplication = StringUtils.toByteArray("A000000167455349474E");
    byte[] cardApplication_ROOT = StringUtils.toByteArray("D2760001448000");
    private static IFD ifd;
    private final static String plaintext;
    private static final Logger logger = LoggerFactory.getLogger(GenericCryptographyProtocolTest.class);

    static {
	try {
	    plaintext = loadFile("plaintext.txt");
	} catch (IOException ex) {
	    throw new RuntimeException(ex);
	}
    }

    private static String loadFile(String resourcePath) throws IOException {
	InputStream in = GenericCryptographyProtocolTest.class.getClassLoader().getResourceAsStream(resourcePath);
	StringWriter w = new StringWriter();
	BufferedReader r = new BufferedReader(new InputStreamReader(in, Charset.forName("utf-8")));
	String nextLine;
	while ((nextLine = r.readLine()) != null) {
	    w.write(nextLine);
	    w.write(String.format("%n")); // platform dependant newline character
	}
	return w.toString();
    }

    @BeforeClass
    public static void setUp() throws Exception {

	env = new ClientEnv();
	Dispatcher d = new MessageDispatcher(env);
	env.setDispatcher(d);
	ifd = new IFD();
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

	ConnectionHandleType connectionHandleType = new ConnectionHandleType();
	connectionHandleType.setContextHandle(ecr.getContextHandle());
	connectionHandleType.setRecognitionInfo(recognitionInfo);
	connectionHandleType.setIFDName(listIFDsResponse.getIFDName().get(0));
	connectionHandleType.setSlotIndex(new BigInteger("0"));

	salCallback.signalEvent(EventType.CARD_RECOGNIZED, connectionHandleType);
	instance = new TinySAL(env, states);
	env.setSAL(instance);

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
	parameters.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet()
		.getCardApplicationPathResult().get(0));
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
	assertTrue(didListResponse.getDIDNameList().getDIDName().size() > 0);

	DIDGet didGet = new DIDGet();
	didGet.setConnectionHandle(result.getConnectionHandle());
	didGet.setDIDName(didListResponse.getDIDNameList().getDIDName().get(0));
	didGet.setDIDScope(DIDScopeType.LOCAL);
	DIDGetResponse didGetResponse = instance.didGet(didGet);

	assertEquals(ECardConstants.Major.OK, didGetResponse.getResult().getResultMajor());
	org.openecard.client.common.sal.anytype.CryptoMarkerType cryptoMarker = 
		new org.openecard.client.common.sal.anytype.CryptoMarkerType(
			(CryptoMarkerType) didGetResponse.getDIDStructure().getDIDMarker());
	assertEquals(cryptoMarker.getCertificateRef().getDataSetName(), "EF.C.CH.AUT");
    }

    /**
     * Test for the Sign Step of the Generic Cryptography protocol. After we connected to the ESIGN application of the
     * eGK, we use DIDList to get a List of DIDs that support the compute signature function. For each DID we let the
     * card compute a signature. If the result is OK we're satisfied.
     * 
     * @throws Exception
     *             when something in this test went unexpectedly wrong
     */
    @Test
    public void testSign() throws Exception {
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(cardApplication);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);
	WSHelper.checkResult(cardApplicationPathResponse);
	CardApplicationConnect parameters = new CardApplicationConnect();
	parameters.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet()
		.getCardApplicationPathResult().get(0));
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
	assertTrue(didListResponse.getDIDNameList().getDIDName().size() > 0);
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

	assertEquals(didAuthenticateResult.getAuthenticationProtocolData().getProtocol(),
		ECardConstants.Protocol.PIN_COMPARE);
	assertEquals(didAuthenticateResult.getAuthenticationProtocolData().getAny().size(), 0);
	assertEquals(ECardConstants.Major.OK, didAuthenticateResult.getResult().getResultMajor());

	for (int numOfDIDs = 0; numOfDIDs < didListResponse.getDIDNameList().getDIDName().size(); numOfDIDs++) {
	    String didName = didListResponse.getDIDNameList().getDIDName().get(numOfDIDs);
	    System.out.println(didName);
	    DIDGet didGet = new DIDGet();
	    didGet.setDIDName(didName);
	    didGet.setDIDScope(DIDScopeType.LOCAL);
	    didGet.setConnectionHandle(result.getConnectionHandle());
	    didGet.getConnectionHandle().setCardApplication(cardApplication);
	    DIDGetResponse didGetResponse = instance.didGet(didGet);

	    org.openecard.client.common.sal.anytype.CryptoMarkerType cryptoMarker = 
		    new org.openecard.client.common.sal.anytype.CryptoMarkerType(
			    (CryptoMarkerType) didGetResponse.getDIDStructure().getDIDMarker());

	    Sign sign = new Sign();
	    byte[] message = StringUtils.toByteArray("616263646263646563646566646566676566676861");

	    if (cryptoMarker.getAlgorithmInfo().getAlgorithmIdentifier().getAlgorithm()
		    .equals(GenericCryptoObjectIdentifier.sigS_ISO9796_2rnd)) {
		// TODO support for sign9796_2_DS2
		continue;
	    }

	    sign.setMessage(message);
	    sign.setConnectionHandle(result.getConnectionHandle());
	    sign.getConnectionHandle().setCardApplication(cardApplication);
	    sign.setDIDName(didName);
	    sign.setDIDScope(DIDScopeType.LOCAL);
	    SignResponse signResponse = instance.sign(sign);
	    assertEquals(ECardConstants.Major.OK, signResponse.getResult().getResultMajor());
	    WSHelper.checkResult(signResponse);
	}
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

    /**
     * Test for the Decipher Step of the Generic Cryptography protocol. After we connected to the ESIGN application
     * of the eGK, we use DIDList to get a List of DIDs that support the Decipher function. We then authenticate with
     * PIN.home and read the contents of the DIDs certificate. With it's public key we encrypt the contents of
     * plaintext.txt and finally let the card decrypt it through a call to Decipher. In the end we match the result with
     * the original plaintext.
     * 
     * @throws Exception when something in this test went unexpectedly wrong
     */
    @Test
    public void testDecipher() throws Exception {
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(cardApplication);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);
	WSHelper.checkResult(cardApplicationPathResponse);
	CardApplicationConnect parameters = new CardApplicationConnect();
	parameters.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet()
		.getCardApplicationPathResult().get(0));
	CardApplicationConnectResponse result = instance.cardApplicationConnect(parameters);
	WSHelper.checkResult(result);
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());

	DIDList didList = new DIDList();
	didList.setConnectionHandle(result.getConnectionHandle());
	DIDQualifierType didQualifier = new DIDQualifierType();
	didQualifier.setApplicationIdentifier(cardApplication);
	didQualifier.setObjectIdentifier(ECardConstants.Protocol.GENERIC_CRYPTO);
	didQualifier.setApplicationFunction("Decipher");
	didList.setFilter(didQualifier);
	DIDListResponse didListResponse = instance.didList(didList);
	assertTrue(didListResponse.getDIDNameList().getDIDName().size() > 0);
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

	assertEquals(didAuthenticateResult.getAuthenticationProtocolData().getProtocol(),
		ECardConstants.Protocol.PIN_COMPARE);
	assertEquals(didAuthenticateResult.getAuthenticationProtocolData().getAny().size(), 0);
	assertEquals(ECardConstants.Major.OK, didAuthenticateResult.getResult().getResultMajor());

	byte[] plaintextBytes = plaintext.getBytes();

	for (int numOfDIDs = 0; numOfDIDs < didListResponse.getDIDNameList().getDIDName().size(); numOfDIDs++) {
	    String didName = didListResponse.getDIDNameList().getDIDName().get(numOfDIDs);
	    DIDGet didGet = new DIDGet();
	    didGet.setDIDName(didName);
	    didGet.setDIDScope(DIDScopeType.LOCAL);
	    didGet.setConnectionHandle(result.getConnectionHandle());
	    didGet.getConnectionHandle().setCardApplication(cardApplication);
	    DIDGetResponse didGetResponse = instance.didGet(didGet);

	    org.openecard.client.common.sal.anytype.CryptoMarkerType cryptoMarker = 
		    new org.openecard.client.common.sal.anytype.CryptoMarkerType(
			    (CryptoMarkerType) didGetResponse.getDIDStructure().getDIDMarker());

	    ByteArrayOutputStream ciphertext = new ByteArrayOutputStream();

	    // read the certificate
	    DSIRead dsiRead = new DSIRead();
	    dsiRead.setConnectionHandle(result.getConnectionHandle());
	    dsiRead.getConnectionHandle().setCardApplication(cardApplication);
	    dsiRead.setDSIName(cryptoMarker.getCertificateRef().getDataSetName());
	    DSIReadResponse dsiReadResponse = instance.dsiRead(dsiRead);
	    assertEquals(ECardConstants.Major.OK, dsiReadResponse.getResult().getResultMajor());
	    assertTrue(dsiReadResponse.getDSIContent().length > 0);

	    // convert the contents to a certificate
	    Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(
		    new ByteArrayInputStream(dsiReadResponse.getDSIContent()));

	    Cipher cipher = null;
	    int blocksize = 0;
	    String algorithmOID = cryptoMarker.getAlgorithmInfo().getAlgorithmIdentifier().getAlgorithm();
	    if (algorithmOID.equals(GenericCryptoObjectIdentifier.rsaEncryption)) {
		cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, cert);
		blocksize = 245; // keysize/8-pkcspadding = (2048)/8-11
	    } else if (algorithmOID.equals(GenericCryptoObjectIdentifier.id_RSAES_OAEP)) {
		cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", new BouncyCastleProvider());
		cipher.init(Cipher.ENCRYPT_MODE, cert);
		blocksize = cipher.getBlockSize();
	    } else {
		logger.warn("Skipping decipher for the unsupported algorithmOID: " + algorithmOID);
		continue;
	    }

	    int rest = plaintextBytes.length % blocksize;

	    // encrypt block for block
	    for (int offset = 0; offset < plaintextBytes.length; offset += blocksize) {
		if ((offset + blocksize) > plaintextBytes.length)
		    ciphertext.write(cipher.doFinal(plaintextBytes, offset, rest));
		else
		    ciphertext.write(cipher.doFinal(plaintextBytes, offset, blocksize));
	    }

	    Decipher decipher = new Decipher();
	    decipher.setCipherText(ciphertext.toByteArray());
	    decipher.setConnectionHandle(result.getConnectionHandle());
	    decipher.getConnectionHandle().setCardApplication(cardApplication);
	    decipher.setDIDName(didName);
	    decipher.setDIDScope(DIDScopeType.LOCAL);
	    DecipherResponse decipherResponse = instance.decipher(decipher);

	    assertEquals(decipherResponse.getPlainText(), plaintextBytes);
	}
    }

    public void testGetRandom() {
	// TODO
    }

    public void testHash() {
	// TODO
    }

    /**
     * Test for the VerifySignature Step of the Generic Cryptography protocol. After we connected to the ESIGN
     * application of the eGK, we use DIDList to get a List of DIDs that support the compute signature function. We
     * then authenticate with PIN.home and let the card sign our message. Afterwards we call VerifySignature for that
     * signature which should return OK.
     * 
     * @throws Exception
     *             when something in this test went unexpectedly wrong
     */
    @Test
    public void testVerifySignature() throws Exception {
	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(cardApplication);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);
	WSHelper.checkResult(cardApplicationPathResponse);
	CardApplicationConnect parameters = new CardApplicationConnect();
	parameters.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet()
		.getCardApplicationPathResult().get(0));
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
	assertTrue(didListResponse.getDIDNameList().getDIDName().size() > 0);
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

	assertEquals(didAuthenticateResult.getAuthenticationProtocolData().getProtocol(),
		ECardConstants.Protocol.PIN_COMPARE);
	assertEquals(didAuthenticateResult.getAuthenticationProtocolData().getAny().size(), 0);
	assertEquals(ECardConstants.Major.OK, didAuthenticateResult.getResult().getResultMajor());

	for (int numOfDIDs = 0; numOfDIDs < didListResponse.getDIDNameList().getDIDName().size(); numOfDIDs++) {
	    String didName = didListResponse.getDIDNameList().getDIDName().get(numOfDIDs);
	    DIDGet didGet = new DIDGet();
	    didGet.setDIDName(didName);
	    didGet.setDIDScope(DIDScopeType.LOCAL);
	    didGet.setConnectionHandle(result.getConnectionHandle());
	    didGet.getConnectionHandle().setCardApplication(cardApplication);
	    DIDGetResponse didGetResponse = instance.didGet(didGet);

	    Sign sign = new Sign();
	    byte[] message = new byte[] { 0x01, 0x02, 0x03 };
	    org.openecard.client.common.sal.anytype.CryptoMarkerType cryptoMarker = 
		    new org.openecard.client.common.sal.anytype.CryptoMarkerType(
			    (CryptoMarkerType) didGetResponse.getDIDStructure().getDIDMarker());

	    String algorithmIdentifier = cryptoMarker.getAlgorithmInfo().getAlgorithmIdentifier().getAlgorithm();

	    if (algorithmIdentifier.equals(GenericCryptoObjectIdentifier.id_RSASSA_PSS)) {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		message = messageDigest.digest(message);
	    } else if (algorithmIdentifier.equals(GenericCryptoObjectIdentifier.sigS_ISO9796_2rnd)) {
		// TODO support for sign9796_2_DS2
		continue;
	    }

	    sign.setMessage(message);
	    sign.setConnectionHandle(result.getConnectionHandle());
	    sign.getConnectionHandle().setCardApplication(cardApplication);
	    sign.setDIDName(didName);
	    sign.setDIDScope(DIDScopeType.LOCAL);
	    SignResponse signResponse = instance.sign(sign);
	    assertEquals(ECardConstants.Major.OK, signResponse.getResult().getResultMajor());
	    WSHelper.checkResult(signResponse);

	    byte[] signature = signResponse.getSignature();

	    VerifySignature verifySignature = new VerifySignature();
	    verifySignature.setConnectionHandle(sign.getConnectionHandle());
	    verifySignature.setDIDName(didName);
	    verifySignature.setDIDScope(DIDScopeType.LOCAL);
	    verifySignature.setMessage(message);
	    verifySignature.setSignature(signature);
	    VerifySignatureResponse verifySignatureResponse = instance.verifySignature(verifySignature);
	    WSHelper.checkResult(verifySignatureResponse);
	}
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
