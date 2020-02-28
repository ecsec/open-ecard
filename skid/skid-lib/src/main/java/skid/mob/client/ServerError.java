/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.client;

/**
 *
 * @author Tobias Wich
 */
public class ServerError extends Exception {

    public final int responseCode;

    public ServerError(int responseCode, String msg) {
	super(msg);
	this.responseCode = responseCode;
    }

    public ServerError(int responseCode, String msg, Throwable cause) {
	super(msg, cause);
	this.responseCode = responseCode;
    }

}
