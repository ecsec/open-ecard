/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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
import org.openecard.binding.tctoken.ex.*;
import org.openecard.bouncycastle.tls.TlsServerCertificate;
import org.openecard.common.util.Pair;
import org.openecard.common.util.StringUtils;
import org.openecard.common.util.TR03112Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openecard.binding.tctoken.ex.ErrorTranslations.INVALID_TCTOKEN_URL;
import static org.openecard.binding.tctoken.ex.ErrorTranslations.NO_TOKEN;


/**
 * This class represents a TC Token request to the client. It contains the {@link TCTokenType} and situational parts
 * like the ifdName or the server certificates received while retrieving the TC Token.
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
public class TCTokenRequest {

    private static final Logger LOG = LoggerFactory.getLogger(TCTokenRequest.class);

    private static final String TC_TOKEN_URL_KEY = "tcTokenURL";
    private static final String CARD_TYPE_KEY = "cardType";
    private static final String DEFAULT_NPA_CARD_TYPE = "http://bsi.bund.de/cif/npa.xml";

    private TCToken token;
    private String cardType = DEFAULT_NPA_CARD_TYPE;

    private List<Pair<URL, TlsServerCertificate>> certificates;
    private TCTokenContext tokenCtx;

    public static TCTokenRequest fetchTCToken(Map<String, String> parameters) throws InvalidRedirectUrlException, SecurityViolationException, UserCancellationException, InvalidTCTokenException, InvalidTCTokenElement, MissingActivationParameterException, AuthServerException, InvalidAddressException {
	Map<String, String> copyParams = new HashMap<>(parameters);
	Pair<TCTokenContext, URL> tokenInfo = extractTCTokenContext(copyParams);
	TCTokenRequest req = convert(copyParams, tokenInfo);
	return req;
    }

    /**
     * Check and evaluate the request parameters and wrap the result in a {@code TCTokenRequest} class.
     *
     * @param parameters The request parameters.
     * @param tokenInfo The token.
     * @return A TCTokenRequest wrapping the parameters.
     * @throws MissingActivationParameterException
     */
    public static TCTokenRequest convert(Map<String, String> parameters, Pair<TCTokenContext, URL> tokenInfo)
	    throws MissingActivationParameterException {
	TCTokenRequest result = parseTCTokenRequestURI(parameters, tokenInfo);
	return result;
    }

    private static TCTokenRequest parseTCTokenRequestURI(Map<String, String> queries, Pair<TCTokenContext, URL> tokenInfo)
	    throws MissingActivationParameterException {
	TCTokenRequest tcTokenRequest = new TCTokenRequest();

	if (tokenInfo == null || tokenInfo.p1 == null || tokenInfo.p2 == null) {
	    throw new MissingActivationParameterException(NO_TOKEN);
	}

	for (Map.Entry<String, String> next : queries.entrySet()) {
	    String k = next.getKey();
	    k = k == null ? "" : k;
	    String v = next.getValue();

	    if (v == null || v.isEmpty()) {
		LOG.info("Skipping query parameter '{}' because it does not contain a value.", k);
	    } else {
		switch (k) {
		    case TC_TOKEN_URL_KEY:
			LOG.info("Skipping given query parameter '{}' because it was already extracted", TC_TOKEN_URL_KEY);
			break;
		    case CARD_TYPE_KEY:
			tcTokenRequest.cardType = v;
			break;
		    default:
			LOG.info("Unknown query element: {}", k);
			break;
		}
	    }
	}

	tcTokenRequest.tokenCtx = tokenInfo.p1;
	tcTokenRequest.token = tokenInfo.p1.getToken();
	tcTokenRequest.certificates = tokenInfo.p1.getCerts();

	return tcTokenRequest;
    }

    private static BigInteger extractSlotIndex(String rawSlotIndex) {
	if (rawSlotIndex == null || rawSlotIndex.isEmpty()) {
	    return null;
	}
	return new BigInteger(rawSlotIndex);
    }

    public static byte[] extractContextHandle(String rawContextHandle) {
	if (rawContextHandle == null || rawContextHandle.isEmpty()) {
	    return null;
	}
	return StringUtils.toByteArray(rawContextHandle);
    }

    /**
     * Checks if checks according to BSI TR03112-7 3.4.2, 3.4.4 and 3.4.5 must be performed.
     *
     * @param cardType The card type.
     * @return {@code true} if checks should be performed, {@code false} otherwise.
     */
   public static boolean isPerformTR03112Checks(String cardType) {
       boolean activationChecks = true;
	// disable checks when not using the nPA
	if (cardType != null && !cardType.equals(DEFAULT_NPA_CARD_TYPE)) {
	    activationChecks = false;
	} else if (TR03112Utils.DEVELOPER_MODE) {
	    activationChecks = false;
	    LOG.warn("DEVELOPER_MODE: All TR-03124-1 security checks are disabled.");
	}
	return activationChecks;
   }

    /**
     * Evaluate and extract the TC Token context from the given parameters.
     * @param queries The request parameters.
     * @return The TC Token context and the URL from which it was derived.
     * @throws AuthServerException
     * @throws InvalidRedirectUrlException
     * @throws InvalidAddressException
     * @throws InvalidTCTokenElement
     * @throws SecurityViolationException
     * @throws UserCancellationException
     * @throws MissingActivationParameterException
     * @throws InvalidTCTokenException
     * @throws InvalidTCTokenUrlException
     */
    private static Pair<TCTokenContext, URL> extractTCTokenContext(Map<String, String> queries) throws AuthServerException,
	    InvalidRedirectUrlException, InvalidAddressException, InvalidTCTokenElement, SecurityViolationException, UserCancellationException,
	    MissingActivationParameterException, InvalidTCTokenException, InvalidTCTokenUrlException {
	    String tcTokenUrl = queries.get(TC_TOKEN_URL_KEY);

	    Pair<TCTokenContext, URL> result = extractTCTokenContextInt(tcTokenUrl);
	    queries.remove(TC_TOKEN_URL_KEY);
	    return result;
    }

    private static Pair<TCTokenContext, URL> extractTCTokenContextInt(String activationTokenUrl) throws AuthServerException,
	    InvalidRedirectUrlException, InvalidAddressException, InvalidTCTokenElement, SecurityViolationException, UserCancellationException,
	    MissingActivationParameterException, InvalidTCTokenException, InvalidTCTokenUrlException {
	if (activationTokenUrl == null) {
	    throw new MissingActivationParameterException(NO_TOKEN);
	}

	URL tokenUrl;
	try {
	    tokenUrl = new URL(activationTokenUrl);
	} catch(MalformedURLException ex) {
	    // TODO: check if the error type is correct, was WRONG_PARAMETER before
	    throw new InvalidTCTokenUrlException(INVALID_TCTOKEN_URL, ex, activationTokenUrl);
	}
	TCTokenContext tokenCtx = TCTokenContext.generateTCToken(tokenUrl);
	return new Pair(tokenCtx, tokenUrl);
    }


    /**
     * Returns the TCToken.
     *
     * @return TCToken
     */
    public TCToken getTCToken() {
	return token;
    }

    /**
     * Returns the card type selected for this authentication process.
     * Defaults to the nPA identifier to provide a fallback.
     *
     * @return Card type
     */
    public String getCardType() {
	return cardType;
    }

    /**
     * Gets the certificates of the servers that have been passed while the TCToken was retrieved.
     *
     * @return List of the X509 server certificates and the requested URLs. May be null under certain circumstances
     *   (e.g. legacy activation).
     */
    public List<Pair<URL, TlsServerCertificate>> getCertificates() {
	return certificates;
    }

    public TCTokenContext getTokenContext() {
	return tokenCtx;
    }

    /**
     * Checks if checks according to BSI TR03112-7 3.4.2, 3.4.4 and 3.4.5 must be performed.
     *
     * @return {@code true} if checks should be performed, {@code false} otherwise.
     */
    public boolean  isPerformTR03112Checks() {
	return isPerformTR03112Checks(this.cardType);
    }
}
