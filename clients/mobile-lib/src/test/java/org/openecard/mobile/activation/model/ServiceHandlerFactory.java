/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.mobile.activation.model;

import org.openecard.common.util.Promise;
import org.openecard.mobile.activation.OpeneCardServiceHandler;
import org.openecard.mobile.activation.ServerErrorResponse;

/**
 *
 * @author Neil Crossley
 */
public final class ServiceHandlerFactory {

    private ServiceHandlerFactory() {
    }

    public static OpeneCardServiceHandler create(Promise<ServerErrorResponse> result) {
	return new OpeneCardServiceHandler() {
	    @Override
	    public void onSuccess() {
		result.deliver(null);
	    }

	    @Override
	    public void onFailure(ServerErrorResponse response) {
		result.deliver(response);
	    }
	};
    }
}
