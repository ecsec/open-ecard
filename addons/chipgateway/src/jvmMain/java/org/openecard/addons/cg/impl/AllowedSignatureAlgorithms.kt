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
package org.openecard.addons.cg.impl

import org.openecard.crypto.common.SignatureAlgorithms
import org.openecard.crypto.common.SignatureAlgorithms.Companion.fromAlgId
import org.openecard.crypto.common.SignatureAlgorithms.Companion.fromJcaName
import org.openecard.crypto.common.UnsupportedAlgorithmException
import java.util.EnumSet

/**
 *
 * @author Tobias Wich
 */
object AllowedSignatureAlgorithms {
	private val ALLOWED_ALGS: EnumSet<SignatureAlgorithms> =
		EnumSet.of(
			SignatureAlgorithms.CKM_RSA_PKCS,
			SignatureAlgorithms.CKM_SHA1_RSA_PKCS,
			SignatureAlgorithms.CKM_SHA256_RSA_PKCS,
			SignatureAlgorithms.CKM_SHA384_RSA_PKCS,
			SignatureAlgorithms.CKM_RSA_PKCS_PSS,
			SignatureAlgorithms.CKM_SHA1_RSA_PKCS_PSS,
			SignatureAlgorithms.CKM_SHA256_RSA_PKCS_PSS,
			SignatureAlgorithms.CKM_SHA384_RSA_PKCS_PSS,
			SignatureAlgorithms.CKM_ECDSA,
			SignatureAlgorithms.CKM_ECDSA_SHA1,
			SignatureAlgorithms.CKM_ECDSA_SHA256,
			SignatureAlgorithms.CKM_ECDSA_SHA384,
		)

	@Throws(UnsupportedAlgorithmException::class)
	fun algIdtoJcaName(algId: String?): String {
		val alg = fromAlgId(algId)
		if (!ALLOWED_ALGS.contains(alg)) {
			val msg = "The requested algorithm is not allowed in the ChipGateway protocol."
			throw UnsupportedAlgorithmException(msg)
		}
		return alg.jcaAlg
	}

	fun isKnownJcaAlgorithm(jcaAlg: String?): Boolean {
		try {
			val alg = fromJcaName(jcaAlg)
			return ALLOWED_ALGS.contains(alg)
		} catch (ex: UnsupportedAlgorithmException) {
			return false
		}
	}
}
