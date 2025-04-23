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
 ***************************************************************************/
package org.openecard.crypto.tls

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.bouncycastle.tls.*
import org.openecard.bouncycastle.tls.crypto.TlsCrypto
import org.openecard.common.OpenecardProperties
import org.openecard.common.util.ByteUtils
import org.openecard.crypto.tls.auth.ContextAware
import org.openecard.crypto.tls.auth.DynamicAuthentication
import java.io.IOException
import java.net.IDN
import java.nio.charset.StandardCharsets
import java.util.*

private val LOG = KotlinLogging.logger { }

/**
 * Standard TLS client also implementing the ClientCertTlsClient interface. <br></br>
 * If not modified, the TlsAuthentication instance returned by [.getAuthentication] is of type
 * [DynamicAuthentication] without further modifications.
 *
 * @author Tobias Wich
 */
open class ClientCertDefaultTlsClient(
	tcf: TlsCrypto,
	private val host: String,
	doSni: Boolean,
) : DefaultTlsClient(tcf),
	ClientCertTlsClient {
	private var tlsAuth: TlsAuthentication? = null
	private var enforceSameSession = false
	private var firstSession: TlsSession? = null
	private var lastSession: TlsSession? = null

	@JvmField
	protected val serverNames: MutableList<ServerName> = mutableListOf()
	override var clientVersion: ProtocolVersion = ProtocolVersion.TLSv12
	protected var minClientVersion: ProtocolVersion = ProtocolVersion.TLSv12

	/**
	 * Create a ClientCertDefaultTlsClient for the given parameters.
	 *
	 * @param tcf Cipher factory to use in this client.
	 * @param host Host or IP address. Value must not be null.
	 * @param doSni Control whether the server should send the SNI Header in the Client Hello.
	 */
	init {
		if (doSni) {
			this.serverNames.add(makeServerName(host))
		}
		val legacyTls = OpenecardProperties.getProperty("legacy.tls1").toBoolean()
		if (legacyTls) {
			this.minClientVersion = ProtocolVersion.TLSv10
		}
	}

	fun setServerName(serverName: String) {
		serverNames.clear()
		serverNames.add(makeServerName(serverName))
	}

	fun setServerName(serverNames: List<String>) {
		this.serverNames.clear()
		for (next in serverNames) {
			this.serverNames.add(makeServerName(next))
		}
	}

	override fun getSNIServerNames(): Vector<*>? = if (serverNames.isEmpty()) null else Vector(serverNames)

	private fun makeServerName(name: String): ServerName {
		var name = name
		name = IDN.toASCII(name)
		return ServerName(NameType.host_name, name.toByteArray(StandardCharsets.US_ASCII))
	}

	override fun setMinimumVersion(minClientVersion: ProtocolVersion) {
		this.minClientVersion = minClientVersion
	}

	fun getMinimumVersion(): ProtocolVersion = this.minClientVersion

	override fun getSupportedVersions(): Array<ProtocolVersion> {
		val desiredVersion = clientVersion
		val minVersion = getMinimumVersion()

		return if (!desiredVersion.isLaterVersionOf(minVersion)) {
			arrayOf(desiredVersion)
		} else {
			clientVersion.downTo(minVersion)
		}
	}

	fun setEnforceSameSession(enforceSameSession: Boolean) {
		this.enforceSameSession = enforceSameSession
	}

	@Synchronized
	@Throws(IOException::class)
	override fun getAuthentication(): TlsAuthentication? {
		if (tlsAuth == null) {
			tlsAuth = DynamicAuthentication(host)
		}
		if (tlsAuth is ContextAware) {
			(tlsAuth as ContextAware).setContext(context)
		}
		return tlsAuth
	}

	@Synchronized
	override fun setAuthentication(tlsAuth: TlsAuthentication?) {
		this.tlsAuth = tlsAuth
	}

	override fun init(context: TlsClientContext?) {
		// save first session so resumption only works with the exact same session
		if (enforceSameSession && firstSession == null && lastSession != null) {
			this.firstSession = lastSession
		}

		super.init(context)
	}

	override fun getCipherSuites(): IntArray {
		val ciphers =
			mutableListOf(
				// recommended ciphers from TR-02102-2 sec. 3.3.1
				CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
				CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
				CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384,
				CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,
				CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
				CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
				CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256, // acceptable in case DHE is not available
				// there seems to be a problem with DH and besides that I don't like them anyways
			/*
	CipherSuite.TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384,
	CipherSuite.TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384,
	CipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256,
	CipherSuite.TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256,
	CipherSuite.TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384,
	CipherSuite.TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384,
	CipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256,
	CipherSuite.TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256,
	CipherSuite.TLS_DH_RSA_WITH_AES_256_GCM_SHA384,
	CipherSuite.TLS_DH_RSA_WITH_AES_128_GCM_SHA256,
	CipherSuite.TLS_DH_RSA_WITH_AES_256_CBC_SHA256,
	CipherSuite.TLS_DH_RSA_WITH_AES_128_CBC_SHA256
			 */
			)

		// when doing TLS 1.0, we need the old SHA1 cipher suites
		if (minClientVersion.isEqualOrEarlierVersionOf(ProtocolVersion.TLSv11)) {
			ciphers.addAll(
				listOf(
					// SHA1 is acceptable until 2015
					CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
					CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
					CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
					CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, // acceptable in case DHE is not available
					// there seems to be a problem with DH and besides that I don't like them anyways
					/*
			CipherSuite.TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA,
			CipherSuite.TLS_ECDH_RSA_WITH_AES_256_CBC_SHA,
			CipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA,
			CipherSuite.TLS_ECDH_RSA_WITH_AES_128_CBC_SHA,
			CipherSuite.TLS_DH_RSA_WITH_AES_256_CBC_SHA,
			CipherSuite.TLS_DH_RSA_WITH_AES_128_CBC_SHA
					 */
				),
			)
		}

		// remove unsupported cipher suites
		val it = ciphers.iterator()
		while (it.hasNext()) {
			val cipher = it.next()
			if (!TlsUtils.isValidCipherSuiteForVersion(cipher, clientVersion)) {
				it.remove()
			}
		}

		return ciphers.toIntArray()
	}

	override fun getSupportedSignatureAlgorithms(): Vector<*> {
		val crypto = context.crypto

		val result = getDefaultSignatureAlgorithms(crypto, minClientVersion === ProtocolVersion.TLSv10)
		return result
	}

	override fun getSupportedGroups(namedGroupRoles: Vector<*>): Vector<*> {
		val groups: Vector<Int> = Vector()

		if (namedGroupRoles.contains(NamedGroupRole.ecdh) || namedGroupRoles.contains(NamedGroupRole.ecdsa)) {
			// other possible parameters TR-02102-2 sec. 3.6
			groups.add(NamedGroup.brainpoolP512r1)
			groups.add(NamedGroup.brainpoolP384r1)
			groups.add(NamedGroup.secp384r1)
			// required parameters TR-03116-4 sec. 4.1.4
			groups.add(NamedGroup.brainpoolP256r1)
			groups.add(NamedGroup.secp256r1)
			groups.add(NamedGroup.secp224r1)
		}

		return groups
	}

	override fun notifyAlertRaised(
		alertLevel: Short,
		alertDescription: Short,
		message: String?,
		cause: Throwable?,
	) {
		val error = TlsError(alertLevel, alertDescription, message, cause)
		if (alertLevel == AlertLevel.warning && LOG.isInfoEnabled()) {
			LOG.info { "TLS warning sent." }
			if (LOG.isDebugEnabled()) {
				LOG.info(cause) { error.toString() }
			} else {
				LOG.info { error.toString() }
			}
		} else if (alertLevel == AlertLevel.fatal) {
			LOG.error { "TLS error sent." }
			LOG.error(cause) { error.toString() }
		}

		super.notifyAlertRaised(alertLevel, alertDescription, message, cause)
	}

	override fun notifyAlertReceived(
		alertLevel: Short,
		alertDescription: Short,
	) {
		val error = TlsError(alertLevel, alertDescription)
		if (alertLevel == AlertLevel.warning && LOG.isInfoEnabled()) {
			LOG.info { "TLS warning received." }
			LOG.info { error.toString() }
		} else if (alertLevel == AlertLevel.fatal) {
			LOG.error { "TLS error received." }
			LOG.error { error.toString() }
		}

		super.notifyAlertReceived(alertLevel, alertDescription)
	}

	override fun getSessionToResume(): TlsSession? =
		if (firstSession != null) {
			firstSession
		} else {
			super.getSessionToResume()
		}

	override fun notifySessionID(sessionID: ByteArray?) {
		if (enforceSameSession) {
			// check if someone tries to resume and raise error
			val s = sessionToResume
			if (s != null) {
				if (ByteUtils.compare(s.sessionID, sessionID)) {
					// the session id is the same meaning the protocol implementation will reject the handshake if the
					// secrets don't match
					LOG.info { "Trying to resume previous TLS session." }
					return
				}
			}

			// resumption not initiated properly
			// terminate connection with RuntimeException as BC will handle this error
			val msg = "TLS Session resumption not successful."
			LOG.error { msg }
			throw RuntimeException(msg)
		}
	}

	@Throws(IOException::class)
	override fun notifyHandshakeComplete() {
		lastSession = if (context != null) context.resumableSession else null
		lastSession?.let {
			lastSession = TlsUtils.importSession(it.sessionID, it.exportSessionParameters())
		}

		super.notifyHandshakeComplete()
	}

	@Throws(IOException::class)
	override fun notifySecureRenegotiation(secureRenegotiation: Boolean) {
		// pretend we accept it
	}

	companion object {
		fun getDefaultSignatureAlgorithms(
			crypto: TlsCrypto,
			withLegacy: Boolean,
		): Vector<*> {
			val result: Vector<SignatureAndHashAlgorithm> = Vector()

			val hashAlgorithms = mutableListOf<Short>()
			hashAlgorithms.add(HashAlgorithm.sha512)
			hashAlgorithms.add(HashAlgorithm.sha384)
			hashAlgorithms.add(HashAlgorithm.sha256)
			hashAlgorithms.add(HashAlgorithm.sha224)
			if (withLegacy) {
				hashAlgorithms.add(HashAlgorithm.sha1)
			}

			val signatureAlgorithms =
				shortArrayOf(
					SignatureAlgorithm.rsa,
					SignatureAlgorithm.ecdsa,
				)

			val intrinsicSigAlgs =
				arrayOf(
					// SignatureAndHashAlgorithm.ed25519,
					// SignatureAndHashAlgorithm.ed448,
					SignatureAndHashAlgorithm.rsa_pss_rsae_sha256,
					SignatureAndHashAlgorithm.rsa_pss_rsae_sha384,
					SignatureAndHashAlgorithm.rsa_pss_rsae_sha512,
					SignatureAndHashAlgorithm.rsa_pss_pss_sha256,
					SignatureAndHashAlgorithm.rsa_pss_pss_sha384,
					SignatureAndHashAlgorithm.rsa_pss_pss_sha512,
				)

			for (sigAlg in intrinsicSigAlgs) {
				if (crypto.hasSignatureAndHashAlgorithm(sigAlg)) {
					result.add(sigAlg)
				}
			}

			for (i in signatureAlgorithms.indices) {
				for (j in hashAlgorithms.indices) {
					val alg = SignatureAndHashAlgorithm(hashAlgorithms[j], signatureAlgorithms[i])
					if (crypto.hasSignatureAndHashAlgorithm(alg)) {
						result.addElement(alg)
					}
				}
			}

			return result
		}
	}
}
