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

import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.Conclusion;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.GetRecognitionTreeResponse;
import iso.std.iso_iec._24727.tech.schema.GetStatus;
import iso.std.iso_iec._24727.tech.schema.GetStatusResponse;
import iso.std.iso_iec._24727.tech.schema.PathSecurityType;
import iso.std.iso_iec._24727.tech.schema.RecognitionTree;
import iso.std.iso_iec._24727.tech.schema.StartPAOS;
import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse;
import iso.std.iso_iec._24727.tech.schema.Wait;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;

import javax.xml.bind.JAXB;
import javax.xml.namespace.QName;

import oasis.names.tc.dss._1_0.core.schema.InternationalStringType;
import oasis.names.tc.dss._1_0.core.schema.Result;

import org.junit.Assert;
import org.junit.Test;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.ws.WSMarshaller;
import org.openecard.client.ws.soap.SOAPHeader;
import org.openecard.client.ws.soap.SOAPMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.bund.bsi.ecard.api._1.InitializeFramework;
import de.bund.bsi.ecard.api._1.InitializeFrameworkResponse;
import de.bund.bsi.ecard.api._1.InitializeFrameworkResponse.Version;

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
    static {
	try {
	    getRecognitionTreeResponseXML = loadXML("GetRecognitionTreeResponse.xml");
	    establishContextXML = loadXML("EstablishContext.xml");
	    getStatusResponse = loadXML("GetStatusResponse.xml");
	    conclusion = loadXML("Conclusion.xml");
	    initializeFramework = loadXML("InitializeFramework.xml");
	    startPAOSResponse = loadXML("StartPAOSResponse.xml");
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
