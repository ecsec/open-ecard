/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

import org.openecard.binding.tctoken.ex.InvalidRedirectUrl;
import generated.TCTokenType;
import java.net.MalformedURLException;
import java.net.URL;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class adding further functionality to the underlying TCTokenType.
 *
 * @author Tobias Wich
 */
public class TCToken extends TCTokenType {

    private static final Logger logger = LoggerFactory.getLogger(TCToken.class);

    /**
     * Gets the redirect address for use in error conditions.
     * If the CommunicationErrorAddress is available this one is used. The RefreshAddress is used as a fallback.
     *
     * @return The error URL.
     * @throws InvalidRedirectUrl In case the address is not present or a valid URL.
     */
    public String getErrorRedirectAddress() throws InvalidRedirectUrl {
	try {
	    checkHttpsUrl(communicationErrorAddress);
	    return communicationErrorAddress;
	} catch (InvalidRedirectUrl ex) {
	    // try next
	    logger.info("Not CommunicationErrorAddress present in TCToken, trying RefreshUrl instead.");
	}
	checkHttpsUrl(refreshAddress);
	return refreshAddress;
    }

    public String getErrorRedirectAddress(@Nonnull String minor) throws InvalidRedirectUrl {
	try {
	    String errorUrl = getErrorRedirectAddress();
	    String result = TCTokenHacks.addParameterToUrl(errorUrl, "ResultMajor", "error");
	    result = TCTokenHacks.addParameterToUrl(result, "ResultMinor", minor);
	    return result;
	} catch (MalformedURLException ex) {
	    // should not happen, but here it is anyways
	    throw new InvalidRedirectUrl("Error redirect URL is not a URL.");
	}
    }

    private static void checkHttpsUrl(@Nullable String urlStr) throws InvalidRedirectUrl {
	if (urlStr != null && ! urlStr.isEmpty()) {
	    try {
		URL url = new URL(urlStr);
		if (! "https".equals(url.getProtocol())) {
		    throw new InvalidRedirectUrl("Error redirect URL is not an HTTPS URL.");
		}
	    } catch (MalformedURLException ex) {
		throw new InvalidRedirectUrl("Error redirect URL is not a URL.");
	    }
	} else {
	    throw new InvalidRedirectUrl("No redirect address available for an error redirect.");
	}
    }

}
