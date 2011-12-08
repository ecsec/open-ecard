package org.openecard.client.common;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class GenericFactoryException extends Exception {

    public GenericFactoryException(String message) {
	super(message);
    }

    public GenericFactoryException(Throwable cause) {
	super(cause);
    }

    public GenericFactoryException(String message, Throwable cause) {
	super(message, cause);
    }

}
