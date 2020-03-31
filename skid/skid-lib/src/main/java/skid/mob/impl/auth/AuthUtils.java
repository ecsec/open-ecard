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

import org.openecard.mobile.activation.ActivationResultCode;
import skid.mob.lib.SkidErrorCodes;

/**
 *
 * @author Tobias Wich
 */
public class AuthUtils {

    public static SkidErrorCodes convCode(ActivationResultCode ac) {
	switch (ac) {
	    case OK:
	    case REDIRECT:
		return null;
	    case INTERRUPTED:
		return SkidErrorCodes.INTERRUPTED;
	    case DEPENDING_HOST_UNREACHABLE:
		return SkidErrorCodes.NETWORK_ERROR;
	    case CLIENT_ERROR:
	    case INTERNAL_ERROR:
		return SkidErrorCodes.INTERNAL_ERROR;
	    case BAD_REQUEST:
		return SkidErrorCodes.UNKNOWN_ERROR;
	    default:
		throw new IllegalStateException("Unhandled case statement for a constant found.");
	}
    }

}
