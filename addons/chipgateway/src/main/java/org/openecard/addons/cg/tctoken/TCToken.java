/****************************************************************************
 * Copyright (C) 2014-2016 ecsec GmbH.
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

package org.openecard.addons.cg.tctoken;

import generated.TCTokenType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.annotation.Nonnull;
import org.openecard.addons.cg.ex.InvalidRedirectUrlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.openecard.addons.cg.ex.ErrorTranslations.*;
import org.openecard.addons.cg.ex.InvalidTCTokenElement;
import org.openecard.addons.cg.ex.ResultMinor;
import org.openecard.common.util.UrlBuilder;


/**
 * Helper class adding further functionality to the underlying TCTokenType.
 *
 * @author Tobias Wich
 */
public class TCToken extends TCTokenType {

    private static final Logger LOG = LoggerFactory.getLogger(TCToken.class);

    public static TCToken generateToken(Map<String, String> params) throws InvalidRedirectUrlException, InvalidTCTokenElement {
	String serverAddress = params.get("ServerAddress");
	String sessionId = params.get("SessionIdentifier");
        String refreshAddr = params.get("RefreshAddress");
	String binding = params.get("Binding");
	String pathSecProto = params.get("PathSecurity-Protocol");
	String pathSecParams = params.get("PathSecurity-Parameters");
	String forceProcessing = params.get("ForceProcessing");

	TCToken token = new TCToken();
	token.setServerAddress(serverAddress);
	token.setSessionIdentifier(sessionId);
        token.setRefreshAddress(refreshAddr);
	token.setBinding(binding);
	token.setPathSecurityProtocol(pathSecProto);
	if (pathSecParams != null) {
	    TCTokenType.PathSecurityParameters pathSecParamsObj = new TCTokenType.PathSecurityParameters();
	    if ("http://ws.openecard.org/pathsecurity/tlsv12-with-pin-encryption".equals(pathSecProto)) {
		pathSecParamsObj.setJWK(pathSecParams);
		token.setPathSecurityParameters(pathSecParamsObj);
	    }
	}
	Boolean forceProcessingBool;
	if (forceProcessing == null || "".equals(forceProcessing)) {
	    // the default when no value is set
	    forceProcessingBool = true;
	} else {
	    forceProcessingBool = Boolean.parseBoolean(forceProcessing);
	}
	token.setForceProcessing(forceProcessingBool);

	// validate TCToken
	TCTokenVerifier verifier = new TCTokenVerifier(token);
	verifier.verifyRequestToken();

	return token;
    }

    public String finalizeErrorAddress(@Nonnull ResultMinor code) throws InvalidRedirectUrlException {
        try {
            URI uri = UrlBuilder.fromUrl(getRefreshAddress())
                    .queryParam("ResultMajor", "error")
                    .queryParam("ResultMinor", code.getValue())
                    .build();
            return uri.toString();
        } catch (URISyntaxException ex) {
            LOG.error("Failed to modify RefreshAddress, this should not happen due to previously executed checks.");
            throw new InvalidRedirectUrlException(INVALID_REFRESH_ADDR);
        }
    }
    
    private String finalizeOkAddressParam(String status) throws InvalidRedirectUrlException {
        try {
            URI uri = UrlBuilder.fromUrl(getRefreshAddress())
                    .queryParam("ResultMajor", "ok")
		    .queryParam("status", status)
                    .build();
            return uri.toString();
        } catch (URISyntaxException ex) {
            LOG.error("Failed to modify RefreshAddress, this should not happen due to previously executed checks.");
            throw new InvalidRedirectUrlException(INVALID_REFRESH_ADDR);
        }
    }

    public String finalizeOkAddress() throws InvalidRedirectUrlException {
	return finalizeOkAddressParam("ok");
    }

    public String finalizeBusyAddress() throws InvalidRedirectUrlException {
	return finalizeOkAddressParam("busy");
    }

}
