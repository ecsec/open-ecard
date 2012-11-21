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

package org.openecard.common.apdu.common;

import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import org.openecard.common.util.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a response APDU.
 * See ISO/IEC 7816-4 Section 5.1.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class CardResponseAPDU extends CardAPDU {

    private static final Logger logger = LoggerFactory.getLogger(CardResponseAPDU.class);

    private byte[] trailer;

    /**
     * Creates a new response APDU.
     */
    public CardResponseAPDU() {
	trailer = new byte[2];
    }

    /**
     * Creates a new response APDU.
     *
     * @param responseAPDU Response APDU
     */
    public CardResponseAPDU(byte[] responseAPDU) {
	trailer = new byte[2];
	data = new byte[responseAPDU.length - 2];

	System.arraycopy(responseAPDU, 0, data, 0, responseAPDU.length - 2);
	System.arraycopy(responseAPDU, responseAPDU.length - 2, trailer, 0, 2);
    }

    /**
     * Creates a new response APDU.
     *
     * @param data Data field
     * @param trailer Trailer (SW1, SW2)
     */
    public CardResponseAPDU(byte[] data, byte[] trailer) {
	setData(data);
	setTrailer(trailer);
    }

    /**
     * Creates a new response APDU.
     *
     * @param transmitResponse TransmitResponse
     */
    public CardResponseAPDU(TransmitResponse transmitResponse) {
	this(transmitResponse.getOutputAPDU().get(0));
    }

    /**
     * Returns the data field of the APDU.
     *
     * @param responseAPDU Response APDU
     * @return Data field of the APDU
     */
    public static byte[] getData(byte[] responseAPDU) {
	if (responseAPDU.length < 2) {
	    throw new IllegalArgumentException("Malformed APDU");
	}

	return ByteUtils.copy(responseAPDU, 0, responseAPDU.length - 2);
    }

    /**
     * Sets the trailer (status bytes) of the APDU.
     *
     * @param trailer Trailer (SW1, SW2)
     */
    public void setTrailer(byte[] trailer) {
	setTrailer(trailer);
    }

    /**
     * Returns the trailer (status bytes) of the APDU.
     *
     * @return Trailer (SW1, SW2)
     */
    public byte[] getTrailer() {
	return trailer;
    }

    /**
     * Returns the trailer (status bytes) of the APDU.
     *
     * @param responseAPDU Response APDU
     * @return Trailer of the APDU
     */
    public static byte[] getTrailer(byte[] responseAPDU) {
	if (responseAPDU.length < 2) {
	    throw new IllegalArgumentException("Malformed APDU");
	}

	return ByteUtils.copy(responseAPDU, responseAPDU.length - 2, 2);
    }

    /**
     * Returns the status byte SW1.
     *
     * @return SW1
     */
    public byte getSW1() {
	return (byte) (trailer[0] & 0xFF);
    }

    /**
     * Sets the status byte SW1.
     *
     * @param sw1 SW1
     */
    protected void setSW1(byte sw1) {
	trailer[0] = sw1;
    }

    /**
     * Returns the status byte SW1.
     *
     * @return SW2
     */
    public byte getSW2() {
	return (byte) (trailer[1] & 0xFF);
    }

    /**
     * Sets the status byte SW2.
     *
     * @param sw2 SW2
     */
    protected void setSW2(byte sw2) {
	trailer[1] = sw2;
    }

    /**
     * Returns the status bytes of the APDU.
     *
     * @return Status bytes
     */
    public short getSW() {
	return (short) (((trailer[0] & 0xFF) << 8) | (trailer[1] & 0xFF));
    }

    /**
     * Returns the status bytes of the APDU.
     *
     * @return Status bytes
     */
    public byte[] getStatusBytes() {
	return getTrailer();
    }

    /**
     * Returns the status message of the APDU.
     *
     * @return Status bytes
     */
    public String getStatusMessage() {
	return CardCommandStatus.getMessage(getTrailer());
    }

    /**
     * Sets the status bytes of the APDU.
     *
     * @param statusbytes Status bytes
     */
    protected void setStatusBytes(byte[] statusbytes) {
	setTrailer(statusbytes);
    }

    /**
     * Checks if the status bytes indicates an normal processing.
     * See ISO/IEC 7816-4 Section 5.1.3
     *
     * @return True if SW = 0x9000, otherwise false
     */
    public boolean isNormalProcessed() {
	if (Arrays.equals(trailer, new byte[]{(byte) 0x90, (byte) 0x00})) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Checks if the status bytes indicates a warning processing.
     * See ISO/IEC 7816-4 Section 5.1.3
     *
     * @return True if SW = 0x62XX or 0x63XX, otherwise false
     */
    public boolean isWarningProcessed() {
	if (trailer[0] == (byte) 0x62 || trailer[0] == (byte) 0x63) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Checks if the status bytes indicates an execution error.
     * See ISO/IEC 7816-4 Section 5.1.3
     *
     * @return True if SW = 0x64XX to 0x66XX, otherwise false
     */
    public boolean isExecutionError() {
	if (trailer[0] == (byte) 0x64 || trailer[0] == (byte) 0x65 || trailer[0] == (byte) 0x66) {
	    return true;
	} else {
	    return false;
	}
    }

    /**
     * Checks if the status bytes indicates an checking error.
     * See ISO/IEC 7816-4 Section 5.1.3
     *
     * @return True if SW = 0x67XX to 0x6FXX, otherwise false
     */
    public boolean isCheckingError() {
	if ((trailer[0] & 0xF0) == (byte) 0x60) {
	    for (byte b = 0x07; b < 0x0F; b++) {
		if ((trailer[0] & 0x0F) == b) {
		    return true;
		}
	    }
	}
	return false;
    }

    /**
     * Checks if the status bytes equals to a element of the list of positive responses.
     *
     * @param responses Positive responses
     * @return True if the status bytes equals to a element of the list of positive responses, otherwise false
     */
    public boolean isPositiveResponse(List<byte[]> responses) {
	for (int i = 0; i < responses.size(); i++) {
	    if (Arrays.equals(responses.get(i), trailer)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Returns the byte encoded APDU: TRAILER | DATA
     *
     * @return Encoded APDU
     */
    public byte[] toByteArray() {
	ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length + 2);

	try {
	    baos.write(trailer);
	    baos.write(data);
	} catch (Exception e) {
	    logger.error("Exception", e);
	}

	return baos.toByteArray();
    }

    /**
     * Returns the bytes of the APDU as a hex encoded string.
     *
     * @return Hex encoded string of the APDU
     */
    public String toHexString() {
	return ByteUtils.toHexString(toByteArray());
    }

    /**
     * Returns the bytes of the APDU as a hex encoded string.
     *
     * @return Hex encoded string of the APDU
     */
    @Override
    public String toString() {
	return ByteUtils.toHexString(toByteArray(), true);
    }

}
