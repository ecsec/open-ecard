/****************************************************************************
 * Copyright (C) 2012-2025 ecsec GmbH.
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

import org.jose4j.jwk.JsonWebKey
import org.jose4j.lang.JoseException
import org.openecard.addons.cg.ex.ErrorTranslations
import org.openecard.addons.cg.ex.InvalidRedirectUrlException
import org.openecard.addons.cg.ex.InvalidTCTokenElement
import org.openecard.common.ECardConstants.PATH_SEC_PROTO_MTLS
import java.net.MalformedURLException
import java.net.URI
import java.net.URL

/**
 * Implements a verifier to check the elements of a TCToken.
 *
 * @param token Token
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
class TCTokenVerifier(
	private val token: TCToken,
) {
	/**
	 * Verifies the elements of the TCToken.
	 *
	 * @throws InvalidRedirectUrlException Thrown in case the RefreshAddress is missing or invalid.
	 * @throws InvalidTCTokenElement Thrown in case any element inside the TCToken is invalid.
	 */
	@Throws(InvalidRedirectUrlException::class, InvalidTCTokenElement::class)
	fun verifyRequestToken() {
		assertRefreshURL(token.refreshAddress)
		assertHttpsURL("ServerAddress", token.serverAddress)
		assertRequired("SessionIdentifier", token.sessionIdentifier)
		assertRequired("PathSecurity-Protocol", token.pathSecurityProtocol)
		checkEqualOR(
			"PathSecurity-Protocol",
			token.getPathSecurityProtocol(),
			PATH_SEC_PROTO_MTLS,
			"http://ws.openecard.org/pathsecurity/tlsv12-with-pin-encryption",
		)
		if (token.pathSecurityProtocol == "http://ws.openecard.org/pathsecurity/tlsv12-with-pin-encryption") {
			assertRequired("JWK", token.jWK)
			try {
				JsonWebKey.Factory.newJwk(token.jWK)
			} catch (ex: JoseException) {
				throw InvalidTCTokenElement("Failed to parse JWK.", ex)
			}
		}
	}

	/**
	 * Checks if the value is "empty".
	 *
	 * @param value Value
	 * @return True if the element is empty, otherwise false
	 */
	private fun checkEmpty(value: Any?): Boolean =
		when (val v = value) {
			is String -> {
				v.isEmpty()
			}
			is URL -> {
				v.toString().isEmpty()
			}
			is ByteArray -> {
				v.isEmpty()
			}
			else -> true
		}

	/**
	 * Checks the value for equality against any of the given reference values.
	 *
	 * @param name Name of the element to check. This value is used to provide a concise error message.
	 * @param value Value to test.
	 * @param reference Reference values to test equality against.
	 * @throws InvalidTCTokenElement Thrown in case the value is not equal to any of the reference values.
	 */
	@Throws(InvalidTCTokenElement::class)
	private fun checkEqualOR(
		name: String?,
		value: String,
		vararg reference: String?,
	) {
		if (!reference.contains(value)) {
			throw InvalidTCTokenElement(ErrorTranslations.ELEMENT_VALUE_INVALID, name)
		}
	}

	/**
	 * Checks if the element is present.
	 *
	 * @param name Name of the element to check. This value is used to provide a concise error message.
	 * @param value Value to test.
	 * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
	 * @throws InvalidTCTokenElement Thrown in case the value is null or empty.
	 */
	@Throws(InvalidTCTokenElement::class)
	private fun assertRequired(
		name: String?,
		value: Any?,
	) {
		if (checkEmpty(value)) {
			throw InvalidTCTokenElement(ErrorTranslations.ELEMENT_MISSING, name)
		}
	}

	@Throws(InvalidTCTokenElement::class)
	private fun assertURL(
		name: String?,
		value: String,
	): URL {
		try {
			return URI(value).toURL()
		} catch (e: MalformedURLException) {
			throw InvalidTCTokenElement(ErrorTranslations.MALFORMED_URL, name)
		}
	}

	@Throws(InvalidTCTokenElement::class)
	private fun assertHttpsURL(
		name: String?,
		value: String,
	): URL {
		val url = assertURL(name, value)
		if ("https" == url.protocol) {
			return url
		}
		throw InvalidTCTokenElement(ErrorTranslations.NO_HTTPS_URL, name)
	}

	@Throws(InvalidRedirectUrlException::class)
	private fun assertRefreshURL(value: String): URL {
		try {
			val url = URI(value).toURL()
			if ("https" == url.protocol) {
				return url
			}
			throw InvalidRedirectUrlException(ErrorTranslations.INVALID_REFRESH_ADDR)
		} catch (ex: MalformedURLException) {
			throw InvalidRedirectUrlException(ErrorTranslations.INVALID_REFRESH_ADDR)
		}
	}
}
