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
import org.openecard.common.event.EventObject;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.mobile.activation.ActivationController;
import org.openecard.mobile.activation.ControllerCallback;
import org.openecard.mobile.activation.PinManagementControllerFactory;
import org.openecard.mobile.activation.PinManagementInteraction;
import org.openecard.mobile.system.OpeneCardContext;
import org.openecard.mobile.ui.InsertCardNavigatorFactory;
import org.openecard.mobile.ui.PINManagementNavigatorFactory;

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
    private final NFCDialogMsgSetter msgSetter;

    public CommonPinManagementControllerFactory(URL activationUrl, ActivationControllerService activationControllerService, NFCDialogMsgSetter msgSetter) {
	this.activationControllerService = activationControllerService;
	this.activationUrl = activationUrl;
	this.msgSetter = msgSetter;
    }

    @Override
    public ActivationController create(ControllerCallback activation, PinManagementInteraction interaction) {

	return create(new HashSet<>(), activation, interaction, msgSetter);
    }

    public ActivationController create(Set<String> supportedCards, ControllerCallback activation, PinManagementInteraction interaction, NFCDialogMsgSetter msgSetter) {

	InteractionPreperationFactory hooks = new InteractionPreperationFactory() {
	    @Override
	    public AutoCloseable create(OpeneCardContext context) {
		final EventDispatcher eventDispatcher = context.getEventDispatcher();
		CommonCardEventHandler handler = new CommonCardEventHandler(interaction, false, msgSetter);

		EventCallback powerDownDevices = new EventCallback() {
		    @Override
		    public void signalEvent(EventType eventType, EventObject eventData) {
			handler.onCardInteractionComplete();
		    }
		};

		eventDispatcher.add(powerDownDevices, EventType.POWER_DOWN_DEVICES);

		return new ArrayBackedAutoCloseable(new AutoCloseable[]{
		    CommonCardEventHandler.hookUp(handler, supportedCards, eventDispatcher, interaction, msgSetter),
		    InteractionRegistrationHandler.hookUp(PINManagementNavigatorFactory.PROTOCOL_TYPE, context, interaction),
		    InteractionRegistrationHandler.hookUp(InsertCardNavigatorFactory.PROTOCOL_TYPE, context, interaction),
		    new AutoCloseable() {
			@Override
			public void close() throws Exception {
			    eventDispatcher.del(powerDownDevices);
			}

		    }
		}
		);
	    }
	};

	CommonActivationController controller = new CommonActivationController(activationUrl, PROTOCOL_TYPE, activationControllerService, activation, hooks, null);

	controller.start();

	return controller;
    }

    @Override
    public void destroy(ActivationController controller) {

    }

    static CommonPinManagementControllerFactory create(ActivationControllerService activationControllerService, NFCDialogMsgSetter msgSetter) throws MalformedURLException {
	return new CommonPinManagementControllerFactory(
		ACTIVATION_URL,
		activationControllerService, msgSetter);
    }

}
