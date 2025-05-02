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

import generated.TCTokenType
import org.openecard.binding.tctoken.ex.*
import org.openecard.bouncycastle.tls.TlsServerCertificate
import org.openecard.common.util.Pair
import org.openecard.common.util.TR03112Utils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.MalformedURLException
import java.net.URL
import kotlin.collections.HashMap
import kotlin.collections.MutableList
import kotlin.collections.MutableMap

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
    var certificates: MutableList<Pair<URL?, TlsServerCertificate?>?>? = null
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
            val isNpa = BINDING_PAOS == tokenCtx!!.getToken().getBinding()
            // disable checks when not using the nPA
            val activationChecks: Boolean
            if (!isNpa) {
                activationChecks = false
            } else if (TR03112Utils.DEVELOPER_MODE) {
                activationChecks = false
                LOG.warn("DEVELOPER_MODE: All TR-03124-1 security checks are disabled.")
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
        private val LOG: Logger = LoggerFactory.getLogger(TCTokenRequest::class.java)

        private const val TC_TOKEN_URL_KEY = "tcTokenURL"

        @Throws(
            InvalidRedirectUrlException::class,
            SecurityViolationException::class,
            UserCancellationException::class,
            InvalidTCTokenException::class,
            InvalidTCTokenElement::class,
            MissingActivationParameterException::class,
            AuthServerException::class,
            InvalidAddressException::class
        )
        fun fetchTCToken(parameters: MutableMap<String?, String?>): TCTokenRequest {
            val copyParams: MutableMap<String?, String> = HashMap<String?, String>(parameters)
            val tokenInfo: Pair<TCTokenContext?, URL?> = extractTCTokenContext(copyParams)
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
        @Throws(MissingActivationParameterException::class)
        fun convert(parameters: MutableMap<String?, String>, tokenInfo: Pair<TCTokenContext?, URL?>): TCTokenRequest {
            val result: TCTokenRequest = parseTCTokenRequestURI(parameters, tokenInfo)
            return result
        }

        @Throws(MissingActivationParameterException::class)
        private fun parseTCTokenRequestURI(
            queries: MutableMap<String?, String>,
            tokenInfo: Pair<TCTokenContext?, URL?>
        ): TCTokenRequest {
            val tcTokenRequest = TCTokenRequest()

            if (tokenInfo == null || tokenInfo.p1 == null || tokenInfo.p2 == null) {
                throw MissingActivationParameterException(ErrorTranslations.NO_TOKEN)
            }

            for (next in queries.entries) {
                var k = next.key
                k = if (k == null) "" else k
                val v: String? = next.value

                if (v == null || v.isEmpty()) {
                    LOG.info("Skipping query parameter '{}' because it does not contain a value.", k)
                } else {
                    when (k) {
                        TC_TOKEN_URL_KEY -> LOG.info(
                            "Skipping given query parameter '{}' because it was already extracted",
                            TC_TOKEN_URL_KEY
                        )

                        else -> LOG.info("Unknown query element: {}", k)
                    }
                }
            }

            tcTokenRequest.tokenCtx = tokenInfo.p1
            tcTokenRequest.token = tokenInfo.p1!!.getToken()
            tcTokenRequest.certificates = tokenInfo.p1!!.certs

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
        @Throws(
            AuthServerException::class,
            InvalidRedirectUrlException::class,
            InvalidAddressException::class,
            InvalidTCTokenElement::class,
            SecurityViolationException::class,
            UserCancellationException::class,
            MissingActivationParameterException::class,
            InvalidTCTokenException::class,
            InvalidTCTokenUrlException::class
        )
        private fun extractTCTokenContext(queries: MutableMap<String?, String>): Pair<TCTokenContext?, URL?> {
            val tcTokenUrl: String = queries.get(TC_TOKEN_URL_KEY)!!

            val result: Pair<TCTokenContext?, URL?> = extractTCTokenContextInt(tcTokenUrl)
            queries.remove(TC_TOKEN_URL_KEY)
            return result
        }

        @Throws(
            AuthServerException::class,
            InvalidRedirectUrlException::class,
            InvalidAddressException::class,
            InvalidTCTokenElement::class,
            SecurityViolationException::class,
            UserCancellationException::class,
            MissingActivationParameterException::class,
            InvalidTCTokenException::class,
            InvalidTCTokenUrlException::class
        )
        private fun extractTCTokenContextInt(activationTokenUrl: String): Pair<TCTokenContext?, URL?> {
            if (activationTokenUrl == null) {
                throw MissingActivationParameterException(ErrorTranslations.NO_TOKEN)
            }

            val tokenUrl: URL?
            try {
                tokenUrl = URL(activationTokenUrl)
            } catch (ex: MalformedURLException) {
                // TODO: check if the error type is correct, was WRONG_PARAMETER before
                throw InvalidTCTokenUrlException(ErrorTranslations.INVALID_TCTOKEN_URL, ex, activationTokenUrl)
            }
            val tokenCtx: TCTokenContext = TCTokenContext.Companion.generateTCToken(tokenUrl)
            return Pair<Any?, Any?>(tokenCtx, tokenUrl)
        }
    }
}
