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

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.client.common.util.ByteUtils;

/**
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class EAC2OutputType {

    private final AuthDataMap authMap;
    private byte[] challenge = null;
    private String efCardSecurity = null;
    private String token = null;
    private String nonce = null;

    /**
     * Constructor for Terminal-Authentication
     * 
     * @param didAuthenticationDataType
     * @param challenge
     * @throws ParserConfigurationException
     */
    public EAC2OutputType(DIDAuthenticationDataType didAuthenticationDataType, byte[] challenge) throws ParserConfigurationException {
	this.authMap = new AuthDataMap(didAuthenticationDataType);
	this.challenge = challenge;
    }

    protected EAC2OutputType(AuthDataMap authMap) {
	this.authMap = authMap;
    }

    /**
     * Constructor for Chip-Authentication
     * 
     * @param didAuthenticate
     * @param efCardSecurity
     * @param token
     * @param nonce
     * @throws ParserConfigurationException
     */
    public EAC2OutputType(DIDAuthenticate didAuthenticate, String efCardSecurity, String token, String nonce)
	    throws ParserConfigurationException {
	this.authMap = new AuthDataMap(didAuthenticate.getAuthenticationProtocolData());
	this.efCardSecurity = efCardSecurity;
	this.token = token;
	this.nonce = nonce;
    }

    public DIDAuthenticationDataType getAuthDataType() {
	AuthDataResponse authResponse = authMap.createResponse(new iso.std.iso_iec._24727.tech.schema.EAC2OutputType());
	if (challenge != null)
	    authResponse.addElement("Challenge", ByteUtils.toHexString(challenge));
	if (efCardSecurity != null)
	    authResponse.addElement("EFCardSecurity", efCardSecurity);
	if (token != null)
	    authResponse.addElement("AuthenticationToken", token);
	if (nonce != null)
	    authResponse.addElement("Nonce", nonce);
	return authResponse.getResponse();
    }

}
