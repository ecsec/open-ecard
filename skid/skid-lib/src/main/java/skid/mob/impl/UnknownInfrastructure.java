/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.impl;

/**
 *
 * @author Tobias Wich
 */
public class UnknownInfrastructure extends Exception {

    public UnknownInfrastructure(String msg) {
	super(msg);
    }

    public UnknownInfrastructure(String msg, Throwable cause) {
	super(msg, cause);
    }

}
