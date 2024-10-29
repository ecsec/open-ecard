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

import com.appstractive.jwt.jwt
import com.appstractive.jwt.sign
import com.appstractive.jwt.signatures.es256
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ReleaseLoaderTest {
	val wm = WireMockServer()

	@BeforeTest
	fun startWiremock() {
		wm.start()
	}

	@AfterTest
	fun stopWiremock() {
		wm.stop()
	}

	@Test
	fun testLoadReleaseInfo() = runBlocking {
		val releaseInfoJson = Json.parseToJsonElement(
			"""
				{
					"version": "2.2.4",
					"latestVersion": {"version": "2.2.4", "artifacts":  []},
					"maintenanceVersions": [{"version": "2.1.4", "artifacts": []}],
					"artifacts": [],
					"versionStatus": {
						"maintained": ["~2.1.0"],
						"security": [">=2.1.0 <=2.1.2"]
					}
				}
			""".trimIndent()
		)
		val jwt = jwt {
			claims {
				issuer = "https://openecard.org"
				audience = "https://openecard.org/app"
				issuedAt = Clock.System.now()
				claim("release-info", releaseInfoJson)
			}
		}.sign {
			es256 { pem(javaClass.getResourceAsStream("/release-test-signer.pem")?.readAllBytes()!!) }
		}.toString()
		wm.stubFor(
			get(urlEqualTo("/release.jwt"))
				.withHeader("Accept", containing("application/jwt"))
				.willReturn(
					okForContentType("application/jwt", jwt)
				)
		)

		val baseUrl = wm.baseUrl()
		val releaseInfo = loadReleaseInfo("$baseUrl/release.jwt").getOrThrow().releaseInfo
		assertEquals("2.2.4", releaseInfo.latestVersion.version.toString())
	}
}
