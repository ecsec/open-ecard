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
package org.openecard.client.common.apdu.common;

import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.util.ByteUtils;


/**
 * Implements a response APDU.
 * See ISO/IEC 7816-4 Section 5.1.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class CardResponseAPDU extends CardAPDU {

    private static final Logger logger = Logger.getLogger("APDU");
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
     * Returns the status byte SW1.
     *
     * @return SW1
     */
    public byte getSW1() {
	return trailer[0];
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
	return trailer[1];
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
	return (short) ((getSW1() << 8) | getSW2());
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
	    logger.log(Level.SEVERE, "Exception", e);
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
