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
package org.openecard.plugins.pinplugin

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse
import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType
import iso.std.iso_iec._24727.tech.schema.Transmit
import iso.std.iso_iec._24727.tech.schema.TransmitResponse
import org.openecard.addon.bind.AppExtensionAction
import org.openecard.addon.sal.SalStateView
import org.openecard.common.ECardConstants
import org.openecard.common.I18n
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.WSHelper.createException
import org.openecard.common.WSHelper.makeResultError
import org.openecard.common.ifd.PACECapabilities
import org.openecard.common.interfaces.CardRecognition
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.interfaces.EventDispatcher
import org.openecard.common.sal.util.InsertCardDialog
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.StringUtils
import org.openecard.gui.UserConsent

private val logger = KotlinLogging.logger { }

/**
 * Common superclass for `ChangePINAction` and `UnblockPINAction`.
 * Bundles methods needed in both actions.
 *
 * @author Dirk Petrautzki
 */
abstract class AbstractPINAction : AppExtensionAction {
	// translation and logger
	protected val lang: I18n? = I18n.getTranslation("pinplugin")
	protected lateinit var dispatcher: Dispatcher
	protected lateinit var gui: UserConsent
	protected lateinit var recognition: CardRecognition
	protected lateinit var evDispatcher: EventDispatcher
	protected lateinit var salStateView: SalStateView

	/**
	 * Recognize the PIN state of the card given through the connection handle.
	 *
	 * @param cHandle The connection handle for the card for which the pin state should be recognized.
	 * @return The recognized State (may be `RecognizedState.UNKNOWN`).
	 */
	@Throws(WSHelper.WSException::class)
	fun recognizeState(cHandle: ConnectionHandleType): RecognizedState {
		val t =
			Transmit().apply {
				slotHandle = cHandle.slotHandle
				inputAPDUInfo.add(
					InputAPDUInfoType().apply {
						inputAPDU = RECOGNIZE_PIN_APDU
					},
				)
			}
		val response = dispatcher.safeDeliver(t) as TransmitResponse

		checkResult<TransmitResponse>(response)

		val responseAPDU = response.outputAPDU[0]

		val state =
			when {
				ByteUtils.compare(RESPONSE_RC3, responseAPDU) -> RecognizedState.PIN_ACTIVATED_RC3
				ByteUtils.compare(RESPONSE_DEACTIVATED, responseAPDU) -> RecognizedState.PIN_DEACTIVATED
				ByteUtils.compare(RESPONSE_RC2, responseAPDU) -> RecognizedState.PIN_ACTIVATED_RC2
				ByteUtils.compare(RESPONSE_SUSPENDED, responseAPDU) -> RecognizedState.PIN_SUSPENDED
				ByteUtils.compare(RESPONSE_BLOCKED, responseAPDU) -> RecognizedState.PIN_BLOCKED

				else -> {
					logger.error { "Unhandled response to the PIN state recognition APDU." }
					RecognizedState.UNKNOWN
				}
			}

		logger.info { "State of the PIN: $state." }
		return state
	}

	@OptIn(ExperimentalStdlibApi::class)
	@Throws(WSHelper.WSException::class)
	fun getPUKStatus(cHandle: ConnectionHandleType) {
		val t =
			Transmit().apply {
				slotHandle = cHandle.slotHandle
				inputAPDUInfo.add(
					InputAPDUInfoType().apply {
						inputAPDU = RECOGNIZE_PUK_APDU
					},
				)
			}
		val response = dispatcher.safeDeliver(t) as TransmitResponse
		checkResult<TransmitResponse>(response)

		val responseAPDU = response.outputAPDU[0]
		logger.debug { "PUK response is ${responseAPDU.toHexString()}" }
	}

	/**
	 * Wait until a card of the specified card type was inserted.
	 *
	 * @param cardType The type of the card that should be inserted.
	 * @return The ConnectionHandle of the inserted card or null if no card was inserted.
	 */
	fun waitForCardType(cardType: String): ConnectionHandleType? {
		val cardName = recognition.getTranslatedCardName(cardType)

		val uc = InsertCardDialog(gui, mapOf(cardName to cardType), evDispatcher, salStateView)

		val results = uc.show()
		if (results == null || results.isEmpty()) {
			return null
		}
		// get(0) should be sufficient we a looking just for one card. i think the possibility to find 2 is very low.
		return results[0]
	}

	/**
	 * Connect to the root application of the card specified with a connection handle using a empty CardApplicationPath
	 * and afterwards a CardApplicationConnect.
	 *
	 * @param cHandle
	 * The connection handle for the card to connect to root application.
	 * @return The updated connection handle (now including a SlotHandle) or null if connecting went wrong.
	 * @throws WSHelper.WSException
	 */
	@Throws(WSHelper.WSException::class)
	fun connectToRootApplication(cHandle: ConnectionHandleType): ConnectionHandleType {
		// Perform a CardApplicationPath and CardApplicationConnect to connect to the card application

		val cardApplicationPath =
			CardApplicationPath().apply {
				cardAppPathRequest = cHandle
			}
		val cardApplicationPathResponse =
			dispatcher.safeDeliver(cardApplicationPath) as CardApplicationPathResponse

		// Check CardApplicationPathResponse
		checkResult<CardApplicationPathResponse>(cardApplicationPathResponse)

		if (cardApplicationPathResponse.cardAppPathResultSet.cardApplicationPathResult.isEmpty()) {
			logger.error("CardApplicationPath failed.")
			val result = makeResultError(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, "Card was removed.")
			val ex = createException(result)
			throw ex
		}

		val cardApplicationConnect =
			CardApplicationConnect().apply {
				setCardApplicationPath(
					cardApplicationPathResponse.cardAppPathResultSet.cardApplicationPathResult[0],
				)
			}
		val cardApplicationConnectResponse =
			dispatcher.safeDeliver(cardApplicationConnect) as CardApplicationConnectResponse

		// Check CardApplicationConnectResponse
		checkResult<CardApplicationConnectResponse>(cardApplicationConnectResponse)

		// Update ConnectionHandle. It now includes a SlotHandle.
		return cardApplicationConnectResponse.connectionHandle
	}

	/**
	 * Check if the selected card reader supports PACE.
	 * In that case, the reader is a standard or comfort reader.
	 *
	 * @param connectionHandle Handle describing the IFD and reader.
	 * @return true when card reader supports genericPACE, false otherwise.
	 * @throws WSException In case request for the terminal capabilities returned an error.
	 */
	@Throws(WSHelper.WSException::class)
	fun genericPACESupport(connectionHandle: ConnectionHandleType): Boolean {
		// Request terminal capabilities
		val capabilitiesRequest =
			GetIFDCapabilities().apply {
				contextHandle = connectionHandle.contextHandle
				ifdName = connectionHandle.ifdName
			}
		val capabilitiesResponse = dispatcher.safeDeliver(capabilitiesRequest) as GetIFDCapabilitiesResponse
		checkResult<GetIFDCapabilitiesResponse>(capabilitiesResponse)

		capabilitiesResponse.ifdCapabilities?.slotCapability?.let {
			// Check all capabilities for generic PACE
			for (capability in it) {
				if (capability.index == connectionHandle.slotHandle) {
					for (protocol in capability.protocol) {
						if (protocol == PACECapabilities.PACECapability.GenericPACE.protocol) {
							return true
						}
					}
				}
			}
		}

		// No PACE capability found
		return false
	}

	companion object {
		// constants
		private val RECOGNIZE_PIN_APDU: ByteArray = StringUtils.toByteArray("0022C1A40F800A04007F00070202040202830103")
		private val RECOGNIZE_PUK_APDU: ByteArray = StringUtils.toByteArray("0022C1A40F800A04007F00070202040202830104")
		private val RESPONSE_RC3 = byteArrayOf(0x90.toByte(), 0x00)
		private val RESPONSE_BLOCKED = byteArrayOf(0x63.toByte(), 0xC0.toByte())
		private val RESPONSE_SUSPENDED = byteArrayOf(0x63.toByte(), 0xC1.toByte())
		private val RESPONSE_RC2 = byteArrayOf(0x63.toByte(), 0xC2.toByte())
		private val RESPONSE_DEACTIVATED = byteArrayOf(0x62.toByte(), 0x83.toByte())
	}
}
