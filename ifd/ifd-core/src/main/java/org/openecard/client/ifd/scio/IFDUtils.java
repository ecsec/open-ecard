/*
 * Copyright 2012 Johannes Schmoelz ecsec GmbH
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

package org.openecard.client.ifd.scio;

import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType;
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.logging.LogManager;


/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class IFDUtils {

    private static final Logger _logger = LogManager.getLogger(IFDUtils.class.getName()); 

    /**
     * Extracts the slot index from the specified ifd name.
     * @param name Name of IFD
     * @return slot index
     * @throws IFDException 
     */
    public static BigInteger getSlotIndex(String name) throws IFDException {
	return BigInteger.ZERO;
    }

    public static boolean arrayEquals(byte[] a, byte[] b) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
            _logger.entering(IFDUtils.class.getName(), "arrayEquals(byte[] a, byte[] b)", new Object[]{a, b});
        } // </editor-fold>
        if (a == null && b == null) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
                _logger.exiting(IFDUtils.class.getName(), "arrayEquals(byte[] a, byte[] b)", Boolean.FALSE);
            } // </editor-fold>
            return false;
	} else {
            Boolean result = Arrays.equals(a, b);
	    // <editor-fold defaultstate="collapsed" desc="log trace">
            if (_logger.isLoggable(Level.FINER)) {
                _logger.exiting(IFDUtils.class.getName(), "arrayEquals(byte[] a, byte[] b)", result);
            } // </editor-fold>
            return result.booleanValue();
	}
    }



    private static byte getByte(char c) throws IFDException {
	if (c >= '0' && c <= '9') {
	    return (byte) (c - '0');
	} else {
	    IFDException ex = new IFDException("Entered PIN contains invalid characters.");
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.throwing(IFDUtils.class.getName(), "getByte(char)", ex);
	    } // </editor-fold>
	    throw ex;
	}
    }

    public static byte[] encodePin(String rawPin, PasswordAttributesType attributes) throws IFDException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(IFDUtils.class.getName(), "encodePin(String, PasswordAttributesType)", new Object[]{rawPin, attributes});
	} // </editor-fold>
	byte[] mask = createPinMask(attributes);
	PasswordTypeType pwdType = attributes.getPwdType();
	int startByte = 0;

	boolean nibbleHandling = pwdType == PasswordTypeType.BCD || pwdType == PasswordTypeType.ISO_9564_1;
	int storedLen = attributes.getStoredLength().intValue();
	if (nibbleHandling) {
	    storedLen *= 2;
	    if (pwdType == PasswordTypeType.ISO_9564_1) {
		storedLen -=2;
	    }
	}
	int minLen = attributes.getMinLength().intValue();
	int maxLen = (attributes.getMaxLength() == null) ? storedLen : attributes.getMaxLength().intValue();
	// check if pin is within boundaries
	if (!(minLen <= rawPin.length() && rawPin.length() <= maxLen)) {
	    IFDException ex = new IFDException("Supplied PIN has wrong length: minLen(" + minLen + ") <= PIN(" + rawPin.length() + ") <= maxLen(" + maxLen + ")");
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.throwing(IFDUtils.class.getName(), "encodePin(String, PasswordAttributesType)", ex);
	    } // </editor-fold>
	}

	// correct pin length if iso encoding
	if (pwdType == PasswordTypeType.ISO_9564_1) {
	    byte pinLen = (byte) rawPin.length();
	    mask[0] = (byte) ((mask[0] & 0xF0) | (pinLen & 0x0F));
	    startByte++;
	}

	int j=0;
	for (int i=startByte; i<mask.length; i++) {
	    if (j>=rawPin.length()) {
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
		    if (j<rawPin.length()) {
			low = getByte(rawPin.charAt(j));
			j++;
		    }
		    mask[i] = (byte) (high | low);
		    break;
		default:
		    IFDException ex = new IFDException(ECardConstants.Minor.IFD.IO.UNKNOWN_PIN_FORMAT, "UTF-8 PINs are not supported.");
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.FINER)) {
			_logger.throwing(IFDUtils.class.getName(), "encodePin(String, PasswordAttributesType)", ex);
		    } // </editor-fold>
		    throw ex;
	    }
	}

	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(IFDUtils.class.getName(), "encodePin(String, PasswordAttributesType)", mask);
	} // </editor-fold>
	return mask;
    }

    public static byte[] createPinMask(PasswordAttributesType attributes) throws IFDException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(IFDUtils.class.getName(), "createPinMask(PasswordAttributesType)", new Object[]{attributes});
	} // </editor-fold>
	PasswordTypeType pwdType = attributes.getPwdType();
	boolean nibbleHandling = pwdType == PasswordTypeType.BCD || pwdType == PasswordTypeType.ISO_9564_1;
	boolean needsPadding = attributes.getPwdFlags().contains("needs-padding");
	int storedLen = attributes.getStoredLength().intValue();
	if (nibbleHandling) {
	    storedLen *= 2;
	    if (pwdType == PasswordTypeType.ISO_9564_1) {
		storedLen -=2;
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
	    padChar = (byte)0xFF;
	} else if (needsPadding) {
	    byte[] padChars = attributes.getPadChar();
	    if (padChars == null || padChars.length == 0) {
		IFDException ex = new IFDException("Unsupported combination of PIN parameters concerning padding.");
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.throwing(IFDUtils.class.getName(), "createPinMask(PasswordAttributesType)", ex);
		} // </editor-fold>
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
		for (int i=0; i<storedLen; i+=2) {
		    o.write(padChar);
		}
		break;
	    case ASCII_NUMERIC:
	    case HALF_NIBBLE_BCD:
		for (int i=0; i<storedLen; i++) {
		    o.write(padChar);
		}
		break;
	    default:
		IFDException ex = new IFDException(ECardConstants.Minor.IFD.IO.UNKNOWN_PIN_FORMAT, "Pin with format '" + pwdType.name() + "' not supported.");
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.throwing(IFDUtils.class.getName(), "createPinMask(PasswordAttributesType)", ex);
		} // </editor-fold>
		throw ex;
	}

	byte[] result = o.toByteArray();
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(IFDUtils.class.getName(), "createPinMask(PasswordAttributesType)", result);
	} // </editor-fold>
	return result;
    }

}
