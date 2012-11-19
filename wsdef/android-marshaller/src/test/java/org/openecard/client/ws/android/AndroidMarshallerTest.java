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

package org.openecard.client.ws.android;

import de.bund.bsi.ecard.api._1.InitializeFramework;
import de.bund.bsi.ecard.api._1.InitializeFrameworkResponse;
import de.bund.bsi.ecard.api._1.InitializeFrameworkResponse.Version;
import iso.std.iso_iec._24727.tech.schema.*;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import javax.xml.bind.JAXB;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import oasis.names.tc.dss._1_0.core.schema.InternationalStringType;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.anytype.AuthDataMap;
import org.openecard.client.common.sal.anytype.CryptoMarkerType;
import org.openecard.client.common.util.StringUtils;
import org.openecard.client.ws.WSMarshaller;
import org.openecard.client.ws.soap.SOAPHeader;
import org.openecard.client.ws.soap.SOAPMessage;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AndroidMarshallerTest {

    private static final String getRecognitionTreeResponseXML;
    private static final String establishContextXML;
    private static final String getStatusResponse;
    private static final String conclusion;
    private static final String initializeFramework;
    private static final String startPAOSResponse;
    private static final String didAuthenticatePACE;
    private static final String didAuthenticateTA;
    private static final String didAuthenticateCA;
    private static final String didAuthenticateResponse;
    private static final String npaCif;
    private static final String egkCif;
    private static final String disconnect;
    private static final String disconnectResponse;
    private static final String destroyChannel;
    private static final String startPAOS;
    private static final String transmit;

    static {
	try {
	    getRecognitionTreeResponseXML = loadXML("GetRecognitionTreeResponse.xml");
	    establishContextXML = loadXML("EstablishContext.xml");
	    getStatusResponse = loadXML("GetStatusResponse.xml");
	    conclusion = loadXML("Conclusion.xml");
	    initializeFramework = loadXML("InitializeFramework.xml");
	    startPAOSResponse = loadXML("StartPAOSResponse.xml");
	    didAuthenticatePACE = loadXML("DIDAuthenticatePACE.xml");
	    didAuthenticateTA = loadXML("DIDAuthenticateTA.xml");
	    didAuthenticateCA = loadXML("DIDAuthenticateCA.xml");
	    didAuthenticateResponse = loadXML("DIDAuthenticateResponse.xml");
	    npaCif = loadXML("nPA_1-0-0.xml");
	    egkCif = loadXML("eGK_1-0-0.xml");
	    disconnect = loadXML("Disconnect.xml");
	    disconnectResponse = loadXML("DisconnectResponse.xml");
	    destroyChannel = loadXML("DestroyChannel.xml");
	    startPAOS = loadXML("StartPAOS.xml");
	    transmit = loadXML("Transmit.xml");
	} catch (IOException ex) {
	    throw new RuntimeException(ex);
	}
    }

    private static String loadXML(String resourcePath) throws IOException {
	InputStream in = AndroidMarshallerTest.class.getClassLoader().getResourceAsStream(resourcePath);
	StringWriter w = new StringWriter();
	BufferedReader r = new BufferedReader(new InputStreamReader(in, Charset.forName("utf-8")));
	String nextLine;
	while ((nextLine = r.readLine()) != null) {
	    w.write(nextLine);
	    w.write(String.format("%n")); // platform dependant newline
					  // character
	}
	return w.toString();
    }

    @Test
    public void testConversionOfCardInfo() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Object o = m.unmarshal(m.str2doc(npaCif));
	if (!(o instanceof CardInfo)) {
	    throw new Exception("Object should be an instace of CardInfo");
	}
	CardInfo cardInfo = (CardInfo) o;
	assertEquals("http://bsi.bund.de/cif/npa.xml", cardInfo.getCardType().getObjectIdentifier());
	assertEquals(new byte[] { 0x3F, 0x00 }, cardInfo.getApplicationCapabilities().getImplicitlySelectedApplication());
	assertEquals(cardInfo.getApplicationCapabilities().getCardApplication().size(), 3);
	assertEquals(cardInfo.getApplicationCapabilities().getCardApplication().get(0).getApplicationName(), "MF");
	assertEquals(cardInfo.getApplicationCapabilities().getCardApplication().get(0).getRequirementLevel(), BasicRequirementsType.PERSONALIZATION_MANDATORY);
	assertEquals(cardInfo.getApplicationCapabilities().getCardApplication().get(0).getCardApplicationACL().getAccessRule().size(), 40);
	assertEquals(cardInfo.getApplicationCapabilities().getCardApplication().get(0).getCardApplicationACL().getAccessRule().get(0).getCardApplicationServiceName(), "CardApplicationServiceAccess");
	assertEquals(cardInfo.getApplicationCapabilities().getCardApplication().get(0).getCardApplicationACL().getAccessRule().get(0).getAction().getAPIAccessEntryPoint(), APIAccessEntryPointName.INITIALIZE);
	assertTrue(cardInfo.getApplicationCapabilities().getCardApplication().get(0).getCardApplicationACL().getAccessRule().get(0).getSecurityCondition().isAlways());

	// last accessrule
	assertEquals(cardInfo.getApplicationCapabilities().getCardApplication().get(0).getCardApplicationACL().getAccessRule().get(39).getAction().getAuthorizationServiceAction(), AuthorizationServiceActionName.ACL_MODIFY);
	assertFalse(cardInfo.getApplicationCapabilities().getCardApplication().get(0).getCardApplicationACL().getAccessRule().get(39).getSecurityCondition().isNever());

	assertEquals(cardInfo.getApplicationCapabilities().getCardApplication().get(0).getDIDInfo().get(0).getRequirementLevel(), BasicRequirementsType.PERSONALIZATION_MANDATORY);
	assertEquals(cardInfo.getApplicationCapabilities().getCardApplication().get(0).getDIDInfo().get(0).getDIDACL().getAccessRule().get(0).getCardApplicationServiceName(), "DifferentialIdentityService");

	assertEquals(cardInfo.getApplicationCapabilities().getCardApplication().get(1).getDataSetInfo().get(0).getRequirementLevel(), BasicRequirementsType.PERSONALIZATION_MANDATORY);
	assertEquals(cardInfo.getApplicationCapabilities().getCardApplication().get(1).getDataSetInfo().get(0).getDataSetACL().getAccessRule().get(0).getCardApplicationServiceName(), "NamedDataService");

	for(DataSetInfoType dataSetInfo : cardInfo.getApplicationCapabilities().getCardApplication().get(2).getDataSetInfo()) {
	    if(dataSetInfo.getDataSetName().equals("EF.C.ZDA.QES")){
		assertEquals(dataSetInfo.getLocalDataSetName().get(0).getLang(), "DE");
		assertEquals(dataSetInfo.getLocalDataSetName().get(0).getValue(), "Zertifikat des ZDA für die QES");
	    }
	}

	// Test eGK
	o = m.unmarshal(m.str2doc(egkCif));
	if (!(o instanceof CardInfo)) {
	    throw new Exception("Object should be an instace of CardInfo");
	}
	cardInfo = (CardInfo) o;
	assertEquals("http://ws.gematik.de/egk/1.0.0", cardInfo.getCardType().getObjectIdentifier());
	CardApplicationType cardApplicationESIGN = cardInfo.getApplicationCapabilities().getCardApplication().get(1);
	assertEquals(cardApplicationESIGN.getDIDInfo().get(0).getDifferentialIdentity().getDIDName(), "PrK.CH.AUT_signPKCS1_V1_5");
	assertEquals(cardApplicationESIGN.getDIDInfo().get(0).getDifferentialIdentity().getDIDProtocol(), "urn:oid:1.3.162.15480.3.0.25");
	CryptoMarkerType cryptoMarkerType = new CryptoMarkerType(cardApplicationESIGN.getDIDInfo().get(0).getDifferentialIdentity().getDIDMarker().getCryptoMarker());
	assertEquals(cryptoMarkerType.getProtocol(), "urn:oid:1.3.162.15480.3.0.25");
	assertEquals(cryptoMarkerType.getAlgorithmInfo().getSupportedOperations().get(0), "Compute-signature");
		
    }

    @Test
    public void testConversionOfEstablishContext() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	EstablishContext establishContext = new EstablishContext();
	Document d = m.marshal(establishContext);
	String s = m.doc2str(d);

	assertEquals(s.trim(), establishContextXML.trim());

	Object o = m.unmarshal(d);
	if (!(o instanceof EstablishContext)) {
	    throw new Exception("Object should be an instace of EstablishContext");
	}
    }

    @Test
    public void testConversionOfDisconnect() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Object o = m.unmarshal(m.str2doc(disconnect));

	if (!(o instanceof Disconnect)) {
	    throw new Exception("Object should be an instace of EstablishContext");
	}

	Disconnect d = (Disconnect) o;
	assertEquals(d.getSlotHandle(), StringUtils.toByteArray("1D8EFC10F063FB6FE8A3BBF8D2E0CA5C"));
	assertEquals(d.getAction(), ActionType.EJECT);
    }

    @Test
    public void testConversionOfDisconnectResponse() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	DisconnectResponse dr = new DisconnectResponse();
	dr.setResult(WSHelper.makeResultOK());
	Document d = m.marshal(dr);
	assertEquals(m.doc2str(d), disconnectResponse);
    }

    @Test
    public void testConversionOfDestroyChannel() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	DestroyChannel destroy = new DestroyChannel();
	destroy.setSlotHandle(new byte[] { 0x0, 0x1, 0x2, 0x3, 0x4, 0x5 });
	Document d = m.marshal(destroy);
	assertEquals(m.doc2str(d), destroyChannel);
    }

    @Test
    public void testConversionOfTransmit() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Object o = m.unmarshal(m.str2doc(transmit));
	if (!(o instanceof Transmit)) {
	    throw new Exception("Object should be an instace of Transmit");
	}
	Transmit t = (Transmit) o;
	assertEquals(t.getSlotHandle(), StringUtils.toByteArray("7695F667EE2B53824F77544D861236DD"));
	assertEquals(t.getInputAPDUInfo().size(), 2);
	assertEquals(t.getInputAPDUInfo().get(0).getInputAPDU(), StringUtils.toByteArray("00A4040C06D27600000102"));
	assertEquals(t.getInputAPDUInfo().get(0).getAcceptableStatusCode().size(), 1);
	assertEquals(t.getInputAPDUInfo().get(0).getAcceptableStatusCode().get(0), StringUtils.toByteArray("9000"));
	assertEquals(t.getInputAPDUInfo().get(1).getInputAPDU(), StringUtils.toByteArray("00A4040C06D27600000103"));
	assertEquals(t.getInputAPDUInfo().get(1).getAcceptableStatusCode().size(), 2);
	assertEquals(t.getInputAPDUInfo().get(1).getAcceptableStatusCode().get(0), StringUtils.toByteArray("9000"));
	assertEquals(t.getInputAPDUInfo().get(1).getAcceptableStatusCode().get(1), StringUtils.toByteArray("6666"));
    }

    @Test
    public void testConversionOfStartPAOS() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	StartPAOS startP = new StartPAOS();
	startP.setSessionIdentifier("5ec5ebb1dd254f392e6ca33cf5bf");
	ConnectionHandleType connectionHandleType = new ConnectionHandleType();
	connectionHandleType.setContextHandle(new BigInteger("94D7439CE657561E7AE3D491FD71AC21F8BCBB5608BA61F5A0EA52269BC01250", 16).toByteArray());
	connectionHandleType.setSlotHandle(new BigInteger("EEB49368C1152BEC379DA59356D59039CA7757AC3EAF9430285F2CBB3DD6EDDD", 16).toByteArray());
	connectionHandleType.setIFDName("Name of IFD");
	connectionHandleType.setSlotIndex(new BigInteger("0"));
	connectionHandleType.setCardApplication(new byte[] { 0x0, 0x0, 0x0 });
	ChannelHandleType channelHandle = new ChannelHandleType();
	channelHandle.setSessionIdentifier("sessionID");
	connectionHandleType.setChannelHandle(channelHandle);
	RecognitionInfo recognitionInfo = new RecognitionInfo();
	recognitionInfo.setCardType("nPA_1-0-0.xml");
	connectionHandleType.setRecognitionInfo(recognitionInfo);
	startP.getConnectionHandle().add(connectionHandleType);

	Document d = m.marshal(startP);
	assertEquals(m.doc2str(d), startPAOS);
    }

    @Test
    public void testConversionOfStartPAOSResponse() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Object o = m.unmarshal(m.str2doc(startPAOSResponse));

	if (!(o instanceof StartPAOSResponse)) {
	    throw new Exception("Object should be an instace of StartPAOSResponse");
	}

	StartPAOSResponse stPaosResponse = (StartPAOSResponse) o;
	assertEquals(stPaosResponse.getResult().getResultMajor(), "http://www.bsi.bund.de/ecard/api/1.1/resultmajor#error");
	assertEquals(stPaosResponse.getResult().getResultMinor(), "http://www.bsi.bund.de/ecard/api/1.1/resultminor/dp#timeout");
	assertEquals(stPaosResponse.getResult().getResultMessage().getValue(), "WaitStartPAOS timeout");
	assertEquals(stPaosResponse.getResult().getResultMessage().getLang(), "en");
    }

    @Test
    public void testSOAP() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	StartPAOS startPAOS = new StartPAOS();
	startPAOS.setSessionIdentifier("5ec5ebb1dd254f392e6ca33cf5bf");
	ConnectionHandleType connectionHandleType = new ConnectionHandleType();
	connectionHandleType.setContextHandle(new BigInteger("94D7439CE657561E7AE3D491FD71AC21F8BCBB5608BA61F5A0EA52269BC01250", 16).toByteArray());
	connectionHandleType.setSlotHandle(new BigInteger("EEB49368C1152BEC379DA59356D59039CA7757AC3EAF9430285F2CBB3DD6EDDD", 16).toByteArray());

	startPAOS.getConnectionHandle().add(connectionHandleType);

	Document contentDoc = m.marshal(startPAOS);

	SOAPMessage msg = m.add2soap(contentDoc);
	SOAPHeader header = msg.getSOAPHeader();
	// fill header with paos stuff
	Element paos = header.addHeaderElement(new QName(ECardConstants.PAOS_VERSION_20, "PAOS"));
	paos.setAttributeNS(ECardConstants.SOAP_ENVELOPE, "actor", ECardConstants.ACTOR_NEXT);
	paos.setAttributeNS(ECardConstants.SOAP_ENVELOPE, "mustUnderstand", "1");
	Element version = header.addChildElement(paos, new QName(ECardConstants.PAOS_VERSION_20, "Version"));
	version.setTextContent(ECardConstants.PAOS_VERSION_20);

	Element endpointReference = header.addChildElement(paos, new QName(ECardConstants.PAOS_VERSION_20, "EndpointReference"));
	Element address = header.addChildElement(endpointReference, new QName(ECardConstants.PAOS_VERSION_20, "Address"));
	address.setTextContent("http://www.projectliberty.org/2006/01/role/paos");
	Element metaData = header.addChildElement(endpointReference, new QName(ECardConstants.PAOS_VERSION_20, "MetaData"));
	Element serviceType = header.addChildElement(metaData, new QName(ECardConstants.PAOS_VERSION_20, "ServiceType"));
	serviceType.setTextContent("http://www.bsi.bund.de/ecard/api/1.0/PAOS/GetNextCommand");

	// add message ids
	SOAPHeader h = msg.getSOAPHeader();
	Element elem = h.addHeaderElement(new QName(ECardConstants.WS_ADDRESSING, "RelatesTo"));

	elem.setTextContent("relatesto");

	elem = h.addHeaderElement(new QName(ECardConstants.WS_ADDRESSING, "MessageID"));

	elem.setTextContent("messageid");

	String responseStr = m.doc2str(msg.getDocument());
	System.out.println(responseStr);

    }

    @Test
    public void testConversionOfInternationalStringType() throws Exception {
	InternationalStringType internationalStringType = new InternationalStringType();
	internationalStringType.setLang("lang");
	internationalStringType.setValue("value");

	WSMarshaller m = new AndroidMarshaller();
	Document d = m.marshal(internationalStringType);

	String s = m.doc2str(d);
	System.out.println(s);
    }

    @Test
    public void testConversionOfResult() throws Exception {
	Result r = new Result();
	r.setResultMajor("major");
	r.setResultMinor("minor");
	InternationalStringType internationalStringType = new InternationalStringType();
	internationalStringType.setLang("lang");
	internationalStringType.setValue("value");
	r.setResultMessage(internationalStringType);

	WSMarshaller m = new AndroidMarshaller();
	Document d = m.marshal(r);

	String s = m.doc2str(d);
	System.out.println(s);
    }

    @Test
    public void testConversionOfEstablishChannelResponse() throws Exception {
	EstablishChannelResponse establishChannelResponse = new EstablishChannelResponse();
	Result r = new Result();
	r.setResultMajor("major");
	r.setResultMinor("minor");
	InternationalStringType internationalStringType = new InternationalStringType();
	internationalStringType.setLang("en");
	internationalStringType.setValue("message");
	r.setResultMessage(internationalStringType);
	establishChannelResponse.setResult(r);

    }

    @Test
    public void testConversionOfEstablishChannel() throws Exception {
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	factory.setNamespaceAware(true);
	DocumentBuilder builder = factory.newDocumentBuilder();
	Document d = builder.newDocument();
	EstablishChannel establishChannel = new EstablishChannel();
	establishChannel.setSlotHandle(new byte[] { 0x0, 0x1, 0x02 });
	DIDAuthenticationDataType establishChannelInput = new DIDAuthenticationDataType();
	establishChannelInput.setProtocol(ECardConstants.Protocol.PACE);
	Element e = d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "PinID");
	e.setTextContent("3"); // Personalausweis-PIN
	establishChannelInput.getAny().add(e);

	e = d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "PIN");
	e.setTextContent("123456"); // Personalausweis-PIN
	establishChannelInput.getAny().add(e);
	establishChannel.setAuthenticationProtocolData(establishChannelInput);

	JAXB.marshal(establishChannel, System.out);
	WSMarshaller m = new AndroidMarshaller();
	Document doc = m.marshal(establishChannel);

	String s = m.doc2str(doc);
	System.out.println(s);
    }

    @Test
    public void testConversionOfDIDAuthenticateResponseCA() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	DIDAuthenticateResponse didAuthenticateResponse = new DIDAuthenticateResponse();
	Result r = new Result();
	r.setResultMajor("major");
	r.setResultMinor("minor");
	InternationalStringType internationalStringType = new InternationalStringType();
	internationalStringType.setLang("en");
	internationalStringType.setValue("message");
	r.setResultMessage(internationalStringType);
	didAuthenticateResponse.setResult(r);

	EAC2OutputType didAuthenticationDataType = new EAC2OutputType();
	didAuthenticationDataType.setProtocol("urn:....");

	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	factory.setNamespaceAware(true);
	DocumentBuilder builder = factory.newDocumentBuilder();
	Document d = builder.newDocument();

	Element e = d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "Signature");
	e.setTextContent("7117D7BF95D8D6BD437A0D43DE48F42528273A98F2605758D6A3A2BFC38141E7577CABB4F8FBC8DF152E3A097D1B3A703597331842425FE4A9D0F1C9067AC4A9");
	didAuthenticationDataType.getAny().add(e);

	didAuthenticateResponse.setAuthenticationProtocolData(didAuthenticationDataType);

	JAXB.marshal(didAuthenticateResponse, System.out);

	Document doc = m.marshal(didAuthenticateResponse);

	String s = m.doc2str(doc);
	System.out.println(s);
	StringReader sr = new StringReader(s);
	DIDAuthenticateResponse didaresp = JAXB.unmarshal(sr, DIDAuthenticateResponse.class);

	JAXB.marshal(didaresp, System.out);
    }

    @Test
    public void testConversionOfDIDAuthenticateResponseTA() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	DIDAuthenticateResponse didAuthenticateResponse = new DIDAuthenticateResponse();
	Result r = new Result();
	r.setResultMajor("major");
	r.setResultMinor("minor");
	InternationalStringType internationalStringType = new InternationalStringType();
	internationalStringType.setLang("en");
	internationalStringType.setValue("message");
	r.setResultMessage(internationalStringType);
	didAuthenticateResponse.setResult(r);

	EAC2OutputType didAuthenticationDataType = new EAC2OutputType();

	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	factory.setNamespaceAware(true);
	DocumentBuilder builder = factory.newDocumentBuilder();
	Document d = builder.newDocument();

	Element e = d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "Challenge");
	e.setTextContent("1331F2B1571E6DC2");
	didAuthenticationDataType.getAny().add(e);

	didAuthenticateResponse.setAuthenticationProtocolData(didAuthenticationDataType);

	JAXB.marshal(didAuthenticateResponse, System.out);

	Document doc = m.marshal(didAuthenticateResponse);

	String s = m.doc2str(doc);
	System.out.println(s);
	StringReader sr = new StringReader(s);
	DIDAuthenticateResponse didaresp = JAXB.unmarshal(sr, DIDAuthenticateResponse.class);

	JAXB.marshal(didaresp, System.out);
    }

    @Test
    public void testConversionOfDIDAuthenticateResponsePACE() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	DIDAuthenticateResponse didAuthenticateResponse = new DIDAuthenticateResponse();
	Result r = new Result();
	r.setResultMajor("major");
	r.setResultMinor("minor");
	InternationalStringType internationalStringType = new InternationalStringType();
	internationalStringType.setLang("en");
	internationalStringType.setValue("message");
	r.setResultMessage(internationalStringType);
	didAuthenticateResponse.setResult(r);

	EAC1OutputType didAuthenticationDataType = new EAC1OutputType();

	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	factory.setNamespaceAware(true);
	DocumentBuilder builder = factory.newDocumentBuilder();
	Document d = builder.newDocument();

	Element e = d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "RetryCounter");
	e.setTextContent("3");
	didAuthenticationDataType.getAny().add(e);

	e = d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "CertificateHolderAuthorizationTemplate");
	e.setTextContent("7F4C12060904007F00070301020253050001009800");
	didAuthenticationDataType.getAny().add(e);

	e = d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "CertificationAuthorityReference");
	e.setTextContent("ZZCVCAATA0001");
	didAuthenticationDataType.getAny().add(e);

	e = d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "EFCardAccess");
	e.setTextContent("31820264300D060804007F0007020202020102300F060A04007F00070202030202020102300F060A04007F00070202040202020101302F060804007F0007020206162341775420655041202D2042447220476D6248202D20546573746B617274652076312E303081FE060904007F0007020203023081F0060B04007F00070101050202023081E0020101302C06072A8648CE3D0101022100A9FB57DBA1EEA9BC3E660A909D838D726E3BF623D52620282013481D1F6E537730440420A9FB57DBA1EEA9BC3E660A909D838D726E3BF623D52620282013481D1F6E53740420662C61C430D84EA4FE66A7733D0B76B7BF93EBC4AF2F49256AE58101FEE92B04044104A3E8EB3CC1CFE7B7732213B23A656149AFA142C47AAFBC2B79A191562E1305F42D996C823439C56D7F7B22E14644417E69BCB6DE39D027001DABE8F35B25C9BE022100A9FB57DBA1EEA9BC3E660A909D838D718C397AA3B561A6F7901E0E82974856A70201013081FE060904007F0007020204023081F0060B04007F00070101050202023081E0020101302C06072A8648CE3D0101022100A9FB57DBA1EEA9BC3E660A909D838D726E3BF623D52620282013481D1F6E537730440420A9FB57DBA1EEA9BC3E660A909D838D726E3BF623D52620282013481D1F6E53740420662C61C430D84EA4FE66A7733D0B76B7BF93EBC4AF2F49256AE58101FEE92B04044104A3E8EB3CC1CFE7B7732213B23A656149AFA142C47AAFBC2B79A191562E1305F42D996C823439C56D7F7B22E14644417E69BCB6DE39D027001DABE8F35B25C9BE022100A9FB57DBA1EEA9BC3E660A909D838D718C397AA3B561A6F7901E0E82974856A7020101");
	didAuthenticationDataType.getAny().add(e);

	e = d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "IDPICC");
	e.setTextContent("4F5311EC8F92D60040EA63365E2B06C832856CDE1CE5F8B3C7E7696DAD7628BD");
	didAuthenticationDataType.getAny().add(e);

	didAuthenticateResponse.setAuthenticationProtocolData(didAuthenticationDataType);

	JAXB.marshal(didAuthenticateResponse, System.out);

	Document doc = m.marshal(didAuthenticateResponse);

	String s = m.doc2str(doc);
	System.out.println(s);
	StringReader sr = new StringReader(s);
	DIDAuthenticateResponse didaresp = JAXB.unmarshal(sr, DIDAuthenticateResponse.class);

	JAXB.marshal(didaresp, System.out);
    }

    @Test
    public void testConversionOfDIDAutheticate() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Document d = m.str2doc(didAuthenticatePACE);

	Object o = m.unmarshal(d);
	if (!(o instanceof DIDAuthenticate)) {
	    throw new Exception("Object should be an instace of DIDAuthenticate");
	}

	DIDAuthenticate didAuthenticate = (DIDAuthenticate) o;
	assertEquals(didAuthenticate.getDIDName(), "PIN");
	assertEquals(didAuthenticate.getConnectionHandle().getSlotHandle(), StringUtils.toByteArray("93F25BA574EF3F94F8AE42796DAF7C05"));
	assertEquals(EAC1InputType.class, didAuthenticate.getAuthenticationProtocolData().getClass());

	for (int i = 0; i < didAuthenticate.getAuthenticationProtocolData().getAny().size(); i++) {
	    if (didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getLocalName().equals("Certificate")) {
		assertEquals("7F218201487F4E8201005F2901004210444544566549444454523130313430317F494F060A04007F000702020202038641041994E6E55DD1F021180CC705C001FECB4BF5B7E978E8F148002D2D4FDBC1E57A159592681039A041E0036C007E784C36A372528C89365AAB77402E07EED6D4115F200E44453030303030303330303542387F4C12060904007F0007030102025305000100D8005F25060102000102065F2406010200010207655E732D060904007F0007030103028020B90F0EB18F30BCB878EE68924E413A2F5D5DE2F844030141EDB64383C3958C56732D060904007F00070301030180207B0E75C3F613B50011CCB5CF95704B91A6B4AC6EC100377C60BC312A2CE6BE1E5F3740A9A8B9829D2820F96FDB96D09303B01A61F09BC10C766581CAB2BBD2609EC32217C8FEB73F7CAB52CC3A0D16DC1B02F348A3049A246F3790B9687F1F72ACE722",
			didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getTextContent());
	    } else if (didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getLocalName().equals("CertificateDescription")) {
		assertEquals("3082032D060A04007F00070301030101A10E0C0C442D547275737420476D6248A2181316687474703A2F2F7777772E642D74727573742E6E6574A32F0C2D436F736D6F73204C6562656E73766572736963686572756E677320416B7469656E676573656C6C736368616674A41513137777772E636F736D6F73646972656B742E6465A58202410C82023D4E616D652C20416E7363687269667420756E6420452D4D61696C2D4164726573736520646573204469656E737465616E626965746572733A0D0A436F736D6F73204C6562656E73766572736963686572756E677320416B7469656E676573656C6C7363686166740D0A48616C6265726773747261C39F652035302D36300D0A363631303120536161726272C3BC636B656E0D0A696E666F40636F736D6F73646972656B742E64650D0A0D0A4765736368C3A46674737A7765636B3A0D0A2D2052656769737472696572756E67202F204C6F67696E2066C3BC7220226D65696E436F736D6F73446972656B7422202D0D0A0D0A48696E7765697320617566206469652066C3BC722064656E204469656E737465616E626965746572207A757374C3A46E646967656E205374656C6C656E2C20646965206469652045696E68616C74756E672064657220566F7273636872696674656E207A756D20446174656E73636875747A206B6F6E74726F6C6C696572656E3A0D0A4D696E697374657269756D2066C3BC7220496E6E6572657320756E64204575726F7061616E67656C6567656E68656974656E0D0A4D61696E7A65722053747261C39F65203133360D0A363631323120536161726272C3BC636B656E0D0A303638312035303120E280932030300D0A706F73747374656C6C6540696E6E656E2E736161726C616E642E64650D0A687474703A2F2F7777772E696E6E656E2E736161726C616E642E64650D0A416E737072656368706172746E65723A20526F6C616E64204C6F72656E7AA768316604202E15788858E56A91A459BB7086943A5A3AB879F88F72EEE72D5B8202B035943D04206D26166C2748B08BFC3AC0A37109C406A8D35317140F6C69C27C4AB77FDD21F80420805AD754568E472C4761D52D410FB99128AB4CE2D750FDA3A8DA8FBA67BB14EB",
			didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getTextContent());
	    } else if (didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getLocalName().equals("RequiredCHAT")) {
		assertEquals("7F4C12060904007F00070301020253050001009800", didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getTextContent());
	    } else if (didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getLocalName().equals("OptionalCHAT")) {
		assertEquals("7F4C12060904007F00070301020253050000004000", didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getTextContent());
	    } else if (didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getLocalName().equals("AuthenticatedAuxiliaryData")) {
		assertEquals("67177315060904007F00070301040253083230313230313236", didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getTextContent());
	    }
	}

	d = m.str2doc(didAuthenticateTA);

	o = m.unmarshal(d);
	if (!(o instanceof DIDAuthenticate)) {
	    throw new Exception("Object should be an instace of DIDAuthenticate");
	}

	didAuthenticate = (DIDAuthenticate) o;
	assertEquals(didAuthenticate.getDIDName(), "PIN");
	assertEquals(didAuthenticate.getConnectionHandle().getSlotHandle(), StringUtils.toByteArray("05D4F40AEBD9919383C22216055EA3DB15056C51"));
	assertEquals(EAC2InputType.class, didAuthenticate.getAuthenticationProtocolData().getClass());
	AuthDataMap eac2input = new AuthDataMap(didAuthenticate.getAuthenticationProtocolData());

	assertEquals(eac2input.getContentAsString("EphemeralPublicKey"), "8D44E99377DA28436D2F7E8620347D7C08B186B179633E3654842E940AB179B498F974970D990D47C61FE5D4D91EBB10831E824EC6F2600D89D6661CDF47F734");
	// assertEquals(eac2input.getContentAsString("Certificate"),
	// "7F2181E47F4E819D5F290100420D5A5A43564341415441303030317F494F060A04007F0007020202020386410452DD32EAFE1FBBB4000CD9CE75F66636CFCF1EDD44F7B1EDAE25B84193DA04A91C77EE87F5C8F959ED276200DE33AB574CE9801135FF4497A37162B7C8548A0C5F200E5A5A4456434141544130303030357F4C12060904007F0007030102025305700301FFB75F25060100000601015F24060100010003015F37406F13AE9A6F4EDDB7839FF3F04D71E0DC377BC4B08FAD295EED241B524328AD0730EB553497B4FB66E9BB7AB90815F04273F09E751D7FD4B861439B4EE65381C3");
	assertEquals(eac2input.getContentAsString("Certificate"),
		"7F218201427F4E81FB5F290100420E5A5A4456434141544130303030357F494F060A04007F0007020202020386410470C07FAA329E927D961F490F5430B395EECF3D2A538194D8B637DE0F8ACF60A9031816AC51B594097EB211FB8F55FAA8507D5800EF7B94E024F9630314116C755F200B5A5A444B423230303033557F4C12060904007F0007030102025305000301DF045F25060100000601085F2406010000070001655E732D060904007F00070301030280207C1901932DB75D08539F2D4A27C938F79E69E083C442C068B299D185BC8AFA78732D060904007F0007030103018020BFD2A6A2E4237948D7DCCF7975D71D40F15307AA59F580A48777CBEED093F54B5F3740618F584E4293F75DDE8977311694B69A3ED73BBE43FDAFEC11B7ECF054F84ACB1231615338CE8D6EC332480883E14E0664950F85134290DD716B7C153232BC96");

	JAXB.marshal(didAuthenticate, System.out);

	d = m.str2doc(didAuthenticateCA);

	o = m.unmarshal(d);
	if (!(o instanceof DIDAuthenticate)) {
	    throw new Exception("Object should be an instace of DIDAuthenticate");
	}

	didAuthenticate = (DIDAuthenticate) o;
	assertEquals(didAuthenticate.getDIDName(), "PIN");
	assertEquals(didAuthenticate.getConnectionHandle().getSlotHandle(), StringUtils.toByteArray("05D4F40AEBD9919383C22216055EA3DB15056C51"));
	assertEquals(EACAdditionalInputType.class, didAuthenticate.getAuthenticationProtocolData().getClass());
	AuthDataMap eacadditionalinput = new AuthDataMap(didAuthenticate.getAuthenticationProtocolData());

	assertEquals(eacadditionalinput.getContentAsString("Signature"), "7117D7BF95D8D6BD437A0D43DE48F42528273A98F2605758D6A3A2BFC38141E7577CABB4F8FBC8DF152E3A097D1B3A703597331842425FE4A9D0F1C9067AC4A9");
	// assertEquals(eac2input.getContentAsString("Certificate"),
	// "7F2181E47F4E819D5F290100420D5A5A43564341415441303030317F494F060A04007F0007020202020386410452DD32EAFE1FBBB4000CD9CE75F66636CFCF1EDD44F7B1EDAE25B84193DA04A91C77EE87F5C8F959ED276200DE33AB574CE9801135FF4497A37162B7C8548A0C5F200E5A5A4456434141544130303030357F4C12060904007F0007030102025305700301FFB75F25060100000601015F24060100010003015F37406F13AE9A6F4EDDB7839FF3F04D71E0DC377BC4B08FAD295EED241B524328AD0730EB553497B4FB66E9BB7AB90815F04273F09E751D7FD4B861439B4EE65381C3");

	JAXB.marshal(didAuthenticate, System.out);
    }

    @Test
    public void testConversionOfInitializeFrameworkResponse() throws Exception {
	InitializeFrameworkResponse initializeFrameworkResponse = new InitializeFrameworkResponse();
	Version version = new Version();
	version.setMajor(new BigInteger("11"));
	version.setMinor(new BigInteger("22"));
	version.setSubMinor(new BigInteger("33"));

	initializeFrameworkResponse.setVersion(version);

	Result r = new Result();
	r.setResultMajor("major");
	r.setResultMinor("minor");
	InternationalStringType internationalStringType = new InternationalStringType();
	internationalStringType.setLang("lang");
	internationalStringType.setValue("value");
	r.setResultMessage(internationalStringType);
	initializeFrameworkResponse.setResult(r);

	JAXB.marshal(initializeFrameworkResponse, System.out);
	WSMarshaller m = new AndroidMarshaller();
	Document d = m.marshal(initializeFrameworkResponse);

	String s = m.doc2str(d);
	System.out.println(s);
    }

    @Test
    public void testConversionOfInitializeFramework() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Document d = m.str2doc(initializeFramework);

	Object o = m.unmarshal(d);
	if (!(o instanceof InitializeFramework)) {
	    throw new Exception("Object should be an instace of InitializeFramework");
	}
    }

    @Test
    public void testConversionOfGetRecognitionTreeResponse() throws Exception {
	WSMarshaller m = new AndroidMarshaller();

	Object o = m.unmarshal(m.str2doc(getRecognitionTreeResponseXML));
	if (o instanceof GetRecognitionTreeResponse) {
	    RecognitionTree tree = ((GetRecognitionTreeResponse) o).getRecognitionTree();
	    StringWriter sw = new StringWriter();

	    JAXB.marshal(tree, sw);

	    Document d = m.marshal(tree);

	    String s = m.doc2str(d);
	    System.out.println(s);
	} else {
	    throw new Exception("Object should be an instace of GetRecognitionTreeResponse");
	}
    }

    @Test
    public void testConversionOfConclusion() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Object o = m.unmarshal(m.str2doc(conclusion));
	Conclusion c = (Conclusion) o;
	assertEquals("http://ws.gematik.de/egk/1.0.0", c.getRecognizedCardType());
	Conclusion cc = JAXB.unmarshal(new StringReader(conclusion), Conclusion.class);
	// TODO
	// assertEquals(c.getTLSMarker().getAny().get(0),
	// cc.getTLSMarker().getAny().get(0));
    }

    @Test
    public void testConversionOfGetStatus() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	GetStatus getStatus = new GetStatus();
	getStatus.setIFDName("ifdName");
	getStatus.setContextHandle(new byte[] { 0x0, 0x1, 0x2 });

	Document d = m.marshal(getStatus);

	String s = m.doc2str(d);
	System.out.println(s);
    }

    @Test
    public void testConversionOfGetStatusResponse() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Object o = m.unmarshal(m.str2doc(getStatusResponse));
	if (!(o instanceof GetStatusResponse)) {
	    throw new Exception("Object should be an instace of GetStatusResponse");
	}
    }

    @Test
    public void testConversionOfWait() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Wait w = new Wait();
	w.setContextHandle(new byte[] { 0x0, 0x1, 0x2 });
	w.setTimeOut(new BigInteger("123"));
	ChannelHandleType channelHandleType = new ChannelHandleType();
	channelHandleType.setBinding("binding");
	channelHandleType.setProtocolTerminationPoint("protocolterminatiopoint");
	channelHandleType.setSessionIdentifier("sessionidentifier");
	PathSecurityType pathSecurityType = new PathSecurityType();
	pathSecurityType.setParameters("omg");
	pathSecurityType.setProtocol("protocol");
	channelHandleType.setPathSecurity(pathSecurityType);
	w.setCallback(channelHandleType);

	Document d = m.marshal(w);

	String s = m.doc2str(d);
	System.out.println(s);
    }

}
