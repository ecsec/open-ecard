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
public class EAC1InputType {

    private ArrayList<CardVerifiableCertificate> certificates = new ArrayList<CardVerifiableCertificate>();
    private byte[] certificateDescription = null;
    private byte[] reuiredCHAT = null;
    private byte[] optionalCHAT = null;
    private byte[] authenticatedAuxiliaryData = null;

    public EAC1InputType(DIDAuthenticationDataType baseType) throws ParserConfigurationException, DOMException, TLVException {
	for (Element elem : baseType.getAny()) {
	    if (elem.getLocalName().equals("CertificateDescription")) {
		this.certificateDescription = StringUtils.toByteArray(elem.getTextContent());
	    } else if (elem.getLocalName().equals("Certificate")) {
		CardVerifiableCertificate cvc = new CardVerifiableCertificate(TLV.fromBER(StringUtils.toByteArray(elem.getTextContent())));
		certificates.add(cvc);
	    } else if (elem.getLocalName().equals("RequiredCHAT")) {
		this.reuiredCHAT = StringUtils.toByteArray(elem.getTextContent());
	    } else if (elem.getLocalName().equals("OptionalCHAT")) {
		this.optionalCHAT = StringUtils.toByteArray(elem.getTextContent());
	    } else if (elem.getLocalName().equals("AuthenticatedAuxiliaryData")) {
		this.authenticatedAuxiliaryData = StringUtils.toByteArray(elem.getTextContent());
	    }
	}
    }

    public ArrayList<CardVerifiableCertificate> getCertificates() {
	return certificates;
    }

    public byte[] getCertificateDescription() {
	return certificateDescription;
    }

    public byte[] getReuiredCHAT() {
	return reuiredCHAT;
    }

    public byte[] getOptionalCHAT() {
	return optionalCHAT;
    }

    public byte[] getAuthenticatedAuxiliaryData() {
	return authenticatedAuxiliaryData;
    }

}
