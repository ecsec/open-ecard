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
 */
package org.openecard.sal.protocol.eac.anytype

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType
import org.openecard.common.anytype.AuthDataMap
import org.openecard.common.util.StringUtils
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificate

/**
 * Implements the EAC2InputType data structure.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.6.6.
 *
 * @author Dirk Petrautzki
 * @author Moritz Horsch
 * @author Tobias Wich
 */
class EAC2InputType(baseType: DIDAuthenticationDataType) {
    //
    private val authMap: AuthDataMap

    /**
     * Returns the set of certificates.
     *
     * @return Certificates
     */
    val certificates: ArrayList<CardVerifiableCertificate?>

    /**
     * Returns the ephemeral public key.
     *
     * @return Ephemeral public key
     */
    val ephemeralPublicKey: ByteArray?

    /**
     * Returns the signature.
     *
     * @return Signature
     */
    val signature: ByteArray?

    /**
     * Creates a new EAC2InputType.
     *
     * @param baseType DIDAuthenticationDataType
     * @throws Exception Thrown in cause the type iss errornous.
     */
    init {
        this.authMap = AuthDataMap(baseType)

        certificates = ArrayList<CardVerifiableCertificate?>()
        for (element in baseType.getAny()) {
            if (element.getLocalName() == CERTIFICATE) {
                val value = StringUtils.toByteArray(element.getTextContent())
                val cvc = CardVerifiableCertificate(value)
                certificates.add(cvc)
            }
        }
        ephemeralPublicKey = authMap.getContentAsBytes(EPHEMERAL_PUBLIC_KEY)
        signature = authMap.getContentAsBytes(SIGNATURE)
    }

    val outputType: EAC2OutputType
        /**
         * Returns a new EAC2OutputType.
         *
         * @return EAC2OutputType
         */
        get() = EAC2OutputType(authMap)

    companion object {
        const val CERTIFICATE: String = "Certificate"
        const val SIGNATURE: String = "Signature"
        const val EPHEMERAL_PUBLIC_KEY: String = "EphemeralPublicKey"
    }
}
