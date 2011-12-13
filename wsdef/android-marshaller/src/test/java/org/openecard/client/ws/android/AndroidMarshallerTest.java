package org.openecard.client.ws.android;

import iso.std.iso_iec._24727.tech.schema.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import javax.xml.bind.JAXB;
import org.junit.Assert;
import org.junit.Test;
import org.openecard.client.ws.WSMarshaller;
import org.w3c.dom.Document;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AndroidMarshallerTest {

    private static final String getRecognitionTreeResponseXML;
    private static final String establishContextXML;
    private static final String getStatusResponse;

    static {
	try {
	    getRecognitionTreeResponseXML = loadXML("GetRecognitionTreeResponse.xml");
	    establishContextXML = loadXML("EstablishContext.xml");
	    getStatusResponse = loadXML("GetStatusResponse.xml");
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
    public void testConversionOfGetRecognitionTreeResponse() throws Exception {
	WSMarshaller m = new AndroidMarshaller();

	Object o = m.unmarshal(m.str2doc(getRecognitionTreeResponseXML));
    	if(o instanceof GetRecognitionTreeResponse) {
	    RecognitionTree tree =  ((GetRecognitionTreeResponse) o).getRecognitionTree();
	    StringWriter sw = new StringWriter();

	    JAXB.marshal(tree, sw);
    	} else {
	    throw new Exception("Object should be an instace of GetRecognitionTreeResponse");
	}
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
	m.unmarshal(m.str2doc(getStatusResponse));
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
