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
public class EAC1OutputType {

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

	authResponse.addElement("RetryCounter", String.valueOf(retryCounter));
	authResponse.addElement("EFCardAccess", ByteUtils.toHexString(efCardAccess));
	authResponse.addElement("CertificationAuthorityReference", ByteUtils.toHexString(car));
	authResponse.addElement("CertificateHolderAuthorizationTemplate", ByteUtils.toHexString(chat));
	authResponse.addElement("IDPICC", ByteUtils.toHexString(idpicc));

	return authResponse.getResponse();
    }

}
