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
import org.openecard.common.ECardException
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.makeResultUnknownError
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.tlv.TLVException
import org.openecard.sal.protocol.eac.anytype.EACAdditionalInputType
import javax.xml.parsers.ParserConfigurationException

private val logger = KotlinLogging.logger { }

/**
 * Implements Chip Authentication protocol step according to BSI-TR-03112-7.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.6.6.
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
class ChipAuthenticationStep(
	ctx: Context,
) : ProtocolStep<DIDAuthenticate, DIDAuthenticateResponse> {
	private val dispatcher: Dispatcher = ctx.dispatcher

	override fun getFunctionType(): FunctionType = FunctionType.DIDAuthenticate

	override fun perform(
		didAuthenticate: DIDAuthenticate,
		internalData: MutableMap<String, Any?>,
	): DIDAuthenticateResponse {
		val response: DIDAuthenticateResponse =
			WSHelper.makeResponse(
				DIDAuthenticateResponse::class.java,
				WSHelper
					.makeResultOK(),
			)

		// EACProtocol.setEmptyResponseData(response);
		val slotHandle = didAuthenticate.getConnectionHandle().getSlotHandle()
		val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)

		try {
			val eacAdditionalInput = EACAdditionalInputType(didAuthenticate.getAuthenticationProtocolData())
			var eac2Output = eacAdditionalInput.outputType

			val ta = TerminalAuthentication(dispatcher, slotHandle)
			val ca = ChipAuthentication(dispatcher, slotHandle)

			// save signature, it is needed in the authentication step
			val signature = eacAdditionalInput.signature
			internalData.put(EACConstants.IDATA_SIGNATURE, signature)

			// perform TA and CA authentication
			val auth = AuthenticationHelper(ta, ca)
			eac2Output = auth.performAuth(eac2Output, internalData)

			response.setAuthenticationProtocolData(eac2Output.authDataType)
		} catch (e: ECardException) {
			logger.error(e) { e.message }
			response.setResult(e.result)
			dynCtx.put(EACProtocol.Companion.AUTHENTICATION_DONE, false)
		} catch (e: ParserConfigurationException) {
			logger.error(e) { "${e.message}" }
			response.setResult(makeResultUnknownError(e.message))
			dynCtx.put(EACProtocol.Companion.AUTHENTICATION_DONE, false)
		} catch (e: TLVException) {
			logger.error(e) { "${e.message}" }
			response.setResult(makeResultUnknownError(e.message))
			dynCtx.put(EACProtocol.Companion.AUTHENTICATION_DONE, false)
		}

		// authentication finished, notify GUI
		dynCtx.put(EACProtocol.Companion.AUTHENTICATION_DONE, true)
		return response
	}
}
