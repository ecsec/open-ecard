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
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Benedikt Biallowons <benedikt.biallowons@ecsec.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TCTokenAction implements AppPluginAction {

    private static final Logger logger = LoggerFactory.getLogger(TCTokenAction.class);

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
	    } catch (CommunicationError ex) {
		String msg = "Redirect Address: {}\nCause: {}";
		logger.debug(msg, ex.communicationErrorAddress, ex.getMessage());
		response = ex.getResult();
		logger.info("Authentication failed, redirecting to error address.");
	    } catch (TCTokenException ex) {
		logger.info(ex.getMessage(), ex);
		// TODO: translate message
		String msg = "Could not fetch or create TCToken.";
		response = new BindingResult(BindingResultCode.RESOURCE_UNAVAILABLE);
		response.setResultMessage(msg);
	    }
	} catch (Exception e) {
	    response = new BindingResult(BindingResultCode.INTERNAL_ERROR);
	    logger.error(e.getMessage(), e);
	}
	return response;
    }

}
