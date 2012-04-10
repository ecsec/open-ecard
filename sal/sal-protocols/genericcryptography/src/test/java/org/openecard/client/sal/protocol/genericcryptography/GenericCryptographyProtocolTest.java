/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg
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

package org.openecard.client.sal.protocol.genericcryptography;

import static org.junit.Assert.assertEquals;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectResponse;
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
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import iso.std.iso_iec._24727.tech.schema.PinCompareDIDAuthenticateInputType;
import iso.std.iso_iec._24727.tech.schema.Sign;
import iso.std.iso_iec._24727.tech.schema.SignResponse;
import java.math.BigInteger;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openecard.bouncycastle.util.encoders.Hex;
import org.openecard.client.common.ClientEnv;
import org.openecard.client.common.ECardConstants;
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


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
@Ignore
public class GenericCryptographyProtocolTest {

    private static ClientEnv env;
    private static TinySAL instance;
    private static CardStateMap states;
    byte[] cardApplication = Hex.decode("A000000167455349474E");

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
    public void testSign() {
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

	DIDAuthenticate didAthenticate = new DIDAuthenticate();
	didAthenticate.setDIDName("PIN.home");
	PinCompareDIDAuthenticateInputType didAuthenticationData = new PinCompareDIDAuthenticateInputType();
	didAthenticate.setAuthenticationProtocolData(didAuthenticationData);
	didAthenticate.setConnectionHandle(result.getConnectionHandle());
	didAuthenticationData.setProtocol(ECardConstants.Protocol.PIN_COMPARE);
	didAthenticate.setAuthenticationProtocolData(didAuthenticationData);
	DIDAuthenticateResponse didAuthenticateResult = instance.didAuthenticate(didAthenticate);

	assertEquals(didAuthenticateResult.getAuthenticationProtocolData().getProtocol(), ECardConstants.Protocol.PIN_COMPARE);
	assertEquals(didAuthenticateResult.getAuthenticationProtocolData().getAny().size(), 0);
	assertEquals(ECardConstants.Major.OK, didAuthenticateResult.getResult().getResultMajor());

	Sign sign = new Sign();
	sign.setMessage(new byte[] { 0x0, 0x0, 0x0 });
	sign.setConnectionHandle(result.getConnectionHandle());
	sign.setDIDName(didListResponse.getDIDNameList().getDIDName().get(0));
	sign.setDIDScope(DIDScopeType.LOCAL);
	SignResponse signResponse = instance.sign(sign);
	assertEquals(ECardConstants.Major.OK, signResponse.getResult().getResultMajor());

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
