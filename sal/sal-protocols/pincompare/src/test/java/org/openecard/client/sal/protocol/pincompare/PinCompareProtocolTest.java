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

package org.openecard.client.sal.protocol.pincompare;

import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectResponse;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.Encipher;
import iso.std.iso_iec._24727.tech.schema.EncipherResponse;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import java.math.BigInteger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
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
import org.openecard.client.gui.swing.SwingDialogWrapper;
import org.openecard.client.gui.swing.SwingUserConsent;
import org.openecard.client.ifd.scio.IFD;
import org.openecard.client.recognition.CardRecognition;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.transport.dispatcher.MessageDispatcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
@Ignore
public class PinCompareProtocolTest {

    private static ClientEnv env;
    private static TinySAL instance;
    private static CardStateMap states;
    byte[] appIdentifier_ROOT = Hex.decode("D2760001448000");

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
	instance.addProtocol(ECardConstants.Protocol.PIN_COMPARE, new PinCompareProtocolFactory());
    }

    @Test
    public void testDIDAuthenticate() throws ParserConfigurationException {

	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(this.appIdentifier_ROOT);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);
	CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	cardApplicationPathType = cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0);
	cardApplicationConnect.setCardApplicationPath(cardApplicationPathType);
	CardApplicationConnectResponse result1 = instance.cardApplicationConnect(cardApplicationConnect);

	DIDAuthenticate parameters = new DIDAuthenticate();
	parameters.setDIDName("PIN.home");
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	factory.setNamespaceAware(true);
	DocumentBuilder builder = factory.newDocumentBuilder();
	Document d = builder.newDocument();
	//FIXME user is always asked for pin no matter if this element exists
	Element elemPin = d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "Pin");
	elemPin.setTextContent("123456");
	DIDAuthenticationDataType didAuthenticationData = new DIDAuthenticationDataType();
	didAuthenticationData.getAny().add(elemPin);
	parameters.setAuthenticationProtocolData(didAuthenticationData);
	parameters.setConnectionHandle(result1.getConnectionHandle());
	didAuthenticationData.setProtocol(ECardConstants.Protocol.PIN_COMPARE);
	parameters.setAuthenticationProtocolData(didAuthenticationData);
	DIDAuthenticateResponse result = instance.didAuthenticate(parameters);

	assertEquals(result.getAuthenticationProtocolData().getProtocol(), ECardConstants.Protocol.PIN_COMPARE);
	assertEquals(result.getAuthenticationProtocolData().getAny().size(), 0);
	assertEquals(ECardConstants.Major.OK, result.getResult().getResultMajor());

    }

    @Test
    public void testDIDCreate(){
	//TODO
    }

    @Test
    public void testDIDUpdate(){
	//TODO
    }

    @Test
    public void testDIDGet(){
	//TODO
    }

    /*
     * [TR-03112-7] The following functions are not supported with this protocol and, when
     * called up, relay an error message to this effect
     * /resultminor/sal#inappropriateProtocolForAction:
     * CardApplicationStartSession, Encipher, Decipher, GetRandom, Hash, Sign,
     * VerifySignature, VerifyCertificate
     */
    @Test
    public void testUnsupportedFunctions(){

	CardApplicationPath cardApplicationPath = new CardApplicationPath();
	CardApplicationPathType cardApplicationPathType = new CardApplicationPathType();
	cardApplicationPathType.setCardApplication(this.appIdentifier_ROOT);
	cardApplicationPath.setCardAppPathRequest(cardApplicationPathType);
	CardApplicationPathResponse cardApplicationPathResponse = instance.cardApplicationPath(cardApplicationPath);
	CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
	cardApplicationPathType = cardApplicationPathResponse.getCardAppPathResultSet().getCardApplicationPathResult().get(0);
	cardApplicationConnect.setCardApplicationPath(cardApplicationPathType);
	CardApplicationConnectResponse result1 = instance.cardApplicationConnect(cardApplicationConnect);

	Encipher encipher = new Encipher();
	encipher.setDIDName("PIN.home");
	encipher.setPlainText(new byte[]{0x0, 0x0, 0x0});
	encipher.setConnectionHandle(result1.getConnectionHandle());
	EncipherResponse encipherResponse = instance.encipher(encipher);
	assertEquals(ECardConstants.Major.ERROR, encipherResponse.getResult().getResultMajor());
	assertEquals(ECardConstants.Minor.SAL.INAPPROPRIATE_PROTOCOL_FOR_ACTION, encipherResponse.getResult().getResultMinor());
	//TODO remaining unsupported functions
    }

}
