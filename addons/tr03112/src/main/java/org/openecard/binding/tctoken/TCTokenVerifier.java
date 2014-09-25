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

import org.openecard.binding.tctoken.ex.InvalidTCTokenElement;
import org.openecard.binding.tctoken.ex.InvalidTCTokenUrlException;
import org.openecard.binding.tctoken.ex.SecurityViolationException;
import org.openecard.binding.tctoken.ex.InvalidRedirectUrlException;
import generated.TCTokenType;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.openecard.binding.tctoken.ex.InvalidActivationAddressException;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.common.DynamicContext;
import org.openecard.common.ECardConstants;
import org.openecard.common.util.Pair;
import org.openecard.common.util.Promise;
import org.openecard.common.util.TR03112Utils;
import org.openecard.crypto.common.asn1.cvc.CertificateDescription;


/**
 * Implements a verifier to check the elements of a TCToken.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public class TCTokenVerifier {

    private final TCToken token;
    private final ResourceContext ctx;

    /**
     * Creates a new TCTokenVerifier to verify a TCToken.
     *
     * @param token Token
     * @param ctx Context over which the token has been received.
     */
    public TCTokenVerifier(@Nonnull TCToken token, ResourceContext ctx) {
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
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws InvalidTCTokenElement Thrown in case one of the values to test is errornous.
     * @throws InvalidTCTokenUrlException Thrown in case a tested URL does not conform to the specification.
     * @throws SecurityViolationException Thrown in case the same origin policy is violated.
     */
    public void verify() throws InvalidRedirectUrlException, InvalidTCTokenElement, InvalidTCTokenUrlException, SecurityViolationException {
	// ordering is important because of the raised errors in case the first two are not https URLs
	verifyRefreshAddress();
	verifyCommunicationErrorAddress();
	verifyServerAddress();
	verifySessionIdentifier();
	verifyBinding();
	verifyPathSecurity();
    }

    /**
     * Verifies the ServerAddress element of the TCToken.
     *
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws InvalidTCTokenElement Thrown in case one of the values to test is errornous.
     * @throws InvalidTCTokenUrlException Thrown in case a tested URL does not conform to the specification.
     * @throws SecurityViolationException
     */
    public void verifyServerAddress() throws InvalidRedirectUrlException, InvalidTCTokenElement, InvalidTCTokenUrlException,
	    SecurityViolationException {
	String value = token.getServerAddress();
	try {
	    assertRequired("ServerAddress", value);
	} catch (InvalidTCTokenElement ex) {
	    try {
		if (token.getRefreshAddress() != null) {
		    // if a RefreshAddress is given we have to check the same origin policy
		    try {
			assertSameChannel("RefreshAddress", token.getRefreshAddress());
			// the assert function sets a redirect to the errorAddress but TR-03124-1 states we have to
			// return to the web session. So lets rewrite the exception
			throw new InvalidTCTokenElement(token.getRefreshAddress(), ex.getMessage());
		    } catch (InvalidRedirectUrlException ex1) {

		    } catch (InvalidTCTokenUrlException ex1) {

		    } catch (SecurityViolationException ex1) {
			// the same origin policy is not met so we have to call the refreshAddress and see if the answer
			// is a ridirect or type 302, 303 or 307
			ResourceContext newResCtx = ResourceContext.getStream(new URL(token.getRefreshAddress()));
			URL resAddr = newResCtx.getFinalResourceAddress();
			try {
			    assertSameChannel("RefreshAddress", resAddr.toString());
			    // the assert function sets a redirect to the errorAddress but TR-03124-1 states we have to
			    // return to the web session. So lets rewrite the exception
			    throw new InvalidTCTokenElement(token.getRefreshAddress(), ex.getMessage());
			} catch (InvalidRedirectUrlException ex2) {

			} catch (InvalidTCTokenUrlException ex2) {

			} catch (SecurityViolationException ex2) {

			}

			
		    }
		} else {
		    throw new InvalidTCTokenElement(token.getErrorRedirectAddress(), "No refreshAddress available.");
		}
	    } catch (ValidationError ex1) {

	    } catch (MalformedURLException ex1) {
		Logger.getLogger(TCTokenVerifier.class.getName()).log(Level.SEVERE, null, ex1);
	    } catch (IOException ex1) {
		Logger.getLogger(TCTokenVerifier.class.getName()).log(Level.SEVERE, null, ex1);
	    } catch (ResourceException ex1) {
		Logger.getLogger(TCTokenVerifier.class.getName()).log(Level.SEVERE, null, ex1);
	    } catch (InvalidActivationAddressException ex1) {
		Logger.getLogger(TCTokenVerifier.class.getName()).log(Level.SEVERE, null, ex1);
	    }
	}
	assertHttpsURL("ServerAddress", value);
    }

    /**
     * Verifies the SessionIdentifier element of the TCToken.
     *
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws InvalidTCTokenElement Thrown in case one of the values to test is errornous.
     */
    public void verifySessionIdentifier() throws InvalidRedirectUrlException, InvalidTCTokenElement {
	String value = token.getSessionIdentifier();
	assertRequired("SessionIdentifier", value);
    }

    /**
     * Verifies the RefreshAddress element of the TCToken.
     *
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws InvalidTCTokenElement Thrown in case one of the values to test is errornous.
     * @throws InvalidTCTokenUrlException Thrown in case a tested URL does not conform to the specification.
     */
    public void verifyRefreshAddress() throws InvalidRedirectUrlException, InvalidTCTokenElement, InvalidTCTokenUrlException {
	String value = token.getRefreshAddress();
	assertRequired("RefreshAddress", value);
	assertHttpsURL("RefreshAddress", value);
    }

    /**
     * Verifies the CommunicationErrorAddress element of the TCToken.
     *
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws InvalidTCTokenElement Thrown in case one of the values to test is errornous.
     * @throws InvalidTCTokenUrlException Thrown in case a tested URL does not conform to the specification.
     */
    public void verifyCommunicationErrorAddress() throws InvalidRedirectUrlException, InvalidTCTokenElement,
	    InvalidTCTokenUrlException {
	String value = token.getCommunicationErrorAddress();
	if (! checkEmpty(value)) {
	    assertRequired("CommunicationErrorAddress", value);
	    assertHttpsURL("CommunicationErrorAddress", value);
	}
    }

    /**
     * Verifies the Binding element of the TCToken.
     *
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws InvalidTCTokenElement Thrown in case one of the values to test is errornous.
     */
    public void verifyBinding() throws InvalidRedirectUrlException, InvalidTCTokenElement {
	String value = token.getBinding();
	assertRequired("Binding", value);
	checkEqualOR("Binding", value, "urn:liberty:paos:2006-08", "urn:ietf:rfc:2616");
    }

    /**
     * Verifies the PathSecurity-Protocol and PathSecurity-Parameters element of the TCToken.
     *
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws InvalidTCTokenElement Thrown in case one of the values to test is errornous.
     * @throws InvalidTCTokenUrlException Thrown in case a tested URL does not conform to the specification.
     * @throws SecurityViolationException Thrown in case the same origin policy is violated.
     */
    public void verifyPathSecurity() throws InvalidRedirectUrlException, InvalidTCTokenElement, InvalidTCTokenUrlException,
	    SecurityViolationException {
	String proto = token.getPathSecurityProtocol();
	TCTokenType.PathSecurityParameters psp = token.getPathSecurityParameters();

	// TR-03124 sec. 2.4.3
	// If no PathSecurity-Protocol/PSK is given in the TC Token, the same TLS channel as established to
	// retrieve the TC Token MUST be used for the PAOS connection, i.e. a new channel MUST NOT be established.
	if (checkEmpty(proto) && checkEmpty(psp)) {
	    assertSameChannel("ServerAddress", token.getServerAddress());
	    return;
	}

	assertRequired("PathSecurityProtocol", proto);
	String[] protos = {"urn:ietf:rfc:4346", "urn:ietf:rfc:5246", "urn:ietf:rfc:4279", "urn:ietf:rfc:5487"};
	checkEqualOR("PathSecurityProtocol", proto, protos);
	if ("urn:ietf:rfc:4279".equals(proto) || "urn:ietf:rfc:5487".equals(proto)) {
	    assertRequired("PathSecurityParameters", psp);
	    assertRequired("PSK", psp.getPSK());
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
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws InvalidTCTokenElement Thrown in case the value is not equal to any of the reference values.
     */
    private void checkEqualOR(String name, String value, String... reference) throws InvalidRedirectUrlException,
	    InvalidTCTokenElement {
	for (String string : reference) {
	    if (value.equals(string)) {
		return;
	    }
	}
	String msg = String.format("Invalid %s in TCToken.", name);
	String minor = ECardConstants.Minor.App.PARM_ERROR;
	String errorUrl = token.getErrorRedirectAddress(minor);
	throw new InvalidTCTokenElement(errorUrl, msg);
    }

    /**
     * Checks if the element is present.
     *
     * @param name Name of the element to check. This value is used to provide a concise error message.
     * @param value Value to test.
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws InvalidTCTokenElement Thrown in case the value is null or empty.
     */
    private void assertRequired(String name, Object value) throws InvalidRedirectUrlException, InvalidTCTokenElement {
	if (checkEmpty(value)) {
	    String msg = String.format("Element %s is required.", name);
	    String minor = ECardConstants.Minor.App.PARM_ERROR;
	    String errorUrl = token.getErrorRedirectAddress(minor);
	    throw new InvalidTCTokenElement(errorUrl, msg);
	}
    }

    private URL assertURL(String name, String value) throws InvalidTCTokenUrlException {
	try {
	    return new URL(value);
	} catch (MalformedURLException e) {
	    throw new InvalidTCTokenUrlException(String.format("Malformed %s URL", name));
	}
    }

    private URL assertHttpsURL(String name, String value) throws InvalidTCTokenUrlException {
	URL url = assertURL(name, value);
	if (! "https".equals(url.getProtocol())) {
	    throw new InvalidTCTokenUrlException(String.format("%s is not a https URL.", name));
	} else {
	    return url;
	}
    }

    private void assertSameChannel(String name, String address) throws InvalidRedirectUrlException,
	    InvalidTCTokenUrlException, SecurityViolationException {
	// check that everything can be handled over the same channel
	// TR-03124-1 does not mention that redirects on the TCToken address are possible and it also states that there
	// are only two channels. So I guess we should force this here as well.
	URL paosUrl = assertURL(name, address);
	List<Pair<URL, Certificate>> urls = ctx.getCerts();
	for (Pair<URL, Certificate> next : urls) {
	    if (! TR03112Utils.checkSameOriginPolicy(paosUrl, next.p1)) {
		String msg = "The same origin policy is violated for the second channel (TLS-2).";
		String minor = ECardConstants.Minor.App.PARM_ERROR;
		String errorUrl = token.getErrorRedirectAddress(minor);
		throw new SecurityViolationException(errorUrl, msg);
	    }
	}
    }

    /**
     * Checks whether a given URL matches the same origin policy with the subjectURL of the eService certificate.
     *
     * @param url The URL to check.
     * @return True if the given {@code url} fulfills the same origin policy together with the subjectURL of the
     * eService certificate, otherwise false.
     */
    private boolean checkSameOriginPolicy(URL url) throws InvalidTCTokenUrlException, ValidationError {
	try {
	    DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	    Promise<Object> descPromise = dynCtx.getPromise(TR03112Keys.ESERVICE_CERTIFICATE_DESC);
	    CertificateDescription desc = (CertificateDescription) descPromise.deref(60, TimeUnit.SECONDS);
	    URL subjectUrl = new URL(desc.getSubjectURL());
	    return TR03112Utils.checkSameOriginPolicy(url, subjectUrl);
	} catch (InterruptedException | TimeoutException ex) {
	    String msg = "Couldn't retrieve the CertificateDescription from the DynamicContext.";
	    throw new ValidationError(msg, ex);
	} catch (MalformedURLException ex) {
	    String msg = "The given url (" + url + ") is malformed and the same origin policy cant be performed";
	    throw new InvalidTCTokenUrlException(msg, ex);
	}
    }

}
