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
 * Alternatively, this file may be used in accordance with the terms and
 * conditions contained in a signed written agreement between you and ecsec.
 *
 ***************************************************************************/

package org.openecard.client.sal.protocol.eac.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import java.util.ArrayList;
import org.openecard.client.common.sal.anytype.AuthDataMap;
import org.openecard.client.common.util.StringUtils;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificate;
import org.w3c.dom.Element;


/**
 * Implements the EAC2InputType data structure.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.6.6.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class EAC2InputType {

    public static final String CERTIFICATE = "Certificate";
    public static final String SIGNATURE = "Signature";
    public static final String EPHEMERAL_PUBLIC_KEY = "EphemeralPublicKey";
    //
    private final AuthDataMap authMap;
    private ArrayList<CardVerifiableCertificate> certificates = new ArrayList<CardVerifiableCertificate>();
    private byte[] ephemeralPublicKey;
    private byte[] signature;

    /**
     * Creates a new EAC2InputType.
     *
     * @param baseType DIDAuthenticationDataType
     * @throws Exception
     */
    public EAC2InputType(DIDAuthenticationDataType baseType) throws Exception {
	this.authMap = new AuthDataMap(baseType);

	ephemeralPublicKey = authMap.getContentAsBytes(EPHEMERAL_PUBLIC_KEY);
	signature = authMap.getContentAsBytes(SIGNATURE);

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
