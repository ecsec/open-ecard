/****************************************************************************
 * Copyright (C) 2013-2014 HS Coburg.
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

package org.openecard.addons.status;

import java.util.List;
import java.util.Map;
import org.openecard.addon.Context;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.Attachment;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.addon.bind.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Action processing Status messages.
 * 
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
public class StatusAction implements AppPluginAction {

    private static final Logger logger = LoggerFactory.getLogger(StatusAction.class);

    private StatusHandler statusHandler;

    @Override
    public void init(Context ctx) {
	statusHandler = new StatusHandler(ctx);
    }

    @Override
    public void destroy() {
	statusHandler = null;
    }

    @Override
    public BindingResult execute(RequestBody body, Map<String, String> parameters, List<Attachment> attachments) {
	BindingResult response;
	try {
	    StatusRequest statusRequest = StatusRequest.convert(parameters);
	    response = statusHandler.handleRequest(statusRequest);
	} catch (Exception e) {
	    response = new BindingResult(BindingResultCode.INTERNAL_ERROR);
	    logger.error(e.getMessage(), e);
	}
	return response;
    }

}
