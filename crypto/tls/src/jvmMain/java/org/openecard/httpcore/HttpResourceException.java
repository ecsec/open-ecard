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
 *
 * @author Tobias Wich
 */
public class HttpResourceException extends Exception {

    public HttpResourceException(String msg) {
	super(msg);
    }

    public HttpResourceException(String msg, Throwable cause) {
	super(msg, cause);
    }

}
