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

package org.openecard.addons.cardlink;

import org.openecard.addon.ActionInitializationException;
import org.openecard.addon.Context;
import org.openecard.addon.bind.*;
import org.openecard.mobile.activation.Websocket;

import java.util.List;
import java.util.Map;

import static org.openecard.mobile.activation.common.CommonCardlinkControllerFactory.WS_KEY;

public class ActivateAction implements AppPluginAction {

	private Context aCtx;

	@Override
	public BindingResult execute(RequestBody body, Map<String, String> parameters, Headers headers, List<Attachment> attachments, Map<String, Object> extraParams) {
		Websocket ws = (Websocket) extraParams.get(WS_KEY);
		if (ws == null) {
			return new BindingResult(BindingResultCode.WRONG_PARAMETER)
				.setResultMessage("Missing websocket in dynamic context.");
		}

		// TODO: implement
		return null;
	}

	@Override
	public void init(Context aCtx) throws ActionInitializationException {
		this.aCtx = aCtx;
	}

	@Override
	public void destroy(boolean force) {
		this.aCtx = null;
	}
}
