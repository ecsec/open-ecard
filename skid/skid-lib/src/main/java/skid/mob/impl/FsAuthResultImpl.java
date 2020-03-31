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

import org.openecard.mobile.activation.ActivationResult;
import skid.mob.impl.auth.AuthUtils;
import skid.mob.lib.FsAuthResult;
import skid.mob.lib.SkidErrorCodes;


/**
 *
 * @author Tobias Wich
 */
public class FsAuthResultImpl extends AuthResultImpl implements FsAuthResult {

    protected final String redirectUrl;

    public FsAuthResultImpl(String redirectUrl, String minor, SkidErrorCodes code, String msg) {
	super(minor, code, msg);
	this.redirectUrl = redirectUrl;
    }

    public FsAuthResultImpl(SkidErrorCodes code, String msg) {
	this(null, null, code, msg);
    }

    public static FsAuthResult fromActivationResult(ActivationResult ar) {
	SkidErrorCodes code = AuthUtils.convCode(ar.getResultCode());
	return new FsAuthResultImpl(ar.getRedirectUrl(), ar.getProcessResultMinor(), code, ar.getErrorMessage());
    }

    @Override
    public String getRedirectUrl() {
	return redirectUrl;
    }

    @Override
    public boolean hasRedirectUrl() {
	return redirectUrl != null;
    }


}
