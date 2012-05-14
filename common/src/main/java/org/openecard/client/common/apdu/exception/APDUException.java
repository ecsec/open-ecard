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

    public APDUException(String msg) {
	makeException(this, msg);
    }

    public APDUException(String minor, String msg) {
	makeException(this, minor, msg);
    }

    public APDUException(Result r) {
	makeException(this, r);
    }

    public APDUException(Throwable cause) {
	makeException(this, cause);
    }

    public APDUException(Throwable cause, TransmitResponse tr) {
	this(cause);

	transmitResponse = tr;
	responseAPDU = new CardResponseAPDU(tr);
    }

    public TransmitResponse getTransmitResponse() {
	return transmitResponse;
    }

    public CardResponseAPDU getResponseAPDU() {
	return responseAPDU;
    }
}
