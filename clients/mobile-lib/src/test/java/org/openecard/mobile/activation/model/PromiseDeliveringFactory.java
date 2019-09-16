/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.mobile.activation.model;

import org.openecard.common.util.Promise;
import org.openecard.mobile.activation.ActivationResult;
import org.openecard.mobile.activation.ControllerCallback;
import org.openecard.mobile.activation.OpeneCardServiceHandler;
import org.openecard.mobile.activation.ServiceErrorResponse;

/**
 *
 * @author Neil Crossley
 */
public final class PromiseDeliveringFactory {

    private PromiseDeliveringFactory() {
    }

    public static OpeneCardServiceHandler createContextServiceDelivery(Promise<ServiceErrorResponse> outcome) {
	return new OpeneCardServiceHandler() {
	    @Override
	    public void onSuccess() {
		outcome.deliver(null);
	    }

	    @Override
	    public void onFailure(ServiceErrorResponse response) {
		outcome.deliver(response);
	    }
	};
    }

    public static ControllerCallback createControllerCallbackDelivery(Promise<ActivationResult> outcome) {
	return new ControllerCallback() {
	    @Override
	    public void onAuthenticationCompletion(ActivationResult result) {
		outcome.deliver(result);
	    }
	};
    }
}
