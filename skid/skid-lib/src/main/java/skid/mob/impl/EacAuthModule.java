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

import org.openecard.mobile.activation.ActivationController;
import org.openecard.mobile.activation.ActivationResult;
import org.openecard.mobile.activation.ActivationResultCode;
import org.openecard.mobile.activation.ControllerCallback;
import org.openecard.mobile.activation.EacControllerFactory;
import org.openecard.mobile.activation.EacInteraction;
import skid.mob.lib.Cancellable;
import skid.mob.lib.EacModule;
import skid.mob.lib.EacResult;
import skid.mob.lib.FinishedCallback;
import skid.mob.lib.ResultHandler;


/**
 *
 * @author Tobias Wich
 */
public class EacAuthModule implements EacModule {

    private final EacControllerFactory eacFac;
    private final String actUrl;
    private final FinishedCallback finishedCb;

    EacAuthModule(EacControllerFactory eacFac, String actUrl, FinishedCallback finishedCb) {
	this.eacFac = eacFac;
	this.actUrl = actUrl;
	this.finishedCb = finishedCb;
    }

    @Override
    public Cancellable runEac(EacInteraction interactionComponent, ResultHandler resultHandler) {
	ActivationController controller = eacFac.create(actUrl, new ControllerCallback() {
	    @Override
	    public void onStarted() {
		// not handed down to the application
	    }

	    @Override
	    public void onAuthenticationCompletion(ActivationResult result) {
		resultHandler.done(new EacResult() {
		    @Override
		    public ActivationResultCode getResultCode() {
			return result.getResultCode();
		    }

		    @Override
		    public String getErrorMessage() {
			return result.getErrorMessage();
		    }
		});
		if (result.getRedirectUrl() != null) {
		    finishedCb.finished(actUrl);
		}
	    }
	}, interactionComponent);

	return controller::cancelOngoingAuthentication;
    }

}
