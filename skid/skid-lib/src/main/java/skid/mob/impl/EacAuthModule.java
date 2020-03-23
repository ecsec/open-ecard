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
import org.openecard.mobile.activation.ControllerCallback;
import org.openecard.mobile.activation.EacControllerFactory;
import skid.mob.lib.Cancellable;
import skid.mob.lib.EacModule;
import skid.mob.lib.ResultHandler;
import skid.mob.lib.SkidEacInteraction;


/**
 *
 * @author Tobias Wich
 */
public class EacAuthModule implements EacModule {

    private final EacControllerFactory eacFac;
    private final String actUrl;

    EacAuthModule(EacControllerFactory eacFac, String actUrl) {
	this.eacFac = eacFac;
	this.actUrl = actUrl;
    }

    @Override
    public Cancellable runEac(SkidEacInteraction interactionComponent, ResultHandler resultHandler) {
	ActivationController controller = eacFac.create(actUrl, new ControllerCallback() {
	    @Override
	    public void onStarted() {
		// not handed down to the application
	    }

	    @Override
	    public void onAuthenticationCompletion(ActivationResult result) {
		resultHandler.done(new EacResultImpl(result));
	    }
	}, new EacInteractionWrapper(interactionComponent));

	return controller::cancelOngoingAuthentication;
    }

}
