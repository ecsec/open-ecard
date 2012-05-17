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
package org.openecard.client.sal.protocol.eac.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import org.openecard.client.common.sal.anytype.AuthDataMap;
import org.openecard.client.common.sal.anytype.AuthDataResponse;
import org.openecard.client.common.util.ByteUtils;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class EAC2OutputType {

    public static final String CHALLENGE = "Challenge";
    public static final String EF_CARDSECURITY = "EFCardSecurity";
    public static final String TOKEN = "AuthenticationToken";
    public static final String NONCE = "Nonce";
    //
    private final AuthDataMap authMap;
    private byte[] challenge;
    private byte[] efCardSecurity;
    private byte[] token;
    private byte[] nonce;

    /**
     * Creates a new EAC2OutputType.
     *
     * @param authMap DIDAuthenticationDataType
     */
    protected EAC2OutputType(AuthDataMap authMap) {
	this.authMap = authMap;
    }

    /**
     * Sets the challenge.
     *
     * @param challenge
     */
    public void setChallenge(byte[] challenge) {
	this.challenge = challenge;
    }

    /**
     * Sets the file content of the EF.CardSecurity.
     *
     * @param efCardSecurity EF.CardSecurity
     */
    public void setEFCardSecurity(byte[] efCardSecurity) {
	this.efCardSecurity = efCardSecurity;
    }

    /**
     * Sets the nonce r_PICC,CA.
     *
     * @param nonce Nonce r_PICC,CA
     */
    public void setNonce(byte[] nonce) {
	this.nonce = nonce;
    }

    /**
     * Sets the AuthenticationToken T_PICC.
     *
     * @param token AuthenticationToken T_PICC
     */
    public void setToken(byte[] token) {
	this.token = token;
    }

    /**
     * Returns the DIDAuthenticationDataType.
     *
     * @return DIDAuthenticationDataType
     */
    public DIDAuthenticationDataType getAuthDataType() {
	AuthDataResponse authResponse = authMap.createResponse(new iso.std.iso_iec._24727.tech.schema.EAC2OutputType());
	if (challenge != null) {
	    authResponse.addElement(CHALLENGE, ByteUtils.toHexString(challenge));
	}
	if (efCardSecurity != null) {
	    authResponse.addElement(EF_CARDSECURITY, ByteUtils.toHexString(efCardSecurity));
	}
	if (token != null) {
	    authResponse.addElement(TOKEN, ByteUtils.toHexString(token));
	}
	if (nonce != null) {
	    authResponse.addElement(NONCE, ByteUtils.toHexString(nonce));
	}
	return authResponse.getResponse();
    }
}
