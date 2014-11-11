/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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
import org.openecard.binding.tctoken.ex.InvalidTCTokenException;
import org.openecard.binding.tctoken.ex.SecurityViolationException;
import org.openecard.binding.tctoken.ex.AuthServerException;
import org.openecard.binding.tctoken.ex.MissingActivationParameterException;
import org.openecard.binding.tctoken.ex.InvalidRedirectUrlException;
import generated.TCTokenType;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.openecard.binding.tctoken.ex.InvalidAddressException;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.common.util.Pair;
import org.openecard.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;


/**
 * This class represents a TC Token request to the client. It contains the {@link TCTokenType} and situational parts
 * like the ifdName or the server certificates received while retrieving the TC Token.
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 */
public class TCTokenRequest {

    private static final Logger logger = LoggerFactory.getLogger(TCTokenRequest.class);

    private TCToken token;
    private String ifdName;
    private BigInteger slotIndex;
    private byte[] contextHandle;
    private String cardType = "http://bsi.bund.de/cif/npa.xml";
    private boolean tokenFromObject;
    private List<Pair<URL, Certificate>> certificates;
    private URL tcTokenURL;
    private TCTokenContext tokenCtx;


    /**
     * Check and evaluate the request parameters and wrap the result in a {@code TCTokenRequest} class.
     *
     * @param parameters The request parameters.
     * @return A TCTokenRequest wrapping the parameters.
     * @throws InvalidTCTokenException
     * @throws MissingActivationParameterException
     * @throws AuthServerException
     * @throws InvalidRedirectUrlException
     * @throws InvalidTCTokenElement
     * @throws InvalidTCTokenUrlException
     * @throws SecurityViolationException
     * @throws InvalidAddressException
     */
    public static TCTokenRequest convert(Map<String, String> parameters) throws InvalidTCTokenException,
	    MissingActivationParameterException, AuthServerException, InvalidRedirectUrlException, InvalidTCTokenElement,
	    InvalidTCTokenUrlException, SecurityViolationException, InvalidAddressException {
	TCTokenRequest result;
	if (parameters.containsKey("tcTokenURL")) {
	    result = parseTCTokenRequestURI(parameters);
	    result.tokenFromObject = false;
	    return result;
	} else if (parameters.containsKey("activationObject")) {
	    result = parseObjectURI(parameters);
	    result.tokenFromObject = true;
	    return result;
	}

	throw new MissingActivationParameterException(NO_PARAMS);
    }


    private static TCTokenRequest parseTCTokenRequestURI(Map<String, String> queries) throws InvalidTCTokenException,
	    MissingActivationParameterException, AuthServerException, InvalidRedirectUrlException, InvalidTCTokenElement,
	    InvalidTCTokenUrlException, SecurityViolationException, InvalidAddressException {
	TCTokenRequest tcTokenRequest = new TCTokenRequest();

	for (Map.Entry<String, String> next : queries.entrySet()) {
	    String k = next.getKey();
	    k = k == null ? "" : k;
	    String v = next.getValue();

	    if (v == null || v.isEmpty()) {
		logger.info("Skipping query parameter '{}' because it does not contain a value.", k);
	    } else {
		switch (k) {
		    case "tcTokenURL":
			try {
			    URL tokenUrl = new URL(v);
			    TCTokenContext tokenCtx = TCTokenContext.generateTCToken(tokenUrl);
			    tcTokenRequest.tokenCtx = tokenCtx;
			    tcTokenRequest.token = tokenCtx.getToken();
			    tcTokenRequest.certificates = tokenCtx.getCerts();
			    tcTokenRequest.tcTokenURL = tokenUrl;
			} catch (MalformedURLException ex) {
			    // TODO: check if the error type is correct, was WRONG_PARAMETER before
			    throw new InvalidTCTokenUrlException(INVALID_TCTOKEN_URL, ex, v);
			}
			break;
		    case "ifdName":
			tcTokenRequest.ifdName = v;
			break;
		    case "contextHandle":
			tcTokenRequest.contextHandle = StringUtils.toByteArray(v);
			break;
		    case "slotIndex":
			tcTokenRequest.slotIndex = new BigInteger(v);
			break;
		    case "cardType":
			tcTokenRequest.cardType = v;
			break;
		    default:
			logger.info("Unknown query element: {}", k);
			break;
		}
	    }
	}

	if (tcTokenRequest.token == null) {
	    throw new MissingActivationParameterException(NO_TOKEN);
	}

	return tcTokenRequest;
    }

    private static TCTokenRequest parseObjectURI(Map<String, String> queries) throws InvalidTCTokenException,
	    MissingActivationParameterException, AuthServerException, InvalidRedirectUrlException, InvalidTCTokenElement,
	    InvalidTCTokenUrlException, SecurityViolationException {
	// TODO: get rid of this crap as soon as possible
	TCTokenRequest tcTokenRequest = new TCTokenRequest();

	for (Map.Entry<String, String> next : queries.entrySet()) {
	    String k = next.getKey();
	    k = k == null ? "" : k;
	    String v = next.getValue();

	    if (v == null || v.isEmpty()) {
		logger.info("Skipping query parameter '{}' because it does not contain a value.", k);
	    } else {
		switch (k) {
		    case "activationObject":
			TCTokenContext tcToken = TCTokenContext.generateTCToken(v);
			tcTokenRequest.token = tcToken.getToken();
			break;
		    case "serverCertificate":
			// TODO: convert base64 and url encoded certificate to Certificate object
			break;
		    default:
			logger.info("Unknown query element: {}", k);
			break;
		}
	    }
	}

	if (tcTokenRequest.token == null) {
	    throw new MissingActivationParameterException(NO_TOKEN);
	}
	return tcTokenRequest;
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
     * Returns the IFD name.
     *
     * @return IFD name
     */
    public String getIFDName() {
	return ifdName;
    }

    /**
     * Returns the context handle.
     *
     * @return Context handle
     */
    public byte[] getContextHandle() {
	return contextHandle;
    }

    /**
     * Returns the slot index.
     *
     * @return Slot index
     */
    public BigInteger getSlotIndex() {
	return slotIndex;
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
     * Gets whether the token was created from an object tag or fetched from a URL.
     *
     * @return {@code true} when the token was created from an object tag, {@code false} otherwise.
     */
    public boolean isTokenFromObject() {
	return tokenFromObject;
    }

    /**
     * Gets the certificates of the servers that have been passed while the TCToken was retrieved.
     *
     * @return List of the X509 server certificates and the requested URLs. May be null under certain circumstances
     *   (e.g. legacy activation).
     */
    public List<Pair<URL, Certificate>> getCertificates() {
	return certificates;
    }

    /**
     * Gets the TC Token URL.
     *
     * @return TC Token URL
     */
    public URL getTCTokenURL() {
	return tcTokenURL;
    }

    public TCTokenContext getTokenContext() {
	return tokenCtx;
    }

}
