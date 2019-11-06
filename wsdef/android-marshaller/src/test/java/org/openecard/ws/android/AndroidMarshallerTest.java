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

package org.openecard.ws.android;

import de.bund.bsi.ecard.api._1.InitializeFramework;
import de.bund.bsi.ecard.api._1.InitializeFrameworkResponse;
import de.bund.bsi.ecard.api._1.InitializeFrameworkResponse.Version;
import iso.std.iso_iec._24727.tech.schema.APIAccessEntryPointName;
import iso.std.iso_iec._24727.tech.schema.ActionType;
import iso.std.iso_iec._24727.tech.schema.AuthorizationServiceActionName;
import iso.std.iso_iec._24727.tech.schema.BasicRequirementsType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationType;
import iso.std.iso_iec._24727.tech.schema.CardInfo;
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.Conclusion;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import iso.std.iso_iec._24727.tech.schema.CreateSession;
import iso.std.iso_iec._24727.tech.schema.CreateSessionResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.DIDInfoType;
import iso.std.iso_iec._24727.tech.schema.DataSetInfoType;
import iso.std.iso_iec._24727.tech.schema.DestroyChannel;
import iso.std.iso_iec._24727.tech.schema.DestroySession;
import iso.std.iso_iec._24727.tech.schema.DestroySessionResponse;
import iso.std.iso_iec._24727.tech.schema.DifferentialIdentityType;
import iso.std.iso_iec._24727.tech.schema.Disconnect;
import iso.std.iso_iec._24727.tech.schema.DisconnectResponse;
import iso.std.iso_iec._24727.tech.schema.EAC1InputType;
import iso.std.iso_iec._24727.tech.schema.EAC1OutputType;
import iso.std.iso_iec._24727.tech.schema.EAC2InputType;
import iso.std.iso_iec._24727.tech.schema.EAC2OutputType;
import iso.std.iso_iec._24727.tech.schema.EACAdditionalInputType;
import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.GetRecognitionTreeResponse;
import iso.std.iso_iec._24727.tech.schema.GetStatus;
import iso.std.iso_iec._24727.tech.schema.GetStatusResponse;
import iso.std.iso_iec._24727.tech.schema.PathSecurityType;
import iso.std.iso_iec._24727.tech.schema.RecognitionTree;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.Wait;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.List;
import javax.xml.bind.JAXB;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import oasis.names.tc.dss._1_0.core.schema.InternationalStringType;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.common.anytype.AuthDataMap;
import org.openecard.common.util.StringUtils;
import org.openecard.crypto.common.sal.did.CryptoMarkerType;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.soap.SOAPHeader;
import org.openecard.ws.soap.SOAPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


/**
 *
 * @author Dirk Petrautzki
 */
public class AndroidMarshallerTest {

    private static final Logger LOG = LoggerFactory.getLogger(AndroidMarshallerTest.class);

    private static final String RECOGNITION_TREE_RESPONSE_XML;
    private static final String ESTABLISH_CONTEXT_XML;
    private static final String GET_STATUS_RESPONSE;
    private static final String CONCLUSION;
    private static final String INITIALIZE_FRAMEWORK;
    private static final String START_PAOS_RESPONSE;
    private static final String DID_AUTHENTICATE_PACE;
    private static final String DID_AUTHENTICATE_TA;
    private static final String DID_AUTHENTICATE_CA;
    private static final String DID_AUTHENTICATE_RESPONSE;
    private static final String NPA_CIF;
    private static final String EGK_CIF;
    private static final String ECARD_AT_CIF;
    private static final String DISCONNECT;
    private static final String DISCONNECT_RESPONSE;
    private static final String DESTROY_CHANNEL;
    private static final String START_PAOS;
    private static final String TRANSMIT;
    private static final String CREATE_SESSION;
    private static final String CREATE_SESSION_RESP;
    private static final String DESTROY_SESSION;
    private static final String DESTROY_SESSION_RESP;

    private static final String TEST_ADDON_BUNDDLE_DESCRIPTION;

    static {
	try {
	    RECOGNITION_TREE_RESPONSE_XML = loadXML("GetRecognitionTreeResponse.xml");
	    ESTABLISH_CONTEXT_XML = loadXML("EstablishContext.xml");
	    GET_STATUS_RESPONSE = loadXML("GetStatusResponse.xml");
	    CONCLUSION = loadXML("Conclusion.xml");
	    INITIALIZE_FRAMEWORK = loadXML("InitializeFramework.xml");
	    START_PAOS_RESPONSE = loadXML("StartPAOSResponse.xml");
	    DID_AUTHENTICATE_PACE = loadXML("DIDAuthenticatePACE.xml");
	    DID_AUTHENTICATE_TA = loadXML("DIDAuthenticateTA.xml");
	    DID_AUTHENTICATE_CA = loadXML("DIDAuthenticateCA.xml");
	    DID_AUTHENTICATE_RESPONSE = loadXML("DIDAuthenticateResponse.xml");
	    NPA_CIF = loadXML("nPA_1-0-0.xml");
	    EGK_CIF = loadXML("eGK_1-0-0.xml");
	    ECARD_AT_CIF = loadXML("ecardAT_0-9-0.xml");
	    DISCONNECT = loadXML("Disconnect.xml");
	    DISCONNECT_RESPONSE = loadXML("DisconnectResponse.xml");
	    DESTROY_CHANNEL = loadXML("DestroyChannel.xml");
	    START_PAOS = loadXML("StartPAOS.xml");
	    TRANSMIT = loadXML("Transmit.xml");
	    CREATE_SESSION = loadXML("CreateSession.xml");
	    CREATE_SESSION_RESP = loadXML("CreateSessionResponse.xml");
	    DESTROY_SESSION = loadXML("DestroySession.xml");
	    DESTROY_SESSION_RESP = loadXML("DestroySessionResponse.xml");
	    TEST_ADDON_BUNDDLE_DESCRIPTION = loadXML("TestAddonBundleDescription.xml");

	} catch (IOException ex) {
	    throw new RuntimeException(ex);
	}
    }

    private static void marshalLog(Object o) {
	StringWriter w = new StringWriter();
	JAXB.marshal(o, w);
	LOG.debug(w.toString());
    }

    private static String loadXML(String resourcePath) throws IOException {
	InputStream in = AndroidMarshallerTest.class.getClassLoader().getResourceAsStream(resourcePath);
	StringWriter w = new StringWriter();
	BufferedReader r = new BufferedReader(new InputStreamReader(in, Charset.forName("utf-8")));
	String nextLine;
	while ((nextLine = r.readLine()) != null) {
	    w.write(nextLine);
	    w.write(String.format("%n")); // platform dependant newline character
	}
	return w.toString();
    }

    @Test
    public void testConversionOfCardInfoNPA_CIF() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Object o = m.unmarshal(m.str2doc(NPA_CIF));
	if (!(o instanceof CardInfo)) {
	    throw new Exception("Object should be an instance of CardInfo");
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

	for (DataSetInfoType dataSetInfo : cardInfo.getApplicationCapabilities().getCardApplication().get(2).getDataSetInfo()) {
	    if (dataSetInfo.getDataSetName().equals("EF.C.ZDA.QES")) {
		assertEquals(dataSetInfo.getLocalDataSetName().get(0).getLang(), "DE");
		assertEquals(dataSetInfo.getLocalDataSetName().get(0).getValue(), "Zertifikat des ZDA für die QES");
	    }
	}

    }

    @Test
    public void testConversionOfCardInfoEGK_CIF() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	// Test eGK
	Object o = m.unmarshal(m.str2doc(EGK_CIF));
	if (!(o instanceof CardInfo)) {
	    throw new Exception("Object should be an instace of CardInfo");
	}
	CardInfo cardInfo = (CardInfo) o;

	assertEquals("http://ws.gematik.de/egk/1.0.0", cardInfo.getCardType().getObjectIdentifier());
	CardApplicationType cardApplicationESIGN = cardInfo.getApplicationCapabilities().getCardApplication().get(2);
	DIDInfoType didInfo = cardApplicationESIGN.getDIDInfo().get(2);
	DifferentialIdentityType differentialIdentity = didInfo.getDifferentialIdentity();
	assertEquals(differentialIdentity.getDIDName(), "PrK.CH.AUT_signPKCS1_V1_5");
	assertEquals(differentialIdentity.getDIDProtocol(), "urn:oid:1.3.162.15480.3.0.25");
	CryptoMarkerType cryptoMarkerType = new CryptoMarkerType(differentialIdentity.getDIDMarker().getCryptoMarker());
	assertEquals(cryptoMarkerType.getProtocol(), "urn:oid:1.3.162.15480.3.0.25");
	assertEquals(cryptoMarkerType.getAlgorithmInfo().getSupportedOperations().get(0), "Compute-signature");

	// uncomment to get output files to make a diff
	/*WSMarshaller jaxbMarshaller = new JAXBMarshaller();
	CardInfo cardInfoJM = (CardInfo) jaxbMarshaller.unmarshal(jaxbMarshaller.str2doc(egkCif));
	File f = new File("cifJM.xml");
	FileOutputStream fos = new FileOutputStream(f);
	File f2 = new File("cifAM.xml");
	FileOutputStream fos2 = new FileOutputStream(f2);
	marshalLog(cardInfoJM, fos);
	marshalLog(cardInfo, fos2);*/
    }
    @Test
    public void testConversionOfCardInfoECARD_AT_CIF() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	// Test ecard AT 0.9.0
	Object o = m.unmarshal(m.str2doc(ECARD_AT_CIF));
	if (! (o instanceof CardInfo)) {
	    throw new Exception("Object should be an instance of CardInfo");
	}
	CardInfo cardInfo = (CardInfo) o;
    }

    @Test
    public void testConversionOfEstablishContext() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	EstablishContext establishContext = new EstablishContext();
	Document d = m.marshal(establishContext);
	String s = m.doc2str(d);

	assertEquals(s.trim(), ESTABLISH_CONTEXT_XML.trim());

	Object o = m.unmarshal(d);
	if (!(o instanceof EstablishContext)) {
	    throw new Exception("Object should be an instance of EstablishContext");
	}
    }

    @Test
    public void testConversionOfDisconnect() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Object o = m.unmarshal(m.str2doc(DISCONNECT));

	if (!(o instanceof Disconnect)) {
	    throw new Exception("Object should be an instance of EstablishContext");
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
	assertEquals(m.doc2str(d), DISCONNECT_RESPONSE);
    }

    @Test
    public void testConversionOfDestroyChannel() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	DestroyChannel destroy = new DestroyChannel();
	destroy.setSlotHandle(new byte[] { 0x0, 0x1, 0x2, 0x3, 0x4, 0x5 });
	Document d = m.marshal(destroy);
	assertEquals(m.doc2str(d), DESTROY_CHANNEL);
    }

    @Test
    public void testConversionOfTransmit() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Object o = m.unmarshal(m.str2doc(TRANSMIT));
	if (!(o instanceof Transmit)) {
	    throw new Exception("Object should be an instance of Transmit");
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

	StartPAOS.UserAgent ua = new StartPAOS.UserAgent();
	ua.setName("Open eCard App");
	ua.setVersionMajor(BigInteger.valueOf(1));
	ua.setVersionMinor(BigInteger.valueOf(3));
	ua.setVersionSubminor(BigInteger.valueOf(0));
	startP.setUserAgent(ua);

	StartPAOS.SupportedAPIVersions av = new StartPAOS.SupportedAPIVersions();
	av.setMajor(BigInteger.valueOf(1));
	av.setMinor(BigInteger.valueOf(3));
	av.setSubminor(BigInteger.valueOf(0));
	startP.getSupportedAPIVersions().add(av);

	startP.getSupportedDIDProtocols().add("EAC");

	Document d = m.marshal(startP);
	assertEquals(m.doc2str(d), START_PAOS);
    }

    @Test
    public void testConversionOfStartPAOSResponse() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Object o = m.unmarshal(m.str2doc(START_PAOS_RESPONSE));

	if (!(o instanceof StartPAOSResponse)) {
	    throw new Exception("Object should be an instance of StartPAOSResponse");
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
	StartPAOS sp = new StartPAOS();
	sp.setSessionIdentifier("5ec5ebb1dd254f392e6ca33cf5bf");
	ConnectionHandleType connectionHandleType = new ConnectionHandleType();
	connectionHandleType.setContextHandle(new BigInteger("94D7439CE657561E7AE3D491FD71AC21F8BCBB5608BA61F5A0EA52269BC01250", 16).toByteArray());
	connectionHandleType.setSlotHandle(new BigInteger("EEB49368C1152BEC379DA59356D59039CA7757AC3EAF9430285F2CBB3DD6EDDD", 16).toByteArray());

	sp.getConnectionHandle().add(connectionHandleType);

	Document contentDoc = m.marshal(sp);

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
	LOG.debug(responseStr);

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
	LOG.debug(s);
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

	marshalLog(establishChannel);
	WSMarshaller m = new AndroidMarshaller();
	Document doc = m.marshal(establishChannel);

	String s = m.doc2str(doc);
	LOG.debug(s);
    }

    @Test
    public void testConversionOfDIDAuthenticateResponseCA() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	DIDAuthenticateResponse didAuthResponse = new DIDAuthenticateResponse();
	Result r = new Result();
	r.setResultMajor("major");
	r.setResultMinor("minor");
	InternationalStringType internationalStringType = new InternationalStringType();
	internationalStringType.setLang("en");
	internationalStringType.setValue("message");
	r.setResultMessage(internationalStringType);
	didAuthResponse.setResult(r);

	EAC2OutputType didAuthenticationDataType = new EAC2OutputType();
	didAuthenticationDataType.setProtocol("urn:....");

	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	factory.setNamespaceAware(true);
	DocumentBuilder builder = factory.newDocumentBuilder();
	Document d = builder.newDocument();

	Element e = d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "Signature");
	e.setTextContent("7117D7BF95D8D6BD437A0D43DE48F42528273A98F2605758D6A3A2BFC38141E7577CABB4F8FBC8DF152E3A097D1B3A703597331842425FE4A9D0F1C9067AC4A9");
	didAuthenticationDataType.getAny().add(e);

	didAuthResponse.setAuthenticationProtocolData(didAuthenticationDataType);

	marshalLog(didAuthResponse);

	Document doc = m.marshal(didAuthResponse);

	String s = m.doc2str(doc);
	LOG.debug(s);
	StringReader sr = new StringReader(s);
	DIDAuthenticateResponse didaresp = JAXB.unmarshal(sr, DIDAuthenticateResponse.class);

	marshalLog(didaresp);
    }

    @Test
    public void testConversionOfDIDAuthenticateResponseTA() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	DIDAuthenticateResponse didAuthResponse = new DIDAuthenticateResponse();
	Result r = new Result();
	r.setResultMajor("major");
	r.setResultMinor("minor");
	InternationalStringType internationalStringType = new InternationalStringType();
	internationalStringType.setLang("en");
	internationalStringType.setValue("message");
	r.setResultMessage(internationalStringType);
	didAuthResponse.setResult(r);

	EAC2OutputType didAuthenticationDataType = new EAC2OutputType();

	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	factory.setNamespaceAware(true);
	DocumentBuilder builder = factory.newDocumentBuilder();
	Document d = builder.newDocument();

	Element e = d.createElementNS("urn:iso:std:iso-iec:24727:tech:schema", "Challenge");
	e.setTextContent("1331F2B1571E6DC2");
	didAuthenticationDataType.getAny().add(e);

	didAuthResponse.setAuthenticationProtocolData(didAuthenticationDataType);

	marshalLog(didAuthResponse);

	Document doc = m.marshal(didAuthResponse);

	String s = m.doc2str(doc);
	LOG.debug(s);
	StringReader sr = new StringReader(s);
	DIDAuthenticateResponse didaresp = JAXB.unmarshal(sr, DIDAuthenticateResponse.class);

	marshalLog(didaresp);
    }

    @Test
    public void testConversionOfDIDAuthenticateResponsePACE() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	DIDAuthenticateResponse didAuthResponse = new DIDAuthenticateResponse();
	Result r = new Result();
	r.setResultMajor("major");
	r.setResultMinor("minor");
	InternationalStringType internationalStringType = new InternationalStringType();
	internationalStringType.setLang("en");
	internationalStringType.setValue("message");
	r.setResultMessage(internationalStringType);
	didAuthResponse.setResult(r);

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

	didAuthResponse.setAuthenticationProtocolData(didAuthenticationDataType);

	marshalLog(didAuthResponse);

	Document doc = m.marshal(didAuthResponse);

	String s = m.doc2str(doc);
	LOG.debug(s);
	StringReader sr = new StringReader(s);
	DIDAuthenticateResponse didaresp = JAXB.unmarshal(sr, DIDAuthenticateResponse.class);

	marshalLog(didaresp);
    }

    @Test
    public void testConversionOfDIDAutheticateDID_AUTHENTICATE_PACE() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Document d = m.str2doc(DID_AUTHENTICATE_PACE);

	Object o = m.unmarshal(d);
	if (!(o instanceof DIDAuthenticate)) {
	    throw new Exception("Object should be an instance of DIDAuthenticate");
	}

	DIDAuthenticate didAuthenticate = (DIDAuthenticate) o;
	assertEquals(didAuthenticate.getDIDName(), "PIN");
	assertEquals(didAuthenticate.getConnectionHandle().getSlotHandle(), StringUtils.toByteArray("47C67AA9C0233947BBDBC4ACC7685A4FD6AD7D3EADA67A2F"));
	assertEquals(EAC1InputType.class, didAuthenticate.getAuthenticationProtocolData().getClass());

	int certIdx = 0;
	List<Element> apd = didAuthenticate.getAuthenticationProtocolData().getAny();
	for (int i = 0; i < didAuthenticate.getAuthenticationProtocolData().getAny().size(); i++) {
	    Element next = apd.get(i);
	    if (next.getLocalName().equals("Certificate") && certIdx == 0) {
		assertEquals("7F218201487F4E8201005F29010042104445445674494447564E4B30303032357F494F060A04007F000702020202038641045D84988F1BD2186E4425E91E0CB532CF6E32D1D39DBCD39A2C4B896069A70E6846434FEB0BAEC395551D82BDCC03FD83F3A169B05F6C2D0285D8BD8B856BB61E5F200E444544454D4F50414130303035367F4C12060904007F0007030102025305000513FF075F25060108000900025F2406010801000002655E732D060904007F0007030103018020E959DFF3F9755D410495A898D5D8ED24ABB4210E9DB8F98C5D32096557CC70E9732D060904007F00070301030280206CCF8EFD02E71B274C8C4F29122310EF2D7FFDFB4C611FE267F8576DA42E7BA25F37402A7CB1AB18D2D48AAD1EE80AED76058007F60AE257722DC85128396528AD46F63A4839BA3B9288623335D2E759B8EE12E543D8393B0590263E1B9417AC05D77D",
			next.getTextContent());
		certIdx++;
	    } else if (next.getLocalName().equals("Certificate") && certIdx == 1) {
		assertEquals("7F218201B67F4E82016E5F290100420E44455445535465494430303030317F4982011D060A04007F000702020202038120A9FB57DBA1EEA9BC3E660A909D838D726E3BF623D52620282013481D1F6E537782207D5A0975FC2C3057EEF67530417AFFE7FB8055C126DC5C6CE94A4B44F330B5D9832026DC5C6CE94A4B44F330B5D9BBD77CBF958416295CF7E1CE6BCCDC18FF8C07B68441048BD2AEB9CB7E57CB2C4B482FFC81B7AFB9DE27E1E3BD23C23A4453BD9ACE3262547EF835C3DAC4FD97F8461A14611DC9C27745132DED8E545C1D54C72F0469978520A9FB57DBA1EEA9BC3E660A909D838D718C397AA3B561A6F7901E0E82974856A7864104096EB58BFD86252238EC2652185C43C3A56C320681A21E37A8E69DDC387C0C5F5513856EFE2FDC656E604893212E29449B365E304605AC5413E75BE31E641F128701015F200E44455445535465494430303030327F4C12060904007F0007030102025305FE0F01FFFF5F25060100000902015F24060103000902015F3740141120A0FDFC011A52F3F72B387A3DC7ACA88B4868D5AE9741780B6FF8A0B49E5F55169A2D298EF5CF95935DCA0C3DF3E9D42DC45F74F2066317154961E6C746",
			next.getTextContent());
		certIdx++;
	    } else if (next.getLocalName().equals("CertificateDescription")) {
		assertEquals("308202AE060A04007F00070301030101A1160C14476F7665726E696B757320546573742044564341A21A1318687474703A2F2F7777772E676F7665726E696B75732E6465A31A0C18476F7665726E696B757320476D6248202620436F2E204B47A420131E68747470733A2F2F746573742E676F7665726E696B75732D6569642E6465A58201510C82014D416E736368726966743A090D0A476F7665726E696B757320476D6248202620436F2E204B470D0A416D2046616C6C7475726D20390D0A3238333539204272656D656E090D0A0D0A452D4D61696C2D416472657373653A09686240626F732D6272656D656E2E6465090D0A0D0A5A7765636B20646573204175736C657365766F7267616E67733A090D0A44656D6F6E7374726174696F6E20646573206549442D53657276696365090D0A0D0A5A757374C3A46E6469676520446174656E73636875747A61756673696368743A090D0A446965204C616E64657362656175667472616774652066C3BC7220446174656E73636875747A20756E6420496E666F726D6174696F6E736672656968656974206465722046726569656E2048616E73657374616474204272656D656E0D0A41726E647473747261C39F6520310D0A3237353730204272656D6572686176656EA64B134968747470733A2F2F746573742E676F7665726E696B75732D6569642E64653A3434332F417574656E742D44656D6F4170706C69636174696F6E2F5265636569766572536572766C6574A7818B318188042048B1397235E55ED163F3280B170B6965CE39033D9B0A83148347FC3FF9DAF0D304208AC4AFBF236CBB8D30D2FBEB0AD990D101E4EFAA7B4DF4CE1705135E530E993F0420D2E54E1D26FC5DFC3408609831BBE4CFE3204365604849E7B094623566B54A760420E224D25B448DC054C023392CA11017751041D762F83D880895B3018D8EC2B290",
			next.getTextContent());
	    } else if (next.getLocalName().equals("RequiredCHAT")) {
		assertEquals("7F4C12060904007F00070301020253050000000000", next.getTextContent());
	    } else if (next.getLocalName().equals("OptionalCHAT")) {
		assertEquals("7F4C12060904007F0007030102025305000513FF05", next.getTextContent());
	    } else if (next.getLocalName().equals("AuthenticatedAuxiliaryData")) {
		assertEquals("672E7315060904007F000703010401530832303030303930377315060904007F00070301040253083230313830393037", next.getTextContent());
	    } else if (next.getLocalName().equals("TransactionInfo")) {
		assertEquals("Ελληνικά", next.getTextContent());
	    } else {
		Assert.fail("Unknown element contained in message.");
	    }
	}
    }
    @Test
    public void testConversionOfDIDAutheticateDID_AUTHENTICATE_TA() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Document d = m.str2doc(DID_AUTHENTICATE_TA);

	Object o = m.unmarshal(d);
	if (!(o instanceof DIDAuthenticate)) {
	    throw new Exception("Object should be an instance of DIDAuthenticate");
	}

	DIDAuthenticate didAuthenticate = (DIDAuthenticate) o;
	assertEquals(didAuthenticate.getDIDName(), "PIN");
	assertEquals(didAuthenticate.getConnectionHandle().getSlotHandle(), StringUtils.toByteArray("05D4F40AEBD9919383C22216055EA3DB15056C51"));
	assertEquals(EAC2InputType.class, didAuthenticate.getAuthenticationProtocolData().getClass());
	AuthDataMap eac2input = new AuthDataMap(didAuthenticate.getAuthenticationProtocolData());

	assertEquals(eac2input.getContentAsString("EphemeralPublicKey"), "8D44E99377DA28436D2F7E8620347D7C08B186B179633E3654842E940AB179B498F974970D990D47C61FE5D4D91EBB10831E824EC6F2600D89D6661CDF47F734");
	// assertEquals(eac2input.getContentAsString("Certificate"),
	// "7F2181E47F4E819D5F290100420D5A5A43564341415441303030317F494F060A04007F0007020202020386410452DD32EAFE1FBBB4000CD9CE75F66636CFCF1EDD44F7B1EDAE25B84193DA04A91C77EE87F5C8F959ED276200DE33AB574CE9801135FF4497A37162B7C8548A0C5F200E5A5A4456434141544130303030357F4C12060904007F0007030102025305700301FFB75F25060100000601015F24060100010003015F37406F13AE9A6F4EDDB7839FF3F04D71E0DC377BC4B08FAD295EED241B524328AD0730EB553497B4FB66E9BB7AB90815F04273F09E751D7FD4B861439B4EE65381C3");
	assertEquals(eac2input.getContentAsString("Certificate"),
		"7F218201427F4E81FB5F290100420E5A5A4456434141544130303030357F494F060A04007F0007020202020386410470C07FAA329E927D961F490F5430B395EECF3D2A538194D8B637DE0F8ACF60A9031816AC51B594097EB211FB8F55FAA8507D5800EF7B94E024F9630314116C755F200B5A5A444B423230303033557F4C12060904007F0007030102025305000301DF045F25060100000601085F2406010000070001655E732D060904007F00070301030280207C1901932DB75D08539F2D4A27C938F79E69E083C442C068B299D185BC8AFA78732D060904007F0007030103018020BFD2A6A2E4237948D7DCCF7975D71D40F15307AA59F580A48777CBEED093F54B5F3740618F584E4293F75DDE8977311694B69A3ED73BBE43FDAFEC11B7ECF054F84ACB1231615338CE8D6EC332480883E14E0664950F85134290DD716B7C153232BC96");

	marshalLog(didAuthenticate);
    }
    @Test
    public void testConversionOfDIDAutheticateDID_AUTHENTICATE_CA() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Document d = m.str2doc(DID_AUTHENTICATE_CA);

	Object o = m.unmarshal(d);
	if (!(o instanceof DIDAuthenticate)) {
	    throw new Exception("Object should be an instance of DIDAuthenticate");
	}

	DIDAuthenticate didAuthenticate = (DIDAuthenticate) o;
	assertEquals(didAuthenticate.getDIDName(), "PIN");
	assertEquals(didAuthenticate.getConnectionHandle().getSlotHandle(), StringUtils.toByteArray("05D4F40AEBD9919383C22216055EA3DB15056C51"));
	assertEquals(EACAdditionalInputType.class, didAuthenticate.getAuthenticationProtocolData().getClass());
	AuthDataMap eacadditionalinput = new AuthDataMap(didAuthenticate.getAuthenticationProtocolData());

	assertEquals(eacadditionalinput.getContentAsString("Signature"), "7117D7BF95D8D6BD437A0D43DE48F42528273A98F2605758D6A3A2BFC38141E7577CABB4F8FBC8DF152E3A097D1B3A703597331842425FE4A9D0F1C9067AC4A9");
	// assertEquals(eac2input.getContentAsString("Certificate"),
	// "7F2181E47F4E819D5F290100420D5A5A43564341415441303030317F494F060A04007F0007020202020386410452DD32EAFE1FBBB4000CD9CE75F66636CFCF1EDD44F7B1EDAE25B84193DA04A91C77EE87F5C8F959ED276200DE33AB574CE9801135FF4497A37162B7C8548A0C5F200E5A5A4456434141544130303030357F4C12060904007F0007030102025305700301FFB75F25060100000601015F24060100010003015F37406F13AE9A6F4EDDB7839FF3F04D71E0DC377BC4B08FAD295EED241B524328AD0730EB553497B4FB66E9BB7AB90815F04273F09E751D7FD4B861439B4EE65381C3");

	marshalLog(didAuthenticate);
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

	marshalLog(initializeFrameworkResponse);
	WSMarshaller m = new AndroidMarshaller();
	Document d = m.marshal(initializeFrameworkResponse);

	String s = m.doc2str(d);
	LOG.debug(s);
    }

    @Test
    public void testConversionOfInitializeFramework() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Document d = m.str2doc(INITIALIZE_FRAMEWORK);

	Object o = m.unmarshal(d);
	if (!(o instanceof InitializeFramework)) {
	    throw new Exception("Object should be an instance of InitializeFramework");
	}
    }

    @Test
    public void testConversionOfGetRecognitionTreeResponse() throws Exception {
	WSMarshaller m = new AndroidMarshaller();

	Object o = m.unmarshal(m.str2doc(RECOGNITION_TREE_RESPONSE_XML));
	if (o instanceof GetRecognitionTreeResponse) {
	    RecognitionTree tree = ((GetRecognitionTreeResponse) o).getRecognitionTree();
	    StringWriter sw = new StringWriter();

	    JAXB.marshal(tree, sw);

	    Document d = m.marshal(tree);

	    String s = m.doc2str(d);
	    LOG.debug(s);
	} else {
	    throw new Exception("Object should be an instance of GetRecognitionTreeResponse");
	}
    }

    @Test
    public void testConversionOfConclusion() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Object o = m.unmarshal(m.str2doc(CONCLUSION));
	Conclusion c = (Conclusion) o;
	assertEquals("http://ws.gematik.de/egk/1.0.0", c.getRecognizedCardType());
	Conclusion cc = JAXB.unmarshal(new StringReader(CONCLUSION), Conclusion.class);
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
	LOG.debug(s);
    }

    @Test
    public void testConversionOfGetStatusResponse() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Object o = m.unmarshal(m.str2doc(GET_STATUS_RESPONSE));
	if (!(o instanceof GetStatusResponse)) {
	    throw new Exception("Object should be an instance of GetStatusResponse");
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
	LOG.debug(s);
    }

    @Test
    public void testConversionOfAddonBundleDescription() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Object o = m.unmarshal(m.str2doc(TEST_ADDON_BUNDDLE_DESCRIPTION));
	if (!(o instanceof AddonSpecification)) {
	    throw new Exception("Object should be an instance of AddonSpecification");
	}
	marshalLog(o);
    }

    @Test
    public void testCreateSession() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Object o = m.unmarshal(m.str2doc(CREATE_SESSION));
	if (!(o instanceof CreateSession)) {
	    throw new Exception("Object should be an instance of CreateSession");
	}

	CreateSession cs = (CreateSession) m.unmarshal(m.marshal(o));
	Assert.assertEquals(cs.getSessionIdentifier(), "05D4F40AEBD9919383C22216055EA3DB15056C51");
    }

    @Test
    public void testCreateSessionResponse() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Object o = m.unmarshal(m.str2doc(CREATE_SESSION_RESP));
	if (!(o instanceof CreateSessionResponse)) {
	    throw new Exception("Object should be an instance of CreateSessionResponse");
	}

	CreateSessionResponse cs = (CreateSessionResponse) m.unmarshal(m.marshal(o));
	WSHelper.checkResult(cs);
	Assert.assertEquals(cs.getConnectionHandle().getChannelHandle().getSessionIdentifier(), "05D4F40AEBD9919383C22216055EA3DB15056C51");
    }

    @Test
    public void testDestroySession() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Object o = m.unmarshal(m.str2doc(DESTROY_SESSION));
	if (!(o instanceof DestroySession)) {
	    throw new Exception("Object should be an instacne of DestroySession");
	}

	DestroySession ds = (DestroySession) m.unmarshal(m.marshal(o));
	Assert.assertEquals(ds.getConnectionHandle().getChannelHandle().getSessionIdentifier(), "05D4F40AEBD9919383C22216055EA3DB15056C51");
    }

    @Test
    public void testDestroySessionResponse() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Object o = m.unmarshal(m.str2doc(DESTROY_SESSION_RESP));
	if (!(o instanceof DestroySessionResponse)) {
	    throw new Exception("Object should be an instance of DestroySessionResponse");
	}

	DestroySessionResponse ds = (DestroySessionResponse) m.unmarshal(m.marshal(o));
	WSHelper.checkResult(ds);
    }

}
