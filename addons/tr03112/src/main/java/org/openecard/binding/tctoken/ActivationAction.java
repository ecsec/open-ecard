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

package org.openecard.binding.tctoken;

import org.openecard.binding.tctoken.ex.ActivationError;
import org.openecard.binding.tctoken.ex.FatalActivationError;
import java.util.List;
import java.util.Map;
import org.openecard.addon.Context;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.Attachment;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.addon.bind.Body;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of a plugin action performing a client activation with a TCToken.
 *
 * @author Dirk Petrautzki
 * @author Benedikt Biallowons
 * @author Tobias Wich
 */
public class ActivationAction implements AppPluginAction {

    private static final Logger logger = LoggerFactory.getLogger(ActivationAction.class);

    private TCTokenHandler tokenHandler;

    @Override
    public void init(Context ctx) {
	tokenHandler = new TCTokenHandler(ctx);
    }

    @Override
    public void destroy() {
	tokenHandler = null;
    }

    @Override
    public BindingResult execute(Body body, Map<String, String> parameters, List<Attachment> attachments) {
	BindingResult response;
	try {
	    try {
		TCTokenRequest tcTokenRequest = TCTokenRequest.convert(parameters);
		response = tokenHandler.handleActivate(tcTokenRequest);
	    } catch (ActivationError ex) {
		logger.error(ex.getMessage());
		logger.debug(ex.getMessage(), ex); // stack trace only in debug level
		logger.debug("Returning result: \n{}", ex.getBindingResult());
		if (ex instanceof FatalActivationError) {
		    logger.info("Authentication failed, displaying error in Browser.");
		} else {
		    logger.info("Authentication failed, redirecting to with errors attached to the URL.");
		}
		response = ex.getBindingResult();
	    }
	} catch (RuntimeException e) {
	    response = new BindingResult(BindingResultCode.INTERNAL_ERROR);
	    logger.error(e.getMessage(), e);
	}
	return response;
    }

}
