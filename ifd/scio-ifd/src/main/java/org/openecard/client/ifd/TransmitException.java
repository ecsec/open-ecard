package org.openecard.client.ifd;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TransmitException extends IFDException {

    public final byte[] rapdu;

    public TransmitException(byte[] rapdu, String msg) {
	super(msg);
	this.rapdu = rapdu;
    }

    public TransmitException(byte[] rapdu) {
	super("Unexpected response code.");
	this.rapdu = rapdu;
    }

    public byte[] getResponseAPDU() {
	return rapdu;
    }

}
