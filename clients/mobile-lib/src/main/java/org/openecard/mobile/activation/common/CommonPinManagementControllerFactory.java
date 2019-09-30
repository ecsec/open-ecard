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
package org.openecard.mobile.activation.common;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import org.openecard.mobile.activation.ActivationController;
import org.openecard.mobile.activation.ControllerCallback;
import org.openecard.mobile.activation.PinManagementControllerFactory;
import org.openecard.mobile.activation.PinManagementInteraction;

/**
 *
 * @author Neil Crossley
 */
public class CommonPinManagementControllerFactory implements PinManagementControllerFactory {

    private static final String PROTOCOL_TYPE = "urn:oid:1.3.162.15480.3.0.9";

    public static final URL ACTIVATION_URL;

    static {
	try {
	    ACTIVATION_URL = new URL("http://localhost:2427/eID-Client?ShowUI=PINManagement");
	} catch (MalformedURLException ex) {
	    throw new IllegalStateException("Could not create PIN management ULR.", ex);
	}
    }

    private final ActivationControllerService activationControllerService;
    private final URL activationUrl;

    public CommonPinManagementControllerFactory(URL activationUrl, ActivationControllerService activationControllerService) {
	this.activationControllerService = activationControllerService;
	this.activationUrl = activationUrl;
    }

    @Override
    public ActivationController create(ControllerCallback activation, PinManagementInteraction interaction) {
	return new CommonActivationController(activationUrl, new HashSet<>(), PROTOCOL_TYPE, activationControllerService, activation, interaction);
    }

    public ActivationController create(Set<String> supportedCard, ControllerCallback activation, PinManagementInteraction interaction) {
	return new CommonActivationController(activationUrl, supportedCard, PROTOCOL_TYPE, activationControllerService, activation, interaction);
    }

    @Override
    public void destroy(ActivationController controller) {

    }

    static CommonPinManagementControllerFactory create(ActivationControllerService activationControllerService) throws MalformedURLException {
	return new CommonPinManagementControllerFactory(
		ACTIVATION_URL,
		activationControllerService);
    }

}
