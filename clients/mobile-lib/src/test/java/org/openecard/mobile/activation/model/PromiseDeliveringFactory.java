/** **************************************************************************
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
 ************************************************************************** */
package org.openecard.mobile.activation.model;

import org.openecard.common.util.Promise;
import org.openecard.mobile.activation.ActivationResult;
import org.openecard.mobile.activation.ActivationSource;
import org.openecard.mobile.activation.ControllerCallback;
import org.openecard.mobile.activation.ServiceErrorResponse;
import org.openecard.mobile.activation.StartServiceHandler;
import org.openecard.mobile.activation.StopServiceHandler;

/**
 *
 * @author Neil Crossley
 */
public final class PromiseDeliveringFactory {

    public static ControllerCallbackDelivery controllerCallback = new ControllerCallbackDelivery();

    private PromiseDeliveringFactory() {
    }

    public static StartServiceHandler createStartServiceDelivery(Promise<ActivationSource> success, Promise<ServiceErrorResponse> failure) {
	return new StartServiceHandler() {
	    @Override
	    public void onSuccess(ActivationSource source) {
		if (success != null) {
		    success.deliver(source);
		}
	    }

	    @Override
	    public void onFailure(ServiceErrorResponse response) {
		if (failure != null) {
		    failure.deliver(response);
		}
	    }
	};
    }

    public static StopServiceHandler createStopServiceDelivery(Promise<ServiceErrorResponse> outcome) {
	return new StopServiceHandler() {
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

    public static final class ControllerCallbackDelivery {

	private ControllerCallbackDelivery() {
	}

	public ControllerCallback deliverCompletion(Promise<ActivationResult> outcome) {
	    return new ControllerCallback() {
		@Override
		public void onAuthenticationCompletion(ActivationResult result) {
		    outcome.deliver(result);
		}

		@Override
		public void onStarted() {
		}
	    };
	}

	public ControllerCallback deliverStarted(Promise<Void> outcome) {
	    return new ControllerCallback() {
		@Override
		public void onAuthenticationCompletion(ActivationResult result) {
		}

		@Override
		public void onStarted() {
		    outcome.deliver(null);
		}
	    };
	}

	public ControllerCallback deliverStartedCompletion(Promise<Void> started, Promise<ActivationResult> completion) {
	    return new ControllerCallback() {
		@Override
		public void onAuthenticationCompletion(ActivationResult result) {
		    completion.deliver(result);
		}

		@Override
		public void onStarted() {
		    started.deliver(null);
		}
	    };
	}
    }

}
