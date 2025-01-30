/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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
package org.openecard.crypto.common.asn1.cvc

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.tlv.TLV
import org.openecard.common.util.ByteUtils
import org.openecard.crypto.common.asn1.eac.oid.CVCertificatesObjectIdentifier
import org.openecard.crypto.common.asn1.eac.oid.TAObjectIdentifier
import org.openecard.crypto.common.asn1.utils.ObjectIdentifierUtils
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.util.*

private val LOG = KotlinLogging.logger {  }

/**
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
object CardVerifiableCertificateVerifier {

    /**
     * Verifies that the certificate description matches the certificate.
     * Verification is done by hashing the description and comparing it
     * to the checksum containing in the certificate extention.
     * See BSI-TR-03110, version 2.10, part 3, section C.3.
     *
     * @param certificate Certificate
     * @param description Description
     * @throws CertificateException
     */
    @JvmStatic
    @Throws(CertificateException::class)
    fun verify(certificate: CardVerifiableCertificate, description: CertificateDescription) {
        try {
            val extentions = certificate.extensions
            val extentionObject = TLV.fromBER(extentions)
            val list = extentionObject.asList()

            for (item in list) {
                val oid = ObjectIdentifierUtils.toString(item.value)

                if (oid == CVCertificatesObjectIdentifier.id_description) {
                    val hashObjects = item.findChildTags(0x80)
                    if (hashObjects != null && !hashObjects.isEmpty()) {
                        val hashObject = hashObjects.get(0)
                        val md = CardVerifiableCertificateVerifier.selectDigest(
                            certificate.getPublicKey().objectIdentifier
                        )
                        val hash = md.digest(description.encoded)
                        if (!ByteUtils.compare(hash, hashObject.getValue())) {
                            throw CertificateException("The checksum of the certificate description cannot be verified!")
                        }
                    }
                } else if (oid == CVCertificatesObjectIdentifier.id_sector) {
                    val firstPublicKeyObjects = item.findChildTags(0x80)
                    if (firstPublicKeyObjects != null && !firstPublicKeyObjects.isEmpty()) {
                        val firstPublicKeyObject = firstPublicKeyObjects[0]
                        //TODO
                    }

                    val secondPublicKeyObjects = item.findChildTags(0x81)
                    if (secondPublicKeyObjects != null && !secondPublicKeyObjects.isEmpty()) {
                        val secondPublicKeyObject = secondPublicKeyObjects[0]
                        //TODO
                    }
                } else {
					LOG.warn { "Unknown OID: $oid" }
                }
            }
        } catch (e: Exception) {
			LOG.debug { e.message }
            throw CertificateException("Verification failed", e)
        }
    }

    @Throws(CertificateException::class)
    fun checkDate(certificate: CardVerifiableCertificate) {
        val systemDate = Date()
        val expDate = certificate.getExpirationDate().getTime()
        val effDate = certificate.getEffectiveDate().getTime()
        if (systemDate.after(expDate) || systemDate.before(effDate)) {
            val msg = "CV Certificate's validity date is different than the current date."
            throw CertificateException(msg)
        }
    }

    @Throws(NoSuchAlgorithmException::class)
    private fun selectDigest(oid: String): MessageDigest {
        if (oid == TAObjectIdentifier.id_TA_ECDSA_SHA_1
            || oid == TAObjectIdentifier.id_TA_RSA_PSS_SHA_1
            || oid == TAObjectIdentifier.id_TA_RSA_v1_5_SHA_1
        ) {
            return MessageDigest.getInstance("SHA-1")
        } else if (oid == TAObjectIdentifier.id_TA_ECDSA_SHA_224) {
            return MessageDigest.getInstance("SHA-224")
        } else if (oid == TAObjectIdentifier.id_TA_ECDSA_SHA_256
            || oid == TAObjectIdentifier.id_TA_RSA_PSS_SHA_256
            || oid == TAObjectIdentifier.id_TA_RSA_v1_5_SHA_256
        ) {
            return MessageDigest.getInstance("SHA-256")
        } else if (oid == TAObjectIdentifier.id_TA_ECDSA_SHA_384
        ) {
            return MessageDigest.getInstance("SHA-384")
        } else if (oid == TAObjectIdentifier.id_TA_ECDSA_SHA_512
            || oid == TAObjectIdentifier.id_TA_RSA_PSS_SHA_512
            || oid == TAObjectIdentifier.id_TA_RSA_v1_5_SHA_512
        ) {
            return MessageDigest.getInstance("SHA-512")
        }
        throw NoSuchAlgorithmException()
    }
}
