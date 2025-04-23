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
import org.openecard.bouncycastle.tls.AlertLevel
import org.openecard.bouncycastle.tls.CipherSuite
import org.openecard.bouncycastle.tls.NameType
import org.openecard.bouncycastle.tls.NamedGroup
import org.openecard.bouncycastle.tls.NamedGroupRole
import org.openecard.bouncycastle.tls.PSKTlsClient
import org.openecard.bouncycastle.tls.ProtocolVersion
import org.openecard.bouncycastle.tls.ServerName
import org.openecard.bouncycastle.tls.TlsAuthentication
import org.openecard.bouncycastle.tls.TlsPSKIdentity
import org.openecard.bouncycastle.tls.TlsUtils
import org.openecard.bouncycastle.tls.crypto.TlsCrypto
import org.openecard.common.OpenecardProperties
import org.openecard.crypto.tls.auth.ContextAware
import org.openecard.crypto.tls.auth.DynamicAuthentication
import java.io.IOException
import java.net.IDN
import java.nio.charset.StandardCharsets
import java.util.Vector

private val LOG = KotlinLogging.logger { }

/**
 * PSK TLS client also implementing the ClientCertTlsClient interface. <br></br>
 * If not modified, the TlsAuthentication instance returned by [.getAuthentication] is of type
 * [DynamicAuthentication] without further modifications.
 *
 * @author Tobias Wich
 */
class ClientCertPSKTlsClient(
	tcf: TlsCrypto,
	pskId: TlsPSKIdentity,
	private val host: String,
	doSni: Boolean,
) : PSKTlsClient(tcf, pskId),
	ClientCertTlsClient {
	private var tlsAuth: TlsAuthentication? = null

	private var serverNames: MutableList<ServerName> = mutableListOf()
	override var clientVersion: ProtocolVersion = ProtocolVersion.TLSv12
	private var minClientVersion: ProtocolVersion? = ProtocolVersion.TLSv12

	/**
	 * Create a ClientCertPSKTlsClient for the given parameters.
	 *
	 * @param tcf Cipher factory to use in this client.
	 * @param pskId PSK to use for this connection.
	 * @param host Host or IP address. Value must not be null.
	 * @param doSni Control whether the server should send the SNI Header in the Client Hello.
	 */
	init {
		if (doSni) {
			this.serverNames.add(makeServerName(host!!))
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

	fun setServerNames(serverNames: List<String>) {
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

	fun getMinimumVersion(): ProtocolVersion? = this.minClientVersion

	override fun getSupportedVersions(): Array<ProtocolVersion> {
		val desiredVersion = clientVersion
		val minVersion = getMinimumVersion()

		return if (!desiredVersion.isLaterVersionOf(minVersion)) {
			arrayOf(desiredVersion)
		} else {
			clientVersion.downTo(minVersion)
		}
	}

	override fun getCipherSuites(): IntArray {
		val ciphers =
			mutableListOf<Int>(
				// recommended ciphers from TR-02102-2 sec. 3.3.1
				CipherSuite.TLS_RSA_PSK_WITH_AES_256_GCM_SHA384,
				CipherSuite.TLS_RSA_PSK_WITH_AES_128_GCM_SHA256,
				CipherSuite.TLS_RSA_PSK_WITH_AES_256_CBC_SHA384,
				CipherSuite.TLS_RSA_PSK_WITH_AES_128_CBC_SHA256, // must have according to TR-03124-1 sec. 4.4
				CipherSuite.TLS_RSA_PSK_WITH_AES_256_CBC_SHA,
			)

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

	@Synchronized
	@Throws(IOException::class)
	override fun getAuthentication(): TlsAuthentication {
		if (tlsAuth == null) {
			tlsAuth = DynamicAuthentication(host)
		}
		if (tlsAuth is ContextAware) {
			(tlsAuth as ContextAware).setContext(context)
		}
		return tlsAuth!!
	}

	@Synchronized
	override fun setAuthentication(tlsAuth: TlsAuthentication?) {
		this.tlsAuth = tlsAuth
	}

	override fun getSupportedSignatureAlgorithms(): Vector<*> {
		val crypto = context.crypto

		val result: Vector<*> =
			ClientCertDefaultTlsClient.Companion.getDefaultSignatureAlgorithms(
				crypto,
				minClientVersion === ProtocolVersion.TLSv10,
			)
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

	@Throws(IOException::class)
	override fun notifySecureRenegotiation(secureRenegotiation: Boolean) {
		// pretend we accept it
	}
}
