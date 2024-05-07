/****************************************************************************
 * Copyright (C) 2012-2014 HS Coburg.
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
 * Implements the EAC1OutputType data structure.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.6.5.
 *
 * @author Dirk Petrautzki
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public class EAC1OutputType {

    public static final String RETRY_COUNTER = "RetryCounter";
    public static final String CHAT = "CertificateHolderAuthorizationTemplate";
    public static final String CAR = "CertificationAuthorityReference";
    public static final String EF_CARDACCESS = "EFCardAccess";
    public static final String ID_PICC = "IDPICC";
    public static final String CHALLENGE = "Challenge";

    private final AuthDataMap authMap;
    private Integer retryCounter;
    private byte[] chat;
    private byte[] currentCar;
    private byte[] previousCar;
    private byte[] efCardAccess;
    private byte[] idpicc;
    private byte[] challenge;

    /**
     * Creates a new EAC1OutputType.
     *
     * @param authMap DIDAuthenticationDataType
     */
    protected EAC1OutputType(AuthDataMap authMap) {
	this.authMap = authMap;
    }

    /**
     * Sets the retry counter.
     *
     * @param retryCounter Retry counter.
     */
    public void setRetryCounter(Integer retryCounter) {
	this.retryCounter = retryCounter;
    }

    /**
     * Sets the Certificate Holder Authorization Template (CHAT).
     *
     * @param chat Certificate Holder Authorization Template (CHAT).
     */
    public void setCHAT(byte[] chat) {
	this.chat = chat;
    }

    /**
     * Sets the most recent Certification Authority Reference (CAR).
     *
     * @param car Certification Authority Reference (CAR).
     */
    public void setCurrentCAR(byte[] car) {
	this.currentCar = car;
    }

    /**
     * Sets the previous Certification Authority Reference (CAR).
     *
     * @param car Certification Authority Reference (CAR).
     */
    public void setPreviousCAR(byte[] car) {
	this.previousCar = car;
    }

    /**
     * Sets the file content of the EF.CardAccess.
     *
     * @param efCardAccess EF.CardAccess
     */
    public void setEFCardAccess(byte[] efCardAccess) {
	this.efCardAccess = efCardAccess;
    }

    /**
     * Sets the card identifier ID_PICC.
     *
     * @param idpicc Card identifier ID_PICC.
     */
    public void setIDPICC(byte[] idpicc) {
	this.idpicc = idpicc;
    }

    /**
     * Sets the challenge.
     *
     * @param challenge Challenge.
     */
    public void setChallenge(byte[] challenge) {
	this.challenge = challenge;
    }

    /**
     * Returns the DIDAuthenticationDataType.
     *
     * @return DIDAuthenticationDataType
     */
    public DIDAuthenticationDataType getAuthDataType() {
	AuthDataResponse authResponse = authMap.createResponse(new iso.std.iso_iec._24727.tech.schema.EAC1OutputType());
	if (retryCounter != null) {
	    authResponse.addElement(RETRY_COUNTER, String.valueOf(retryCounter));
	}
	authResponse.addElement(CHAT, ByteUtils.toHexString(chat));
	authResponse.addElement(CAR, new String(currentCar));
	if (previousCar != null) {
	    authResponse.addElement(CAR, new String(previousCar));
	}
	authResponse.addElement(EF_CARDACCESS, ByteUtils.toHexString(efCardAccess));
	authResponse.addElement(ID_PICC, ByteUtils.toHexString(idpicc));
	authResponse.addElement(CHALLENGE, ByteUtils.toHexString(challenge));

	return authResponse.getResponse();
    }

}
