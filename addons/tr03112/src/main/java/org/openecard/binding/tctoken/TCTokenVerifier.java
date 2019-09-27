/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
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

import org.openecard.httpcore.ValidationError;
import org.openecard.httpcore.CertificateValidator;
import org.openecard.binding.tctoken.ex.InvalidTCTokenElement;
import org.openecard.binding.tctoken.ex.InvalidTCTokenUrlException;
import org.openecard.binding.tctoken.ex.SecurityViolationException;
import org.openecard.binding.tctoken.ex.InvalidRedirectUrlException;
import generated.TCTokenType;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import javax.annotation.Nonnull;
import org.openecard.binding.tctoken.ex.ActivationError;
import org.openecard.common.util.Pair;
import org.openecard.common.util.TR03112Utils;
import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;
import org.openecard.binding.tctoken.ex.ResultMinor;
import org.openecard.binding.tctoken.ex.UserCancellationException;
import org.bouncycastle.tls.TlsServerCertificate;
import org.openecard.common.DynamicContext;
import org.openecard.common.util.UrlBuilder;
import org.openecard.httpcore.HttpResourceException;
import org.openecard.httpcore.InvalidProxyException;
import org.openecard.httpcore.InvalidUrlException;
import org.openecard.httpcore.ResourceContext;


/**
 * Implements a verifier to check the elements of a TCToken.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public class TCTokenVerifier {

    private final TCToken token;
    private final ResourceContext ctx;

    /**
     * Creates a new TCTokenVerifier to verifyUrlToken a TCToken.
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
     * @throws UserCancellationException Thrown in case the user aborted the insert card dialog.
     */
    public void verifyUrlToken() throws InvalidRedirectUrlException, InvalidTCTokenElement, InvalidTCTokenUrlException,
	    SecurityViolationException, UserCancellationException {
	// ordering is important because of the raised errors in case the first two are not https URLs
	initialUrlTokenCheck();
	verifyRefreshAddress();
	verifyCommunicationErrorAddress();
	checkUserCancellation();
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
     * @throws UserCancellationException Thrown in case the user aborted the insert card dialog but this should never
     * happen in this case.
     */
    public void verifyServerAddress() throws InvalidRedirectUrlException, InvalidTCTokenElement,
	    UserCancellationException {
	String value = token.getServerAddress();
	try {
	    assertRequired("ServerAddress", value);
	} catch (InvalidTCTokenElement ex) {
	    determineRefreshAddress(ex);
	}

	try {
	    assertHttpsURL("ServerAddress", value);
	} catch (InvalidTCTokenUrlException ex) {
	    determineRefreshAddress(ex);
	}
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
     * @throws UserCancellationException Thrown in case the user aborted the insert card dialog but this should never
     * happen in this case.
     */
    public void verifyRefreshAddress() throws InvalidRedirectUrlException, InvalidTCTokenElement,
	    UserCancellationException {
	String value = token.getRefreshAddress();
	assertRequired("RefreshAddress", value);
	try {
	    assertHttpsURL("RefreshAddress", value);
	} catch (InvalidTCTokenUrlException ex) {
	    determineRefreshAddress(ex);
	}
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
     * @throws UserCancellationException Thrown in case the user aborted the insert card dialog but this should never
     *   happen in this case.
     */
    public void verifyPathSecurity() throws InvalidRedirectUrlException, InvalidTCTokenElement, InvalidTCTokenUrlException,
	    SecurityViolationException, UserCancellationException {
	String proto = token.getPathSecurityProtocol();
	TCTokenType.PathSecurityParameters psp = token.getPathSecurityParameters();

	// TR-03124 sec. 2.4.3
	// If no PathSecurity-Protocol/PSK is given in the TC Token, the same TLS channel as established to
	// retrieve the TC Token MUST be used for the PAOS connection, i.e. a new channel MUST NOT be established.
	if ((checkEmpty(proto) && checkEmpty(psp))) {
	    assertSameChannel("ServerAddress", token.getServerAddress());
	    return;
	} else if ((! checkEmpty(proto)) && "urn:ietf:rfc:4279".equals(proto) && checkEmpty(psp) && ! token.isInvalidPSK()) {
	    assertSameChannel("ServerAddress", token.getServerAddress());
	    return;
	}

	assertRequired("PathSecurityProtocol", proto);
	String[] protos = {"urn:ietf:rfc:5246", "urn:ietf:rfc:4279"};
	checkEqualOR("PathSecurityProtocol", proto, protos);
	if ("urn:ietf:rfc:4279".equals(proto)) {
	    if (token.isInvalidPSK()) {
		String minor = ResultMinor.COMMUNICATION_ERROR;
		String errorUrl = token.getComErrorAddressWithParams(minor);
		determineRefreshAddress(new InvalidTCTokenElement(errorUrl, INVALID_ELEMENT, "PSK"));
	    }
	    try {
		assertRequired("PathSecurityParameters", psp);
		assertRequired("PSK", psp.getPSK());
	    } catch (InvalidTCTokenElement ex) {
		determineRefreshAddress(ex);
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

	String minor = ResultMinor.COMMUNICATION_ERROR;
	String errorUrl = token.getComErrorAddressWithParams(minor);
	throw new InvalidTCTokenElement(errorUrl, INVALID_ELEMENT, (Object) name);
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
	    String minor = ResultMinor.COMMUNICATION_ERROR;
	    String errorUrl = token.getComErrorAddressWithParams(minor);
	    throw new InvalidTCTokenElement(errorUrl, MISSING_ELEMENT, name);
	}
    }

    private URL assertURL(String name, String value) throws InvalidTCTokenUrlException {
	try {
	    return new URL(value);
	} catch (MalformedURLException e) {
	    throw new InvalidTCTokenUrlException(MALFORMED_URL, name);
	}
    }

    private URL assertHttpsURL(String name, String value) throws InvalidTCTokenUrlException {
	URL url = assertURL(name, value);
	if (! "https".equals(url.getProtocol())) {
	    throw new InvalidTCTokenUrlException(NO_HTTPS_URL, name);
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
	List<Pair<URL, TlsServerCertificate>> urls = ctx.getCerts();
	for (Pair<URL, TlsServerCertificate> next : urls) {
	    if (! TR03112Utils.checkSameOriginPolicy(paosUrl, next.p1)) {
		String minor = ResultMinor.COMMUNICATION_ERROR;
		String errorUrl = token.getComErrorAddressWithParams(minor);
		throw new SecurityViolationException(errorUrl, FAILED_SOP);
	    }
	}
    }

    /**
     * Creates an{@link URL} with a communication error as parameters and also a given error message.
     *
     * @param refreshAddress The address which should get the error parameters.
     * @param minor The result minor to attach to the {@code refreshAddress}.
     * @param minorMessage The result message.
     * @return An {@link URL} object containing the error query parameters.
     * @throws URISyntaxException Thrown if the given {@code refreshAddress} is not a valid URL.
     */
    private URI createUrlWithErrorParams(String refreshAddress, String minor, String minorMessage)
	    throws URISyntaxException {
	return UrlBuilder.fromUrl(refreshAddress)
		.queryParam("ResultMajor", "error")
		.queryParamUrl("ResultMinor", TCTokenHacks.fixResultMinor(minor))
		.queryParam("ResultMessage", minorMessage)
		.build();
    }

    /**
     * Checks whether the TCToken is empty except the CommunicationErrorAddress.
     *
     * @throws InvalidRedirectUrlException
     * @throws InvalidTCTokenElement
     */
    private void initialUrlTokenCheck() throws InvalidRedirectUrlException, InvalidTCTokenElement {
	if (token.getCommunicationErrorAddress() != null && ! token.getCommunicationErrorAddress().isEmpty() &&
		token.getRefreshAddress().isEmpty() && token.getServerAddress().isEmpty() &&
		token.getSessionIdentifier().isEmpty() && token.getBinding().isEmpty() &&
		token.getPathSecurityProtocol().isEmpty()) {
	    String errorUrl = token.getComErrorAddressWithParams(ResultMinor.COMMUNICATION_ERROR);
	    throw new InvalidTCTokenElement(errorUrl, ESERVICE_FAIL);
	}
    }

    /**
     * Determines the refresh URL.
     *
     * @param ex The exception which caused the abort of the TCToken verification.
     * @throws InvalidRedirectUrlException If the CommunicationErrorAddress cant be determined.
     * @throws InvalidTCTokenElement If a determination of a refresh or CommunicationError address was successful.
     * @throws UserCancellationException Thrown in case {@code ex} is an instance of {@link UserCancellationException}.
     */
    private void determineRefreshAddress(ActivationError ex) throws InvalidRedirectUrlException,
	    InvalidTCTokenElement, UserCancellationException {
	if (token.getRefreshAddress() != null) {
	    try {
		CertificateValidator validator = new RedirectCertificateValidator(true);
		ResourceContext newResCtx = new TrResourceContextLoader().getStream(new URL(token.getRefreshAddress()), validator);
		newResCtx.closeStream();
		List<Pair<URL, TlsServerCertificate>> resultPoints = newResCtx.getCerts();
		Pair<URL, TlsServerCertificate> last = resultPoints.get(resultPoints.size() - 1);
		URL resAddr = last.p1;
		String refreshUrl = resAddr.toString();

		if (ex instanceof UserCancellationException) {
		    UserCancellationException uex = (UserCancellationException) ex;
		    URI refreshUrlAsUrl = createUrlWithErrorParams(refreshUrl, ResultMinor.CANCELLATION_BY_USER,
			    ex.getMessage());
		    throw new UserCancellationException(refreshUrlAsUrl.toString(), ex);
		}

		URI refreshUrlAsUrl = createUrlWithErrorParams(refreshUrl,
			ResultMinor.TRUSTED_CHANNEL_ESTABLISHMENT_FAILED, ex.getMessage());
		throw new InvalidTCTokenElement(refreshUrlAsUrl.toString(), ex);
	    } catch (IOException | HttpResourceException | InvalidUrlException | InvalidProxyException | ValidationError | URISyntaxException ex1) {
		String errorUrl = token.getComErrorAddressWithParams(ResultMinor.COMMUNICATION_ERROR);
		throw new InvalidTCTokenElement(errorUrl, INVALID_REFRESH_ADDRESS, ex1);
	    }
	} else {
	    String errorUrl = token.getComErrorAddressWithParams(ResultMinor.COMMUNICATION_ERROR);
	    throw new InvalidTCTokenElement(errorUrl, NO_REFRESH_ADDRESS);
	}
    }

    private void checkUserCancellation() throws InvalidRedirectUrlException, InvalidTCTokenElement,
	    UserCancellationException {
	DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	UserCancellationException ex = (UserCancellationException) dynCtx.get(TR03112Keys.CARD_SELECTION_CANCELLATION);
	if (ex != null) {
	    determineRefreshAddress(ex);
	}
    }

}
