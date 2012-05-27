package org.openecard.client.common.apdu.exception;

import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.ECardException;
import org.openecard.client.common.apdu.common.CardResponseAPDU;


/**
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class APDUException extends ECardException {

    private TransmitResponse transmitResponse;
    private CardResponseAPDU responseAPDU;

    /**
     * Creates a new APDUException.
     *
     * @param msg Message
     */
    public APDUException(String msg) {
	makeException(this, msg);
    }

    /**
     * Creates a new APDUException.
     *
     * @param minor Minor message
     * @param msg Message
     */
    public APDUException(String minor, String msg) {
	makeException(this, minor, msg);
    }

    /**
     * Creates a new APDUException.
     *
     * @param r Result
     */
    public APDUException(Result r) {
	makeException(this, r);
    }

    /**
     * Creates a new APDUException.
     *
     * @param cause Cause
     */
    public APDUException(Throwable cause) {
	makeException(this, cause);
    }

    /**
     * Creates a new APDUException.
     *
     * @param cause Cause
     * @param tr TransmitResponse
     */
    public APDUException(Throwable cause, TransmitResponse tr) {
	this(cause);

	transmitResponse = tr;
	responseAPDU = new CardResponseAPDU(tr);
    }

    /**
     * Returns the TransmitResponse.
     *
     * @return TransmitResponseTransmitResponse
     */
    public TransmitResponse getTransmitResponse() {
	return transmitResponse;
    }

    /**
     * Returns the ResponseAPDU.
     *
     * @return ResponseAPDU
     */
    public CardResponseAPDU getResponseAPDU() {
	return responseAPDU;
    }
}
