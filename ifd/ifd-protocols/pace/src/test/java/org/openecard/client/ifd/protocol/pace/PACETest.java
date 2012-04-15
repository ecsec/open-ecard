/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.ifd.protocol.pace;

import iso.std.iso_iec._24727.tech.schema.*;
import java.math.BigInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.UnsupportedDataTypeException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Ignore;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.ifd.anytype.AuthDataMap;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.ifd.scio.IFD;
import org.openecard.client.ifd.scio.wrapper.SCChannel;
import org.openecard.client.ws.WSMarshaller;
import org.openecard.client.ws.WSMarshallerException;
import org.openecard.client.ws.WSMarshallerFactory;
import org.xml.sax.SAXException;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PACETest {

    private static final Logger logger = Logger.getLogger("Test");

    @Ignore
    @Test
    public void executePACE_PIN() throws UnsupportedDataTypeException, JAXBException, SAXException, WSMarshallerException {
	// Setup logger
	ConsoleHandler ch = new ConsoleHandler();
	ch.setLevel(Level.FINEST);
	LogManager.getLogger("PACE").addHandler(ch);
	LogManager.getLogger(SCChannel.class.getName()).addHandler(ch);

//        LogManager.getLogger("Test").addHandler(ch);

	IFD ifd = new IFD();
	ifd.addProtocol(ECardConstants.Protocol.PACE, new PACEProtocolFactory());
	EstablishContext eCtx = new EstablishContext();
	byte[] ctxHandle = ifd.establishContext(eCtx).getContextHandle();

	ListIFDs listIFDs = new ListIFDs();
	listIFDs.setContextHandle(ctxHandle);
	String ifdName = ifd.listIFDs(listIFDs).getIFDName().get(0);

	Connect connect = new Connect();
	connect.setContextHandle(ctxHandle);
	connect.setIFDName(ifdName);
	connect.setSlot(BigInteger.ZERO);
	byte[] slotHandle = ifd.connect(connect).getSlotHandle();

	// PinID: 02 = CAN
	String xmlCall = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
		+ "<iso:EstablishChannel xmlns:iso=\"urn:iso:std:iso-iec:24727:tech:schema\">\n"
		+ "  <iso:SlotHandle>" + ByteUtils.toHexString(slotHandle) + "</iso:SlotHandle>\n"
		+ "  <iso:AuthenticationProtocolData Protocol=\"urn:oid:0.4.0.127.0.7.2.2.4\">\n"
		+ "    <iso:PinID>02</iso:PinID>\n"
		+ "    <iso:CHAT>7f4c12060904007f0007030102025305300301ffb7</iso:CHAT>\n"
		+ "    <iso:PIN>142390</iso:PIN>\n"
		+ "  </iso:AuthenticationProtocolData>\n"
		+ "</iso:EstablishChannel>";
	WSMarshaller m = WSMarshallerFactory.createInstance();
	EstablishChannel eCh = (EstablishChannel) m.unmarshal(m.str2doc(xmlCall));

	EstablishChannelResponse eChR = ifd.establishChannel(eCh);

	DIDAuthenticationDataType data = eChR.getAuthenticationProtocolData();
	System.out.println(data.getClass());
	AuthDataMap map;
	try {
	    map = new AuthDataMap(data);
	    System.out.println(ByteUtils.toHexString(map.getContentAsBytes("EFCardAccess")));
	} catch (ParserConfigurationException ex) {
	    Logger.getLogger(PACETest.class.getName()).log(Level.SEVERE, null, ex);
	}


//        PACEInputType pi = new PACECapabilities(slotHandle)
	logger.log(Level.INFO, eChR.getResult().getResultMajor());
	System.out.println(eChR.getResult().getResultMajor());
	System.out.println(eChR.getResult().getResultMessage().getValue());
    }

}
