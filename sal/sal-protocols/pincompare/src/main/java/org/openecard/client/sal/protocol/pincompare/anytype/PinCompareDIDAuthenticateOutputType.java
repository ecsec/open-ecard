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

package org.openecard.client.sal.protocol.pincompare.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import java.math.BigInteger;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.client.common.sal.anytype.AuthDataMap;
import org.openecard.client.common.sal.anytype.AuthDataResponse;


/**
 * [TR-03112-7] MAY contain the value of the PIN. If this element is missing, it
 * is input at the terminal.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PinCompareDIDAuthenticateOutputType {

    private final AuthDataMap authMap;
    /**
     * If user verification failed, this contains the current value of the
     * RetryCounter.
     */
    private BigInteger retryCounter = null;

    public PinCompareDIDAuthenticateOutputType(DIDAuthenticationDataType didAuthenticationDataType) throws ParserConfigurationException {
	this.authMap = new AuthDataMap(didAuthenticationDataType);
    }

    protected PinCompareDIDAuthenticateOutputType(AuthDataMap authMap) {
	this.authMap = authMap;
    }

    public BigInteger getRetryCounter() {
	return retryCounter;
    }

    public void setRetryCounter(BigInteger counter) {
	this.retryCounter = counter;
    }

    public DIDAuthenticationDataType getAuthDataType() {
	AuthDataResponse authResponse = authMap.createResponse(new iso.std.iso_iec._24727.tech.schema.PinCompareDIDAuthenticateOutputType());
	if (retryCounter != null) {
	    authResponse.addElement("RetryCounter", retryCounter.toString());
	}
	return authResponse.getResponse();
    }

}
