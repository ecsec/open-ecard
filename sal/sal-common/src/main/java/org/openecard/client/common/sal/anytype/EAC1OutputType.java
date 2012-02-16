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
import org.openecard.client.common.util.ByteUtils;

/**
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class EAC1OutputType {

    private final AuthDataMap authMap;
    private byte[] efCardAccess;
    private byte[] certificationAuthorityReference;
    private byte[] chat;
    private byte[] idpicc;
    private byte retryCounter;

    public EAC1OutputType(DIDAuthenticationDataType establishChannelOutput, byte[] chat) throws ParserConfigurationException {
	this.authMap = new AuthDataMap(establishChannelOutput);
	this.efCardAccess = this.authMap.getContentAsBytes("EFCardAccess");
	this.certificationAuthorityReference = this.authMap.getContentAsBytes("CARcurr");
	this.chat = chat;
	this.idpicc = this.authMap.getContentAsBytes("IDPICC");
	this.retryCounter = this.authMap.getContentAsBytes("RetryCounter")[0];
    }

    protected EAC1OutputType(AuthDataMap authMap) {
	this.authMap = authMap;
    }

    public byte[] getEfCardAccess() {
	return efCardAccess;
    }

    public byte[] getCertificationAuthorityReference() {
	return certificationAuthorityReference;
    }

    public byte[] getChat() {
	return chat;
    }

    public byte[] getIdpicc() {
	return idpicc;
    }

    public byte getRetryCounter() {
	return retryCounter;
    }

    public void setEFCardAccess(byte[] efCardAccess) {
	this.efCardAccess = efCardAccess;
    }

    public void setCertificationAuthorityReference(byte[] car) {
	this.certificationAuthorityReference = car;
    }

    public void setCHAT(byte[] chat) {
	this.chat = chat;
    }

    public void setIDPICC(byte[] idpicc) {
	this.idpicc = idpicc;
    }

    public void setRetryCounter(byte counter) {
	this.retryCounter = counter;
    }

    public DIDAuthenticationDataType getAuthDataType() {
	AuthDataResponse authResponse = authMap.createResponse(new iso.std.iso_iec._24727.tech.schema.EAC1OutputType());
	authResponse.addElement("RetryCounter", String.valueOf(retryCounter));
	authResponse.addElement("EFCardAccess", ByteUtils.toHexString(efCardAccess));
	authResponse.addElement("CertificationAuthorityReference", new String(certificationAuthorityReference));
	authResponse.addElement("CertificateHolderAuthorizationTemplate", ByteUtils.toHexString(chat));
	authResponse.addElement("IDPICC", ByteUtils.toHexString(idpicc));
	return authResponse.getResponse();
    }

}
