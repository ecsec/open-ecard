/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.connector.tctoken;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.ValueValidator;
import org.openecard.client.connector.common.ConnectorConstants.ConnectorError;


/**
 * Implements a verifier to check the elements of a TCToken.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenVerifier {

    private List<TCToken> tokens;
    private TCToken token;

    /**
     * Creates a new TCTokenVerifier to verify a TCToken.
     *
     * @param tokens Tokens
     */
    public TCTokenVerifier(List<TCToken> tokens) {
	this.tokens = tokens;
    }

    /**
     * Creates a new TCTokenVerifier to verify a TCToken.
     *
     * @param token Token
     */
    public TCTokenVerifier(TCToken token) {
	tokens = new ArrayList<TCToken>();
	tokens.add(token);
    }

    /**
     * Verifies the elements of the TCToken.
     *
     * @throws TCTokenException
     */
    public void verify() throws TCTokenException {
	if (tokens.isEmpty()) {
	    String message = ConnectorError.TC_TOKEN_NOT_AVAILABLE.toString();
	    Logger.getLogger(TCTokenVerifier.class.getName()).log(Level.SEVERE, message);
	    throw new TCTokenException(message);
	}

	try {
	    for (Iterator<TCToken> it = tokens.iterator(); it.hasNext();) {
		token = it.next();

		verifyServerAddress();
		verifySessionIdentifier();
		verifyRefreshAddress();
		verifyBinding();
		verifyPathSecurityParameter();
		verifyPathSecurityProtocol();
	    }
	} catch (Exception e) {
	    String message = ConnectorError.TC_TOKEN_REFUSED.toString();
	    Logger.getLogger(TCTokenVerifier.class.getName()).log(Level.SEVERE, message, e);
	    throw new TCTokenException(message, e);
	}
    }

    /**
     * Verifies the ServerAddress element of the TCToken.
     *
     * @throws Exception
     */
    public void verifyServerAddress() throws Exception {
	URL value = token.getServerAddress();
	checkEmpty(value);
    }

    /**
     * Verifies the SessionIdentifier element of the TCToken.
     *
     * @throws Exception
     */
    public void verifySessionIdentifier() throws Exception {
	String value = token.getSessionIdentifier();
	checkRequired(value);
	checkSessionLength(value);
    }

    /**
     * Verifies the RefreshAddress element of the TCToken.
     *
     * @throws Exception
     */
    public void verifyRefreshAddress() throws Exception {
	URL value = token.getRefreshAddress();
	checkRequired(value);
    }

    /**
     * Verifies the Binding element of the TCToken.
     *
     * @throws Exception
     */
    public void verifyBinding() throws Exception {
	String value = token.getBinding();
	checkRequired(value);
	checkEqual(value, "urn:liberty:paos:2006-08");
    }

    /**
     * Verifies the PathSecurity-Protocol element of the TCToken.
     *
     * @throws Exception
     */
    public void verifyPathSecurityProtocol() throws Exception {
	String value = token.getPathSecurityProtocol();
	if (!checkEmpty(value)) {
	    checkEqual(value, "urn:ietf:rfc:4346");
	    checkEqual(value, "urn:ietf:rfc:4279");
	    checkEqual(value, "urn:ietf:rfc:5487");
	}
    }

    /**
     * Verifies the PathSecurity-Parameter element of the TCToken.
     *
     * @throws Exception
     */
    public void verifyPathSecurityParameter() throws Exception {
	if (token.getPathSecurityProtocol().equals("urn:ietf:rfc:4279")
	   || token.getPathSecurityProtocol().equals("urn:ietf:rfc:5487")) {
	    TCToken.PathSecurityParameter psp = token.getPathSecurityParameter();
	    if (!checkEmpty(psp)) {
		checkRequired(psp.getPSK());
		checkPSKLength(ByteUtils.toHexString(psp.getPSK()));
	    }
	}
    }

    /**
     * Checks if the value is "empty".
     *
     * @param value Value
     * @return True if the element is empty, otherwise false
     * @throws Exception
     */
    private boolean checkEmpty(Object value) throws Exception {
	if (value != null) {
	    if (value instanceof String) {
		if (!((String) value).isEmpty()) {
		    return true;
		}
	    } else if (value instanceof URL) {
		if (!((URL) value).toString().isEmpty()) {
		    return true;
		}
	    } else if (value instanceof byte[]) {
		if (((byte[]) value).length != 0) {
		    return true;
		}
	    }
	    return false;
	}
	return false;
    }

    /**
     * Checks if the value is equal.
     *
     * @param value Value
     * @param equal Equal
     * @throws Exception
     */
    private void checkEqual(String value, String equal) throws Exception {
	if (!value.equals(equal)) {
	    throw new Exception("Element is not equal to " + equal);
	}
    }

    /**
     * Checks if the element is present.
     *
     * @param value Value
     * @throws Exception
     */
    private void checkRequired(Object value) throws Exception {
	if (!checkEmpty(value)) {
	    throw new Exception("Element is required.");
	}
    }


    private void checkSessionLength(String value) throws Exception {
	// FIXME: 16, use session method in ValueValidator class
	if (ValueValidator.checkHexStrength(value, 8)) {
	    throw new Exception("The number of bytes in the session is too small.");
	}
    }
    private void checkPSKLength(String value) throws Exception {
	if (ValueValidator.checkPSKStrength(value)) {
	    throw new Exception("The number of bytes in the PSK is too small.");
	}
    }

}
