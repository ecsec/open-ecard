/****************************************************************************
 * Copyright (C) 2012-2013 ecsec GmbH.
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
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.common.util.Pair;
import org.openecard.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class represents a TC Token request to the client. It contains the {@link TCTokenType} and situational parts
 * like the ifdName or the server certificates received while retrieving the TC Token.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class TCTokenRequest {

    private static final Logger logger = LoggerFactory.getLogger(TCTokenRequest.class);

    private TCTokenType token;
    private String ifdName;
    private BigInteger slotIndex;
    private byte[] contextHandle;
    private String cardType = "http://bsi.bund.de/cif/npa.xml";
    private boolean tokenFromObject;
    private List<Pair<URL, Certificate>> certificates;
    private URL tcTokenURL;
    private TCTokenContext tokenCtx;


    /**
     * Check the request parameters and wrap them in a {@code TCTokenRequest} class.
     *
     * @param parameters The request parameters.
     * @return A TCTokenRequest wrapping the parameters.
     * @throws TCTokenException Thrown in case not all required parameters are available.
     */
    public static TCTokenRequest convert(Map<String, String> parameters) throws TCTokenException {
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

	throw new TCTokenException("No suitable set of parameters given in the request.");
    }


    private static TCTokenRequest parseTCTokenRequestURI(Map<String, String> queries) throws TCTokenException {
	TCTokenRequest tcTokenRequest = new TCTokenRequest();

	for (Map.Entry<String, String> next : queries.entrySet()) {
	    String k = next.getKey();
	    String v = next.getValue();

	    if (k.equals("tcTokenURL")) {
		if (v != null && ! v.isEmpty()) {
		    try {
			URL tokenUrl = new URL(v);
			TCTokenContext tokenCtx = TCTokenContext.generateTCToken(tokenUrl);
			tcTokenRequest.token = tokenCtx.getToken();
			tcTokenRequest.certificates = tokenCtx.getCerts();
			tcTokenRequest.tcTokenURL = tokenUrl;
		    } catch (MalformedURLException ex) {
			String msg = "The tcTokenURL parameter contains an invalid URL: " + v;
			throw new TCTokenException(msg, ex);
		    } catch (IOException | TCTokenException | ResourceException | ValidationError ex) {
			throw new TCTokenException("Failed to fetch TCToken.", ex);
		    }
		} else {
		    throw new TCTokenException("Parameter tcTokenURL contains no value.");
		}

	    } else if (k.equals("ifdName")) {
		if (v != null && ! v.isEmpty()) {
		    tcTokenRequest.ifdName = v;
		} else {
		    throw new TCTokenException("Parameter ifdName contains no value.");
		}

	    } else if (k.equals("contextHandle")) {
		if (v != null && ! v.isEmpty()) {
		    tcTokenRequest.contextHandle = StringUtils.toByteArray(v);
		} else {
		    throw new TCTokenException("Parameter contextHandle contains no value.");
		}

	    } else if (k.equals("slotIndex")) {
		if (v != null && ! v.isEmpty()) {
		    tcTokenRequest.slotIndex = new BigInteger(v);
		} else {
		    throw new TCTokenException("Parameter slotIndex contains no value.");
		}
	    } else if (k.equals("cardType")) {
		if (v != null && ! v.isEmpty()) {
		    tcTokenRequest.cardType = v;
		} else {
		    throw new TCTokenException("Parameter cardType contains no value.");
		}
	    } else {
		logger.info("Unknown query element: {}", k);
	    }
	}

	return tcTokenRequest;
    }

    private static TCTokenRequest parseObjectURI(Map<String, String> queries) throws TCTokenException {
	TCTokenRequest tcTokenRequest = new TCTokenRequest();

	for (Map.Entry<String, String> next : queries.entrySet()) {
	    String k = next.getKey();
	    String v = next.getValue();

	    if ("activationObject".equals(k)) {
		TCTokenContext tcToken = TCTokenContext.generateTCToken(v);
		tcTokenRequest.token = tcToken.getToken();
	    } else if ("serverCertificate".equals(k)) {
		// TODO: convert base64 and url encoded certificate to Certificate object
	    }
	}

	return tcTokenRequest;
    }


    /**
     * Returns the TCToken.
     *
     * @return TCToken
     */
    public TCTokenType getTCToken() {
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
