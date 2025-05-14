/****************************************************************************
 * Copyright (C) 2013-2017 ecsec GmbH.
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
import org.openecard.binding.tctoken.ex.ErrorTranslations
import org.openecard.bouncycastle.tls.BasicTlsPSKIdentity
import org.openecard.bouncycastle.tls.ProtocolVersion
import org.openecard.bouncycastle.tls.TlsClient
import org.openecard.bouncycastle.tls.TlsClientProtocol
import org.openecard.bouncycastle.tls.TlsPSKIdentity
import org.openecard.bouncycastle.tls.crypto.TlsCrypto
import org.openecard.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto
import org.openecard.common.DynamicContext
import org.openecard.common.ECardConstants.PATH_SEC_PROTO_MTLS
import org.openecard.common.ECardConstants.PATH_SEC_PROTO_TLS_PSK
import org.openecard.crypto.common.ReusableSecureRandom
import org.openecard.crypto.tls.ClientCertDefaultTlsClient
import org.openecard.crypto.tls.ClientCertPSKTlsClient
import org.openecard.crypto.tls.ClientCertTlsClient
import org.openecard.crypto.tls.auth.CredentialFactory
import org.openecard.crypto.tls.auth.DynamicAuthentication
import org.openecard.crypto.tls.proxy.ProxySettings.Companion.default
import org.openecard.crypto.tls.verify.JavaSecVerifier
import org.openecard.crypto.tls.verify.SameCertVerifier
import java.lang.Boolean
import java.net.MalformedURLException
import java.net.URL
import kotlin.Int
import kotlin.String

/**
 *
 * @author Tobias Wich
 */
class TlsConnectionHandler(
	private val tokenRequest: TCTokenRequest,
) {
	private var serverAddress: URL? = null
	private var hostname: String? = null
	var port: Int = 0
		private set
	private var resource: String? = null
	private var sessionId: String? = null
	private var tlsClient: ClientCertTlsClient? = null
	private var verifyCertificates = true
	private var credentialFactory: CredentialFactory? = null

	fun setSmartCardCredential(credentialFactory: CredentialFactory?) {
		this.credentialFactory = credentialFactory
	}

	fun setUpClient() {
		try {
			val token: TCTokenType = tokenRequest.tCToken

			sessionId = token.getSessionIdentifier()
			serverAddress = URL(token.getServerAddress())
			val serverHost = serverAddress!!.host

			// extract connection parameters from endpoint
			hostname = serverAddress!!.host
			port = serverAddress!!.port
			if (port == -1) {
				port = serverAddress!!.defaultPort
			}
			resource = serverAddress!!.file
			resource = if (resource!!.isEmpty()) "/" else resource

			val secProto = token.getPathSecurityProtocol()
			// use same channel as demanded in TR-03124 sec. 2.4.3
			if (tokenRequest.isSameChannel) {
				tlsClient = tokenRequest.tokenContext.tlsClient
				if (tlsClient is ClientCertDefaultTlsClient) {
					(tlsClient as ClientCertDefaultTlsClient).setEnforceSameSession(true)
				}
				// save the info that we have a same channel situation
				val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)!!
				dynCtx.put(TR03112Keys.SAME_CHANNEL, Boolean.TRUE)
			} else {
				// kill open channel in tctoken request, it is not needed anymore
				tokenRequest.tokenContext.closeStream()

				// Set up TLS connection
				val tlsAuth = DynamicAuthentication(serverHost)

				val crypto: TlsCrypto = BcTlsCrypto(ReusableSecureRandom.instance)
				when (secProto) {
					PATH_SEC_PROTO_TLS_PSK -> {
						val psk = token.getPathSecurityParameters().getPSK()
						val pskId: TlsPSKIdentity = BasicTlsPSKIdentity(sessionId, psk)
						tlsClient = ClientCertPSKTlsClient(crypto, pskId, serverHost, true)
					}

					PATH_SEC_PROTO_MTLS -> {
						// use a smartcard for client authentication if one is set
						tlsAuth.setCredentialFactory(credentialFactory)
						tlsClient = ClientCertDefaultTlsClient(crypto, serverHost, true)
						// add PKIX verifier
						if (verifyCertificates) {
							tlsAuth.addCertificateVerifier(JavaSecVerifier())
						}
					}

					else -> throw ConnectionError(ErrorTranslations.UNKNOWN_SEC_PROTOCOL, secProto)
				}

				// make sure nobody changes the server when the connection gets reestablished
				tlsAuth.addCertificateVerifier(SameCertVerifier())
				// save eService certificate for use in EAC
				tlsAuth.addCertificateVerifier(SaveEidServerCertHandler())

				// set the authentication class in the tls client
				tlsClient!!.setAuthentication(tlsAuth)
			}
		} catch (ex: MalformedURLException) {
			throw ConnectionError(ErrorTranslations.MALFORMED_URL, ex, "ServerAddress")
		}
	}

	fun setVerifyCertificates(verifyCertificates: kotlin.Boolean) {
		this.verifyCertificates = verifyCertificates
	}

	fun getServerAddress(): URL = serverAddress!!

	fun getHostname(): String = hostname!!

	fun getResource(): String = resource!!

	fun getSessionId(): String = sessionId!!

	fun getTlsClient(): TlsClient? = tlsClient

	fun createTlsConnection(tlsVersion: ProtocolVersion = tlsClient!!.clientVersion): TlsClientProtocol {
		if (!tokenRequest.isSameChannel) {
			// normal procedure, create a new channel
			return createNewTlsConnection(tlsVersion)
		} else {
			// if something fucks up the channel we may try session resumption
			val proto = tokenRequest.tokenContext.tlsClientProto
			return if (proto!!.isClosed) {
				createNewTlsConnection(tlsVersion)
			} else {
				proto
			}
		}
	}

	private fun createNewTlsConnection(tlsVersion: ProtocolVersion): TlsClientProtocol {
		val socket = default.getSocket("https", hostname!!, port)
		tlsClient!!.clientVersion = tlsVersion
		// TLS
		val sockIn = socket.getInputStream()
		val sockOut = socket.getOutputStream()
		val handler = TlsClientProtocol(sockIn, sockOut)
		handler.connect(tlsClient)

		return handler
	}
}
