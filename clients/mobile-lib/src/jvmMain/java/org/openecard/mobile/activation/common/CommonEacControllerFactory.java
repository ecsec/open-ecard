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
import org.openecard.mobile.activation.ActivationController;
import org.openecard.mobile.activation.ControllerCallback;
import org.openecard.mobile.activation.EacControllerFactory;
import org.openecard.mobile.activation.EacInteraction;
import org.openecard.mobile.system.OpeneCardContext;
import org.openecard.mobile.ui.EacNavigatorFactory;

/**
 *
 * @author Neil Crossley
 */
public class CommonEacControllerFactory implements EacControllerFactory {

    private static final String PROTOCOL_TYPE = "urn:oid:1.3.162.15480.3.0.14";

    private final ActivationControllerService activationControllerService;
    private final NFCDialogMsgSetter msgSetter;

    public CommonEacControllerFactory(ActivationControllerService activationControllerService, NFCDialogMsgSetter msgSetter) {
	this.activationControllerService = activationControllerService;
	this.msgSetter = msgSetter;
    }

    @Override
    public ActivationController create(String url, ControllerCallback activation, EacInteraction interaction) {
	if (url == null) {
	    throw new IllegalArgumentException("The given URL cannot be null");
	}
	if (activation == null) {
	    throw new IllegalArgumentException("The given controller callbacks cannot be null");
	}
	if (interaction == null) {
	    throw new IllegalArgumentException("The given interaction callbacks cannot be null");
	}
	URL activationUrl;
	try {
	    activationUrl = new URL(prepareURL(url));
	} catch (MalformedURLException ex) {
	    throw new RuntimeException("The given url string could not be parsed as a URL.", ex);
	}

	Set<String> supportedCards = new HashSet<>();

	CommonCardEventHandler created = new CommonCardEventHandler(interaction, false, msgSetter);

	InteractionPreperationFactory hooks = new InteractionPreperationFactory() {
	    @Override
	    public AutoCloseable create(OpeneCardContext context) {
		return new ArrayBackedAutoCloseable(new AutoCloseable[] {
		    CommonCardEventHandler.hookUp(created, supportedCards, context.getEventDispatcher(), interaction, msgSetter),
		    InteractionRegistrationHandler.hookUp(EacNavigatorFactory.PROTOCOL_TYPE, context, interaction)
		});
	    }
	};

	CommonActivationController controller = new CommonActivationController(activationUrl, PROTOCOL_TYPE, activationControllerService, activation, hooks, null);

	controller.start();

	return controller;
    }

    @Override
    public void destroy(ActivationController controller) {
    }

    static CommonEacControllerFactory create(ActivationControllerService activationControllerService, NFCDialogMsgSetter msgSetter) {
	return new CommonEacControllerFactory(
		activationControllerService, msgSetter);
    }

    String prepareURL(String inputUrl) {
	return inputUrl.replaceFirst("^eid:\\/\\/", "http://");
    }
}
