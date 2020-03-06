/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.impl.client;

/**
 *
 * @author Tobias Wich
 */
public class NotFound extends ServerError {

    public NotFound(String msg) {
	super(404, msg);
    }

    public NotFound(String msg, Throwable cause) {
	super(404, msg, cause);
    }

}
