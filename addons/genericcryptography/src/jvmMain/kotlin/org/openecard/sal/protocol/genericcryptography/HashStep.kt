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
package org.openecard.sal.protocol.genericcryptography

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.Hash
import iso.std.iso_iec._24727.tech.schema.HashGenerationInfoType
import iso.std.iso_iec._24727.tech.schema.HashResponse
import org.openecard.addon.sal.FunctionType
import org.openecard.addon.sal.ProtocolStep
import org.openecard.common.ECardConstants
import org.openecard.common.ECardException
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.makeResult
import org.openecard.common.WSHelper.makeResultError
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.sal.util.SALUtils
import org.openecard.crypto.common.SignatureAlgorithms.Companion.fromAlgId
import org.openecard.crypto.common.UnsupportedAlgorithmException
import org.openecard.crypto.common.sal.did.CryptoMarkerType
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

private val logger = KotlinLogging.logger { }

/**
 * Implements the Hash step of the Generic cryptography protocol.
 * See TR-03112, version 1.1.2, part 7, section 4.9.8.
 *
 * @param dispatcher Dispatcher
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
class HashStep(
	private val dispatcher: Dispatcher,
) : ProtocolStep<Hash?, HashResponse?> {
	override fun getFunctionType(): FunctionType = FunctionType.Hash

	override fun perform(
		request: Hash?,
		internalData: Map<String, Any>,
	): HashResponse {
		val response: HashResponse =
			WSHelper.makeResponse<Class<HashResponse>, HashResponse>(
				iso.std.iso_iec._24727.tech.schema.HashResponse::class.java,
				org.openecard.common.WSHelper
					.makeResultOK(),
			)

		try {
			val connectionHandle = SALUtils.getConnectionHandle(request)
			val didName = SALUtils.getDIDName(request)
			val cardStateEntry = SALUtils.getCardStateEntry(internalData, connectionHandle)
			val didStructure = SALUtils.getDIDStructure(request, didName, cardStateEntry, connectionHandle)
			val cryptoMarker = CryptoMarkerType(didStructure.didMarker)

			val hashInfo = cryptoMarker.hashGenerationInfo
			if (hashInfo == HashGenerationInfoType.NOT_ON_CARD) {
				val algId = cryptoMarker.algorithmInfo!!.algorithmIdentifier.algorithm
				val alg = fromAlgId(algId)
				val hashAlg = alg.hashAlg
				if (hashAlg == null) {
					val msg = "Algorithm $algId does not specify a Hash algorithm."
					logger.error { msg }
					val minor = ECardConstants.Minor.App.INCORRECT_PARM
					response.setResult(makeResultError(minor, msg))
				} else {
					// calculate hash
					val md = MessageDigest.getInstance(hashAlg.jcaAlg)
					md.update(request!!.message)
					val digest = md.digest()
					response.setHash(digest)
				}
			} else {
				// TODO: implement hashing on card
				val msg = "Unsupported Hash generation type ($hashInfo) requested."
				logger.error { msg }
				val minor = ECardConstants.Minor.SAL.INAPPROPRIATE_PROTOCOL_FOR_ACTION
				response.setResult(makeResultError(minor, msg))
			}
		} catch (e: ECardException) {
			response.setResult(e.result)
		} catch (e: UnsupportedAlgorithmException) {
			logger.warn(e) { e.message }
		} catch (e: NoSuchAlgorithmException) {
			logger.warn(e) { e.message }
		} catch (e: Exception) {
			logger.warn(e) { e.message }
			response.setResult(makeResult(e))
		}

		return response
	}
}
