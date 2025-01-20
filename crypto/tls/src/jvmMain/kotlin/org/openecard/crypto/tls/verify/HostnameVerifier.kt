/****************************************************************************
 * Copyright (C) 2014-2017 ecsec GmbH.
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
package org.openecard.crypto.tls.verify

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.bouncycastle.asn1.x500.style.BCStrictStyle
import org.openecard.bouncycastle.asn1.x509.Certificate
import org.openecard.bouncycastle.asn1.x509.Extension
import org.openecard.bouncycastle.asn1.x509.GeneralName
import org.openecard.bouncycastle.asn1.x509.GeneralNames
import org.openecard.bouncycastle.tls.TlsServerCertificate
import org.openecard.bouncycastle.util.IPAddress
import org.openecard.common.util.DomainUtils
import org.openecard.crypto.tls.CertificateVerificationException
import org.openecard.crypto.tls.CertificateVerifier
import java.io.IOException

private val LOG = KotlinLogging.logger {  }

/**
 * Certificate verifier which only checks the hostname against the received certificate.
 *
 * @author Tobias Wich
 */
class HostnameVerifier : CertificateVerifier {

    @Throws(CertificateVerificationException::class)
    override fun isValid(chain: TlsServerCertificate, hostOrIp: String) {
        try {
            val tlsCert = chain.certificate.getCertificateAt(0)
            val cert = Certificate.getInstance(tlsCert.encoded)
            validInt(cert, hostOrIp)
        } catch (ex: IOException) {
            throw CertificateVerificationException("Invalid certificate received from server.", ex)
        }
    }

    @Throws(CertificateVerificationException::class)
    private fun validInt(cert: Certificate, hostOrIp: String) {
        var success = false
        val isIPAddr = IPAddress.isValid(hostOrIp)

        // check hostname against Subject CN
        if (!isIPAddr) {
            val cn = cert.subject.getRDNs(BCStrictStyle.CN)
            if (cn.size != 0) {
                // CN is always a string type
                val hostNameReference = cn[0].first.value.toString()
                success = checkWildcardName(hostOrIp, hostNameReference)
            } else {
				LOG.debug { "No CN entry in certificate's Subject." }
            }
        } else {
			LOG.debug { "Given name is an IP Address. Validation relies solely on the SubjectAlternativeName." }
        }
        // stop execution when we found a valid name
        if (success) {
            return
        }

        // evaluate subject alternative name
        val ext = cert.tbsCertificate.getExtensions()
        val subjAltExt = ext.getExtension(Extension.subjectAlternativeName)
        if (subjAltExt != null) {
            // extract SubjAltName from Extensions
            val gns = GeneralNames.fromExtensions(ext, Extension.subjectAlternativeName)
            val names = gns.names
            for (name in names) {
                val reference = name.name
                when (name.tagNo) {
                    GeneralName.dNSName -> if (!isIPAddr) {
                        success = checkWildcardName(hostOrIp, reference.toString())
                    }

                    GeneralName.iPAddress -> if (isIPAddr) {
                        // TODO: validate IP Addresses
						LOG.warn { "IP Address verification not supported." }
                    }

                    else -> LOG.debug { "Unsupported GeneralName (${name.tagNo}) tag in SubjectAlternativeName." }
                }
                // stop execution when we found a valid name
                if (success) {
                    return
                }
            }
        }

        // evaluate result
        if (!success) {
            val errorMsg = "Hostname in certificate differs from actually requested host."
            throw CertificateVerificationException(errorMsg)
        }
    }

}

@Throws(CertificateVerificationException::class)
private fun checkWildcardName(givenHost: String, wildcardHost: String): Boolean {
	LOG.debug { "Comparing connection hostname against certificate hostname: [${givenHost}] [${wildcardHost}]" }
	try {
		return DomainUtils.checkHostName(wildcardHost, givenHost, true)
	} catch (ex: IllegalArgumentException) {
		val msg = "Invalid domain name found in certificate or requested hostname."
		throw CertificateVerificationException(msg, ex)
	}
}
