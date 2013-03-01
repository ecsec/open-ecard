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

package org.openecard.sal.protocol.eac.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import org.openecard.common.anytype.AuthDataMap;
import org.openecard.common.anytype.AuthDataResponse;
import org.openecard.common.util.ByteUtils;


/**
 * Implements the EAC2OutputType data structure.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.6.6.
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
