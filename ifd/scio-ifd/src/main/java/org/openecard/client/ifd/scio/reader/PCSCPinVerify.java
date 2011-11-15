package org.openecard.client.ifd.scio.reader;

import org.openecard.client.common.USBLangID;
import org.openecard.client.common.util.Helper;
import org.openecard.client.ifd.scio.IFDException;
import org.openecard.client.ifd.scio.IFDUtils;
import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType;
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType;
import java.io.ByteArrayOutputStream;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PCSCPinVerify {

    private final PasswordTypeType pwdType;
    private final int minLen;
    private final int storedLen;
    private final int maxLen;

    public PCSCPinVerify(PasswordAttributesType attributes, byte[] cmdTemplate) throws IFDException {
	this.pwdType = attributes.getPwdType();
	this.minLen = attributes.getMinLength().intValue();
	this.storedLen = attributes.getStoredLength().intValue();
	if (attributes.getMaxLength() != null) {
	    this.maxLen = attributes.getMaxLength().intValue();
	} else {
	    if (pwdType == PasswordTypeType.ISO_9564_1) {
		this.maxLen = (storedLen*2) - 2;
	    } else if (pwdType == PasswordTypeType.BCD) {
		this.maxLen = storedLen*2;
	    } else {
		this.maxLen = this.storedLen;
	    }
	}
	// initialise content needed for serialisation
	prepareStructure(attributes, cmdTemplate);
    }

    private void prepareStructure(PasswordAttributesType attributes, byte[] cmdTemplate) throws IFDException {
	// get apdu and pin template
	byte[] pinTemplate = IFDUtils.createPinMask(attributes);
	byte[] template = Helper.concatenate(cmdTemplate, (byte)pinTemplate.length);
	setData(Helper.concatenate(template, pinTemplate));

	boolean nibbleHandling = pwdType == PasswordTypeType.BCD || pwdType == PasswordTypeType.ISO_9564_1;
	boolean isoPin = pwdType == PasswordTypeType.ISO_9564_1;
	int pinLenIdx = template.length; // pointer to byte containing pin length in iso encoding
	int pinPos = isoPin ? pinLenIdx+1 : pinLenIdx;

	// prepare bmFormatString
	byte bmSysUnits = 1; // bytes
	byte bmPinPos = (byte) (isoPin ? 1 : 0);
	byte bmJustify = 0; // left
	byte bmPinType = 0; // binary
	if (nibbleHandling) {
	    bmPinType = 1;
	} else if (pwdType == PasswordTypeType.ASCII_NUMERIC) {
	    bmPinType = 2;
	}
	this.bmFormatString = (byte) ((bmSysUnits<<7) | (bmPinPos<<3) | (bmJustify<<2) | bmPinType);

	// prepare pin block string
	byte bmPinManagement = (byte) (isoPin ? 4 : 0); // number of bits of the length field
	byte pinSize = (byte) (isoPin ? storedLen-1 : storedLen);
	this.bmPINBlockString = (byte) ((bmPinManagement<<4) | pinSize);

	// pin length format
	byte bmPinLengthUnit = 0; // bits
	byte bmPinBytePos = (byte) (isoPin ? 4 : 0);
	bmPINLengthFormat = (byte) ((bmPinLengthUnit<<4) | bmPinBytePos);

	setMinPINSize((byte)minLen);
	setMaxPINSize((byte)maxLen);
    }




    /** timeout in seconds, 0 means default */
    public byte bTimeOut = 0;
    /** timeout in seconds after first keystroke */
    public byte bTimeOut2 = 15;
    /** formatting options, USB_CCID_PIN_FORMAT */
    public byte bmFormatString = 0;
    /** bits 7-4 bit size of PIN length in APDU, bits 3-0 PIN block size in bytes after justification and formatting */
    public byte bmPINBlockString = 0;
    /** bits 7-5 RFU, bit 4 set if system units are bytes clear if system units are bits, bits 3-0 PIN length position in system units */
    public byte bmPINLengthFormat = 0;
    /** XXYY, where XX is minimum PIN size in digits, YY is maximum */
    private short wPINMaxExtraDigit = 0;
    /** Conditions under which PIN entry should be considered complete.
     * <p>The value is a bit wise OR operation:
     * <ul><li>0x1 Max size reached</li>
     *     <li>0x2 Validation key pressed</li>
     *     <li>0x4 Timeout occurred</li></ul></p> */
    private byte bEntryValidationCondition = 0x2;
    /** Number of messages to display for PIN verification management.
     * <p>The value is one of:
     * <ul><li>0x0 no string</li>
     *     <li>0x1 Message indicated by msg idx</li>
     *     <li>0xFF default CCID message</li></ul></p> */
    private byte bNumberMessage = (byte) 0xFF;
    /** Language for messages */
    private short wLangId = USBLangID.German_Standard.getCode(); // this software is international, so use german of couse ;-)
    /** Message index (should be 00).
     * <p>The first three messages should be as follows in the reader:
     * <ul><li>0x0 PIN insertion prompt: "ENTER PIN"</li>
     *     <li>0x1 PIN modification prompt: "ENTER NEW PIN"</li>
     *     <li>0x2 New PIN confirmation prompt: "CONFIRM NEW PIN"</li></ul></p> */
    private byte bMsgIndex = 0;
    /** T=1 I-block prologue field to use (fill with 00) */
    private final byte[] bTeoPrologue = new byte[] {0,0,0};
    /** length of Data to be sent to the ICC */
    private int ulDataLength = 0;
    /** Data to send to the ICC */
    private byte[] abData;


    public void setMinPINSize(byte minSize) {
	wPINMaxExtraDigit = (short) ((wPINMaxExtraDigit & 0x00FF) | (minSize << 8));
    }
    public byte getMinPINSize() {
	return (byte) ((wPINMaxExtraDigit >> 8) & 0xFF);
    }

    public void setMaxPINSize(byte maxSize) {
	wPINMaxExtraDigit = (short) ((wPINMaxExtraDigit & 0xFF00) | maxSize);
    }
    public byte getMaxPINSize() {
	return (byte) (wPINMaxExtraDigit & 0xFF);
    }

    public void setData(byte[] data) {
	if (data != null) {
	    ulDataLength = data.length;
	    abData = data;
	}
    }


    public byte[] toBytes() {
	ByteArrayOutputStream o = new ByteArrayOutputStream(42); // just a random magic number ^^
	// write all numbers to the stream
	o.write(bTimeOut);
	o.write(bTimeOut2);
	o.write(bmFormatString);
	o.write(bmPINBlockString);
	o.write(bmPINLengthFormat);
	o.write(getMaxPINSize());
	o.write(getMinPINSize());
	o.write(bEntryValidationCondition);
	o.write(bNumberMessage);
	byte lang_low  = (byte) (wLangId & 0xFF);
	byte lang_high = (byte) ((wLangId >> 8) & 0xFF);
	o.write(lang_high);
	o.write(lang_low);
	o.write(bMsgIndex);
	o.write(bTeoPrologue, 0, bTeoPrologue.length);
	byte[] ulDataLength_bytes = Helper.convertPosIntToByteArray(ulDataLength);
	for (int i=ulDataLength_bytes.length-1; i>=0; i--) {
	    o.write(ulDataLength_bytes[i]);
	}
	// write missing bytes to length field
	for (int i=ulDataLength_bytes.length; i<4; i++) {
	    o.write(0);
	}
	if (ulDataLength > 0) {
	    o.write(abData, 0, abData.length);
	}

	byte[] result = o.toByteArray();
	return result;
    }

}
