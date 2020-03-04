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
public class InvalidServerData extends Exception {

    public InvalidServerData(String msg) {
	super(msg);
    }

    public InvalidServerData(String msg, Throwable cause) {
	super(msg, cause);
    }

}
