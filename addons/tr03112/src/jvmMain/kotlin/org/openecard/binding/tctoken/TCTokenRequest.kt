/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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
package org.openecard.binding.tctoken

import dev.icerock.moko.resources.format
import generated.TCTokenType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.binding.tctoken.ex.AuthServerException
import org.openecard.binding.tctoken.ex.InvalidAddressException
import org.openecard.binding.tctoken.ex.InvalidRedirectUrlException
import org.openecard.binding.tctoken.ex.InvalidTCTokenElement
import org.openecard.binding.tctoken.ex.InvalidTCTokenException
import org.openecard.binding.tctoken.ex.InvalidTCTokenUrlException
import org.openecard.binding.tctoken.ex.MissingActivationParameterException
import org.openecard.binding.tctoken.ex.SecurityViolationException
import org.openecard.binding.tctoken.ex.UserCancellationException
import org.openecard.bouncycastle.tls.TlsServerCertificate
import org.openecard.common.ECardConstants.BINDING_PAOS
import org.openecard.common.ECardConstants.PATH_SEC_PROTO_TLS_PSK
import org.openecard.common.util.Pair
import org.openecard.common.util.TR03112Utils
import org.openecard.i18n.I18N
import java.net.MalformedURLException
import java.net.URL

private val logger = KotlinLogging.logger { }

/**
 * This class represents a TC Token request to the client. It contains the [TCTokenType] and situational parts
 * like the ifdName or the server certificates received while retrieving the TC Token.
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
class TCTokenRequest {
	private var token: TCToken? = null

	/**
	 * Gets the certificates of the servers that have been passed while the TCToken was retrieved.
	 *
	 * @return List of the X509 server certificates and the requested URLs. May be null under certain circumstances
	 * (e.g. legacy activation).
	 */
	var certificates: List<Pair<URL, TlsServerCertificate>>? = null
		private set
	private var tokenCtx: TCTokenContext? = null

	val tCToken: TCToken
		/**
		 * Returns the TCToken.
		 *
		 * @return TCToken
		 */
		get() = token!!

	val tokenContext: TCTokenContext
		get() = tokenCtx!!

	val isPerformTR03112Checks: Boolean
		/**
		 * Checks if checks according to BSI TR03112-7 3.4.2, 3.4.4 and 3.4.5 must be performed.
		 *
		 * @return `true` if checks should be performed, `false` otherwise.
		 */
		get() {
			// nobody uses PAOS, so for now use this as an indicator, that we need to use the nPA
			val isNpa = BINDING_PAOS == tokenCtx!!.token.getBinding()
			// disable checks when not using the nPA
			val activationChecks: Boolean
			if (!isNpa) {
				activationChecks = false
			} else if (TR03112Utils.DEVELOPER_MODE) {
				activationChecks = false
				logger.warn { "DEVELOPER_MODE: All TR-03124-1 security checks are disabled." }
			} else {
				activationChecks = true
			}
			return activationChecks
		}

	val isSameChannel: Boolean
		get() {
			val token: TCTokenType = this.tCToken
			val secProto = token.getPathSecurityProtocol()
			// check security proto
			if (secProto == null || "" == secProto) {
				return true
			}
			// check PSK value
			if (secProto == PATH_SEC_PROTO_TLS_PSK) {
				val pathsecParams = token.getPathSecurityParameters()
				return pathsecParams == null || pathsecParams.getPSK() == null || pathsecParams.getPSK().size == 0
			} else {
				return false
			}
		}

	companion object {
		private const val TC_TOKEN_URL_KEY = "tcTokenURL"

		fun fetchTCToken(parameters: Map<String, String>): TCTokenRequest {
			val copyParams: Map<String, String> = parameters.toMap()
			val tokenInfo: Pair<TCTokenContext, URL> = extractTCTokenContext(copyParams)
			val req: TCTokenRequest = convert(copyParams, tokenInfo)
			return req
		}

		/**
		 * Check and evaluate the request parameters and wrap the result in a `TCTokenRequest` class.
		 *
		 * @param parameters The request parameters.
		 * @param tokenInfo The token.
		 * @return A TCTokenRequest wrapping the parameters.
		 * @throws MissingActivationParameterException
		 */
		fun convert(
			parameters: Map<String, String>,
			tokenInfo: Pair<TCTokenContext, URL>,
		): TCTokenRequest {
			val result: TCTokenRequest = parseTCTokenRequestURI(parameters, tokenInfo)
			return result
		}

		private fun parseTCTokenRequestURI(
			queries: Map<String, String>,
			tokenInfo: Pair<TCTokenContext, URL>,
		): TCTokenRequest {
			val tcTokenRequest = TCTokenRequest()

			for (next in queries.entries) {
				val k = next.key
				val v: String? = next.value

				if (v == null || v.isEmpty()) {
					logger.info {
						"Skipping query parameter '$k' because it does not contain a value."
					}
				} else {
					when (k) {
						TC_TOKEN_URL_KEY ->
							logger.info {
								"Skipping given query parameter '$TC_TOKEN_URL_KEY' because it was already extracted"
							}

						else -> logger.info { "Unknown query element: $k" }
					}
				}
			}

			tcTokenRequest.tokenCtx = tokenInfo.p1
			tcTokenRequest.token = tokenInfo.p1.token
			tcTokenRequest.certificates = tokenInfo.p1.certs

			return tcTokenRequest
		}

		/**
		 * Evaluate and extract the TC Token context from the given parameters.
		 * @param queries The request parameters.
		 * @return The TC Token context and the URL from which it was derived.
		 * @throws AuthServerException
		 * @throws InvalidRedirectUrlException
		 * @throws InvalidAddressException
		 * @throws InvalidTCTokenElement
		 * @throws SecurityViolationException
		 * @throws UserCancellationException
		 * @throws MissingActivationParameterException
		 * @throws InvalidTCTokenException
		 * @throws InvalidTCTokenUrlException
		 */

		private fun extractTCTokenContext(queries: Map<String, String>): Pair<TCTokenContext, URL> {
			val tcTokenUrl = queries[TC_TOKEN_URL_KEY]

			val result = extractTCTokenContextInt(tcTokenUrl)
			return result
		}

		private fun extractTCTokenContextInt(activationTokenUrl: String?): Pair<TCTokenContext, URL> {
			if (activationTokenUrl == null) {
				throw MissingActivationParameterException(
					I18N.strings.tr03112_missing_activation_parameter_exception_no_valid_tctoken_available.localized(),
				)
			}

			val tokenUrl: URL
			try {
				tokenUrl = URL(activationTokenUrl)
			} catch (ex: MalformedURLException) {
				// TODO: check if the error type is correct, was WRONG_PARAMETER before
				throw InvalidTCTokenUrlException(
					I18N.strings.tr03112_invalid_tctoken_url_exception_invalid_tctoken_url
						.format(activationTokenUrl)
						.localized(),
					ex,
				)
			}
			val tokenCtx: TCTokenContext = TCTokenContext.generateTCToken(tokenUrl)
			return Pair(tokenCtx, tokenUrl)
		}
	}
}
