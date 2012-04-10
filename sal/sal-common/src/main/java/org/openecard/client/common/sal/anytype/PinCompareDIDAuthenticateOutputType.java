/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.common.sal.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import java.math.BigInteger;
import javax.xml.parsers.ParserConfigurationException;


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
	AuthDataResponse authResponse = authMap
	    .createResponse(new iso.std.iso_iec._24727.tech.schema.PinCompareDIDAuthenticateOutputType());
	if (retryCounter != null)
	    authResponse.addElement("RetryCounter", retryCounter.toString());
	return authResponse.getResponse();
    }

}
