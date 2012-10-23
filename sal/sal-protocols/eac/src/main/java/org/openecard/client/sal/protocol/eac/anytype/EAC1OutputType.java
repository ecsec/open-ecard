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

package org.openecard.client.sal.protocol.eac.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import org.openecard.client.common.anytype.AuthDataMap;
import org.openecard.client.common.anytype.AuthDataResponse;
import org.openecard.client.common.util.ByteUtils;


/**
 * Implements the EAC1OutputType data structure.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.6.5.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class EAC1OutputType {

    public static final String RETRY_COUNTER = "RetryCounter";
    public static final String EF_CARDACCESS = "EFCardAccess";
    public static final String CAR = "CertificationAuthorityReference";
    public static final String CHAT = "CertificateHolderAuthorizationTemplate";
    public static final String ID_PICC = "IDPICC";

    private final AuthDataMap authMap;
    private byte[] efCardAccess;
    private byte[] car;
    private byte[] chat;
    private byte[] idpicc;
    private int retryCounter;

    /**
     * Creates a new EAC1OutputType.
     *
     * @param authMap DIDAuthenticationDataType
     */
    protected EAC1OutputType(AuthDataMap authMap) {
	this.authMap = authMap;
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
     * Sets the Certification Authority Reference (CAR).
     *
     * @param car Certification Authority Reference (CAR).
     */
    public void setCAR(byte[] car) {
	this.car = car;
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
     * Sets the card identifier ID_PICC..
     *
     * @param idpicc Card identifier ID_PICC.
     */
    public void setIDPICC(byte[] idpicc) {
	this.idpicc = idpicc;
    }

    /**
     * Sets the retry counter.
     *
     * @param retryCounter Retry counter.
     */
    public void setRetryCounter(int retryCounter) {
	this.retryCounter = retryCounter;
    }

    /**
     * Returns the DIDAuthenticationDataType.
     *
     * @return DIDAuthenticationDataType
     */
    public DIDAuthenticationDataType getAuthDataType() {
	AuthDataResponse authResponse = authMap.createResponse(new iso.std.iso_iec._24727.tech.schema.EAC1OutputType());

	authResponse.addElement(RETRY_COUNTER, String.valueOf(retryCounter));
	authResponse.addElement(EF_CARDACCESS, ByteUtils.toHexString(efCardAccess));
	authResponse.addElement(CAR, new String(car));
	authResponse.addElement(CHAT, ByteUtils.toHexString(chat));
	authResponse.addElement(ID_PICC, ByteUtils.toHexString(idpicc));

	return authResponse.getResponse();
    }

}
