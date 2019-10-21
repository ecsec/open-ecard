/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.mobile.activation.common;

import java.net.URL;
import org.openecard.mobile.activation.ActivationController;
import org.openecard.mobile.activation.ControllerCallback;

/**
 *
 * @author Neil Crossley
 */
class CommonActivationController implements ActivationController {

    private final URL requestURI;
    private final String protocolType;
    private final ActivationControllerService activationControllerService;
    private final ControllerCallback activation;
    private final InteractionPreperationFactory hooks;

    public CommonActivationController(URL requestURI, String protocolType, ActivationControllerService activationControllerService, ControllerCallback activation, InteractionPreperationFactory hooks) {
	this.requestURI = requestURI;
	this.protocolType = protocolType;
	this.activationControllerService = activationControllerService;
	this.activation = activation;
	this.hooks = hooks;
    }

    @Override
    public void start() {
	this.activationControllerService.start(this.requestURI, this.activation, this.hooks);
    }

    @Override
    public void cancelAuthentication() {
	this.activationControllerService.cancelAuthentication(this.activation);
    }

    @Override
    public String getProtocolType() {
	return this.protocolType;
    }
}
