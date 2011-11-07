package org.openecard.client.common.tlv;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TLVException extends Exception {

    public TLVException(String msg) {
	super(msg);
    }

    public TLVException(Throwable t) {
	super(t);
    }

}
