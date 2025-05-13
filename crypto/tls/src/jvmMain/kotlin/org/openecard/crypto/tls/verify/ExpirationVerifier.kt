/****************************************************************************
 * Copyright (C) 2015-2017 ecsec GmbH.
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

import org.openecard.bouncycastle.asn1.x509.Certificate
import org.openecard.bouncycastle.tls.TlsServerCertificate
import org.openecard.crypto.tls.CertificateVerificationException
import org.openecard.crypto.tls.CertificateVerifier
import java.io.IOException
import java.util.Date

/**
 * Verifier checking the expiration date of each certificate in the chain.
 *
 * @author Tobias Wich
 */
class ExpirationVerifier : CertificateVerifier {
	@Throws(CertificateVerificationException::class)
	override fun isValid(
		chain: TlsServerCertificate,
		hostOrIp: String,
	) {
		try {
			val now = Date()
			for (next in chain.certificate.getCertificateList()) {
				val c = Certificate.getInstance(next.encoded)
				val expDate = c.endDate.date
				if (now.after(expDate)) {
					val msg = String.format("The certificate '%s' expired at %s.", c.subject, expDate)
					throw CertificateVerificationException(msg)
				}
			}
		} catch (ex: IOException) {
			throw CertificateVerificationException("Invalid certificate received from server.", ex)
		}
	}
}
