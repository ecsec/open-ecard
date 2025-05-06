/****************************************************************************
 * Copyright (C) 2019-2025 ecsec GmbH.
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
import iso.std.iso_iec._24727.tech.schema.ActionType
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnect
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.PrepareDevices
import iso.std.iso_iec._24727.tech.schema.PrepareDevicesResponse
import org.openecard.addon.sal.SalStateView
import org.openecard.common.DynamicContext
import org.openecard.common.ECardConstants
import org.openecard.common.ECardConstants.NPA_CARD_TYPE
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.WSHelper.minorIsOneOf
import org.openecard.common.event.EventObject
import org.openecard.common.interfaces.Dispatcher

private val logger = KotlinLogging.logger { }

/**
 *
 * @author Neil Crossley
 */
class CardCapturer internal constructor(
	private val sessionHandle: ConnectionHandleType,
	private val dispatcher: Dispatcher,
	private val pinAction: AbstractPINAction,
	private var areDevicesPoweredDown: Boolean,
	private val salStateView: SalStateView,
) {
	private var deviceSessionCount = 0
	private val emptyState =
		ReadOnlyCardStateView(
			connectionHandle = sessionHandle,
			pinState = RecognizedState.UNKNOWN,
			capturePin = true,
			removed = true,
			preparedDeviceSession = deviceSessionCount,
		)
	private val cardViewLock = Any()
	private val devicesLock = Any()
	private val cardStateView = DelegatingCardStateView(emptyState)
	private var hasInitialized = false

	fun aquireView(): CardStateView = cardStateView

	@Throws(WSHelper.WSException::class)
	fun updateCardState(): Boolean {
		synchronized(cardViewLock) {
			if (!hasInitialized ||
				areDevicesPoweredDown ||
				this.cardStateView.preparedDeviceSession() != deviceSessionCount
			) {
				val ctx = DynamicContext.getInstance(GetCardsAndPINStatusAction.Companion.DYNCTX_INSTANCE_KEY)
				var createdState = initialState(ctx)
				val success: Boolean
				if (createdState == null) {
					logger.debug { "Could not initialize initial state" }
					createdState = emptyState
					success = false
				} else {
					success = true
				}

				this.cardStateView.delegate = createdState
				this.hasInitialized = true
				return success
			} else {
				if (this.cardStateView.pinState != RecognizedState.PIN_RESUMED &&
					isDisconnected(this.cardStateView)
				) {
					// do not update in case of status resumed, it destroys the the pace channel and there is no disconnect after
					// the verification of the CAN so the handle stays the same
					updateConnectionHandle()
				}
				return true
			}
		}
	}

	@Throws(WSHelper.WSException::class)
	private fun initialState(ctx: DynamicContext): ReadOnlyCardStateView? {
		synchronized(devicesLock) {
			if (areDevicesPoweredDown) {
				logger.debug { "Call prepare devices" }
				val pd =
					PrepareDevices().apply {
						contextHandle = sessionHandle.contextHandle
					}
				val response = dispatcher.safeDeliver(pd) as PrepareDevicesResponse
				try {
					checkResult<PrepareDevicesResponse>(response)
				} catch (ex: WSHelper.WSException) {
					if (minorIsOneOf<WSHelper.WSException>(
							ex,
							ECardConstants.Minor.IFD.CANCELLATION_BY_USER,
							ECardConstants.Minor.IFD.Terminal.WAIT_FOR_DEVICE_TIMEOUT,
						)
					) {
						logger.debug { "Device was not prepared." }
						return null
					} else {
						logger.warn(ex) { "Could not prepare device." }
						throw ex
					}
				}
			}
		}

		// check if a german identity card is inserted, if not wait for it
		var cHandle = pinAction.waitForCardType(NPA_CARD_TYPE)
		if (cHandle == null) {
			logger.debug { "User cancelled card insertion." }
			return null
		}
		copySession(sessionHandle, cHandle)
		cHandle = pinAction.connectToRootApplication(cHandle)
		val pinState = pinAction.recognizeState(cHandle)

		pinAction.getPUKStatus(cHandle)
		ctx.put(GetCardsAndPINStatusAction.Companion.PIN_STATUS, pinState)
		val nativePace = pinAction.genericPACESupport(cHandle)
		val capturePin = !nativePace
		val cardState =
			ReadOnlyCardStateView(
				cHandle,
				pinState,
				capturePin,
				false,
				deviceSessionCount,
			)
		return cardState
	}

	/**
	 * Update the connection handle.
	 * This is necessary after every step because we Disconnect the card with a reset if we have success or not.
	 */
	private fun updateConnectionHandle() {
		val handle = cardStateView.handle
		val cPath =
			CardApplicationPath().apply {
				cardAppPathRequest =
					CardApplicationPathType().apply {
						channelHandle = handle.channelHandle
					}
			}

		val cPathResp = dispatcher.safeDeliver(cPath) as CardApplicationPathResponse
		val cRes = cPathResp.cardAppPathResultSet.cardApplicationPathResult
		for (capt in cRes) {
			val cConn =
				CardApplicationConnect().apply {
					cardApplicationPath = capt
				}
			val conRes = dispatcher.safeDeliver(cConn) as CardApplicationConnectResponse

			val cardType = conRes.connectionHandle.recognitionInfo.cardType
			val cHandleNew = conRes.connectionHandle
			if (cardType == NPA_CARD_TYPE) {
				// ensure same terminal and get the new slothandle
				if (cHandleNew.ifdName == handle.ifdName &&
					!cHandleNew.slotHandle.contentEquals(handle.slotHandle)
				) {
					this.cardStateView.delegate =
						ReadOnlyCardStateView(
							cHandleNew,
							cardStateView.pinState,
							cardStateView.capturePin(),
							cardStateView.isRemoved,
							cardStateView.preparedDeviceSession(),
						)

					break
					// also end if the connection handle found as before than it is still valid
				} else if (cHandleNew.ifdName == handle.ifdName &&
					cHandleNew
						.slotHandle
						.contentEquals(handle.slotHandle)
				) {
					break
				}
			} else {
				val disconnect =
					CardApplicationDisconnect().apply {
						connectionHandle = conRes.connectionHandle
						action = ActionType.RESET
					}
				dispatcher.safeDeliver(disconnect)
			}
		}
	}

	private fun copySession(
		source: ConnectionHandleType,
		target: ConnectionHandleType,
	) {
		val sourceChannel = source.channelHandle
		var targetChannel = target.channelHandle
		if (targetChannel == null) {
			targetChannel = ChannelHandleType()
			target.setChannelHandle(targetChannel)
		}
		targetChannel.sessionIdentifier = sourceChannel.sessionIdentifier
	}

	fun notifyCardStateChange(pinState: RecognizedState) {
		synchronized(cardViewLock) {
			val ctx = DynamicContext.getInstance(GetCardsAndPINStatusAction.Companion.DYNCTX_INSTANCE_KEY)
			ctx.put(GetCardsAndPINStatusAction.Companion.PIN_STATUS, pinState)

			val newView: CardStateView =
				ReadOnlyCardStateView(
					cardStateView.handle,
					pinState,
					cardStateView.capturePin(),
					cardStateView.isRemoved,
					cardStateView.preparedDeviceSession(),
				)
			cardStateView.delegate = newView
		}
	}

	fun onCardRemoved(eventData: EventObject) {
		synchronized(cardViewLock) {
			val currentView = cardStateView.delegate
			val eventConnHandle = eventData.handle
			val viewConnHandle = currentView.handle
			if (eventConnHandle != null && viewConnHandle != null) {
				val viewIfdName = viewConnHandle.ifdName
				val viewSlotIndex = viewConnHandle.slotIndex

				if (viewIfdName != null &&
					viewIfdName == eventConnHandle.ifdName &&
					viewSlotIndex != null &&
					viewSlotIndex == eventConnHandle.slotIndex
				) {
					val newView: CardStateView =
						ReadOnlyCardStateView(
							viewConnHandle,
							currentView.pinState,
							currentView.capturePin(),
							true,
							this.cardStateView.preparedDeviceSession(),
						)
					cardStateView.delegate = newView
				}
			}
		}
	}

	fun onPowerDownDevices(eventData: EventObject?) {
		synchronized(devicesLock) {
			areDevicesPoweredDown = true
		}
	}

	fun onPrepareDevices(eventData: EventObject?) {
		synchronized(devicesLock) {
			areDevicesPoweredDown = false
			deviceSessionCount += 1
		}
	}

	private fun isDisconnected(cardStateView: CardStateView): Boolean {
		val givenHandle = cardStateView.handle
		val givenIfdName = givenHandle.ifdName
		val givenSlotIndex = givenHandle.slotHandle
		val givenContextHandle = givenHandle.contextHandle
		return this.salStateView.isDisconnected(givenContextHandle, givenIfdName, givenSlotIndex)
	}
}
