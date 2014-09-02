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

package org.openecard.binding.tctoken;

import generated.TCTokenType;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.annotation.Nonnull;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.common.ECardConstants;
import org.openecard.common.util.Pair;
import org.openecard.common.util.TR03112Utils;


/**
 * Implements a verifier to check the elements of a TCToken.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public class TCTokenVerifier {

    private final TCTokenType token;
    private final ResourceContext ctx;

    /**
     * Creates a new TCTokenVerifier to verify a TCToken.
     *
     * @param token Token
     * @param ctx Context over which the token has been received.
     */
    public TCTokenVerifier(@Nonnull TCTokenType token, ResourceContext ctx) {
	this.token = token;
	this.ctx = ctx;
    }

    /**
     * Checks if the token is a response to an error.
     * These kind of token only contain the CommunicationErrorAdress field.
     *
     * @return {@code true} if this is an error token, {@code false} otherwise.
     */
    public boolean isErrorToken() {
	if (token.getCommunicationErrorAddress() != null) {
	    // refresh address is essential, if that one is missing, it must be an error token
	    return token.getRefreshAddress() == null;
	}
	return false;
    }

    /**
     * Verifies the elements of the TCToken.
     *
     * @throws TCTokenException
     * @throws org.openecard.binding.tctoken.CommunicationError
     */
    public void verify() throws TCTokenException, CommunicationError {
	verifyServerAddress();
	verifySessionIdentifier();
	verifyRefreshAddress();
	verifyCommunicationErrorAddress();
	verifyBinding();
	verifyPathSecurity();
    }

    /**
     * Verifies the ServerAddress element of the TCToken.
     *
     * @throws TCTokenException
     */
    public void verifyServerAddress() throws TCTokenException {
	String value = token.getServerAddress();
	assertRequired("ServerAddress", value);
	assertHttpsURL("ServerAddress", value);
    }

    /**
     * Verifies the SessionIdentifier element of the TCToken.
     *
     * @throws TCTokenException
     */
    public void verifySessionIdentifier() throws TCTokenException {
	String value = token.getSessionIdentifier();
	assertRequired("SessionIdentifier", value);
    }

    /**
     * Verifies the RefreshAddress element of the TCToken.
     *
     * @throws TCTokenException
     */
    public void verifyRefreshAddress() throws TCTokenException {
	String value = token.getRefreshAddress();
	assertRequired("RefreshAddress", value);
	assertURL("RefreshAddress", value);
    }

    /**
     * Verifies the CommunicationErrorAddress element of the TCToken.
     *
     * @throws TCTokenException
     */
    public void verifyCommunicationErrorAddress() throws TCTokenException {
	String value = token.getCommunicationErrorAddress();
	if (! checkEmpty(value)) {
	    assertURL("CommunicationErrorAddress", value);
	    assertRequired("CommunicationErrorAddress", value);
	}
    }

    /**
     * Verifies the Binding element of the TCToken.
     *
     * @throws TCTokenException
     */
    public void verifyBinding() throws TCTokenException {
	String value = token.getBinding();
	assertRequired("Binding", value);
	checkEqualOR("Binding", value, "urn:liberty:paos:2006-08", "urn:ietf:rfc:2616");
    }

    /**
     * Verifies the PathSecurity-Protocol and PathSecurity-Parameters element of the TCToken.
     *
     * @throws TCTokenException
     * @throws CommunicationError
     */
    public void verifyPathSecurity() throws TCTokenException, CommunicationError {
	String proto = token.getPathSecurityProtocol();
	TCTokenType.PathSecurityParameters psp = token.getPathSecurityParameters();

	// TR-03124 sec. 2.4.3
	// If no PathSecurity-Protocol/PSK is given in the TC Token, the same TLS channel as established to
	// retrieve the TC Token MUST be used for the PAOS connection, i.e. a new channel MUST NOT be established.
	if (checkEmpty(proto) && checkEmpty(psp)) {
	    assertSameChannel();
	    return;
	}

	assertRequired("PathSecurityProtocol", proto);
	String[] protos = {"urn:ietf:rfc:4346", "urn:ietf:rfc:5246", "urn:ietf:rfc:4279", "urn:ietf:rfc:5487"};
	checkEqualOR("PathSecurityProtocol", proto, protos);
	if ("urn:ietf:rfc:4279".equals(proto) || "urn:ietf:rfc:5487".equals(proto)) {
	    assertRequired("PathSecurityParameters", psp);
	    assertRequired("PSK", psp.getPSK());
	    assertEvenNumber(psp.getPSK());
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


    private void checkEqualOR(String name, String value, String... equal) throws TCTokenException {
	for (String string : equal) {
	    if (value.equals(string)) {
		return;
	    }
	}
	throw new TCTokenException(String.format("Invalid %s in TCToken.", name));
    }

    /**
     * Checks if the element is present.
     *
     * @param value Value
     * @throws Exception
     */
    private void assertRequired(String name, Object value) throws TCTokenException {
	if (checkEmpty(value)) {
	    throw new TCTokenException(String.format("Element %s is required.", name));
	}
    }

    private URL assertURL(String name, String value) throws TCTokenException {
	try {
	    return new URL(value);
	} catch (MalformedURLException e) {
	    throw new TCTokenException(String.format("Malformed %s URL", name));
	}
    }

    private URL assertHttpsURL(String name, String value) throws TCTokenException {
	URL url = assertURL(name, value);
	if (! "https".equals(url.getProtocol())) {
	    throw new TCTokenException(String.format("%s is not a https URL.", name));
	} else {
	    return url;
	}
    }

    private void assertEvenNumber(byte[] val) throws CommunicationError {
	if ((val.length % 2) != 0) {
	    String msg = "";
	    String errorAddr = token.getCommunicationErrorAddress();
	    throw new CommunicationError(errorAddr, ECardConstants.Minor.App.INCORRECT_PARM, msg);
	}
    }

    private void assertSameChannel() throws TCTokenException {
	// check that everything can be handled over the same channel
	// TR-03124-1 does not mention that redirects on the TCToken address are possible and it also states that there
	// are only two channels. So I guess we should force this here as well.
	URL paosUrl = assertURL("ServerAddress", token.getServerAddress());
	List<Pair<URL, Certificate>> urls = ctx.getCerts();
	for (Pair<URL, Certificate> next : urls) {
	    if (! TR03112Utils.checkSameOriginPolicy(paosUrl, next.p1)) {
		throw new TCTokenException("The same origin policy is violated for the PAOS channel (TLS-2).");
	    }
	}
    }

}
