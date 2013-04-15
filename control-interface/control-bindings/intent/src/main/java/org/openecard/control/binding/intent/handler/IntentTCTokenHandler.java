/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.control.binding.intent.handler;

import android.content.Intent;
import android.net.Uri;
import java.net.URI;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.common.ECardConstants;
import org.openecard.control.module.tctoken.GenericTCTokenHandler;
import org.openecard.control.module.tctoken.TCTokenRequest;
import org.openecard.control.module.tctoken.TCTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class IntentTCTokenHandler extends IntentControlHandler {

    private static final Logger logger = LoggerFactory.getLogger(IntentTCTokenHandler.class);

    private GenericTCTokenHandler genericTCTokenHandler;

    /**
     * Create a new TCTokenHandler.
     *
     * @param genericTCTokenHandler to handle the generic part of the TCToken request
     *
     */
    public IntentTCTokenHandler(GenericTCTokenHandler genericTCTokenHandler) {
	super("/eID-Client");
	this.genericTCTokenHandler = genericTCTokenHandler;
    }

    /**
     * Handles the response from the genericTCTokenHandler.
     *
     * @param response TCTokenResponse from the generic handler
     * @return Intent for the Browser to redirect or null
     */
    private Intent handleResponse(TCTokenResponse response) {

	Result result = response.getResult();

	if (result.getResultMajor().equals(ECardConstants.Major.OK)) {
	    if (response.getRefreshAddress() != null) {
		return handleRedirectResponse(Uri.parse(response.getRefreshAddress().toString()));
	    } else {
		// TODO
	    }
	} else {
	    if (result.getResultMessage().getValue() != null) {
		return handleErrorResponse(response.getResult().getResultMinor());
	    } else {
		// TODO
	    }
	}

	return null;
    }

    /**
     * Handle a redirect response.
     *
     * @param location Redirect location
     * @return HTTP response
     */
    private Intent handleRedirectResponse(Uri location) {
	Intent browserIntent = new Intent(Intent.ACTION_VIEW, location);
	return browserIntent;
    }

    /**
     * Handle a error response.
     *
     * @param message Message
     * @return HTTP response
     */
    private Intent handleErrorResponse(String message) {
	return new Intent(message);
    }

    @Override
    public Intent handle(Intent i) {
	try {
	    URI requestURI = URI.create(i.getDataString());
	    TCTokenRequest tcTokenRequest = genericTCTokenHandler.parseRequestURI(requestURI);
	    TCTokenResponse response = genericTCTokenHandler.handleActivate(tcTokenRequest);
	    Intent intentresponse = this.handleResponse(response);
	    intentresponse.putExtra("isTokenFromObject", tcTokenRequest.isTokenFromObject());
	    return intentresponse;
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    // throw new HTTPException(HttpStatus.SC_BAD_REQUEST, e.getMessage());
	}
	return i;

    }

}
