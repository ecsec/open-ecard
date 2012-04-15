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
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.client.common.sal.anytype.AuthDataMap;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificate;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class EAC2InputType {

    private final AuthDataMap authMap;
    private ArrayList<CardVerifiableCertificate> certificates = new ArrayList<CardVerifiableCertificate>();
    private byte[] ephemeralPublicKey;
    private byte[] signature;

    /**
     * Creates a new EAC2InputType.
     *
     * @param baseType DIDAuthenticationDataType
     * @throws ParserConfigurationException
     * @throws TLVException
     */
    public EAC2InputType(DIDAuthenticationDataType baseType) throws ParserConfigurationException, TLVException {
	this.authMap = new AuthDataMap(baseType);

	ephemeralPublicKey = authMap.getContentAsBytes("EphemeralPublicKey");
	signature = authMap.getContentAsBytes("Signature");

	while (authMap.containsContent("Cerificate")) {
	    TLV cardVerifiableCertificate = TLV.fromBER(authMap.getContentAsBytes("Certificate"));
	    certificates.add(new CardVerifiableCertificate(cardVerifiableCertificate));
	}
    }

    /**
     * Returns the set of certificates.
     *
     * @return Certificates
     */
    public ArrayList<CardVerifiableCertificate> getCertificates() {
	return certificates;
    }

    /**
     * Returns the ephemeral public key.
     *
     * @return Ephemeral public key
     */
    public byte[] getEphemeralPublicKey() {
	return ephemeralPublicKey;
    }

    /**
     * Returns the signature.
     *
     * @return Signature
     */
    public byte[] getSignature() {
	return signature;
    }

    /**
     * Returns a new EAC2OutputType.
     *
     * @return EAC2OutputType
     */
    public EAC2OutputType getOutputType() {
	return new EAC2OutputType(authMap);
    }

}
