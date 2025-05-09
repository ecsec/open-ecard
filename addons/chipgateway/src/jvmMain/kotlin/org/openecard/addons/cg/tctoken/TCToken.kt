/****************************************************************************
 * Copyright (C) 2014-2025 ecsec GmbH.
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
package org.openecard.addons.cg.tctoken

import generated.TCTokenType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addons.cg.ex.ErrorTranslations
import org.openecard.addons.cg.ex.InvalidRedirectUrlException
import org.openecard.addons.cg.ex.InvalidTCTokenElement
import org.openecard.addons.cg.ex.ResultMinor
import org.openecard.common.util.UrlBuilder
import java.net.URISyntaxException

private val LOG = KotlinLogging.logger { }

/**
 * Helper class adding further functionality to the underlying TCTokenType.
 *
 * @author Tobias Wich
 */
class TCToken : TCTokenType() {
	var isForceProcessing: Boolean = false
		private set
	var jWK: String? = null
		private set

	@Throws(InvalidRedirectUrlException::class)
	fun finalizeErrorAddress(code: ResultMinor): String =
		try {
			UrlBuilder
				.fromUrl(getRefreshAddress())
				.queryParam("ResultMajor", "error")
				.queryParam("ResultMinor", code.value)
				.build()
				.toString()
		} catch (ex: URISyntaxException) {
			LOG.error {
				"Failed to modify RefreshAddress from resultMinor, this should not happen due to previously executed checks."
			}
			throw InvalidRedirectUrlException(ErrorTranslations.INVALID_REFRESH_ADDR)
		}

	@Throws(InvalidRedirectUrlException::class)
	private fun finalizeOkAddressParam(status: String?): String =
		try {
			UrlBuilder
				.fromUrl(getRefreshAddress())
				.queryParam("ResultMajor", "ok")
				.queryParam("status", status)
				.build()
				.toString()
		} catch (ex: URISyntaxException) {
			LOG.error { "Failed to modify RefreshAddress from code, this should not happen due to previously executed checks." }
			throw InvalidRedirectUrlException(ErrorTranslations.INVALID_REFRESH_ADDR)
		}

	@Throws(InvalidRedirectUrlException::class)
	fun finalizeOkAddress(): String = finalizeOkAddressParam("ok")

	@Throws(InvalidRedirectUrlException::class)
	fun finalizeBusyAddress(): String = finalizeOkAddressParam("busy")

	companion object {
		@Throws(InvalidRedirectUrlException::class, InvalidTCTokenElement::class)
		fun generateToken(params: Map<String, String>): TCToken {
			val pathSecProto = params["PathSecurity-Protocol"]
			val forceProcessing = params["ForceProcessing"]

			val pathSecParams = params["PathSecurity-Parameters"]
			val token =
				TCToken().apply {
					this.serverAddress = params["ServerAddress"]
					sessionIdentifier = params["SessionIdentifier"]
					refreshAddress = params["RefreshAddress"]
					this.binding = params["Binding"]
					pathSecurityProtocol = pathSecParams
				}
			pathSecParams?.let {
				if ("http://ws.openecard.org/pathsecurity/tlsv12-with-pin-encryption" == pathSecProto) {
					token.jWK = it
				}
			}

			token.isForceProcessing =
				if (forceProcessing == null || "" == forceProcessing) {
					// the default when no value is set
					true
				} else {
					forceProcessing.toBoolean()
				}

			// validate TCToken
			TCTokenVerifier(token).verifyRequestToken()

			return token
		}
	}
}
