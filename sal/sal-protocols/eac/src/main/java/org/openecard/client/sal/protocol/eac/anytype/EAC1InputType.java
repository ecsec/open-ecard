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
import org.openecard.client.common.sal.anytype.AuthDataMap;
import org.openecard.client.common.util.StringUtils;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificate;
import org.w3c.dom.Element;


/**
 * Implements the EAC1InputType data structure.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.6.5.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class EAC1InputType {

    public static final String CERTIFICATE = "Certificate";
    public static final String CERTIFICATE_DESCRIPTION = "CertificateDescription";
    public static final String REQUIRED_CHAT = "RequiredCHAT";
    public static final String OPTIONAL_CHAT = "OptionalCHAT";
    public static final String AUTHENTICATED_AUXILIARY_DATA = "AuthenticatedAuxiliaryData";
    //
    private final AuthDataMap authMap;
    private ArrayList<CardVerifiableCertificate> certificates;
    private byte[] certificateDescription;
    private byte[] requiredCHAT;
    private byte[] optionalCHAT;
    private byte[] authenticatedAuxiliaryData;

    /**
     * Creates a new EAC1InputType.
     *
     * @param baseType DIDAuthenticationDataType
     * @throws Exception
     */
    public EAC1InputType(DIDAuthenticationDataType baseType) throws Exception {
	authMap = new AuthDataMap(baseType);

	certificateDescription = authMap.getContentAsBytes(CERTIFICATE_DESCRIPTION);
	requiredCHAT = authMap.getContentAsBytes(REQUIRED_CHAT);
	optionalCHAT = authMap.getContentAsBytes(OPTIONAL_CHAT);
	authenticatedAuxiliaryData = authMap.getContentAsBytes(AUTHENTICATED_AUXILIARY_DATA);

	certificates = new ArrayList<CardVerifiableCertificate>();
	for (Element element : baseType.getAny()) {
	    if (element.getLocalName().equals(CERTIFICATE)) {
		byte[] value = StringUtils.toByteArray(element.getTextContent());
		CardVerifiableCertificate cvc = new CardVerifiableCertificate(value);
		certificates.add(cvc);
	    }
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
     * Returns the certificate description.
     *
     * @return Certificate description
     */
    public byte[] getCertificateDescription() {
	return certificateDescription;
    }

    /**
     * Returns the required CHAT.
     *
     * @return Required CHAT
     */
    public byte[] getRequiredCHAT() {
	return requiredCHAT;
    }

    /**
     * Returns the optional CHAT.
     *
     * @return Optional CHAT
     */
    public byte[] getOptionalCHAT() {
	return optionalCHAT;
    }

    /**
     * Returns the AuthenticatedAuxiliaryData.
     *
     * @return AuthenticatedAuxiliaryData
     */
    public byte[] getAuthenticatedAuxiliaryData() {
	return authenticatedAuxiliaryData;
    }

    /**
     * Returns a new EAC1OutputType.
     *
     * @return EAC1OutputType
     */
    public EAC1OutputType getOutputType() {
	return new EAC1OutputType(authMap);
    }
}
