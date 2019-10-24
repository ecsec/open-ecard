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
package org.openecard.mobile.activation.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.mobile.activation.ActivationController;
import org.openecard.mobile.activation.ControllerCallback;
import org.openecard.mobile.activation.EacControllerFactory;
import org.openecard.mobile.activation.EacInteraction;

/**
 *
 * @author Neil Crossley
 */
public class CommonEacControllerFactory implements EacControllerFactory {

    private static final String PROTOCOL_TYPE = "urn:oid:1.3.162.15480.3.0.14";

    private final ActivationControllerService activationControllerService;

    public CommonEacControllerFactory(ActivationControllerService activationControllerService) {
	this.activationControllerService = activationControllerService;
    }

    @Override
    public ActivationController create(String url, ControllerCallback activation, EacInteraction interaction) {
	URL activationUrl;
	try {
	    activationUrl = new URL(url);
	} catch (MalformedURLException ex) {
	    throw new RuntimeException("The given url string could not be parsed as a URL.", ex);
	}

	Set<String> supportedCards = new HashSet<>();

	CommonCardEventHandler created = new CommonCardEventHandler(interaction, false, false);

	InteractionPreperationFactory hooks = new InteractionPreperationFactory() {
	    @Override
	    public AutoCloseable create(EventDispatcher dispatcher) {
		return new ArrayBackedAutoCloseable(new AutoCloseable[] {
		    CommonCardEventHandler.hookUp(created, supportedCards, dispatcher, interaction),
		    EacCardEventHandler.hookUp(new EacCardEventHandler(), dispatcher, interaction)
		});
	    }
	};

	CommonActivationController controller = new CommonActivationController(activationUrl, PROTOCOL_TYPE, activationControllerService, activation, hooks);

	controller.start();

	return controller;
    }

    @Override
    public void destroy(ActivationController controller) {
    }

    static CommonEacControllerFactory create(ActivationControllerService activationControllerService) {
	return new CommonEacControllerFactory(
		activationControllerService);
    }
}
