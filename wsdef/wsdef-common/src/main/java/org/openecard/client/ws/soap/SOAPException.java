package org.openecard.client.ws.soap;

import org.openecard.client.ws.WSMarshallerException;



/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SOAPException extends WSMarshallerException {

    public SOAPException(String message) {
        super(message);
    }

    public SOAPException(Throwable cause) {
        super(cause);
    }

    public SOAPException(String message, Throwable cause) {
        super(message, cause);
    }

}
