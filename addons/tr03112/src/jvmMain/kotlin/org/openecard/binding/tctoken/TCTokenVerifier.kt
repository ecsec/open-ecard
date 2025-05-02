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
import org.openecard.common.util.TR03112Utils
import org.openecard.common.util.UrlBuilder
import org.openecard.httpcore.*
import java.io.IOException
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import javax.annotation.Nonnull

/**
 * Implements a verifier to check the elements of a TCToken.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
class TCTokenVerifier
/**
 * Creates a new TCTokenVerifier to verifyUrlToken a TCToken.
 *
 * @param token Token
 * @param ctx Context over which the token has been received.
 */(@param:Nonnull private val token: TCToken, private val ctx: ResourceContext) {
    val isErrorToken: Boolean
        /**
         * Checks if the token is a response to an error.
         * These kind of token only contain the CommunicationErrorAdress field.
         *
         * @return `true` if this is an error token, `false` otherwise.
         */
        get() {
            if (token.getCommunicationErrorAddress() != null) {
                // refresh address is essential, if that one is missing, it must be an error token
                return token.getRefreshAddress() == null
            }
            return false
        }

    /**
     * Verifies the elements of the TCToken.
     *
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws InvalidTCTokenElement Thrown in case one of the values to test is errornous.
     * @throws InvalidTCTokenUrlException Thrown in case a tested URL does not conform to the specification.
     * @throws SecurityViolationException Thrown in case the same origin policy is violated.
     * @throws UserCancellationException Thrown in case the user aborted the insert card dialog.
     */
    @Throws(
        InvalidRedirectUrlException::class,
        InvalidTCTokenElement::class,
        InvalidTCTokenUrlException::class,
        SecurityViolationException::class,
        UserCancellationException::class
    )
    fun verifyUrlToken() {
        // ordering is important because of the raised errors in case the first two are not https URLs
        initialUrlTokenCheck()
        verifyRefreshAddress()
        verifyCommunicationErrorAddress()
        checkUserCancellation()
        verifyServerAddress()
        verifySessionIdentifier()
        verifyBinding()
        verifyPathSecurity()
    }

    /**
     * Verifies the ServerAddress element of the TCToken.
     *
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws InvalidTCTokenElement Thrown in case one of the values to test is errornous.
     * @throws UserCancellationException Thrown in case the user aborted the insert card dialog but this should never
     * happen in this case.
     */
    @Throws(InvalidRedirectUrlException::class, InvalidTCTokenElement::class, UserCancellationException::class)
    fun verifyServerAddress() {
        val value = token.getServerAddress()
        try {
            assertRequired("ServerAddress", value)
        } catch (ex: InvalidTCTokenElement) {
            determineRefreshAddress(ex)
        }

        try {
            assertHttpsURL("ServerAddress", value)
        } catch (ex: InvalidTCTokenUrlException) {
            determineRefreshAddress(ex)
        }
    }

    /**
     * Verifies the SessionIdentifier element of the TCToken.
     *
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws InvalidTCTokenElement Thrown in case one of the values to test is errornous.
     */
    @Throws(InvalidRedirectUrlException::class, InvalidTCTokenElement::class)
    fun verifySessionIdentifier() {
        val value = token.getSessionIdentifier()
        assertRequired("SessionIdentifier", value)
    }

    /**
     * Verifies the RefreshAddress element of the TCToken.
     *
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws InvalidTCTokenElement Thrown in case one of the values to test is errornous.
     * @throws UserCancellationException Thrown in case the user aborted the insert card dialog but this should never
     * happen in this case.
     */
    @Throws(InvalidRedirectUrlException::class, InvalidTCTokenElement::class, UserCancellationException::class)
    fun verifyRefreshAddress() {
        val value = token.getRefreshAddress()
        assertRequired("RefreshAddress", value)
        try {
            assertHttpsURL("RefreshAddress", value)
        } catch (ex: InvalidTCTokenUrlException) {
            determineRefreshAddress(ex)
        }
    }

    /**
     * Verifies the CommunicationErrorAddress element of the TCToken.
     *
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws InvalidTCTokenElement Thrown in case one of the values to test is errornous.
     * @throws InvalidTCTokenUrlException Thrown in case a tested URL does not conform to the specification.
     */
    @Throws(InvalidRedirectUrlException::class, InvalidTCTokenElement::class, InvalidTCTokenUrlException::class)
    fun verifyCommunicationErrorAddress() {
        val value = token.getCommunicationErrorAddress()
        if (!checkEmpty(value)) {
            assertRequired("CommunicationErrorAddress", value)
            assertHttpsURL("CommunicationErrorAddress", value!!)
        }
    }

    /**
     * Verifies the Binding element of the TCToken.
     *
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws InvalidTCTokenElement Thrown in case one of the values to test is errornous.
     */
    @Throws(InvalidRedirectUrlException::class, InvalidTCTokenElement::class)
    fun verifyBinding() {
        val value = token.getBinding()
        assertRequired("Binding", value)
        checkEqualOR("Binding", value, BINDING_PAOS, BINDING_HTTP)
    }

    /**
     * Verifies the PathSecurity-Protocol and PathSecurity-Parameters element of the TCToken.
     *
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws InvalidTCTokenElement Thrown in case one of the values to test is errornous.
     * @throws InvalidTCTokenUrlException Thrown in case a tested URL does not conform to the specification.
     * @throws SecurityViolationException Thrown in case the same origin policy is violated.
     * @throws UserCancellationException Thrown in case the user aborted the insert card dialog but this should never
     * happen in this case.
     */
    @Throws(
        InvalidRedirectUrlException::class,
        InvalidTCTokenElement::class,
        InvalidTCTokenUrlException::class,
        SecurityViolationException::class,
        UserCancellationException::class
    )
    fun verifyPathSecurity() {
        val proto = token.getPathSecurityProtocol()
        val psp = token.getPathSecurityParameters()

        // TR-03124 sec. 2.4.3
        // If no PathSecurity-Protocol/PSK is given in the TC Token, the same TLS channel as established to
        // retrieve the TC Token MUST be used for the PAOS connection, i.e. a new channel MUST NOT be established.
        if ((checkEmpty(proto) && checkEmpty(psp))) {
            assertSameChannel("ServerAddress", token.getServerAddress())
            return
        } else if ((!checkEmpty(proto)) && PATH_SEC_PROTO_TLS_PSK == proto && checkEmpty(psp) && !token.isInvalidPSK()) {
            assertSameChannel("ServerAddress", token.getServerAddress())
            return
        }

        assertRequired("PathSecurityProtocol", proto)
        val protos = arrayOf<String?>(PATH_SEC_PROTO_MTLS, PATH_SEC_PROTO_TLS_PSK)
        checkEqualOR("PathSecurityProtocol", proto, *protos)
        if (PATH_SEC_PROTO_TLS_PSK == proto) {
            if (token.isInvalidPSK()) {
                val minor = ResultMinor.COMMUNICATION_ERROR
                val errorUrl = token.getComErrorAddressWithParams(minor)
                determineRefreshAddress(InvalidTCTokenElement(errorUrl, ErrorTranslations.INVALID_ELEMENT, "PSK"))
            }
            try {
                assertRequired("PathSecurityParameters", psp)
                assertRequired("PSK", psp.getPSK())
            } catch (ex: InvalidTCTokenElement) {
                determineRefreshAddress(ex)
            }
        }
    }

    /**
     * Checks if the value is "empty".
     *
     * @param value Value
     * @return True if the element is empty, otherwise false
     */
    private fun checkEmpty(value: Any?): Boolean {
        if (value != null) {
            if (value is String) {
                if (value.isEmpty()) {
                    return true
                }
            } else if (value is URL) {
                if (value.toString().isEmpty()) {
                    return true
                }
            } else if (value is ByteArray) {
                if (value.size == 0) {
                    return true
                }
            }
            return false
        }
        return true
    }


    /**
     * Checks the value for equality against any of the given reference values.
     *
     * @param name Name of the element to check. This value is used to provide a concise error message.
     * @param value Value to test.
     * @param reference Reference values to test equality against.
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws InvalidTCTokenElement Thrown in case the value is not equal to any of the reference values.
     */
    @Throws(InvalidRedirectUrlException::class, InvalidTCTokenElement::class)
    private fun checkEqualOR(name: String?, value: String, vararg reference: String?) {
        for (string in reference) {
            if (value == string) {
                return
            }
        }

        val minor = ResultMinor.COMMUNICATION_ERROR
        val errorUrl = token.getComErrorAddressWithParams(minor)
        throw InvalidTCTokenElement(errorUrl, ErrorTranslations.INVALID_ELEMENT, name as Any?)
    }

    /**
     * Checks if the element is present.
     *
     * @param name Name of the element to check. This value is used to provide a concise error message.
     * @param value Value to test.
     * @throws InvalidRedirectUrlException Thrown in case no redirect URL could be determined.
     * @throws InvalidTCTokenElement Thrown in case the value is null or empty.
     */
    @Throws(InvalidRedirectUrlException::class, InvalidTCTokenElement::class)
    private fun assertRequired(name: String?, value: Any?) {
        if (checkEmpty(value)) {
            val minor = ResultMinor.COMMUNICATION_ERROR
            val errorUrl = token.getComErrorAddressWithParams(minor)
            throw InvalidTCTokenElement(errorUrl, ErrorTranslations.MISSING_ELEMENT, name)
        }
    }

    @Throws(InvalidTCTokenUrlException::class)
    private fun assertURL(name: String?, value: String): URL {
        try {
            return URL(value)
        } catch (e: MalformedURLException) {
            throw InvalidTCTokenUrlException(ErrorTranslations.MALFORMED_URL, name)
        }
    }

    @Throws(InvalidTCTokenUrlException::class)
    private fun assertHttpsURL(name: String?, value: String): URL {
        val url = assertURL(name, value)
        if ("https" != url.getProtocol()) {
            throw InvalidTCTokenUrlException(ErrorTranslations.NO_HTTPS_URL, name)
        } else {
            return url
        }
    }

    @Throws(InvalidRedirectUrlException::class, InvalidTCTokenUrlException::class, SecurityViolationException::class)
    private fun assertSameChannel(name: String?, address: String) {
        // check that everything can be handled over the same channel
        // TR-03124-1 does not mention that redirects on the TCToken address are possible and it also states that there
        // are only two channels. So I guess we should force this here as well.
        val paosUrl = assertURL(name, address)
        val urls: MutableList<Pair<URL?, TlsServerCertificate?>> = ctx.certs
        for (next in urls) {
            if (!TR03112Utils.checkSameOriginPolicy(paosUrl, next.p1)) {
                val minor = ResultMinor.COMMUNICATION_ERROR
                val errorUrl = token.getComErrorAddressWithParams(minor)
                throw SecurityViolationException(errorUrl, ErrorTranslations.FAILED_SOP)
            }
        }
    }

    /**
     * Creates an[URL] with a communication error as parameters and also a given error message.
     *
     * @param refreshAddress The address which should get the error parameters.
     * @param minor The result minor to attach to the `refreshAddress`.
     * @param minorMessage The result message.
     * @return An [URL] object containing the error query parameters.
     * @throws URISyntaxException Thrown if the given `refreshAddress` is not a valid URL.
     */
    @Throws(URISyntaxException::class)
    private fun createUrlWithErrorParams(refreshAddress: String?, minor: String, minorMessage: String?): URI {
        return UrlBuilder.fromUrl(refreshAddress)
            .queryParam("ResultMajor", "error")
            .queryParamUrl("ResultMinor", TCTokenHacks.fixResultMinor(minor))
            .queryParam("ResultMessage", minorMessage)
            .build()
    }

    /**
     * Checks whether the TCToken is empty except the CommunicationErrorAddress.
     *
     * @throws InvalidRedirectUrlException
     * @throws InvalidTCTokenElement
     */
    @Throws(InvalidRedirectUrlException::class, InvalidTCTokenElement::class)
    private fun initialUrlTokenCheck() {
        if (token.getCommunicationErrorAddress() != null && !token.getCommunicationErrorAddress().isEmpty() &&
            token.getRefreshAddress().isEmpty() && token.getServerAddress().isEmpty() &&
            token.getSessionIdentifier().isEmpty() && token.getBinding().isEmpty() &&
            token.getPathSecurityProtocol().isEmpty()
        ) {
            val errorUrl = token.getComErrorAddressWithParams(ResultMinor.COMMUNICATION_ERROR)
            throw InvalidTCTokenElement(errorUrl, ErrorTranslations.ESERVICE_FAIL)
        }
    }

    /**
     * Determines the refresh URL.
     *
     * @param ex The exception which caused the abort of the TCToken verification.
     * @throws InvalidRedirectUrlException If the CommunicationErrorAddress cant be determined.
     * @throws InvalidTCTokenElement If a determination of a refresh or CommunicationError address was successful.
     * @throws UserCancellationException Thrown in case `ex` is an instance of [UserCancellationException].
     */
    @Throws(InvalidRedirectUrlException::class, InvalidTCTokenElement::class, UserCancellationException::class)
    private fun determineRefreshAddress(ex: ActivationError) {
        if (token.getRefreshAddress() != null) {
            try {
                val validator: CertificateValidator = RedirectCertificateValidator(true)
                val newResCtx = TrResourceContextLoader().getStream(URL(token.getRefreshAddress()), validator)
                newResCtx!!.closeStream()
                val resultPoints: MutableList<Pair<URL?, TlsServerCertificate?>> = newResCtx.certs
                val last = resultPoints.get(resultPoints.size - 1)
                val resAddr: URL = last.p1!!
                val refreshUrl: String? = resAddr.toString()

                if (ex is UserCancellationException) {
                    val uex = ex
                    val refreshUrlAsUrl = createUrlWithErrorParams(
                        refreshUrl, ResultMinor.CANCELLATION_BY_USER,
                        ex.message
                    )
                    throw UserCancellationException(refreshUrlAsUrl.toString(), ex)
                }

                val refreshUrlAsUrl = createUrlWithErrorParams(
                    refreshUrl,
                    ResultMinor.TRUSTED_CHANNEL_ESTABLISHMENT_FAILED, ex.message
                )
                throw InvalidTCTokenElement(refreshUrlAsUrl.toString(), ex)
            } catch (ex1: IOException) {
                val errorUrl = token.getComErrorAddressWithParams(ResultMinor.COMMUNICATION_ERROR)
                throw InvalidTCTokenElement(errorUrl, ErrorTranslations.INVALID_REFRESH_ADDRESS, ex1)
            } catch (ex1: HttpResourceException) {
                val errorUrl = token.getComErrorAddressWithParams(ResultMinor.COMMUNICATION_ERROR)
                throw InvalidTCTokenElement(errorUrl, ErrorTranslations.INVALID_REFRESH_ADDRESS, ex1)
            } catch (ex1: InvalidUrlException) {
                val errorUrl = token.getComErrorAddressWithParams(ResultMinor.COMMUNICATION_ERROR)
                throw InvalidTCTokenElement(errorUrl, ErrorTranslations.INVALID_REFRESH_ADDRESS, ex1)
            } catch (ex1: InvalidProxyException) {
                val errorUrl = token.getComErrorAddressWithParams(ResultMinor.COMMUNICATION_ERROR)
                throw InvalidTCTokenElement(errorUrl, ErrorTranslations.INVALID_REFRESH_ADDRESS, ex1)
            } catch (ex1: ValidationError) {
                val errorUrl = token.getComErrorAddressWithParams(ResultMinor.COMMUNICATION_ERROR)
                throw InvalidTCTokenElement(errorUrl, ErrorTranslations.INVALID_REFRESH_ADDRESS, ex1)
            } catch (ex1: URISyntaxException) {
                val errorUrl = token.getComErrorAddressWithParams(ResultMinor.COMMUNICATION_ERROR)
                throw InvalidTCTokenElement(errorUrl, ErrorTranslations.INVALID_REFRESH_ADDRESS, ex1)
            }
        } else {
            val errorUrl = token.getComErrorAddressWithParams(ResultMinor.COMMUNICATION_ERROR)
            throw InvalidTCTokenElement(errorUrl, ErrorTranslations.NO_REFRESH_ADDRESS)
        }
    }

    @Throws(InvalidRedirectUrlException::class, InvalidTCTokenElement::class, UserCancellationException::class)
    private fun checkUserCancellation() {
        val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)
        val ex = dynCtx.get(TR03112Keys.CARD_SELECTION_CANCELLATION) as UserCancellationException?
        if (ex != null) {
            determineRefreshAddress(ex)
        }
    }
}
