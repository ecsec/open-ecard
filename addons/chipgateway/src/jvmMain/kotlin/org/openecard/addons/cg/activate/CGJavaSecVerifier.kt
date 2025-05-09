/****************************************************************************
 * Copyright (C) 2016-2025 ecsec GmbH.
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
package org.openecard.addons.cg.activate

import org.openecard.addons.cg.impl.AllowedApiEndpoints
import org.openecard.addons.cg.impl.ChipGatewayProperties
import org.openecard.bouncycastle.tls.TlsServerCertificate
import org.openecard.crypto.tls.CertificateVerificationException
import org.openecard.crypto.tls.verify.JavaSecVerifier
import java.io.IOException
import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate

/**
 *
 * @author Tobias Wich
 */
class CGJavaSecVerifier : JavaSecVerifier(ChipGatewayProperties.isRevocationCheck) {
	override val trustStore: Set<TrustAnchor>
		get() = CGTrustStoreLoader().trustAnchors

	@Throws(CertificateVerificationException::class)
	override fun isValid(
		chain: TlsServerCertificate,
		hostname: String,
	) {
		try {
			val certPathBuilderResult = validateCertificate(chain, hostname)

			if (ChipGatewayProperties.isUseApiEndpointWhitelist) {
				val cert = certPathBuilderResult!!.certPath.certificates[0] as X509Certificate
				val subj = cert.subjectX500Principal
				if (!AllowedApiEndpoints.isInSubjects(subj)) {
					val msg = "The certificate used in the signature has an invalid subject: ${subj.name}"
					throw CertificateVerificationException(msg)
				}
			}
		} catch (ex: IOException) {
			if (ex is CertificateVerificationException) {
				throw ex
			}
			throw CertificateVerificationException("Error converting certificate chain to java.security format.")
		}
	}
}
