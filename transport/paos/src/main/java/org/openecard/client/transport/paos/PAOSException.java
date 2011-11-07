package org.openecard.client.transport.paos;

/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PAOSException extends Exception {

    public PAOSException(String msg) {
	super(msg);
    }

    public PAOSException(String msg, Throwable cause) {
	super(msg, cause);
    }

}
