/****************************************************************************
 * Copyright (C) 2024 ecsec GmbH.
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

import org.openecard.common.DynamicContext;
import org.openecard.mobile.activation.*;
import org.openecard.mobile.system.OpeneCardContext;
import org.openecard.mobile.ui.EacNavigatorFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Tobias Wich
 */
public class CommonCardlinkControllerFactory implements CardlinkControllerFactory {

	public static final String WS_KEY = "websocket";


	private static final String PROTOCOL_TYPE = "cardlink";

	private final ActivationControllerService activationControllerService;
	private final NFCDialogMsgSetter msgSetter;

	public CommonCardlinkControllerFactory(ActivationControllerService activationControllerService, NFCDialogMsgSetter msgSetter) {
		this.activationControllerService = activationControllerService;
		this.msgSetter = msgSetter;
	}

	@Override
	public ActivationController create(Websocket websocket, ControllerCallback activation, CardlinkInteraction interaction) {
		if (websocket == null) {
			throw new IllegalArgumentException("The given websocket object cannot be null");
		}
		if (activation == null) {
			throw new IllegalArgumentException("The given controller callbacks cannot be null");
		}
		if (interaction == null) {
			throw new IllegalArgumentException("The given interaction callbacks cannot be null");
		}
		URL activationUrl = prepareURL();

		Map<String, Object> extraParams = new HashMap<>();
		extraParams.put(WS_KEY, websocket);

		Set<String> supportedCards = new HashSet<>();

		CommonCardEventHandler created = new CommonCardEventHandler(interaction, false, msgSetter);

		InteractionPreperationFactory hooks = new InteractionPreperationFactory() {
			@Override
			public AutoCloseable create(OpeneCardContext context) {
				return new ArrayBackedAutoCloseable(new AutoCloseable[]{
					CommonCardEventHandler.hookUp(created, supportedCards, context.getEventDispatcher(), interaction, msgSetter),
				});
			}
		};

		CommonActivationController controller = new CommonActivationController(activationUrl, PROTOCOL_TYPE, activationControllerService, activation, hooks, extraParams);

		controller.start();

		return controller;
	}

	private void prepareDynamicContext(Object websocket) {
		DynamicContext dctx = DynamicContext.getInstance("ws_context");
		dctx.put("websocket", websocket);
	}

	@Override
	public void destroy(ActivationController controller) {
	}

	static CommonCardlinkControllerFactory create(ActivationControllerService activationControllerService, NFCDialogMsgSetter msgSetter) {
		return new CommonCardlinkControllerFactory(
			activationControllerService, msgSetter);
	}

	private URL prepareURL() {
		try {
			return new URL("http://localhost:24727/cardlink");
		} catch (MalformedURLException ex) {
			throw new RuntimeException("The given url string could not be parsed as a URL.", ex);
		}
	}
}
