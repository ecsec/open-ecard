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
import javax.xml.parsers.ParserConfigurationException;


/**
 * [TR-03112-7] This type specifies the structure of the
 * DIDAuthenticationDataType for the PIN Compare protocol when DIDAuthenticate
 * is called.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PinCompareDIDAuthenticateInputType {

    /** MAY contain the value of the PIN. If this element is missing, it is input at the terminal. */
    private String pin = null;
    private final AuthDataMap authMap;

    public PinCompareDIDAuthenticateInputType(DIDAuthenticationDataType baseType) throws ParserConfigurationException {
	authMap = new AuthDataMap(baseType);
	// Optional contents
	pin = authMap.getContentAsString("Pin");
    }

    public String getPin() {
	return pin;
    }

    public PinCompareDIDAuthenticateOutputType getOutputType() {
	return new PinCompareDIDAuthenticateOutputType(authMap);
    }

}
