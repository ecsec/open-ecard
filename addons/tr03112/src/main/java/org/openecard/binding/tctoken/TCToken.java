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
import java.net.MalformedURLException;
import java.net.URL;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;


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
	    String errorUrl = TCToken.this.getCommunicationErrorAddress();
	    checkUrl(errorUrl);
	    String result = TCTokenHacks.addParameterToUrl(errorUrl, "ResultMajor", "error");
	    result = TCTokenHacks.addParameterToUrl(result, "ResultMinor", TCTokenHacks.fixResultMinor(minor));
	    return result;
	} catch (MalformedURLException ex) {
	    // should not happen, but here it is anyways
	    throw new InvalidRedirectUrlException(NO_URL);
	}
    }

    private static void checkUrl(@Nullable String urlStr) throws InvalidRedirectUrlException {
	if (urlStr != null && ! urlStr.isEmpty()) {
	    try {
		URL url = new URL(urlStr);
	    } catch (MalformedURLException ex) {
		throw new InvalidRedirectUrlException(NO_URL);
	    }
	} else {
	    throw new InvalidRedirectUrlException(NO_REDIRECT_AVAILABLE);
	}
    }

}
