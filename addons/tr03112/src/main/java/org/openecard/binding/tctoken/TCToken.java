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

import org.openecard.binding.tctoken.ex.InvalidRedirectUrlException;
import generated.TCTokenType;
import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;
import org.openecard.common.util.UrlBuilder;


/**
 * Helper class adding further functionality to the underlying TCTokenType.
 *
 * @author Tobias Wich
 */
public class TCToken extends TCTokenType {

    private static final Logger logger = LoggerFactory.getLogger(TCToken.class);


    /**
     * Gets the CoomunicationErrorAddress for use in error conditions.
     * If the CommunicationErrorAddress is available this one is used.
     *
     * @param minor The ResultMinor string.
     * @return The error URL.
     * @throws InvalidRedirectUrlException In case the address is not present or a valid URL.
     */
    public String getComErrorAddressWithParams(@Nonnull String minor) throws InvalidRedirectUrlException {
	try {
	    String errorUrl = getCommunicationErrorAddress();
	    URI url = checkUrl(errorUrl);
	    String result = UrlBuilder.fromUrl(url).queryParam("ResultMajor", "error")
		    .queryParamUrl("ResultMinor", TCTokenHacks.fixResultMinor(minor))
		    .build().toString();
	    return result;
	} catch (URISyntaxException ex) {
	    // should not happen, but here it is anyways
	    logger.error("Construction of redirect URL failed.", ex);
	    throw new InvalidRedirectUrlException(NO_URL);
	}
    }

    private static URI checkUrl(@Nullable String urlStr) throws InvalidRedirectUrlException {
	if (urlStr != null && ! urlStr.isEmpty()) {
	    try {
		URI url = new URI(urlStr);
		return url;
	    } catch (URISyntaxException ex) {
		logger.error("No valid CommunicationErrorAddress provided.");
		throw new InvalidRedirectUrlException(NO_URL);
	    }
	} else {
	    logger.error("No CommunicationErrorAddress to perform a redirect provided.");
	    throw new InvalidRedirectUrlException(NO_REDIRECT_AVAILABLE);
	}
    }

}
