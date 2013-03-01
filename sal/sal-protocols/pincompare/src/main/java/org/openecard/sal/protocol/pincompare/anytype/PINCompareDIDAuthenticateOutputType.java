/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.sal.protocol.pincompare.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import java.math.BigInteger;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.common.anytype.AuthDataMap;
import org.openecard.common.anytype.AuthDataResponse;


/**
 * Implements the PINCompareDIDAuthenticateOutputType.
 * See TR-03112, version 1.1.2, part 7, section 4.1.5.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PINCompareDIDAuthenticateOutputType {

    private final AuthDataMap authMap;
    private BigInteger retryCounter = null;

    /**
     * Creates a new PINCompareDIDAuthenticateOutputType.
     *
     * @param data DIDAuthenticationDataType
     * @throws ParserConfigurationException
     */
    public PINCompareDIDAuthenticateOutputType(DIDAuthenticationDataType data) throws ParserConfigurationException {
	authMap = new AuthDataMap(data);
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
    public DIDAuthenticationDataType getAuthDataType() {
	AuthDataResponse authResponse = authMap.createResponse(new iso.std.iso_iec._24727.tech.schema.PinCompareDIDAuthenticateOutputType());
	if (retryCounter != null) {
	    authResponse.addElement("RetryCounter", retryCounter.toString());
	}

	return authResponse.getResponse();
    }

}
