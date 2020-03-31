/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.impl.auth;

import skid.mob.impl.SkidResultImpl;
import skid.mob.lib.AuthResult;
import skid.mob.lib.SkidErrorCodes;


/**
 *
 * @author Tobias Wich
 */
public class AuthResultImpl extends SkidResultImpl implements AuthResult {

    protected final String minor;

    public AuthResultImpl(String minor, SkidErrorCodes code, String msg) {
	super(code, msg);
	this.minor = minor;
    }

    @Override
    public String getProcessResultMinor() {
	return minor;
    }

}
