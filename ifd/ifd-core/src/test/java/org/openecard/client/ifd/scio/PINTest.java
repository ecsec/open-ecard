package org.openecard.client.ifd.scio;

import org.openecard.client.ifd.scio.reader.PCSCPinVerify;
import org.openecard.client.common.util.Helper;
import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.InputUnitType;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType;
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType;
import iso.std.iso_iec._24727.tech.schema.PinInputType;
import iso.std.iso_iec._24727.tech.schema.VerifyUser;
import iso.std.iso_iec._24727.tech.schema.VerifyUserResponse;
import java.math.BigInteger;
import javax.activation.UnsupportedDataTypeException;
import javax.xml.bind.JAXBException;
import org.junit.Ignore;
import static org.junit.Assert.*;
import static iso.std.iso_iec._24727.tech.schema.PasswordTypeType.*;
import org.junit.Test;
import org.openecard.client.gui.swing.SwingUserConsent;
import org.openecard.client.ws.WSMarshaller;
import org.openecard.client.ws.WSMarshallerException;
import org.openecard.client.ws.WSMarshallerFactory;
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
	assertArrayEquals(new byte[] {0x20,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF,(byte)0xFF}, pinMask);

	byte[] pinResult = IFDUtils.encodePin("123456789", pwdAttr);
	assertArrayEquals(new byte[] {0x29,0x12,0x34,0x56,0x78,(byte)0x9F,(byte)0xFF,(byte)0xFF}, pinResult);
    }

    @Test
    public void testBCD() throws IFDException {
	PasswordAttributesType pwdAttr = create(true, BCD, 4, 3, 6);
	pwdAttr.setPadChar(new byte[]{(byte)0xFF});

	byte[] pinMask = IFDUtils.createPinMask(pwdAttr);
	assertArrayEquals(new byte[] {(byte)0xFF,(byte)0xFF,(byte)0xFF}, pinMask);

	byte[] pinResult = IFDUtils.encodePin("12345", pwdAttr);
	assertArrayEquals(new byte[] {(byte)0x12,(byte)0x34,(byte)0x5F}, pinResult);
    }

    @Test
    public void testASCII() throws IFDException {
	PasswordAttributesType pwdAttr = create(false, ASCII_NUMERIC, 6, 6);

	byte[] pinResult = IFDUtils.encodePin("123456", pwdAttr);
	assertArrayEquals(new byte[] {0x31,0x32,0x33,0x34,0x35,0x36}, pinResult);

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
	assertArrayEquals(new byte[] {(byte)0xF1,(byte)0xF2,(byte)0xF3,(byte)0xF4,(byte)0xF5,(byte)0xF6}, pinResult);

	pwdAttr = create(true, HALF_NIBBLE_BCD, 6, 7);
	pwdAttr.setPadChar(new byte[]{(byte)0xFF});

	pinResult = IFDUtils.encodePin("123456", pwdAttr);
	assertArrayEquals(new byte[] {(byte)0xF1,(byte)0xF2,(byte)0xF3,(byte)0xF4,(byte)0xF5,(byte)0xF6,(byte)0xFF}, pinResult);
    }

    @Test
    public void verifyISO() throws IFDException {
	PasswordAttributesType pwdAttr = create(true, ISO_9564_1, 4, 8);
	PCSCPinVerify ctrlStruct = new PCSCPinVerify(pwdAttr, Helper.convStringToByteArray("00200001"));
	byte[] structData = ctrlStruct.toBytes();
	String pinStr = "00 20 00 01 08 20 FF FF FF FF FF FF FF"; // length=13
	String ctrlStr = "00 0F 89 47 04 0E04 02 FF 0407 00 000000 0D000000";
	byte[] referenceData = Helper.convStringWithWSToByteArray(ctrlStr + pinStr);
	assertArrayEquals(referenceData, structData);
    }

    @Test
    public void verifyASCII() throws IFDException {
	PasswordAttributesType pwdAttr = create(false, ASCII_NUMERIC, 4, 4);
	PCSCPinVerify ctrlStruct = new PCSCPinVerify(pwdAttr, Helper.convStringToByteArray("00200001"));
	byte[] structData = ctrlStruct.toBytes();
	String pinStr = "00 20 00 01 04 FF FF FF FF"; // length=9
	String ctrlStr = "00 0F 82 04 00 0404 02 FF 0407 00 000000 09000000";
	byte[] referenceData = Helper.convStringWithWSToByteArray(ctrlStr + pinStr);
	assertArrayEquals(referenceData, structData);
    }

    @Ignore
    @Test
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
	verify.setTemplate(Helper.convStringWithWSToByteArray("00 20 00 01"));
	VerifyUserResponse verifyR = ifd.verifyUser(verify);
	byte[] responseCode = verifyR.getResponse();
    }

    @Ignore
    @Test
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
		"  <iso:SlotHandle>" + Helper.convByteArrayToString(slotHandle) + "</iso:SlotHandle>\n" +
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
