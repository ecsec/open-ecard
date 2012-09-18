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
package org.openecard.client.control.module.tctoken;

import generated.TCTokenType;
import java.net.URL;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.ValueValidator;


/**
 * Implements a verifier to check the elements of a TCToken.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenVerifier {

    private TCTokenType token;

    /**
     * Creates a new TCTokenVerifier to verify a TCToken.
     *
     * @param token Token
     */
    public TCTokenVerifier(TCTokenType token) {
	this.token = token;
    }

    /**
     * Verifies the elements of the TCToken.
     *
     * @throws TCTokenException
     */
    public void verify() throws TCTokenException {
	if (token == null) {
	    throw new IllegalStateException();
	}

	try {
	    verifyServerAddress();
	    verifySessionIdentifier();
	    verifyRefreshAddress();
	    verifyBinding();
	    verifyPathSecurityParameters();
	    verifyPathSecurityProtocol();
	} catch (TCTokenException e) {
	    throw new TCTokenException("TCToken is malformed", e);
	}
    }

    /**
     * Verifies the ServerAddress element of the TCToken.
     *
     * @throws Exception
     */
    public void verifyServerAddress() throws TCTokenException {
	try {
	    String value = token.getServerAddress();
	    assertURL(value);
	    assertRequired(value);
	} catch (TCTokenException e) {
	    throw new TCTokenException("Malformed ServerAddress");
	}
    }

    /**
     * Verifies the SessionIdentifier element of the TCToken.
     *
     * @throws Exception
     */
    public void verifySessionIdentifier() throws TCTokenException {
	try {
	    String value = token.getSessionIdentifier();
	    assertRequired(value);
	    checkSessionLength(value);
	} catch (TCTokenException e) {
	    throw new TCTokenException("Malformed SessionIdentifier");
	}
    }

    /**
     * Verifies the RefreshAddress element of the TCToken.
     *
     * @throws Exception
     */
    public void verifyRefreshAddress() throws TCTokenException {
	try {
	    String value = token.getRefreshAddress();
	    assertURL(value);
	    assertRequired(value);
	} catch (TCTokenException e) {
	    throw new TCTokenException("Malformed RefreshAddress");
	}
    }

    /**
     * Verifies the Binding element of the TCToken.
     *
     * @throws Exception
     */
    public void verifyBinding() throws TCTokenException {
	try {
	    String value = token.getBinding();
	    assertRequired(value);
	    checkEqual(value, "urn:liberty:paos:2006-08");
	} catch (TCTokenException e) {
	    throw new TCTokenException("Malformed Binding");
	}
    }

    /**
     * Verifies the PathSecurity-Protocol element of the TCToken.
     *
     * @throws Exception
     */
    public void verifyPathSecurityProtocol() throws TCTokenException {
	try {
	    String value = token.getPathSecurityProtocol();
	    if (!checkEmpty(value)) {
		checkEqualOR(value, "urn:ietf:rfc:4346", "urn:ietf:rfc:4279", "urn:ietf:rfc:5487");
	    }
	} catch (TCTokenException e) {
	    throw new TCTokenException("Malformed PathSecurityProtocol");
	}
    }

    /**
     * Verifies the PathSecurity-Parameter element of the TCToken.
     *
     * @throws Exception
     */
    public void verifyPathSecurityParameters() throws TCTokenException {
	try {
	    if (token.getPathSecurityProtocol().equals("urn:ietf:rfc:4279")
		    || token.getPathSecurityProtocol().equals("urn:ietf:rfc:5487")) {
		TCTokenType.PathSecurityParameters psp = token.getPathSecurityParameters();
		if (!checkEmpty(psp)) {
		    assertRequired(psp.getPSK());
		    checkPSKLength(ByteUtils.toHexString(psp.getPSK()));
		}
	    }
	} catch (TCTokenException e) {
	    throw new TCTokenException("Malformed PathSecurityParameters");
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
     * Checks if the value is equal.
     *
     * @param value Value
     * @param equal Equal
     * @throws Exception
     */
    private void checkEqual(String value, String equal) throws TCTokenException {
	if (!value.equals(equal)) {
	    throw new TCTokenException("Element is not equal to " + equal);
	}
    }

    private void checkEqualOR(String value, String... equal) throws TCTokenException {
	for (String string : equal) {
	    if (value.equals(string)) {
		return;
	    }
	}
	throw new TCTokenException();
    }

    /**
     * Checks if the element is present.
     *
     * @param value Value
     * @throws Exception
     */
    private void assertRequired(Object value) throws TCTokenException {
	if (checkEmpty(value)) {
	    throw new TCTokenException("Element is required.");
	}
    }

    private void assertURL(Object value) throws TCTokenException {
	try {
	    new URL(value.toString());
	} catch (Exception e) {
	    throw new TCTokenException("Malformed URL");
	}
    }

    private void checkSessionLength(String value) throws TCTokenException {
	if (!ValueValidator.checkSessionStrength(value)) {
	    throw new TCTokenException("The number of bytes in the session is too small.");
	}
    }

    private void checkPSKLength(String value) throws TCTokenException {
	if (!ValueValidator.checkPSKStrength(value)) {
	    throw new TCTokenException("The number of bytes in the PSK is too small.");
	}
    }

}
