/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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
package org.openecard.sal.protocol.eac

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse
import org.openecard.addon.Context
import org.openecard.addon.sal.FunctionType
import org.openecard.addon.sal.ProtocolStep
import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.common.DynamicContext
import org.openecard.common.ECardConstants
import org.openecard.common.ECardException
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.makeResultError
import org.openecard.common.WSHelper.makeResultUnknownError
import org.openecard.common.interfaces.Dispatcher
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificateChain
import org.openecard.sal.protocol.eac.anytype.EAC2InputType

/**
 * Implements TerminalAuthentication protocol step according to BSI-TR-03112-7.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.6.6.
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */

private val LOG = KotlinLogging.logger { }

class TerminalAuthenticationStep(
	ctx: Context,
) : ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {
	private val dispatcher: Dispatcher = ctx.dispatcher

	override fun getFunctionType(): FunctionType = FunctionType.DIDAuthenticate

	override fun perform(
		didAuthenticate: DIDAuthenticate,
		internalData: MutableMap<String, Any?>,
	): DIDAuthenticateResponse {
		val response: DIDAuthenticateResponse =
			WSHelper.makeResponse<Class<DIDAuthenticateResponse>, DIDAuthenticateResponse>(
				iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse::class.java,
				org.openecard.common.WSHelper
					.makeResultOK(),
			)
		val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)

		val slotHandle = didAuthenticate.getConnectionHandle().getSlotHandle()

		try {
			val eac2Input = EAC2InputType(didAuthenticate.getAuthenticationProtocolData())
			var eac2Output = eac2Input.outputType

			val ta = TerminalAuthentication(dispatcher, slotHandle)

			// Build certificate chain
			var certificateChain: CardVerifiableCertificateChain =
				internalData.get(
					EACConstants.IDATA_CERTIFICATES,
				) as CardVerifiableCertificateChain
			certificateChain.addCertificates(eac2Input.certificates)

			val currentCAR = internalData.get(EACConstants.IDATA_CURRENT_CAR) as ByteArray
			val previousCAR = internalData.get(EACConstants.IDATA_PREVIOUS_CAR) as ByteArray?
			var tmpChain = certificateChain.getCertificateChainFromCAR(currentCAR)
			// try again with previous car if it didn't work
			if (tmpChain.certificates.isEmpty() && previousCAR != null) {
				tmpChain = certificateChain.getCertificateChainFromCAR(previousCAR)
			}
			certificateChain = tmpChain

			if (certificateChain.certificates.isEmpty()) {
				val msg = "Failed to create a valid certificate chain from the transmitted certificates."
				LOG.error { msg }
				response.setResult(makeResultError(ECardConstants.Minor.App.INCORRECT_PARM, msg))
				return response
			}

			// TA: Step 1 - Verify certificates
			ta.verifyCertificates(certificateChain)

			// save values for later use
			val terminalCertificate = certificateChain.terminalCertificate
			val key = eac2Input.ephemeralPublicKey
			val signature = eac2Input.signature
			internalData.put(EACConstants.IDATA_PK_PCD, key)
			internalData.put(EACConstants.IDATA_SIGNATURE, signature)
			internalData.put(EACConstants.IDATA_TERMINAL_CERTIFICATE, terminalCertificate)

			if (signature != null) {
				LOG.trace { "Signature has been provided in EAC2InputType." }

				// perform TA and CA authentication
				val ca = ChipAuthentication(dispatcher, slotHandle)
				val auth = AuthenticationHelper(ta, ca)
				eac2Output = auth.performAuth(eac2Output, internalData)

				// no third step needed, notify GUI
				val ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)
				ctx.put(EACProtocol.AUTHENTICATION_DONE, true)
			} else {
				LOG.trace { "Signature has not been provided in EAC2InputType." }

				// send challenge again
				val rPICC = internalData[EACConstants.IDATA_CHALLENGE] as ByteArray
				eac2Output.setChallenge(rPICC)
			}

			response.setAuthenticationProtocolData(eac2Output.authDataType)
		} catch (e: ECardException) {
			LOG.error(e) { e.message }
			response.setResult(e.result)
			dynCtx.put(EACProtocol.AUTHENTICATION_DONE, false)
		} catch (e: Exception) {
			LOG.error(e) { "${e.message}" }
			response.setResult(makeResultUnknownError(e.message))
			dynCtx.put(EACProtocol.AUTHENTICATION_DONE, false)
		}

		return response
	}
}
