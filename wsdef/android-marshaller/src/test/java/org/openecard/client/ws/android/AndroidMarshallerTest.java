package org.openecard.client.ws.android;


import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.GetRecognitionTreeResponse;
import iso.std.iso_iec._24727.tech.schema.GetStatus;
import iso.std.iso_iec._24727.tech.schema.PathSecurityType;
import iso.std.iso_iec._24727.tech.schema.RecognitionTree;
import iso.std.iso_iec._24727.tech.schema.Wait;

import java.io.StringWriter;
import java.math.BigInteger;

import javax.xml.bind.JAXB;

import junit.framework.Assert;

import org.junit.Test;
import org.openecard.client.ws.WSMarshaller;
import org.w3c.dom.Document;

/**
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 *
 */
public class AndroidMarshallerTest {
	private static final String getRecognitionTreeResponseXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?> <ns2:GetRecognitionTreeResponse xmlns=\"urn:oasis:names:tc:dss:1.0:core:schema\" 	xmlns:ns2=\"urn:iso:std:iso-iec:24727:tech:schema\" xmlns:ns3=\"http://www.w3.org/2000/09/xmldsig#\" 	xmlns:ns4=\"http://www.bsi.bund.de/ecard/api/1.1\" xmlns:ns5=\"http://ws.openecard.org/gui/v1.0\" 	xmlns:ns6=\"http://uri.etsi.org/02231/v3.1.2#\" xmlns:ns7=\"http://uri.etsi.org/02231/v2.1.1#\" 	xmlns:ns8=\"http://uri.etsi.org/02231/v2.x#\" xmlns:ns9=\"http://www.setcce.org/schemas/ers\" 	Profile=\"http://www.bsi.bund.de/ecard/api/1.1\"> 	<Result> 		<ResultMajor>http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok 		</ResultMajor> 	</Result> 	<ns2:RecognitionTree> 		<ns2:CardCall> 			<ns2:CommandAPDU>00A4000C023F00</ns2:CommandAPDU> 			<ns2:ResponseAPDU> 				<ns2:Trailer>9000</ns2:Trailer> 				<ns2:Conclusion> 					<ns2:CardCall> 						<ns2:CommandAPDU>00A4020C022F00</ns2:CommandAPDU> 						<ns2:ResponseAPDU> 							<ns2:Trailer>9000</ns2:Trailer> 							<ns2:Conclusion> 								<ns2:CardCall> 									<ns2:CommandAPDU>00B20404FF</ns2:CommandAPDU> 									<ns2:ResponseAPDU> 										<ns2:Body> 											<ns2:MatchingData> 												<ns2:Offset>00</ns2:Offset> 												<ns2:Length>3B</ns2:Length> 												<ns2:MatchingValue>61394F08D0400000170013015015476577C3B6686E6C69636865205369676E6174757251023F057312300804063F003F055031A00604043F005032 												</ns2:MatchingValue> 											</ns2:MatchingData> 										</ns2:Body> 										<ns2:Trailer>6282</ns2:Trailer> 										<ns2:Conclusion> 											<ns2:RecognizedCardType>http://cif.chipkarte.at/e-card/g3 											</ns2:RecognizedCardType> 										</ns2:Conclusion> 									</ns2:ResponseAPDU> 									<ns2:ResponseAPDU> 										<ns2:Body> 											<ns2:Tag>61</ns2:Tag> 											<ns2:MatchingData> 												<ns2:Offset>00</ns2:Offset> 												<ns2:Length>11</ns2:Length> 												<ns2:MatchingValue>4F0FE828BD080FA000000167455349474E 												</ns2:MatchingValue> 											</ns2:MatchingData> 										</ns2:Body> 										<ns2:Trailer>6282</ns2:Trailer> 										<ns2:Conclusion> 											<ns2:RecognizedCardType>http://ws.gematik.de/egk/1.0.0 											</ns2:RecognizedCardType> 										</ns2:Conclusion> 									</ns2:ResponseAPDU> 								</ns2:CardCall> 							</ns2:Conclusion> 						</ns2:ResponseAPDU> 					</ns2:CardCall> 				</ns2:Conclusion> 			</ns2:ResponseAPDU> 		</ns2:CardCall> 		<ns2:CardCall> 			<ns2:CommandAPDU>00A4020C022F00</ns2:CommandAPDU> 			<ns2:ResponseAPDU> 				<ns2:Trailer>9000</ns2:Trailer> 				<ns2:Conclusion> 					<ns2:CardCall> 						<ns2:CommandAPDU>00B000005A</ns2:CommandAPDU> 						<ns2:ResponseAPDU> 							<ns2:Body> 								<ns2:MatchingData> 									<ns2:Offset>00</ns2:Offset> 									<ns2:Length>5A</ns2:Length> 									<ns2:MatchingValue>61324F0FE828BD080FA000000167455349474E500F434941207A752044462E655369676E5100730C4F0AA000000167455349474E61094F07A0000002471001610B4F09E80704007F00070302610C4F0AA000000167455349474E 									</ns2:MatchingValue> 								</ns2:MatchingData> 							</ns2:Body> 							<ns2:Trailer>9000</ns2:Trailer> 							<ns2:Conclusion> 								<ns2:RecognizedCardType>http://bsi.bund.de/cif/npa.xml 								</ns2:RecognizedCardType> 							</ns2:Conclusion> 						</ns2:ResponseAPDU> 					</ns2:CardCall> </ns2:Conclusion> 			</ns2:ResponseAPDU> 		</ns2:CardCall> 	</ns2:RecognitionTree> </ns2:GetRecognitionTreeResponse>";

	private static final String establishContextXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\r\n"
			+ "<iso:EstablishContext xmlns:iso=\"urn:iso:std:iso-iec:24727:tech:schema\"/>";

	private static final String getStatusResponse  = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns3:GetStatusResponse Profile=\"http://www.bsi.bund.de/ecard/api/1.1\" xmlns:ns2=\"http://www.w3.org/2000/09/xmldsig#\" xmlns=\"urn:oasis:names:tc:dss:1.0:core:schema\" xmlns:ns4=\"http://www.bsi.bund.de/ecard/api/1.1\" xmlns:ns3=\"urn:iso:std:iso-iec:24727:tech:schema\" xmlns:ns9=\"http://uri.etsi.org/02231/v2.x#\" xmlns:ns5=\"http://ws.openecard.org/gui/v1.0\" xmlns:ns6=\"http://www.setcce.org/schemas/ers\" xmlns:ns7=\"http://uri.etsi.org/02231/v3.1.2#\" xmlns:ns8=\"http://uri.etsi.org/02231/v2.1.1#\"><Result><ResultMajor>http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok</ResultMajor></Result><ns3:IFDStatus><ns3:IFDName>REINER SCT cyberJack RFID basis 0</ns3:IFDName><ns3:Connected>true</ns3:Connected><ns3:SlotStatus><ns3:Index>0</ns3:Index><ns3:CardAvailable>false</ns3:CardAvailable></ns3:SlotStatus></ns3:IFDStatus><ns3:IFDStatus><ns3:IFDName>RIM BlackBerry Smart Card Reader 0</ns3:IFDName><ns3:Connected>true</ns3:Connected><ns3:SlotStatus><ns3:Index>0</ns3:Index><ns3:CardAvailable>false</ns3:CardAvailable></ns3:SlotStatus></ns3:IFDStatus></ns3:GetStatusResponse>";

	  
	  
	@Test
	public void testConversionOfEstablishContext() throws Exception {
		WSMarshaller m = new AndroidMarshaller();
		EstablishContext establishContext = new EstablishContext();
		Document d = m.marshal(establishContext);
		String s = m.doc2str(d);

		Assert.assertEquals(s.trim(), establishContextXML.trim());

		Object o = m.unmarshal(d);
		if (!(o instanceof EstablishContext))
			throw new Exception(
					"Object should be an instace of EstablishContext");

		
	}
	
	@Test
	public void testConversionOfGetRecognitionTreeResponse() throws Exception {
		WSMarshaller m = new AndroidMarshaller();
		
		Object o = m.unmarshal(m.str2doc(getRecognitionTreeResponseXML));
    	if(o instanceof GetRecognitionTreeResponse) {
    		RecognitionTree tree =  ((GetRecognitionTreeResponse) o).getRecognitionTree();
    		StringWriter sw = new StringWriter();
    		
    		JAXB.marshal(tree, sw);
    		
    		
    		
    	} else 
    		throw new Exception(
					"Object should be an instace of GetRecognitionTreeResponse");

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
