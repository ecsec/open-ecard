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

package org.openecard.client.ifd.scio;

import static iso.std.iso_iec._24727.tech.schema.PasswordTypeType.*;
import iso.std.iso_iec._24727.tech.schema.*;
import java.math.BigInteger;
import javax.activation.UnsupportedDataTypeException;
import javax.xml.bind.JAXBException;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.StringUtils;
import org.openecard.client.gui.swing.SwingDialogWrapper;
import org.openecard.client.gui.swing.SwingUserConsent;
import org.openecard.client.ifd.scio.reader.PCSCPinVerify;
import org.openecard.client.ws.WSMarshaller;
import org.openecard.client.ws.WSMarshallerException;
import org.openecard.client.ws.WSMarshallerFactory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PINTest {

    private static PasswordAttributesType create(boolean needsPadding, PasswordTypeType pwdType, int minLen, int storedLen, int maxLen) {
	PasswordAttributesType r = create(needsPadding, pwdType, minLen, storedLen);
	r.setMaxLength(BigInteger.valueOf(maxLen));
	return r;
    }
    private static PasswordAttributesType create(boolean needsPadding, PasswordTypeType pwdType, int minLen, int storedLen) {
	PasswordAttributesType r = new PasswordAttributesType();
	r.setMinLength(BigInteger.valueOf(minLen));
	r.setStoredLength(BigInteger.valueOf(storedLen));
	r.setPwdType(pwdType);
	if (needsPadding) {
	    r.getPwdFlags().add("needs-padding");
	}
	return r;
    }


    @Test
    public void testISO() throws IFDException {
	PasswordAttributesType pwdAttr = create(true, ISO_9564_1, 4, 8, 12);

	byte[] pinMask = IFDUtils.createPinMask(pwdAttr);
	assertEquals(new byte[] {0x20,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF}, pinMask);

	byte[] pinResult = IFDUtils.encodePin("123456789", pwdAttr);
	assertEquals(new byte[] {0x29,0x12,0x34,0x56,0x78,(byte)0x9F,(byte)0xFF,(byte)0xFF}, pinResult);
    }

    @Test
    public void testBCD() throws IFDException {
	PasswordAttributesType pwdAttr = create(true, BCD, 4, 3, 6);
	pwdAttr.setPadChar(new byte[]{(byte)0xFF});

	byte[] pinMask = IFDUtils.createPinMask(pwdAttr);
	assertEquals(new byte[] {(byte)0xFF,(byte)0xFF,(byte)0xFF}, pinMask);

	byte[] pinResult = IFDUtils.encodePin("12345", pwdAttr);
	assertEquals(new byte[] {(byte)0x12,(byte)0x34,(byte)0x5F}, pinResult);
    }

    @Test
    public void testASCII() throws IFDException {
	PasswordAttributesType pwdAttr = create(false, ASCII_NUMERIC, 6, 6);

	byte[] pinResult = IFDUtils.encodePin("123456", pwdAttr);
	assertEquals(new byte[] {0x31,0x32,0x33,0x34,0x35,0x36}, pinResult);

	try {
	    pwdAttr = create(true, ASCII_NUMERIC, 6, 6);
	    IFDUtils.encodePin("123456", pwdAttr);
	    fail(); // padding needed, but no char given
	} catch (IFDException ex) {
	}
	try {
	    pwdAttr = create(false, ASCII_NUMERIC, 6, 7);
	    IFDUtils.encodePin("123456", pwdAttr);
	    fail(); // padding inferred, but no char given
	} catch (IFDException ex) {
	}
    }

    @Test
    public void testHalfNibble() throws IFDException {
	PasswordAttributesType pwdAttr = create(false, HALF_NIBBLE_BCD, 6, 6);

	byte[] pinResult = IFDUtils.encodePin("123456", pwdAttr);
	assertEquals(new byte[] {(byte)0xF1,(byte)0xF2,(byte)0xF3,(byte)0xF4,(byte)0xF5,(byte)0xF6}, pinResult);

	pwdAttr = create(true, HALF_NIBBLE_BCD, 6, 7);
	pwdAttr.setPadChar(new byte[]{(byte)0xFF});

	pinResult = IFDUtils.encodePin("123456", pwdAttr);
	assertEquals(new byte[] {(byte)0xF1,(byte)0xF2,(byte)0xF3,(byte)0xF4,(byte)0xF5,(byte)0xF6,(byte)0xFF}, pinResult);
    }

    @Test
    public void verifyISO() throws IFDException {
	PasswordAttributesType pwdAttr = create(true, ISO_9564_1, 4, 8);
	PCSCPinVerify ctrlStruct = new PCSCPinVerify(pwdAttr, StringUtils.toByteArray("00200001"));
	byte[] structData = ctrlStruct.toBytes();
	String pinStr = "00 20 00 01 08 20 FF FF FF FF FF FF FF"; // length=13
	String ctrlStr = "00 0F 89 47 04 0E04 02 FF 0407 00 000000 0D000000";
	byte[] referenceData = StringUtils.toByteArray(ctrlStr + pinStr, true);
	assertEquals(referenceData, structData);
    }

    @Test
    public void verifyASCII() throws IFDException {
	PasswordAttributesType pwdAttr = create(false, ASCII_NUMERIC, 4, 4);
	PCSCPinVerify ctrlStruct = new PCSCPinVerify(pwdAttr, StringUtils.toByteArray("00200001"));
	byte[] structData = ctrlStruct.toBytes();
	String pinStr = "00 20 00 01 04 FF FF FF FF"; // length=9
	String ctrlStr = "00 0F 82 04 00 0404 02 FF 0407 00 000000 09000000";
	byte[] referenceData = StringUtils.toByteArray(ctrlStr + pinStr, true);
	assertEquals(referenceData, structData);
    }


    @Test(enabled=false)
    public void verifyeGK() {
	IFD ifd = new IFD();
	ifd.setGUI(new SwingUserConsent(new SwingDialogWrapper()));
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

	VerifyUser verify = new VerifyUser();
	verify.setSlotHandle(slotHandle);
	InputUnitType inputUnit = new InputUnitType();
	verify.setInputUnit(inputUnit);
	PinInputType pinInput = new PinInputType();
	inputUnit.setPinInput(pinInput);
	pinInput.setIndex(BigInteger.ZERO);
	pinInput.setPasswordAttributes(create(true, ISO_9564_1, 6, 8, 8));
	verify.setTemplate(StringUtils.toByteArray("00 20 00 01", true));
	VerifyUserResponse verifyR = ifd.verifyUser(verify);
	byte[] responseCode = verifyR.getResponse();
    }


    @Test(enabled=false)
    public void executePACE_PIN() throws UnsupportedDataTypeException, JAXBException, SAXException, WSMarshallerException {
	IFD ifd = new IFD();
	ifd.setGUI(new SwingUserConsent(new SwingDialogWrapper()));
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

	// prepare pace call
	String xmlCall = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<iso:EstablishChannel xmlns:iso=\"urn:iso:std:iso-iec:24727:tech:schema\">\n" +
		"  <iso:SlotHandle>" + ByteUtils.toHexString(slotHandle) + "</iso:SlotHandle>\n" +
		"  <iso:AuthenticationProtocolData Protocol=\"urn:oid:0.4.0.127.0.7.2.2.4\">\n" +
		"    <iso:PinID>03</iso:PinID>\n" +
		"  </iso:AuthenticationProtocolData>\n" +
		"</iso:EstablishChannel>";
	WSMarshaller m = WSMarshallerFactory.createInstance();
	EstablishChannel eCh = (EstablishChannel) m.unmarshal(m.str2doc(xmlCall));

	// send pace call
	EstablishChannelResponse eChR = ifd.establishChannel(eCh);
    }

}
