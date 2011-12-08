package org.openecard.client.ws;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class MarshallingTypeException extends WSMarshallerException {

    public MarshallingTypeException(String message) {
        super(message);
    }

    public MarshallingTypeException(Throwable cause) {
        super(cause);
    }

    public MarshallingTypeException(String message, Throwable cause) {
        super(message, cause);
    }

}
