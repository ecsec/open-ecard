/****************************************************************************
 * Copyright (C) 2012-2016 HS Coburg.
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

package org.openecard.common.anytype.pin;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.PinCompareDIDAuthenticateOutputType;
import java.math.BigInteger;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.common.anytype.AuthDataMap;
import org.openecard.common.anytype.AuthDataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements the PINCompareDIDAuthenticateOutputType.
 * See TR-03112, version 1.1.2, part 7, section 4.1.5.
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
public class PINCompareDIDAuthenticateOutputType {

    private static final Logger LOG = LoggerFactory.getLogger(PINCompareDIDAuthenticateOutputType.class);
    private static final String ISO_NS = "urn:iso:std:iso-iec:24727:tech:schema";

    private final AuthDataMap authMap;
    private BigInteger retryCounter;

    /**
     * Creates a new PINCompareDIDAuthenticateOutputType.
     *
     * @param data DIDAuthenticationDataType Generic type containing a PinCompareDIDAuthenticateOutputType.
     * @throws ParserConfigurationException
     */
    public PINCompareDIDAuthenticateOutputType(DIDAuthenticationDataType data) throws ParserConfigurationException {
	authMap = new AuthDataMap(data);

	String retryCounterStr = authMap.getContentAsString(ISO_NS, "RetryCounter");
	if (retryCounterStr != null) {
	    try {
		retryCounter = new BigInteger(retryCounterStr);
	    } catch (NumberFormatException ex) {
		LOG.warn("Can not convert malformed RetryCounter value to an integer.", ex);
	    }
	}
    }

    /**
     * Creates a new PINCompareDIDAuthenticateOutputType.
     *
     * @param authMap AuthDataMap
     */
    protected PINCompareDIDAuthenticateOutputType(AuthDataMap authMap) {
	this.authMap = authMap;
    }

    /**
     * Returns the retry counter.
     *
     * @return Retry counter
     */
    public BigInteger getRetryCounter() {
	return retryCounter;
    }

    /**
     * Sets the retry counter.
     *
     * @param retryCounter Retry counter
     */
    public void setRetryCounter(BigInteger retryCounter) {
	this.retryCounter = retryCounter;
    }

    /**
     *
     * @return the PinCompareDIDAuthenticateOutputType
     */
    public PinCompareDIDAuthenticateOutputType getAuthDataType() {
	PinCompareDIDAuthenticateOutputType pinCompareOutput;
	pinCompareOutput = new PinCompareDIDAuthenticateOutputType();
	AuthDataResponse<PinCompareDIDAuthenticateOutputType> authResponse = authMap.createResponse(pinCompareOutput);
	if (retryCounter != null) {
	    authResponse.addElement(ISO_NS, "RetryCounter", retryCounter.toString());
	}

	return authResponse.getResponse();
    }

}
