/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
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

import org.openecard.binding.tctoken.ex.*
import org.openecard.bouncycastle.tls.TlsServerCertificate
import org.openecard.common.DynamicContext
import org.openecard.common.util.Pair
import org.openecard.httpcore.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URL

/**
 * Class to fetch a TCToken.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
class TCTokenContext private constructor(
	val token: TCToken?,
	base: ResourceContext,
) : ResourceContext(base.tlsClient, base.tlsClientProto, base.certs) {
	companion object {
		private val LOG: Logger = LoggerFactory.getLogger(TCTokenContext::class.java)

		@JvmStatic
		@Throws(
			InvalidTCTokenException::class,
			AuthServerException::class,
			InvalidRedirectUrlException::class,
			InvalidTCTokenElement::class,
			InvalidTCTokenUrlException::class,
			SecurityViolationException::class,
			InvalidAddressException::class,
			UserCancellationException::class,
		)
		fun generateTCToken(tcTokenURL: URL): TCTokenContext {
			// Get TCToken from the given url
			try {
				val ctx = TrResourceContextLoader().getStream(tcTokenURL)
				return Companion.generateTCToken(ctx!!.data!!, ctx)
			} catch (ex: InsecureUrlException) {
				throw InvalidAddressException(ErrorTranslations.INVALID_ADDRESS)
			} catch (ex: InvalidRedirectChain) {
				throw InvalidAddressException(ErrorTranslations.INVALID_REFRESH_ADDRESS_NOSOP)
			} catch (ex: IOException) {
				throw TCTokenRetrievalException(ErrorTranslations.RETRIEVAL_FAILED, ex)
			} catch (ex: HttpResourceException) {
				throw TCTokenRetrievalException(ErrorTranslations.RETRIEVAL_FAILED, ex)
			} catch (ex: InvalidProxyException) {
				throw TCTokenRetrievalException(ErrorTranslations.RETRIEVAL_FAILED, ex)
			} catch (ex: ValidationError) {
				throw TCTokenRetrievalException(ErrorTranslations.RETRIEVAL_FAILED, ex)
			}
		}

		@Throws(
			InvalidTCTokenException::class,
			AuthServerException::class,
			InvalidRedirectUrlException::class,
			InvalidTCTokenElement::class,
			InvalidTCTokenUrlException::class,
			SecurityViolationException::class,
			UserCancellationException::class,
		)
		private fun generateTCToken(
			data: String,
			base: ResourceContext,
		): TCTokenContext {
			// correct common TCToken shortcomings
			var data = data
			LOG.debug("Received TCToken:\n{}", data)
			data = TCTokenHacks.fixPathSecurityParameters(data)
			LOG.debug("Cleaned up TCToken:\n{}", data)

			// Parse the TCToken
			val parser = TCTokenParser()
			val tokens = parser.parse(data)

			if (tokens.isEmpty()) {
				throw InvalidTCTokenException(ErrorTranslations.NO_TCTOKEN_IN_DATA)
			}

			// Verify the TCToken
			val token = tokens.get(0)
			val ver = TCTokenVerifier(token, base)
			if (ver.isErrorToken()) {
				val minor = ResultMinor.CLIENT_ERROR
				throw AuthServerException(token.getComErrorAddressWithParams(minor), ErrorTranslations.ESERVICE_ERROR)
			}

			val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)
			val resultPoints: MutableList<Pair<URL?, TlsServerCertificate?>> = base.certs
			// probably just for tests
			if (!resultPoints.isEmpty()) {
				val last = resultPoints.get(0)
				dynCtx.put(TR03112Keys.TCTOKEN_URL, last.p1)
			}

			ver.verifyUrlToken()

			return TCTokenContext(token, base)
		}
	}
}
