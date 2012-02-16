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
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.util.StringUtils;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificate;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class EAC2InputType {

    private ArrayList<CardVerifiableCertificate> certificates = new ArrayList<CardVerifiableCertificate>();
    private byte[] ephemeralPublicKey = null;
    private byte[] compressedEphemeralPublicKey = null;

    public EAC2InputType(DIDAuthenticationDataType baseType) throws ParserConfigurationException, DOMException, TLVException {
	for (Element elem : baseType.getAny()) {
	    if (elem.getTagName().equals("EphemeralPublicKey")) {
		this.ephemeralPublicKey = StringUtils.toByteArray(elem.getTextContent());
		this.compressedEphemeralPublicKey = StringUtils.toByteArray(elem.getTextContent().substring(0,64)); //get first 32 Byte == 64 chars
	    } else if (elem.getTagName().equals("Certificate")) {
		CardVerifiableCertificate cvc = new CardVerifiableCertificate(TLV.fromBER(StringUtils.toByteArray(elem.getTextContent())));
		certificates.add(cvc);
	    }
	}
    }

    public ArrayList<CardVerifiableCertificate> getCertificates() {
	return certificates;
    }

    public byte[] getEphemeralPublicKey() {
	return ephemeralPublicKey;
    }

    public byte[] getCompressedEphemeralPublicKey() {
	return compressedEphemeralPublicKey;
    }

}
