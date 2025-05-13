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
import org.openecard.bouncycastle.tls.TlsServerCertificate
import org.openecard.common.util.ByteUtils
import org.openecard.crypto.tls.CertificateVerificationException
import org.openecard.crypto.tls.CertificateVerifier
import java.io.IOException

private val LOG = KotlinLogging.logger { }

/**
 * Certifier asserting that the same certificate is used between different TLS connections.
 *
 * @author Tobias Wich
 */
class SameCertVerifier : CertificateVerifier {
	private var firstCert: TlsServerCertificate? = null

	@Throws(CertificateVerificationException::class)
	override fun isValid(
		serverCertificate: TlsServerCertificate,
		hostOrIp: String,
	) {
		if (firstCert == null) {
			firstCert = serverCertificate
		} else {
			// we have a saved certificate, try to validate it by comparison
			// chains must be of equal length
			if (firstCert!!.certificate.length != serverCertificate.certificate.length) {
				val msg = "Server certificate changed during transaction.."
				LOG.error { msg }
				throw CertificateVerificationException(msg)
			} else {
				// compare each certificate in the chain
				for (i in 0..<firstCert!!.certificate.length) {
					val first: ByteArray
					val second: ByteArray
					try {
						first = firstCert!!.certificate.getCertificateAt(i).encoded
						second = serverCertificate.certificate.getCertificateAt(i).encoded
					} catch (ex: IOException) {
						val msg = "Failed to serialize certificate"
						LOG.error { msg }
						throw CertificateVerificationException(msg, ex)
					}
					if (!ByteUtils.compare(first, second)) {
						val msg = "Certificates retransmitted by the server differ."
						LOG.error { msg }
						throw CertificateVerificationException(msg)
					}
				}
			}
		}
	}
}
