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
package org.openecard.sal.protocol.pincompare

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse
import iso.std.iso_iec._24727.tech.schema.DIDScopeType
import iso.std.iso_iec._24727.tech.schema.DifferentialIdentityServiceActionName
import iso.std.iso_iec._24727.tech.schema.InputUnitType
import iso.std.iso_iec._24727.tech.schema.PinInputType
import iso.std.iso_iec._24727.tech.schema.TransmitResponse
import iso.std.iso_iec._24727.tech.schema.VerifyUser
import iso.std.iso_iec._24727.tech.schema.VerifyUserResponse
import org.openecard.addon.sal.FunctionType
import org.openecard.addon.sal.ProtocolStep
import org.openecard.common.ECardException
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.WSHelper.makeResult
import org.openecard.common.anytype.pin.PINCompareDIDAuthenticateInputType
import org.openecard.common.anytype.pin.PINCompareMarkerType
import org.openecard.common.apdu.common.CardResponseAPDU
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.sal.Assert
import org.openecard.common.sal.util.SALUtils
import org.openecard.common.util.PINUtils
import java.math.BigInteger
import java.util.Arrays

private val logger = KotlinLogging.logger { }

/**
 * Implements the DIDAuthenticate step of the PIN Compare protocol.
 * See TR-03112, version 1.1.2, part 7, section 4.1.5.
 *
 * @author Dirk Petrautzki
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 * @author Tobias Wich
 * Creates a new DIDAuthenticateStep.
 *
 * @param dispatcher Dispatcher
 */
class DIDAuthenticateStep(
	private val dispatcher: Dispatcher,
) : ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {
	override val functionType: FunctionType
		get() = FunctionType.DIDAuthenticate

	override fun perform(
		request: DIDAuthenticate,
		internalData: MutableMap<String, Any>,
	): DIDAuthenticateResponse {
		val response: DIDAuthenticateResponse =
			WSHelper.makeResponse(
				DIDAuthenticateResponse::class.java,
				WSHelper.makeResultOK(),
			)

		val req =
			request

		var rawPIN: CharArray? = null
		try {
			val connectionHandle = SALUtils.getConnectionHandle(req)
			val didName = SALUtils.getDIDName(req)
			val stateEntry = SALUtils.getCardStateEntry(internalData, connectionHandle)
			val cardStateEntry = stateEntry.cardEntry
			val pinCompareInput = PINCompareDIDAuthenticateInputType(req.getAuthenticationProtocolData())
			val pinCompareOutput = pinCompareInput.outputType

			val cardApplication =
				if (req.didScope != null && req.didScope == DIDScopeType.GLOBAL) {
					cardStateEntry.cif.getApplicationIdByDidName(
						req.didName,
						req.didScope,
					)
				} else {
					connectionHandle.cardApplication
				}
			Assert.securityConditionDID(
				cardStateEntry,
				cardApplication,
				didName,
				DifferentialIdentityServiceActionName.DID_AUTHENTICATE,
			)

			val didStructure = cardStateEntry.getDIDStructure(didName, cardApplication)
			val pinCompareMarker = PINCompareMarkerType(didStructure.didMarker)
			val keyRef = pinCompareMarker.pINRef!!.keyRef[0]
			val slotHandle = connectionHandle.slotHandle
			val attributes = pinCompareMarker.passwordAttributes!!
			rawPIN = pinCompareInput.pIN
			pinCompareInput.pIN = null // delete pin from memory of the structure
			val template = byteArrayOf(0x00, 0x20, 0x00, keyRef)
			var responseCode: ByteArray

			// [TR-03112-6] The structure of the template corresponds to the
			// structure of an APDU for the VERIFY command in accordance
			// with [ISO7816-4] (Section 7.5.6).
			if (rawPIN == null || rawPIN.isEmpty()) {
				val inputUnit =
					InputUnitType().apply {
						pinInput =
							PinInputType().apply {
								setIndex(BigInteger.ZERO)
								setPasswordAttributes(attributes)
							}
					}
				val verify =
					VerifyUser().apply {
						setSlotHandle(slotHandle)
						setInputUnit(inputUnit)
						setTemplate(template)
					}

				val verifyR = dispatcher.safeDeliver(verify) as VerifyUserResponse
				checkResult<VerifyUserResponse>(verifyR)
				responseCode = verifyR.getResponse()
			} else {
				val verifyTransmit = PINUtils.buildVerifyTransmit(rawPIN, attributes, template, slotHandle)
				try {
					val transResp = dispatcher.safeDeliver(verifyTransmit) as TransmitResponse
					checkResult<TransmitResponse>(transResp)
					responseCode = transResp.outputAPDU[0]
				} finally {
					// blank PIN APDU
					for (apdu in verifyTransmit.getInputAPDUInfo()) {
						val rawApdu = apdu.getInputAPDU()
						if (rawApdu != null) {
							Arrays.fill(rawApdu, 0.toByte())
						}
					}
				}
			}

			val verifyResponseAPDU = CardResponseAPDU(responseCode)
			if (verifyResponseAPDU.isWarningProcessed) {
				pinCompareOutput.retryCounter = BigInteger((verifyResponseAPDU.sW2.toInt() and 0x0F).toString())
			}

			cardStateEntry.addAuthenticated(didName, cardApplication)
			response.authenticationProtocolData = pinCompareOutput.authDataType
		} catch (e: ECardException) {
			logger.error(e) { e.message }
			response.setResult(e.result)
		} catch (e: Exception) {
			if (e is RuntimeException) {
				throw e
			}
			logger.error(e) { e.message }
			response.setResult(makeResult(e))
		} finally {
			if (rawPIN != null) {
				org.openecard.bouncycastle.util.Arrays
					.fill(rawPIN, ' ')
			}
		}

		return response
	}
}
