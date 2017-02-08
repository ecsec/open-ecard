/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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

import org.openecard.addons.cg.ex.InvalidTCTokenElement;
import org.openecard.addons.cg.ex.InvalidRedirectUrlException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.annotation.Nonnull;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;
import static org.openecard.addons.cg.ex.ErrorTranslations.*;


/**
 * Implements a verifier to check the elements of a TCToken.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public class TCTokenVerifier {

    private final TCToken token;

    /**
     * Creates a new TCTokenVerifier to verifyUrlToken a TCToken.
     *
     * @param token Token
     */
    public TCTokenVerifier(@Nonnull TCToken token) {
	this.token = token;
    }

    /**
     * Verifies the elements of the TCToken.
     *
     * @throws InvalidRedirectUrlException Thrown in case the RefreshAddress is missing or invalid.
     * @throws InvalidTCTokenElement Thrown in case any element inside the TCToken is invalid.
     */
    public void verifyRequestToken() throws InvalidRedirectUrlException, InvalidTCTokenElement {
        assertRefreshURL(token.getRefreshAddress());
	assertHttpsURL("ServerAddress", token.getServerAddress());
	assertRequired("SessionIdentifier", token.getSessionIdentifier());
        assertRequired("PathSecurity-Protocol", token.getPathSecurityProtocol());
        checkEqualOR("PathSecurity-Protocol", token.getPathSecurityProtocol(),
                "urn:ietf:rfc:5246",
                "http://ws.openecard.org/pathsecurity/tlsv12-with-pin-encryption");
        if (token.getPathSecurityProtocol().equals("http://ws.openecard.org/pathsecurity/tlsv12-with-pin-encryption")) {
            assertRequired("PathSecurity-Parameters", token.getPathSecurityParameters());
	    assertRequired("JWK", token.getPathSecurityParameters().getJWK());
	    try {
		JsonWebKey key = JsonWebKey.Factory.newJwk(token.getPathSecurityParameters().getJWK());
	    } catch (JoseException ex) {
		throw new InvalidTCTokenElement("Failed to parse JWK.", ex);
	    }
        }
    }


    /**
     * Checks if the value is "empty".
     *
     * @param value Value
     * @return True if the element is empty, otherwise false
     */
    private boolean checkEmpty(Object value) {
	if (value != null) {
	    if (value instanceof String) {
		if (((String) value).isEmpty()) {
		    return true;
		}
	    } else if (value instanceof URL) {
		if (((URL) value).toString().isEmpty()) {
		    return true;
		}
	    } else if (value instanceof byte[]) {
		if (((byte[]) value).length == 0) {
		    return true;
		}
	    }
	    return false;
	}
	return true;
    }


    /**
     * Checks the value for equality against any of the given reference values.
     *
     * @param name Name of the element to check. This value is used to provide a concise error message.
     * @param value Value to test.
     * @param reference Reference values to test equality against.
     * @throws InvalidTCTokenElement Thrown in case the value is not equal to any of the reference values.
     */
    private void checkEqualOR(String name, String value, String... reference) throws InvalidTCTokenElement {
	for (String string : reference) {
	    if (value.equals(string)) {
		return;
	    }
	}

	throw new InvalidTCTokenElement(ELEMENT_VALUE_INVALID, name);
    }

    /**
     * Checks if the element is present.
     *
     * @param name Name of the element to check. This value is used to provide a concise error message.
     * @param value Value to test.
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws InvalidTCTokenElement Thrown in case the value is null or empty.
     */
    private void assertRequired(String name, Object value) throws InvalidTCTokenElement {
	if (checkEmpty(value)) {
	    throw new InvalidTCTokenElement(ELEMENT_MISSING, name);
	}
    }

    private URL assertURL(String name, String value) throws InvalidTCTokenElement {
	try {
	    return new URL(value);
	} catch (MalformedURLException e) {
	    throw new InvalidTCTokenElement(MALFORMED_URL, name);
	}
    }

    private URL assertHttpsURL(String name, String value) throws InvalidTCTokenElement {
	URL url = assertURL(name, value);
	if (! "https".equals(url.getProtocol())) {
	    throw new InvalidTCTokenElement(NO_HTTPS_URL, name);
	} else {
	    return url;
	}
    }

    private URL assertRefreshURL(String value) throws InvalidRedirectUrlException {
        try {
            URL url = new URL(value);
            if (! "https".equals(url.getProtocol())) {
                throw new InvalidRedirectUrlException(INVALID_REFRESH_ADDR);
            } else {
                return url;
            }
        } catch (MalformedURLException ex) {
            throw new InvalidRedirectUrlException(INVALID_REFRESH_ADDR);
        }
    }

}
