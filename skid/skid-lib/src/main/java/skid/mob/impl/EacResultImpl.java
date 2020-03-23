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
import org.openecard.mobile.activation.ActivationResultCode;
import skid.mob.lib.EacResult;

/**
 *
 * @author Tobias Wich
 */
public class EacResultImpl implements EacResult {

    private final ActivationResult result;

    public EacResultImpl(ActivationResult result) {
	this.result = result;
    }

    @Override
    public ActivationResultCode getResultCode() {
	return result.getResultCode();
    }

    @Override
    public String getErrorMessage() {
	return result.getErrorMessage();
    }

    @Override
    public String getRedirectUrl() {
	return result.getRedirectUrl();
    }

    @Override
    public String getProcessResultMinor() {
	return result.getProcessResultMinor();
    }

}
