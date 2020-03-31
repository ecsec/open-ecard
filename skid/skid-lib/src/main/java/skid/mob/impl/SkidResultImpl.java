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

import org.openecard.mobile.activation.ServiceErrorResponse;
import skid.mob.lib.SkidErrorCodes;
import skid.mob.lib.SkidResult;


/**
 *
 * @author Tobias Wich
 */
public class SkidResultImpl implements SkidResult {
    
    private final SkidErrorCodes code;
    private final String msg;

    public SkidResultImpl() {
	code = null;
	msg = null;
    }

    public SkidResultImpl(SkidErrorCodes code, String msg) {
	this.code = code;
	this.msg = msg;
    }

    public static SkidResult fromOecServiceError(ServiceErrorResponse oecError) {
	// TODO: map error codes
	return new SkidResultImpl(SkidErrorCodes.UNKNOWN_ERROR, "Unknown Error occurred.");
    }

    @Override
    public boolean isError() {
	return code != null;
    }

    @Override
    public boolean isOk() {
	return ! isError();
    }

    @Override
    public SkidErrorCodes resultCode() {
	return code;
    }

    @Override
    public String msg() {
	return msg;
    }

}
