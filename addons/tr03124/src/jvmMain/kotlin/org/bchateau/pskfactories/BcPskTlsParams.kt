/*
 * Copyright (C) 2024 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bchateau.pskfactories

import org.bouncycastle.tls.CipherSuite
import org.bouncycastle.tls.ProtocolVersion
import java.util.Arrays

/**
 * Provides basic conversion methods and specifies supported cipher suites and TLS protocol
 * versions. If older/unusual cipher suites are to be used some internal methods here will need
 * to be expanded.
 */
class BcPskTlsParams {
	private val supportedProtocolVersions: Array<ProtocolVersion>
	private val supportedCipherSuiteCodes: IntArray
	private val supportedCipherSuites: Array<String>
	private val supportedProtocols: Array<String>

	/**
	 * Create an instance that supports TLSv1.2 and TLSv1.3 with the following cipher suites:
	 * ```
	 * TLS_AES_128_GCM_SHA256,
	 * TLS_AES_256_GCM_SHA384,
	 * TLS_ECDHE_PSK_WITH_AES_128_GCM_SHA256,
	 * TLS_ECDHE_PSK_WITH_CHACHA20_POLY1305_SHA256,
	 * TLS_ECDHE_PSK_WITH_AES_256_GCM_SHA384,
	 * ```
	 */
	constructor() {
		this.supportedProtocolVersions = defaultSupportedProtocolVersions.clone()
		this.supportedCipherSuiteCodes = defaultSupportedCipherSuiteCodes.clone()
		this.supportedCipherSuites = cipherSuiteCodesToStrings(supportedCipherSuiteCodes)
		this.supportedProtocols = protocolVersionsToStrings(supportedProtocolVersions)
	}

	/**
	 * Create an instance that supports the given protocol versions and cipher suites. It is up to
	 * the caller to ensure the given cipher suites are compatible with the given protocol versions
	 * (i.e.: if TLSv1.2 only is given then only TLSv1.2 cipher suites must be given).
	 */
	constructor(supportedProtocolVersions: Array<ProtocolVersion>, supportedCipherSuiteCodes: IntArray) {
		this.supportedProtocolVersions = supportedProtocolVersions.clone()
		// As-is Bouncy Castle breaks if TLSv1.2 is listed earlier than TLSv1.3, this ensures the
		// protocols are ordered by highest version first
		Arrays.sort(this.supportedProtocolVersions, protocolComparator)
		this.supportedCipherSuiteCodes = supportedCipherSuiteCodes.clone()
		this.supportedCipherSuites = cipherSuiteCodesToStrings(supportedCipherSuiteCodes)
		this.supportedProtocols = protocolVersionsToStrings(supportedProtocolVersions)
	}

	fun getSupportedCipherSuites(): Array<String> = supportedCipherSuites.clone()

	fun getSupportedProtocols(): Array<String> = supportedProtocols.clone()

	fun getSupportedCipherSuiteCodes(): IntArray = supportedCipherSuiteCodes.clone()

	fun getSupportedProtocolVersions(): Array<ProtocolVersion> = supportedProtocolVersions.clone()

	companion object {
		// Currently this is a subset of org.bouncycastle.tls.PSKTlsClient cipher suites that are most secure
		private val defaultSupportedCipherSuiteCodes =
			intArrayOf(
				CipherSuite.TLS_AES_128_GCM_SHA256,
				CipherSuite.TLS_AES_256_GCM_SHA384,
				CipherSuite.TLS_ECDHE_PSK_WITH_AES_128_GCM_SHA256,
				CipherSuite.TLS_ECDHE_PSK_WITH_CHACHA20_POLY1305_SHA256,
				CipherSuite.TLS_ECDHE_PSK_WITH_AES_256_GCM_SHA384,
			)

		private val defaultSupportedProtocolVersions: Array<ProtocolVersion> =
			arrayOf<ProtocolVersion>(
				ProtocolVersion.TLSv13,
				ProtocolVersion.TLSv12,
			)

		private val protocolComparator =
			Comparator { o1: ProtocolVersion, o2: ProtocolVersion? ->
				if (o1.equals(o2)) {
					return@Comparator 0
				} else if (o1.isEarlierVersionOf(o2)) {
					return@Comparator 1
				}
				-1
			}

		fun toJavaName(version: ProtocolVersion): String {
			when (version.fullVersion) {
				0x0301 -> return "TLSv1.0"
				0x0302 -> return "TLSv1.1"
				0x0303 -> return "TLSv1.2"
				0x0304 -> return "TLSv1.3"
			}

			throw IllegalArgumentException("Unable to get java name for: $version")
		}

		fun fromJavaName(version: String): ProtocolVersion {
			when (version) {
				"TLSv1.0" -> return ProtocolVersion.TLSv10
				"TLSv1.1" -> return ProtocolVersion.TLSv11
				"TLSv1.2" -> return ProtocolVersion.TLSv12
				"TLSv1.3" -> return ProtocolVersion.TLSv13
			}

			throw IllegalArgumentException("Unable to get protocol version for: $version")
		}

		private val suiteToCodeMap = HashMap<String, Int>()
		private val codeToSuiteMap = HashMap<Int, String>()

		init {
			// Update as needed, see org/bouncycastle/tls/CipherSuite.java
			suiteToCodeMap["TLS_DHE_PSK_WITH_AES_128_CBC_SHA"] = 0x0090
			suiteToCodeMap["TLS_DHE_PSK_WITH_AES_128_GCM_SHA256"] = 0x00AA
			suiteToCodeMap["TLS_DHE_PSK_WITH_AES_256_GCM_SHA384"] = 0x00AB
			suiteToCodeMap["TLS_DHE_PSK_WITH_AES_128_CBC_SHA256"] = 0x00B2
			suiteToCodeMap["TLS_DHE_PSK_WITH_AES_256_CBC_SHA384"] = 0x00B3
			suiteToCodeMap["TLS_AES_128_GCM_SHA256"] = 0x1301
			suiteToCodeMap["TLS_AES_256_GCM_SHA384"] = 0x1302
			suiteToCodeMap["TLS_CHACHA20_POLY1305_SHA256"] = 0x1303
			suiteToCodeMap["TLS_AES_128_CCM_SHA256"] = 0x1304
			suiteToCodeMap["TLS_AES_128_CCM_8_SHA256"] = 0x1305
			suiteToCodeMap["TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA"] = 0xC035
			suiteToCodeMap["TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA256"] = 0xC037
			suiteToCodeMap["TLS_ECDHE_PSK_WITH_CHACHA20_POLY1305_SHA256"] = 0xCCAC
			suiteToCodeMap["TLS_DHE_PSK_WITH_CHACHA20_POLY1305_SHA256"] = 0xCCAD
			suiteToCodeMap["TLS_ECDHE_PSK_WITH_AES_128_GCM_SHA256"] = 0xD001
			suiteToCodeMap["TLS_ECDHE_PSK_WITH_AES_256_GCM_SHA384"] = 0xD002
			// needed for OeC
			suiteToCodeMap["TLS_RSA_PSK_WITH_AES_256_CBC_SHA"] = 0x0095
			suiteToCodeMap["TLS_RSA_PSK_WITH_AES_256_CBC_SHA384"] = 0x00b7
			suiteToCodeMap["TLS_RSA_PSK_WITH_AES_128_CBC_SHA256"] = 0x00b6
			suiteToCodeMap["TLS_RSA_PSK_WITH_AES_256_GCM_SHA384"] = 0x00ad
			suiteToCodeMap["TLS_RSA_PSK_WITH_AES_128_GCM_SHA256"] = 0x00ac

			for (entry in suiteToCodeMap.entries) {
				codeToSuiteMap[entry.value] = entry.key
			}
		}

		fun toCipherSuiteString(code: Int): String {
			val suite = codeToSuiteMap.get(code)
			requireNotNull(suite) { "Unsupported TLS cipher code: $code" }
			return suite
		}

		fun fromCipherSuiteString(name: String): Int {
			val code = suiteToCodeMap.get(name)
			requireNotNull(code) { "Unsupported TLS cipher: $name" }
			return code
		}

		private fun protocolVersionsToStrings(versions: Array<ProtocolVersion>): Array<String> =
			versions
				.map {
					toJavaName(it)
				}.toTypedArray()

		private fun cipherSuiteCodesToStrings(codes: IntArray): Array<String> =
			codes
				.map {
					toCipherSuiteString(it)
				}.toTypedArray()

		fun fromSupportedCipherSuiteCodes(jsseCipherSuites: Array<String>): IntArray =
			jsseCipherSuites
				.map {
					fromCipherSuiteString(it)
				}.toIntArray()

		fun fromSupportedProtocolVersions(jsseProtocols: Array<String>): Array<ProtocolVersion> =
			jsseProtocols
				.map {
					fromJavaName(it)
				}.toTypedArray()
	}
}
