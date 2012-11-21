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
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import java.io.ByteArrayOutputStream;
import org.openecard.common.ECardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
* Implements convenience methods for dealing with PINs.
*
* @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
* @author Tobias Wich <tobias.wich@ecsec.de>
* @author Dirk Petrautzki <petrautzki@hs-coburg.de>
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
    public static Transmit buildVerifyTransmit(String rawPIN, PasswordAttributesType attributes, byte[] template, byte[] slotHandle) throws UtilException {

	// concatenate template with encoded pin
	byte[] pin = PINUtils.encodePin(rawPIN, attributes);
	byte[] pinCmd = ByteUtils.concatenate(template, (byte) pin.length);
	pinCmd = ByteUtils.concatenate(pinCmd, pin);

	Transmit transmit = new Transmit();
	transmit.setSlotHandle(slotHandle);
	InputAPDUInfoType pinApdu = new InputAPDUInfoType();
	pinApdu.setInputAPDU(pinCmd);
	transmit.getInputAPDUInfo().add(pinApdu);
	return transmit;
    }

    public static byte[] encodePin(String rawPin, PasswordAttributesType attributes) throws UtilException {
	byte[] mask = createPinMask(attributes);
	PasswordTypeType pwdType = attributes.getPwdType();
	int startByte = 0;

	boolean nibbleHandling = pwdType == PasswordTypeType.BCD || pwdType == PasswordTypeType.ISO_9564_1;
	int storedLen = attributes.getStoredLength().intValue();
	if (nibbleHandling) {
	    storedLen *= 2;
	    if (pwdType == PasswordTypeType.ISO_9564_1) {
		storedLen -= 2;
	    }
	}
	int minLen = attributes.getMinLength().intValue();
	int maxLen = (attributes.getMaxLength() == null) ? storedLen : attributes.getMaxLength().intValue();
	// check if pin is within boundaries
	if (!(minLen <= rawPin.length() && rawPin.length() <= maxLen)) {
	    UtilException ex = new UtilException("Supplied PIN has wrong length: minLen(" + minLen + ") <= PIN(" + rawPin.length() + ") <= maxLen(" + maxLen + ")");
	    logger.error(ex.getMessage(), ex);
	}

	// correct pin length if iso encoding
	if (pwdType == PasswordTypeType.ISO_9564_1) {
	    byte pinLen = (byte) rawPin.length();
	    mask[0] = (byte) ((mask[0] & 0xF0) | (pinLen & 0x0F));
	    startByte++;
	}

	int j = 0;
	for (int i = startByte; i < mask.length; i++) {
	    if (j >= rawPin.length()) {
		break;
	    }
	    byte correction = 0;
	    switch (pwdType) {
		case ASCII_NUMERIC:
		    correction = '0';
		    mask[i] = (byte) (getByte(rawPin.charAt(j)) + correction);
		    j++;
		    break;
		case HALF_NIBBLE_BCD:
		    mask[i] = (byte) (0xF0 | (getByte(rawPin.charAt(j)) + correction));
		    j++;
		    break;
		case ISO_9564_1:
		case BCD:
		    byte high = (byte) ((getByte(rawPin.charAt(j)) << 4) & 0xF0);
		    j++;
		    byte low = (byte) (0x0F & mask[i]);
		    if (j < rawPin.length()) {
			low = getByte(rawPin.charAt(j));
			j++;
		    }
		    mask[i] = (byte) (high | low);
		    break;
		default:
		    UtilException ex = new UtilException(ECardConstants.Minor.IFD.IO.UNKNOWN_PIN_FORMAT, "UTF-8 PINs are not supported.");
		    logger.error(ex.getMessage(), ex);
		    throw ex;
	    }
	}

	return mask;
    }

    public static byte[] createPinMask(PasswordAttributesType attributes) throws UtilException {
	PasswordTypeType pwdType = attributes.getPwdType();
	boolean nibbleHandling = pwdType == PasswordTypeType.BCD || pwdType == PasswordTypeType.ISO_9564_1;
	boolean needsPadding = attributes.getPwdFlags().contains("needs-padding");
	int storedLen = attributes.getStoredLength().intValue();
	if (nibbleHandling) {
	    storedLen *= 2;
	    if (pwdType == PasswordTypeType.ISO_9564_1) {
		storedLen -= 2;
	    }
	}
	int minLen = attributes.getMinLength().intValue();

	// check if pad char and stuff is correctly supplied
	if (minLen < storedLen) {
	    // if minlength is smaller than storedlength
	    needsPadding = true;
	} else if (nibbleHandling && (storedLen % 2) != 0) {
	    // bcd and iso use only nibbles so the stored length must be even or padding is needed
	    needsPadding = true;
	}
	byte padChar;
	if (pwdType == PasswordTypeType.ISO_9564_1) {
	    needsPadding = true; // if not already set
	    padChar = (byte) 0xFF;
	} else if (needsPadding) {
	    byte[] padChars = attributes.getPadChar();
	    if (padChars == null || padChars.length == 0) {
		UtilException ex = new UtilException("Unsupported combination of PIN parameters concerning padding.");
		logger.error(ex.getMessage(), ex);
		throw ex;
	    } else {
		padChar = padChars[0];
	    }
	} else {
	    // half-nibble BCD may fall into this category
	    padChar = (byte) 0xFF;
	}

	// assemble mask for pin
	ByteArrayOutputStream o = new ByteArrayOutputStream();
	switch (pwdType) {
	    case ISO_9564_1:
		o.write(0x20);
	    case BCD:
		for (int i = 0; i < storedLen; i += 2) {
		    o.write(padChar);
		}
		break;
	    case ASCII_NUMERIC:
	    case HALF_NIBBLE_BCD:
		for (int i = 0; i < storedLen; i++) {
		    o.write(padChar);
		}
		break;
	    default:
		UtilException ex = new UtilException(ECardConstants.Minor.IFD.IO.UNKNOWN_PIN_FORMAT, "Pin with format '" + pwdType.name() + "' not supported.");
		logger.error(ex.getMessage(), ex);
		throw ex;
	}

	byte[] result = o.toByteArray();
	return result;
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

}
