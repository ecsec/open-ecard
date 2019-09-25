/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.httpcore;


/**
 * Base error for problems with URLs seen during the execution of an HTTP exchange.
 *
 * @author Tobias Wich
 */
public class InvalidUrlException extends Exception {

    public InvalidUrlException(String msg) {
	super(msg);
    }

    public InvalidUrlException(String msg, Throwable cause) {
	super(msg, cause);
    }

}
