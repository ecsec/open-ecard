/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.ifd.protocol.pace;

import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import java.math.BigInteger;
import javax.activation.UnsupportedDataTypeException;
import javax.xml.bind.JAXBException;
import org.openecard.common.ClientEnv;
import org.openecard.common.ECardConstants;
import org.openecard.common.util.ByteUtils;
import org.openecard.gui.swing.SwingDialogWrapper;
import org.openecard.gui.swing.SwingUserConsent;
import org.openecard.ifd.scio.IFD;
import org.openecard.transport.dispatcher.MessageDispatcher;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PACETest {

    private static final Logger logger = LoggerFactory.getLogger(PACETest.class);

    @Test(enabled = false)
    public void executePACE_PIN() throws UnsupportedDataTypeException, JAXBException, SAXException, WSMarshallerException {
	ClientEnv env = new ClientEnv();
	MessageDispatcher dispatcher = new MessageDispatcher(env);
	IFD ifd = new IFD();

	SwingUserConsent gui = new SwingUserConsent(new SwingDialogWrapper());
	ifd.setGUI(gui);

	env.setIFD(ifd);
	env.setDispatcher(dispatcher);
	ifd.setDispatcher(dispatcher);
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

	// PinID: 02 = CAN, 03 = PIN
	String xmlCall = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
		+ "<iso:EstablishChannel xmlns:iso=\"urn:iso:std:iso-iec:24727:tech:schema\">\n"
		+ "  <iso:SlotHandle>" + ByteUtils.toHexString(slotHandle) + "</iso:SlotHandle>\n"
		+ "  <iso:AuthenticationProtocolData Protocol=\"urn:oid:0.4.0.127.0.7.2.2.4\">\n"
		+ "    <iso:PinID>02</iso:PinID>\n"
		+ "    <iso:CHAT>7f4c12060904007f0007030102025305300301ffb7</iso:CHAT>\n"
		// Remove PIN element to active the GUI
		+ "    <iso:PIN>142390</iso:PIN>\n"
		//		+ "    <iso:PIN>123456</iso:PIN>\n"
		+ "  </iso:AuthenticationProtocolData>\n"
		+ "</iso:EstablishChannel>";
	WSMarshaller m = WSMarshallerFactory.createInstance();
	EstablishChannel eCh = (EstablishChannel) m.unmarshal(m.str2doc(xmlCall));

	EstablishChannelResponse eChR = ifd.establishChannel(eCh);

	logger.info("PACE result: {}", eChR.getResult().getResultMajor());
	try {
	    logger.info("{}", eChR.getResult().getResultMinor());
	    logger.info("{}", eChR.getResult().getResultMessage().getValue());
	} catch (Exception ignore) {
	}
    }

}
