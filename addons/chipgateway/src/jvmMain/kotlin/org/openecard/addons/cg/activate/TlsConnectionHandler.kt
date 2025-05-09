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
package org.openecard.addons.cg.activate

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addons.cg.ex.ErrorTranslations
import org.openecard.addons.cg.ex.InvalidTCTokenElement
import org.openecard.addons.cg.impl.ChipGatewayProperties
import org.openecard.addons.cg.tctoken.TCToken
import org.openecard.bouncycastle.tls.ProtocolVersion
import org.openecard.bouncycastle.tls.TlsClient
import org.openecard.bouncycastle.tls.TlsClientProtocol
import org.openecard.bouncycastle.tls.crypto.TlsCrypto
import org.openecard.bouncycastle.tls.crypto.impl.bc.BcTlsCrypto
import org.openecard.common.ECardConstants.PATH_SEC_PROTO_MTLS
import org.openecard.crypto.common.ReusableSecureRandom
import org.openecard.crypto.tls.ClientCertDefaultTlsClient
import org.openecard.crypto.tls.ClientCertTlsClient
import org.openecard.crypto.tls.auth.DynamicAuthentication
import org.openecard.crypto.tls.proxy.ProxySettings.Companion.default
import org.openecard.crypto.tls.verify.SameCertVerifier
import java.io.IOException
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL

private val logger = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
class TlsConnectionHandler(
	private val token: TCToken,
) {
	private lateinit var serverAddress: URL
	private lateinit var hostname: String
	var port: Int = 0
		private set
	private lateinit var resource: String
	var sessionId: String? = null
		private set
	private lateinit var tlsClient: ClientCertTlsClient

	@Throws(InvalidTCTokenElement::class)
	fun setUpClient() {
		try {
			sessionId = token.sessionIdentifier
			serverAddress = URI(token.serverAddress).toURL()
			val serverHost = serverAddress.host

			// extract connection parameters from endpoint
			hostname = serverAddress.host
			port = serverAddress.port
			if (port == -1) {
				port = serverAddress.defaultPort
			}
			resource = serverAddress.file
			resource = resource.ifEmpty { "/" }

			val secProto = token.pathSecurityProtocol

			when (secProto) {
				PATH_SEC_PROTO_MTLS, "http://ws.openecard.org/pathsecurity/tlsv12-with-pin-encryption" -> {}
			}

			// Set up TLS connection
			val tlsAuth = DynamicAuthentication(serverHost)

			when (secProto) {
				PATH_SEC_PROTO_MTLS, "http://ws.openecard.org/pathsecurity/tlsv12-with-pin-encryption" -> {
					// use a smartcard for client authentication if needed
					val crypto: TlsCrypto = BcTlsCrypto(ReusableSecureRandom.instance)
					tlsClient = ClientCertDefaultTlsClient(crypto, serverHost, true)
					// add PKIX verifier
					if (ChipGatewayProperties.isValidateServerCert) {
						tlsAuth.addCertificateVerifier(CGJavaSecVerifier())
					} else {
						logger.warn { "Skipping server certificate validation of the ChipGateway server." }
					}
				}

				else -> throw InvalidTCTokenElement(ErrorTranslations.ELEMENT_VALUE_INVALID, "PathSecurity-Protocol")
			}

			// make sure nobody changes the server when the connection gets reestablished
			tlsAuth.addCertificateVerifier(SameCertVerifier())

			// set the authentication class in the tls client
			tlsClient.authentication = tlsAuth
		} catch (ex: MalformedURLException) {
			throw InvalidTCTokenElement(ErrorTranslations.MALFORMED_URL, "ServerAddress")
		}
	}

	fun getServerAddress(): URL = serverAddress

	fun getHostname(): String = hostname

	fun getResource(): String = resource

	fun getTlsClient(): TlsClient = tlsClient

	@JvmOverloads
	@Throws(IOException::class, URISyntaxException::class)
	fun createTlsConnection(tlsVersion: ProtocolVersion = tlsClient.clientVersion): TlsClientProtocol {
		// normal procedure, create a new channel
		val socket = default.getSocket("https", hostname, port)
		tlsClient.clientVersion = tlsVersion
		// TLS
		val sockIn = socket.inputStream
		val sockOut = socket.outputStream
		val handler =
			TlsClientProtocol(sockIn, sockOut).apply {
				connect(tlsClient)
			}

		return handler
	}
}
