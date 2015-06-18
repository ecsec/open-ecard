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

package org.openecard.common.util;

import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType;
import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType;
import static iso.std.iso_iec._24727.tech.schema.PasswordTypeType.*;
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import org.openecard.common.ECardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
* Implements convenience methods for dealing with PINs.
*
* @author Johannes Schmoelz
* @author Tobias Wich
* @author Dirk Petrautzki
*/
public class PINUtils {

    private static final Logger logger = LoggerFactory.getLogger(PINUtils.class);

    /**
     * Build a Transmit containing a verify APDU.
     *
     * @param rawPIN the pin as entered by the user
     * @param attributes attributes of the password (e.g. encoding and length)
     * @param template the verify template
     * @param slotHandle slot handle
     * @return Transmit containing the built verify APDU
     * @throws UtilException if an pin related error occurs (e.g. wrong PIN length)
     */
    public static Transmit buildVerifyTransmit(String rawPIN, PasswordAttributesType attributes, byte[] template,
	    byte[] slotHandle) throws UtilException {
	// concatenate template with encoded pin
    	byte[] pin = PINUtils.encodePin(rawPIN, attributes);
	byte[] pinCmd = ByteUtils.concatenate(template, (byte) pin.length);
	pinCmd = ByteUtils.concatenate(pinCmd, pin);

	Transmit transmit = new Transmit();
	transmit.setSlotHandle(slotHandle);
	InputAPDUInfoType pinApdu = new InputAPDUInfoType();
	pinApdu.setInputAPDU(pinCmd);
	pinApdu.getAcceptableStatusCode().add(new byte[] {(byte)0x90, (byte)0x00});
	transmit.getInputAPDUInfo().add(pinApdu);
	return transmit;
    }

    public static byte[] encodePin(String rawPin, PasswordAttributesType attributes) throws UtilException {
	// extract attributes
	PasswordTypeType pwdType = attributes.getPwdType();
	int minLen = attributes.getMinLength().intValue();
	int maxLen = (attributes.getMaxLength() == null) ? 0 : attributes.getMaxLength().intValue();
	int storedLen = attributes.getStoredLength().intValue();
	boolean needsPadding = needsPadding(attributes);
	// check if padding is inferred

	byte padChar = getPadChar(attributes, needsPadding);

	// helper variables
	String encoding = "UTF-8";

	try {
	    switch (pwdType) {
		case ASCII_NUMERIC:
		    encoding = "US-ASCII";
		case UTF_8:
		    byte[] textPin = encodeTextPin(encoding, rawPin, minLen, storedLen, maxLen, needsPadding, padChar);
		    return textPin;
		case ISO_9564_1:
		case BCD:
		case HALF_NIBBLE_BCD:
		    byte[] bcdPin = encodeBcdPin(pwdType, rawPin, minLen, storedLen, maxLen, needsPadding, padChar);
		    return bcdPin;
		default:
		    String msg = "Unsupported PIN encoding requested.";
		    UtilException ex = new UtilException(ECardConstants.Minor.IFD.IO.UNKNOWN_PIN_FORMAT, msg);
		    logger.error(ex.getMessage(), ex);
		    throw ex;
	    }
	} catch (UnsupportedEncodingException ex) {
	    throw new UtilException(ex);
	} catch (IOException ex) {
	    throw new UtilException(ex);
	}
    }

    public static byte[] createPinMask(PasswordAttributesType attributes) throws UtilException {
	// extract attributes
	PasswordTypeType pwdType = attributes.getPwdType();
	int minLen = attributes.getMinLength().intValue();
	int maxLen = (attributes.getMaxLength() == null) ? 0 : attributes.getMaxLength().intValue();
	int storedLen = attributes.getStoredLength().intValue();
	boolean needsPadding = needsPadding(attributes);

	// opt out if needs-padding is not on
	if (! needsPadding) {
	    return new byte[0];
	}

	byte padChar = getPadChar(attributes, needsPadding);

	if (storedLen <= 0) {
	    throw new UtilException("PIN mask can only be created when storage size is known.");
	}

	// they are all the same except half nibble which
	if (HALF_NIBBLE_BCD == pwdType) {
	    padChar = (byte) (padChar | 0xF0);
	}

	byte[] mask = new byte[storedLen];
	Arrays.fill(mask, padChar);

	// iso needs a sligth correction
	if (ISO_9564_1 == pwdType) {
	    mask[0] = 0x20;
	}

	return mask;
    }

    public static byte[] encodeTextPin(String encoding, String rawPin, int minLen, int storedLen, int maxLen,
	    boolean needsPadding, byte padChar) throws UnsupportedEncodingException, UtilException {
	// perform some basic checks
	if (needsPadding && storedLen <= 0) {
	    String msg = "Padding is required, but no stored length is given.";
	    throw new UtilException(msg);
	}
	if (rawPin.length() < minLen) {
	    String msg = String.format("Entered PIN is too short, enter at least %d characters.", minLen);
	    throw new UtilException(msg);
	    //throw new UtilException("PIN contains invalid symbols.");
	}
	if (maxLen > 0 && rawPin.length() > maxLen) {
	    String msg = String.format("Entered PIN is too long, enter at most %d characters.", maxLen);
	    throw new UtilException(msg);
	}

	// get the pin string and validate it is within stored length
	Charset charset = Charset.forName(encoding);
	byte[] pinBytes = rawPin.getBytes(charset);
	if (storedLen > 0 && pinBytes.length > storedLen) {
	    String msg = String.format("Storage size for PIN exceeded, only %d bytes are allowed.", storedLen);
	    throw new UtilException(msg);
	}
	// if the pin is too short, append the necessary padding bytes
	if (needsPadding && pinBytes.length < storedLen) {
	    int missingBytes = storedLen - pinBytes.length;
	    byte[] filler = new byte[missingBytes];
	    Arrays.fill(filler, padChar);
	    pinBytes = ByteUtils.concatenate(pinBytes, filler);
	}

	return pinBytes;
    }

    public static byte[] encodeBcdPin(PasswordTypeType pwdType, String rawPin, int minLen, int storedLen, int maxLen,
	    boolean needsPadding, byte padChar) throws UtilException, IOException {
	ByteArrayOutputStream o = new ByteArrayOutputStream();
	int pinSize = rawPin.length();

	if (ISO_9564_1 == pwdType) {
	    byte head = (byte) (0x20 | (0x0F & pinSize));
	    o.write(head);
	}

	if (HALF_NIBBLE_BCD == pwdType) {
	    for (int i = 0; i < pinSize; i++) {
		char nextChar = rawPin.charAt(i);
		byte digit = (byte) (0xF0 | getByte(nextChar));
		o.write(digit);
	    }
	} else if (BCD == pwdType || ISO_9564_1 == pwdType) {
	    for (int i = 0; i < pinSize; i += 2) {
		byte b1 = (byte) (getByte(rawPin.charAt(i)) << 4);
		byte b2 = (byte) (padChar & 0x0F); // lower nibble set to pad byte
		// one char left, replace pad nibble with it
		if (i + 1 < pinSize) {
		    b2 = (byte) (getByte(rawPin.charAt(i + 1)) & 0x0F);
		}
		byte b = (byte) (b1 | b2);
		o.write(b);
	    }
	}

	// add padding bytes if needed
	if (needsPadding && o.size() < storedLen) {
	    int missingBytes = storedLen - o.size();
	    byte[] filler = new byte[missingBytes];
	    Arrays.fill(filler, padChar);
	    o.write(filler);
	}

	return o.toByteArray();
    }

    private static byte getByte(char c) throws UtilException {
	if (c >= '0' && c <= '9') {
	    return (byte) (c - '0');
	} else {
	    UtilException ex = new UtilException("Entered PIN contains invalid characters.");
	    logger.error(ex.getMessage(), ex);
	    throw ex;
	}
    }

    private static byte getPadChar(PasswordAttributesType attributes, boolean needsPadding) throws UtilException {
	if (PasswordTypeType.ISO_9564_1.equals(attributes.getPwdType())) {
	    return (byte) 0xFF;
	} else {
	    byte[] padChars = attributes.getPadChar();
	    if (padChars != null && padChars.length == 1) {
		return padChars[0];
	    } else if (needsPadding) {
		UtilException ex = new UtilException("Unsupported combination of PIN parameters concerning padding.");
		throw ex;
	    } else {
		// just return a value, it is not gonna be used in this case
		return 0;
	    }
	}
    }

    private static boolean needsPadding(PasswordAttributesType attributes) {
	PasswordTypeType pwdType = attributes.getPwdType();
	if (ISO_9564_1 == pwdType) {
	    return true;
	} else {
	    boolean needsPadding = attributes.getPwdFlags().contains("needs-padding");
	    return needsPadding;
	}
    }

}
