/****************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
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
package org.openecard.crypto.common.sal.did

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.*
import org.openecard.bouncycastle.util.encoders.Base64
import org.openecard.common.util.StringUtils
import org.openecard.ws.marshal.WSMarshaller
import org.openecard.ws.marshal.WSMarshallerException
import org.openecard.ws.marshal.WSMarshallerFactory.Companion.createInstance
import org.w3c.dom.Node
import java.math.BigInteger
import java.util.*

private val LOG = KotlinLogging.logger {}

/**
 * The class implements a CryptoMarkerType object according to BSI TR-0312 part 7.
 *
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 * @author Tobias Wich
 * @author Sebastian Schuberth
 */
class CryptoMarkerType(baseType: DIDAbstractMarkerType) : AbstractMarkerType(baseType) {
	private val m: WSMarshaller

	/**
	 * Get the value of the property LegacyKeyName if it exists.
	 *
	 * @return A byte array containing a key name. If no such key name is available `null` is returned.
	 */
	var legacyKeyName: ByteArray? = null
		private set

	/**
	 * Get the value of the property AlgorithmInfo if it exists.
	 *
	 * @return An [AlgorithmInfoType] object which contains all information available about a key. If no such
	 * information exists NULL is returned.
	 */
	var algorithmInfo: AlgorithmInfoType? = null
		private set

	/**
	 * Get the value of the property HashGenetationInfo if it exists.
	 *
	 * @return A [HashGenerationInfoType] object containing all necessary information about the creation of a
	 * hash. If no such information is available `null` is returned.
	 */
	var hashGenerationInfo: HashGenerationInfoType = HashGenerationInfoType.NOT_ON_CARD
		private set

	/**
	 * Get the value of the property CertificateRefs if it exists.
	 * Per convention the first certificate in the list is the one to use for TLS authentication or signature creation
	 * all other certificates are part of the certificate chain for the validation.
	 *
	 * @return A list of [CertificateRefType] object which contains references to a certificate object and the
	 * possible chain. If no such object exists an empty list is returned.
	 */
	val certificateRefs: MutableList<CertificateRefType> = ArrayList()

	/**
	 * Get the value of the property cryptoKeyInfo if it exists.
	 *
	 * @return A [CryptoKeyInfoType] object which contains all known information about a key. If no such
	 * information is available `null` is returned.
	 */
	var cryptoKeyInfo: CryptoKeyInfoType? = null
		private set
	private var signatureGenerationInfo: Array<String>? = null
	private var legacySignatureGenerationInfo: MutableList<Any>? = null

	/**
	 * Gets the value of the outputForm attribute in LegacySignatureGenerationInfo if one is present.
	 *
	 * @return The value of the attribute, `null` otherwise.
	 */
	var legacyOutputFormat: String? = null
		private set

	/**
	 * The constructor gets an [DIDAbstractMarkerType] object and parses the object to a CryptoMarkerType object.
	 * The CryptoMarkerType object is based on the CryptoMarkerType from BSI TR-03112-7
	 *
	 * @param baseType a [DIDAbstractMarkerType] object to parse.
	 */
	init {
		try {
			m = createInstance()
		} catch (ex: WSMarshallerException) {
			throw RuntimeException("Failed to instantiate WSMarshaller.", ex)
		}

		for (elem in marker.getAny()) {
			when (elem.localName) {
				"AlgorithmInfo" -> {
					val algInfo = AlgorithmInfoType()
					algorithmInfo = algInfo
					val algorithmInfoNodes = elem.childNodes
					var i = 0
					while (i < algorithmInfoNodes.length) {
						val node = algorithmInfoNodes.item(i)
						when (node.localName) {
							"Algorithm" -> algInfo.setAlgorithm(node.textContent)
							"AlgorithmIdentifier" -> {
								val algorithmIdentifierType = AlgorithmIdentifierType()
								val nodeList = node.childNodes
								var y = 0
								while (y < nodeList.length) {
									val n = nodeList.item(y)
									if (null != n.localName) {
										when (n.localName) {
											"Algorithm" -> algorithmIdentifierType.setAlgorithm(n.textContent)
											"Parameters" -> algorithmIdentifierType.setParameters(n)
										}
									}
									y++
								}
								algInfo.setAlgorithmIdentifier(algorithmIdentifierType)
							}

							"SupportedOperations" -> {
								val supportedOperations =
									node.textContent.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
								algInfo.getSupportedOperations()
									.addAll(supportedOperations)
							}

							"CardAlgRef" -> algInfo.setCardAlgRef(StringUtils.toByteArray(node.textContent))
							"HashAlgRef" -> algInfo.setHashAlgRef(StringUtils.toByteArray(node.textContent))
						}
						i++
					}
				}

				"KeyInfo" -> {
					cryptoKeyInfo = CryptoKeyInfoType()
					val nodeList = elem.childNodes
					var i = 0
					while (i < nodeList.length) {
						val n = nodeList.item(i)
						when (n.localName) {
							"KeyRef" -> {
								val keyRef = getCryptoKeyRef(n)
								cryptoKeyInfo!!.setKeyRef(keyRef)
							}

							"KeySize" -> cryptoKeyInfo!!.setKeySize(BigInteger(n.textContent))
						}
						i++
					}
				}

				"SignatureGenerationInfo" -> signatureGenerationInfo =
					elem.textContent.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

				"LegacySignatureGenerationInfo" -> {
					// get outputFormat attribute
					if (elem.hasAttribute("outputFormat")) {
						legacyOutputFormat = elem.getAttribute("outputFormat")
					}

					// get commands
					val nodeList = elem.childNodes
					legacySignatureGenerationInfo = ArrayList()
					var i = 0
					while (i < nodeList.length) {
						val n = nodeList.item(i)
						if (n.localName == "CardCommand") {
							try {
								val e = m.unmarshal(n, CardCallTemplateType::class.java)
								legacySignatureGenerationInfo!!.add(e.getValue())
							} catch (ex: WSMarshallerException) {
								LOG.error(ex) { "Failed to unmarshal CardCommand." }
							}
						} else if (n.localName == "APICommand") {
							try {
								val e = m.unmarshal(n, LegacySignatureGenerationType.APICommand::class.java)
								legacySignatureGenerationInfo!!.add(e.getValue())
							} catch (ex: WSMarshallerException) {
								LOG.error(ex) { "Failed to unmarshal APICommand." }
							}
						}
						i++
					}
				}

				"HashGenerationInfo" -> hashGenerationInfo = HashGenerationInfoType.fromValue(elem.getTextContent())
				"CertificateRef" -> {
					val certificateRef = CertificateRefType()
					val nodeList = elem.childNodes
					var i = 0
					while (i < nodeList.length) {
						val n = nodeList.item(i)
						when (n.localName) {
							"DataSetName" -> certificateRef.setDataSetName(n.textContent)
							"DSIName" -> certificateRef.setDSIName(n.textContent)
							"CertificateType" -> certificateRef.setCertificateType(n.textContent)
						}
						i++
					}
					certificateRefs.add(certificateRef)
				}

				"LegacyKeyName" -> this.legacyKeyName = Base64.decode(elem.textContent)
				"StateInfo" -> {}
			}
		}
	}

	/**
	 * Get the value of the property SignatureGenerationInfo if it exists.
	 *
	 * @return An array containing predefined strings from BSI TR-03112-7 page 82 SignatureGenerationInfo.  If no such
	 * information is available NULL is returned.
	 */
	fun getSignatureGenerationInfo(): Array<String>? = signatureGenerationInfo?.clone()

	/**
	 * Get the value of the property LegacySignatureGenerationIndo if it exists.
	 *
	 * @return A list of [CardCallTemplateType] objects which contain specific APDUs to generate a signature with
	 * the currently used card. If no such information is available `null` is returned.
	 */
	fun getLegacySignatureGenerationInfo(): List<Any>? = legacySignatureGenerationInfo

	val stateInfo: StateInfo?
		/**
		 * Get the value of the property StateInfo if it exists.
		 * <br></br><br></br>
		 * NOTE: This method is currently not implemented and throws an UnsupportedOperationException if the method is
		 * called.
		 *
		 * @return A [StateInfo] object which contains information about the available states. If no such information
		 * exists `null` is returned.
		 */
		get() {
			throw UnsupportedOperationException("Not yet implemented")
		}

	private fun getCryptoKeyRef(parentRef: Node): KeyRefType? {
		var refPresent = false
		val keyRef = KeyRefType()
		val children = parentRef.childNodes
		for (i in 0..<children.length) {
			val n = children.item(i)
			when (n.localName) {
				"KeyRef" -> {
					refPresent = true
					keyRef.setKeyRef(StringUtils.toByteArray(n.textContent))
				}

				"Protected" -> keyRef.isProtected = n.textContent.toBoolean()
			}
		}

		return if (refPresent) keyRef else null
	}

}
