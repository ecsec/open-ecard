package org.openecard.common.ifd.scio;

/**
 * 
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class SCIOException extends Exception {

    public SCIOException(Throwable cause) {
	super(cause);
    }

    public SCIOException(String message) {
	super(message);
    }

    public SCIOException(String message, Throwable cause) {
	super(message, cause);
    }

}
