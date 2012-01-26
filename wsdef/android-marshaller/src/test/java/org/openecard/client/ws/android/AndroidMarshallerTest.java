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

package org.openecard.client.ws.android;

import de.bund.bsi.ecard.api._1.InitializeFramework;
import de.bund.bsi.ecard.api._1.InitializeFrameworkResponse;
import de.bund.bsi.ecard.api._1.InitializeFrameworkResponse.Version;
import iso.std.iso_iec._24727.tech.schema.*;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import javax.xml.bind.JAXB;
import javax.xml.namespace.QName;
import oasis.names.tc.dss._1_0.core.schema.InternationalStringType;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.junit.Assert;
import org.junit.Test;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.util.StringUtils;
import org.openecard.client.ws.WSMarshaller;
import org.openecard.client.ws.soap.SOAPHeader;
import org.openecard.client.ws.soap.SOAPMessage;
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
    private static final String didAuthenticate;
    static {
	try {
	    getRecognitionTreeResponseXML = loadXML("GetRecognitionTreeResponse.xml");
	    establishContextXML = loadXML("EstablishContext.xml");
	    getStatusResponse = loadXML("GetStatusResponse.xml");
	    conclusion = loadXML("Conclusion.xml");
	    initializeFramework = loadXML("InitializeFramework.xml");
	    startPAOSResponse = loadXML("StartPAOSResponse.xml");
	    didAuthenticate = loadXML("DIDAuthenticate.xml");
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
	    w.write(String.format("%n")); // platform dependant newline character
	}
	return w.toString();
    }


    @Test
    public void testConversionOfEstablishContext() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	EstablishContext establishContext = new EstablishContext();
	Document d = m.marshal(establishContext);
	String s = m.doc2str(d);

	Assert.assertEquals(s.trim(), establishContextXML.trim());

	Object o = m.unmarshal(d);
	if (!(o instanceof EstablishContext)) {
	    throw new Exception("Object should be an instace of EstablishContext");
	}
    }
    
    
    @Test 
    public void testConversionOfStartPAOS() throws Exception {
    	WSMarshaller m = new AndroidMarshaller();
    	StartPAOS startPAOS = new StartPAOS();
    	startPAOS.setSessionIdentifier("5ec5ebb1dd254f392e6ca33cf5bf");
    	ConnectionHandleType connectionHandleType = new ConnectionHandleType();
    	connectionHandleType.setContextHandle(new BigInteger("94D7439CE657561E7AE3D491FD71AC21F8BCBB5608BA61F5A0EA52269BC01250",16).toByteArray());
    	connectionHandleType.setSlotHandle(new BigInteger("EEB49368C1152BEC379DA59356D59039CA7757AC3EAF9430285F2CBB3DD6EDDD",16).toByteArray());
    	
		startPAOS.getConnectionHandle().add(connectionHandleType);
    	
		Document d = m.marshal(startPAOS);
		String s = m.doc2str(d);
    	System.out.println(s);
    	
    	JAXB.marshal(startPAOS, System.out);
		
    }
    @Test
    public void testConversionOfStartPAOSResponse() throws Exception {
    	WSMarshaller m = new AndroidMarshaller();
    	Object o =  m.unmarshal(m.str2doc(startPAOSResponse));

    	if (!(o instanceof StartPAOSResponse)) {
    	    throw new Exception("Object should be an instace of StartPAOSResponse");
    	}
    	
    	StartPAOSResponse stPaosResponse = (StartPAOSResponse) o;
    	Assert.assertEquals(stPaosResponse.getResult().getResultMajor(), "http://www.bsi.bund.de/ecard/api/1.1/resultmajor#error");
    	Assert.assertEquals(stPaosResponse.getResult().getResultMinor(), "http://www.bsi.bund.de/ecard/api/1.1/resultminor/dp#timeout");
    	Assert.assertEquals(stPaosResponse.getResult().getResultMessage().getValue(), "WaitStartPAOS timeout");
    	Assert.assertEquals(stPaosResponse.getResult().getResultMessage().getLang(), "en");
    }
    
    @Test
    public void testSOAP() throws Exception {
    	WSMarshaller m = new AndroidMarshaller();
    	StartPAOS startPAOS = new StartPAOS();
    	startPAOS.setSessionIdentifier("5ec5ebb1dd254f392e6ca33cf5bf");
    	ConnectionHandleType connectionHandleType = new ConnectionHandleType();
    	connectionHandleType.setContextHandle(new BigInteger("94D7439CE657561E7AE3D491FD71AC21F8BCBB5608BA61F5A0EA52269BC01250",16).toByteArray());
    	connectionHandleType.setSlotHandle(new BigInteger("EEB49368C1152BEC379DA59356D59039CA7757AC3EAF9430285F2CBB3DD6EDDD",16).toByteArray());
    	
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
    	Element address = 	header.addChildElement(endpointReference, new QName(ECardConstants.PAOS_VERSION_20,"Address"));
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
    public void testConversionOfDIDAutheticate() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Document d = m.str2doc(didAuthenticate);
	
	Object o = m.unmarshal(d);
	if (!(o instanceof DIDAuthenticate)) {
	    throw new Exception("Object should be an instace of DIDAuthenticate");
	}
	
	DIDAuthenticate didAuthenticate = (DIDAuthenticate) o;
	Assert.assertEquals(didAuthenticate.getDIDName(), "PIN");
	Assert.assertArrayEquals(didAuthenticate.getConnectionHandle().getSlotHandle(), StringUtils.toByteArray("93F25BA574EF3F94F8AE42796DAF7C05"));
	Assert.assertEquals(EAC1InputType.class, didAuthenticate.getAuthenticationProtocolData().getClass());
	
	for(int i = 0;i<didAuthenticate.getAuthenticationProtocolData().getAny().size();i++){
	    if(didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getLocalName().equals("Certificate")){
		Assert.assertEquals("7F218201487F4E8201005F2901004210444544566549444454523130313430317F494F060A04007F000702020202038641041994E6E55DD1F021180CC705C001FECB4BF5B7E978E8F148002D2D4FDBC1E57A159592681039A041E0036C007E784C36A372528C89365AAB77402E07EED6D4115F200E44453030303030303330303542387F4C12060904007F0007030102025305000100D8005F25060102000102065F2406010200010207655E732D060904007F0007030103028020B90F0EB18F30BCB878EE68924E413A2F5D5DE2F844030141EDB64383C3958C56732D060904007F00070301030180207B0E75C3F613B50011CCB5CF95704B91A6B4AC6EC100377C60BC312A2CE6BE1E5F3740A9A8B9829D2820F96FDB96D09303B01A61F09BC10C766581CAB2BBD2609EC32217C8FEB73F7CAB52CC3A0D16DC1B02F348A3049A246F3790B9687F1F72ACE722", didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getTextContent());
	    } else if (didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getLocalName().equals("CertificateDescription")){
		Assert.assertEquals("3082032D060A04007F00070301030101A10E0C0C442D547275737420476D6248A2181316687474703A2F2F7777772E642D74727573742E6E6574A32F0C2D436F736D6F73204C6562656E73766572736963686572756E677320416B7469656E676573656C6C736368616674A41513137777772E636F736D6F73646972656B742E6465A58202410C82023D4E616D652C20416E7363687269667420756E6420452D4D61696C2D4164726573736520646573204469656E737465616E626965746572733A0D0A436F736D6F73204C6562656E73766572736963686572756E677320416B7469656E676573656C6C7363686166740D0A48616C6265726773747261C39F652035302D36300D0A363631303120536161726272C3BC636B656E0D0A696E666F40636F736D6F73646972656B742E64650D0A0D0A4765736368C3A46674737A7765636B3A0D0A2D2052656769737472696572756E67202F204C6F67696E2066C3BC7220226D65696E436F736D6F73446972656B7422202D0D0A0D0A48696E7765697320617566206469652066C3BC722064656E204469656E737465616E626965746572207A757374C3A46E646967656E205374656C6C656E2C20646965206469652045696E68616C74756E672064657220566F7273636872696674656E207A756D20446174656E73636875747A206B6F6E74726F6C6C696572656E3A0D0A4D696E697374657269756D2066C3BC7220496E6E6572657320756E64204575726F7061616E67656C6567656E68656974656E0D0A4D61696E7A65722053747261C39F65203133360D0A363631323120536161726272C3BC636B656E0D0A303638312035303120E280932030300D0A706F73747374656C6C6540696E6E656E2E736161726C616E642E64650D0A687474703A2F2F7777772E696E6E656E2E736161726C616E642E64650D0A416E737072656368706172746E65723A20526F6C616E64204C6F72656E7AA768316604202E15788858E56A91A459BB7086943A5A3AB879F88F72EEE72D5B8202B035943D04206D26166C2748B08BFC3AC0A37109C406A8D35317140F6C69C27C4AB77FDD21F80420805AD754568E472C4761D52D410FB99128AB4CE2D750FDA3A8DA8FBA67BB14EB", didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getTextContent());
	    } else if (didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getLocalName().equals("RequiredCHAT")){
		Assert.assertEquals("7F4C12060904007F00070301020253050001009800", didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getTextContent());
	    } else if(didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getLocalName().equals("OptionalCHAT")){
		Assert.assertEquals("7F4C12060904007F00070301020253050000004000", didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getTextContent());
	    } else if(didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getLocalName().equals("AuthenticatedAuxiliaryData")){
		Assert.assertEquals("67177315060904007F00070301040253083230313230313236", didAuthenticate.getAuthenticationProtocolData().getAny().get(i).getTextContent());
	    }
	}
	
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
    	if(o instanceof GetRecognitionTreeResponse) {
	    RecognitionTree tree =  ((GetRecognitionTreeResponse) o).getRecognitionTree();
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
    	Assert.assertEquals("http://ws.gematik.de/egk/1.0.0", c.getRecognizedCardType());
    	Conclusion cc = JAXB.unmarshal(new StringReader(conclusion), Conclusion.class);
    	//TODO 
    	//Assert.assertEquals(c.getTLSMarker().getAny().get(0), cc.getTLSMarker().getAny().get(0));

        }
    
    
    @Test
    public void testConversionOfGetStatus() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	GetStatus getStatus = new GetStatus();
	getStatus.setIFDName("ifdName");
	getStatus.setContextHandle(new byte[] {0x0, 0x1, 0x2});

	Document d = m.marshal(getStatus);

	String s = m.doc2str(d);
	System.out.println(s);
    }


    @Test
    public void testConversionOfGetStatusResponse() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Object o = m.unmarshal(m.str2doc(getStatusResponse));
	if(!(o instanceof GetStatusResponse))
		throw new Exception("Object should be an instace of GetStatusResponse");
    }


    @Test
    public void testConversionOfWait() throws Exception {
	WSMarshaller m = new AndroidMarshaller();
	Wait w = new Wait();
	w.setContextHandle(new byte[] {0x0, 0x1, 0x2} );
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
