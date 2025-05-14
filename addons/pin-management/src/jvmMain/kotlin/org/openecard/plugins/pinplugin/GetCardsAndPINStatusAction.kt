/****************************************************************************
 * Copyright (C) 2014-2025 ecsec GmbH.
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
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.CreateSession
import iso.std.iso_iec._24727.tech.schema.CreateSessionResponse
import iso.std.iso_iec._24727.tech.schema.DestroyChannel
import iso.std.iso_iec._24727.tech.schema.DestroySession
import iso.std.iso_iec._24727.tech.schema.Disconnect
import iso.std.iso_iec._24727.tech.schema.PowerDownDevices
import org.openecard.addon.ActionInitializationException
import org.openecard.addon.Context
import org.openecard.addon.bind.AppExtensionException
import org.openecard.addon.sal.SalStateView
import org.openecard.common.DynamicContext
import org.openecard.common.ECardConstants
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.event.EventObject
import org.openecard.common.event.EventType
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.interfaces.DispatcherExceptionUnchecked
import org.openecard.common.interfaces.EventCallback
import org.openecard.common.interfaces.EventDispatcher
import org.openecard.common.interfaces.EventFilter
import org.openecard.common.interfaces.InvocationTargetExceptionUnchecked
import org.openecard.common.util.Promise
import org.openecard.common.util.SysUtils
import org.openecard.gui.ResultStatus
import org.openecard.plugins.pinplugin.gui.CardRemovedFilter
import org.openecard.plugins.pinplugin.gui.PINDialog
import java.lang.AutoCloseable
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory

private val logger = KotlinLogging.logger { }

/**
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
class GetCardsAndPINStatusAction : AbstractPINAction() {
	@Throws(AppExtensionException::class)
	override fun execute() {
		// init dyn ctx
		val ctx = DynamicContext.getInstance(DYNCTX_INSTANCE_KEY)!!

		var sessionHandle: ConnectionHandleType? = null

		var managedCardCapturer: Pair<CardCapturer?, AutoCloseable?>? = null

		var pinManagement: Future<ResultStatus?>? = null
		try {
			sessionHandle = createSessionHandle()

			managedCardCapturer = createCardCapturer(sessionHandle, dispatcher, evDispatcher, this, salStateView)

			val cardCapturer: CardCapturer = managedCardCapturer.first

			val success = cardCapturer.updateCardState()

			if (!success && !SysUtils.isMobileDevice) {
				// User cancelled card insertion.
				logger.debug { "User cancelled card insertion" }
				return
			}

			try {
				val es =
					Executors.newSingleThreadExecutor(
						ThreadFactory { action: Runnable? ->
							Thread(
								action,
								"ShowPINManagementDialog",
							)
						},
					)

				val errorPromise = Promise<Throwable?>()

				pinManagement =
					es.submit<ResultStatus?>(
						Callable {
							val uc = PINDialog(gui, dispatcher, cardCapturer, errorPromise)
							uc.show()
						},
					)

				val disconnectEventSink = registerListeners(cardCapturer.aquireView(), pinManagement)

				try {
					val result = pinManagement.get()
					if (result == ResultStatus.CANCEL || result == ResultStatus.INTERRUPTED) {
						val pinChangeError: Any? = errorPromise.derefNonblocking()
						val minor =
							when (pinChangeError) {
								is WSHelper.WSException -> pinChangeError.resultMinor
								is CancellationException -> ECardConstants.Minor.IFD.CANCELLATION_BY_USER
								null -> ECardConstants.Minor.IFD.CANCELLATION_BY_USER
								else -> ECardConstants.Minor.App.INT_ERROR
							}

						logger.debug { "Pin management completed with $minor from $pinChangeError" }

						throw AppExtensionException(minor ?: "", "PIN Management was cancelled.")
					}
				} finally {
					disconnectEventSink?.let {
						evDispatcher.del(it)
					}
				}
			} catch (ex: InterruptedException) {
				logger.info(ex) { "waiting for PIN management to stop interrupted." }
				pinManagement?.cancel(true)
			} catch (ex: ExecutionException) {
				logger.warn(ex) { "Pin Management failed" }
			} catch (ex: CancellationException) {
				val msg = "PIN Management was cancelled."
				logger.warn(ex) { msg }
				throw AppExtensionException(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, msg)
			} finally {
				pinManagement = null

				val cardView = cardCapturer.aquireView()

				val slotHandle = cardView.handle.slotHandle

				if (slotHandle != null && slotHandle.isNotEmpty()) {
					// destroy the pace channel
					val destChannel =
						DestroyChannel().apply {
							setSlotHandle(slotHandle)
						}
					dispatcher.safeDeliver(destChannel)

					// Transaction based communication does not work on java 8 so the PACE channel is not closed after an
					// EndTransaction call. So do a reset of the card to close the PACE channel.
					val disconnect =
						Disconnect().apply {
							setSlotHandle(cardView.handle.slotHandle)
							action = ActionType.RESET
						}
					dispatcher.safeDeliver(disconnect)
				}
			}
		} catch (ex: WSHelper.WSException) {
			logger.debug(ex) { "Error while executing PIN Management." }
			throw AppExtensionException(ex.resultMinor ?: "", ex.message)
		} finally {
			try {
				managedCardCapturer?.second?.close()
			} catch (ex: Exception) {
				logger.error(ex) { "Error while cleaning up card management." }
			}

			try {
				sessionHandle?.let {
					val request =
						DestroySession().apply {
							connectionHandle = it
						}
					this.dispatcher.safeDeliver(request)
				}
			} catch (ex: Exception) {
				logger.error(ex) { "Error while cleaning up card management." }
			}

			try {
				val pdd =
					PowerDownDevices().apply {
						sessionHandle?.let { contextHandle = it.contextHandle }
					}
				this.dispatcher.safeDeliver(pdd)
			} catch (ex: Exception) {
				logger.error(ex) { "Error while powering down devices" }
			}
			ctx.clear()
		}
	}

	private fun registerListeners(
		cardView: CardStateView,
		pinManagement: Future<ResultStatus?>,
	): EventCallback? {
		val disconnectEventSink: EventCallback?

		if (!SysUtils.isMobileDevice) {
			disconnectEventSink =
				object : EventCallback {
					override fun signalEvent(
						eventType: EventType,
						eventData: EventObject,
					) {
						if (eventType == EventType.CARD_REMOVED) {
							logger.info { "Card has been removed. Shutting down PIN Management process." }
							pinManagement.cancel(true)
						}
					}
				}

			val evFilter: EventFilter =
				CardRemovedFilter(
					cardView.handle.ifdName,
					cardView.handle.slotIndex,
				)
			evDispatcher.add(disconnectEventSink, evFilter)
		} else {
			disconnectEventSink = null
		}
		return disconnectEventSink
	}

	@Throws(DispatcherExceptionUnchecked::class, InvocationTargetExceptionUnchecked::class, WSHelper.WSException::class)
	private fun createSessionHandle(): ConnectionHandleType {
		val response = this.dispatcher.safeDeliver(CreateSession()) as CreateSessionResponse
		checkResult<CreateSessionResponse>(response)

		return response.connectionHandle
	}

	@Throws(ActionInitializationException::class)
	override fun init(aCtx: Context) {
		dispatcher = aCtx.dispatcher
		this.gui = aCtx.userConsent
		this.recognition = aCtx.recognition
		this.evDispatcher = aCtx.eventDispatcher
		this.salStateView = aCtx.salStateView
	}

	override fun destroy(force: Boolean) {
		// ignore
	}

	companion object {
		const val DYNCTX_INSTANCE_KEY: String = "GetCardsAndPINStatusAction"

		const val PIN_STATUS: String = "pin-status"
		const val PIN_CORRECT: String = "pin-correct"
		const val CAN_CORRECT: String = "can-correct"
		const val PUK_CORRECT: String = "puk-correct"

		private fun createCardCapturer(
			sessionHandle: ConnectionHandleType,
			dispatcher: Dispatcher,
			eventDispatcher: EventDispatcher,
			pinAction: AbstractPINAction,
			salStateView: SalStateView,
		): Pair<CardCapturer, AutoCloseable> {
			val isMobileDevice = SysUtils.isMobileDevice
			val cardCapturer =
				CardCapturer(
					sessionHandle,
					dispatcher,
					pinAction,
					isMobileDevice,
					salStateView,
				)

			if (isMobileDevice) {
				val disconnectEventSink =
					object : EventCallback {
						override fun signalEvent(
							eventType: EventType,
							eventData: EventObject,
						) {
							when (eventType) {
								EventType.CARD_REMOVED -> cardCapturer.onCardRemoved(eventData)
								EventType.POWER_DOWN_DEVICES -> cardCapturer.onPowerDownDevices(eventData)
								EventType.PREPARE_DEVICES -> cardCapturer.onPrepareDevices(eventData)
								else -> {}
							}
						}
					}
				eventDispatcher.add(
					disconnectEventSink,
					EventType.CARD_REMOVED,
					EventType.POWER_DOWN_DEVICES,
					EventType.PREPARE_DEVICES,
				)

				return (
					cardCapturer to
						AutoCloseable {
							eventDispatcher.del(disconnectEventSink)
						}
				)
			} else {
				return (cardCapturer to AutoCloseable({}))
			}
		}
	}
}
