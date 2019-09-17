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
import java.util.Set;
import org.openecard.mobile.activation.ActivationController;
import org.openecard.mobile.activation.ActivationInteraction;
import org.openecard.mobile.activation.ControllerCallback;

/**
 *
 * @author Neil Crossley
 */
class CommonActivationController implements ActivationController {

    private final URL requestURI;
    private final Set<String> supportedCards;
    private final String protocolType;
    private final ActivationControllerService activationControllerService;
    private final ControllerCallback activation;
    private final ActivationInteraction interaction;

    public CommonActivationController(URL requestURI, Set<String> supportedCards, String protocolType, ActivationControllerService activationControllerService, ControllerCallback activation, ActivationInteraction interaction) {
	this.requestURI = requestURI;
	this.supportedCards = supportedCards;
	this.protocolType = protocolType;
	this.activationControllerService = activationControllerService;
	this.activation = activation;
	this.interaction = interaction;
    }

    @Override
    public void start() {
	this.activationControllerService.start(this.requestURI, supportedCards, this.activation, this.interaction);
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
