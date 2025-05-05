/****************************************************************************
 * Copyright (C) 2012-2016 HS Coburg.
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
package org.openecard.sal.protocol.genericcryptography

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.DSIRead
import iso.std.iso_iec._24727.tech.schema.DSIReadResponse
import iso.std.iso_iec._24727.tech.schema.VerifySignature
import iso.std.iso_iec._24727.tech.schema.VerifySignatureResponse
import org.openecard.addon.sal.FunctionType
import org.openecard.addon.sal.ProtocolStep
import org.openecard.bouncycastle.jce.provider.BouncyCastleProvider
import org.openecard.common.ECardException
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.WSHelper.makeResult
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.sal.exception.IncorrectParameterException
import org.openecard.common.sal.exception.InvalidSignatureException
import org.openecard.common.sal.util.SALUtils
import org.openecard.crypto.common.sal.did.CryptoMarkerType
import java.io.ByteArrayInputStream
import java.security.Signature
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.MGF1ParameterSpec
import java.security.spec.PSSParameterSpec

private val LOG = KotlinLogging.logger { }

/**
 * Implements the Hash step of the Generic cryptography protocol.
 * See TR-03112, version 1.1.2, part 7, section 4.9.10.
 *
 * @param dispatcher Dispatcher
 *
 * @author Dirk Petrautzki
 */
class VerifySignatureStep(
	private val dispatcher: Dispatcher,
) : ProtocolStep<VerifySignature?, VerifySignatureResponse?> {
	override fun getFunctionType(): FunctionType = FunctionType.VerifySignature

	override fun perform(
		request: VerifySignature?,
		internalData: MutableMap<String?, Any?>,
	): VerifySignatureResponse {
		val response: VerifySignatureResponse =
			WSHelper.makeResponse<Class<VerifySignatureResponse>, VerifySignatureResponse>(
				iso.std.iso_iec._24727.tech.schema.VerifySignatureResponse::class.java,
				org.openecard.common.WSHelper
					.makeResultOK(),
			)

		try {
			val connectionHandle = SALUtils.getConnectionHandle(request)
			val cardStateEntry = SALUtils.getCardStateEntry(internalData, connectionHandle)
			val didName = SALUtils.getDIDName(request)
			val didStructure = SALUtils.getDIDStructure(request, didName, cardStateEntry, connectionHandle)

			// required
			val signature = request!!.signature

			// optional
			val message = request.message

			val cryptoMarker = CryptoMarkerType(didStructure.didMarker)

			val dataSetNameCertificate = cryptoMarker.certificateRefs[0].dataSetName
			val algorithmIdentifier = cryptoMarker.algorithmInfo!!.algorithmIdentifier.algorithm

			val dsiRead =
				DSIRead().apply {
					setConnectionHandle(connectionHandle)
					dsiName = dataSetNameCertificate
				}

			val dsiReadResponse = dispatcher.safeDeliver(dsiRead) as DSIReadResponse
			checkResult<DSIReadResponse>(dsiReadResponse)

			val certFactory = CertificateFactory.getInstance("X.509")
			val cert: Certificate =
				certFactory.generateCertificate(
					ByteArrayInputStream(dsiReadResponse.getDSIContent()),
				) as X509Certificate

			val signatureAlgorithm: Signature
			when (algorithmIdentifier) {
				GenericCryptoUris.RSA_ENCRYPTION -> {
					signatureAlgorithm = Signature.getInstance("RSA", BouncyCastleProvider())
				}
				GenericCryptoUris.RSASSA_PSS_SHA256 -> {
					signatureAlgorithm = Signature.getInstance("RAWRSASSA-PSS", BouncyCastleProvider())
					signatureAlgorithm.setParameter(
						PSSParameterSpec(
							"SHA-256",
							"MGF1",
							MGF1ParameterSpec("SHA-256"),
							32,
							1,
						),
					)
				}
				GenericCryptoUris.sigS_ISO9796_2 -> {
					return WSHelper.makeResponse<Class<VerifySignatureResponse>, VerifySignatureResponse>(
						iso.std.iso_iec._24727.tech.schema.VerifySignatureResponse::class.java,
						org.openecard.common.WSHelper
							.makeResultUnknownError("$algorithmIdentifier Not supported yet."),
					)
				}
				GenericCryptoUris.sigS_ISO9796_2rnd -> {
					return WSHelper.makeResponse<Class<VerifySignatureResponse>, VerifySignatureResponse>(
						iso.std.iso_iec._24727.tech.schema.VerifySignatureResponse::class.java,
						org.openecard.common.WSHelper
							.makeResultUnknownError("$algorithmIdentifier Not supported yet."),
					)
				}
				else -> {
					throw IncorrectParameterException("Unknown signature algorithm.")
				}
			}
			signatureAlgorithm.initVerify(cert)
			message?.let {
				signatureAlgorithm.update(it)
			}

			if (!signatureAlgorithm.verify(signature)) {
				throw InvalidSignatureException()
			}
		} catch (e: ECardException) {
			LOG.error(e) { e.message }
			response.setResult(e.result)
		} catch (e: Exception) {
			response.setResult(makeResult(e))
		}

		return response
	}
}
