/****************************************************************************
 * Copyright (C) 2024 ecsec GmbH.
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

package org.openecard.releases

import com.appstractive.jwt.*
import com.appstractive.jwt.signatures.es256
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.json.*

data class ReleaseInfoResult(
	val jwt: String,
	val releaseInfo: ReleaseInfo,
)

suspend fun loadReleaseInfo(releaseInfoUrl: String): Result<ReleaseInfoResult> = runCatching {
	val client = createHttpClient()
	val jws: String = client.get(releaseInfoUrl) {
		header("Accept", "application/jwt")
	}.body()
	val json = verifyReleaseInfoJwt(jws).getOrThrow()
	val releaseInfo: ReleaseInfo = Json.decodeFromJsonElement(json)
	ReleaseInfoResult(jws, releaseInfo)
}

suspend fun verifyReleaseInfoJwt(jwtStr: String): Result<JsonObject> = runCatching {
	val verificationKey = MR.files.release_verifier_pem.readText()
	val jwt = JWT.from(jwtStr)
	val valid = jwt.verify {
		es256 {
			pem(verificationKey)
		}
		issuer("https://openecard.org")
		audience("https://openecard.org/app")
		//notBefore()
	}

	if (!valid) {
		throw IllegalArgumentException("JWT verification failed")
	}

	jwt.claims["release-info"]?.jsonObject
		?: throw IllegalArgumentException("JWT does not contain release-info")
}
