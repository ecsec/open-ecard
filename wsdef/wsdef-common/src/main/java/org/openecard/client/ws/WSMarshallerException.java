package org.openecard.client.ws;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class WSMarshallerException extends Exception {

    public WSMarshallerException(String message) {
        super(message);
    }

    public WSMarshallerException(Throwable cause) {
        super(cause);
    }

    public WSMarshallerException(String message, Throwable cause) {
        super(message, cause);
    }

}
