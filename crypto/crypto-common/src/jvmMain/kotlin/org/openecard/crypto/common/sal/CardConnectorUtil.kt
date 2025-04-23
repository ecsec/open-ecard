/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 */
package org.openecard.crypto.common.sal

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.*
import org.openecard.common.ECardException
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.event.EventObject
import org.openecard.common.event.EventType
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.interfaces.EventCallback
import org.openecard.common.interfaces.EventDispatcher
import org.openecard.common.interfaces.EventFilter
import org.openecard.common.util.HandlerBuilder
import org.openecard.common.util.HandlerUtils
import org.openecard.common.util.Promise
import org.openecard.common.util.SysUtils

private val LOG = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
class CardConnectorUtil(
	private val dispatcher: Dispatcher,
	private val eventHandler: EventDispatcher,
	private val cardTypes: Set<String>,
	private val session: String?,
	private val ctxHandle: ByteArray?,
	private val ifdName: String?,
) {
	@Throws(InterruptedException::class)
	fun waitForCard(): CardApplicationPathType {
		val foundCardHandle: Promise<ConnectionHandleType> = Promise()
		val callbacks: MutableList<EventCallback> = ArrayList(2)

		val commonCallback = CardFound(foundCardHandle)
		callbacks.add(commonCallback)
		eventHandler.add(commonCallback, TypeFilter())

		if (SysUtils.isIOS()) {
			val cancelCallback = CancelOnCardRemovedFilter(foundCardHandle)
			callbacks.add(cancelCallback)
			eventHandler.add(cancelCallback, CardRemovalFilter())
		}

		try {
			// check if there is a card already present
			if (!cardTypes.isEmpty()) {
				val h = checkType()
				if (h != null) {
					return h
				}
			}

			val eventCardHandle = foundCardHandle.deref()
			return HandlerUtils.copyPath(eventCardHandle)
		} finally {
			for (callback in callbacks) {
				eventHandler.del(callback)
			}
		}
	}

	private fun checkType(): CardApplicationPathType? {
		val preq = CardApplicationPath()
		val pt =
			HandlerBuilder
				.create()
				.setSessionId(session)
				.setContextHandle(ctxHandle)
				.setIfdName(ifdName)
				.buildAppPath()
		preq.setCardAppPathRequest(pt)

		val res = dispatcher.safeDeliver(preq) as CardApplicationPathResponse
		val resSet = res.getCardAppPathResultSet()
		if (resSet != null && !resSet.getCardApplicationPathResult().isEmpty()) {
			for (path in resSet.getCardApplicationPathResult()) {
				// connect card and check type
				val con = CardApplicationConnect()
				con.setCardApplicationPath(path)
				val conRes = dispatcher.safeDeliver(con) as CardApplicationConnectResponse
				try {
					checkResult<CardApplicationConnectResponse>(conRes)
					val card = conRes.getConnectionHandle()
					try {
						if (cardTypes.contains(card.getRecognitionInfo().getCardType())) {
							return HandlerUtils.copyPath(card)
						}
					} finally {
						val dis = CardApplicationDisconnect()
						dis.setConnectionHandle(card)
						dispatcher.safeDeliver(dis)
					}
				} catch (ex: ECardException) {
					LOG.warn(ex) { "Error occurred while checking a card." }
				}
			}
		}

		return null
	}

	private inner class CardFound(
		private val foundCardHandle: Promise<ConnectionHandleType>,
	) : EventCallback {
		override fun signalEvent(
			eventType: EventType,
			eventData: EventObject,
		) {
			try {
				if (eventType == EventType.CARD_RECOGNIZED) {
					foundCardHandle.deliver(eventData.handle)
				}
			} catch (ex: IllegalStateException) {
				// caused if callback is called multiple times, but this is fine
				LOG.warn(ex) { "Card in an illegal state." }
			}
		}
	}

	private inner class TypeFilter : EventFilter {
		override fun matches(
			t: EventType,
			o: EventObject,
		): Boolean {
			val h = o.handle
			if (t == EventType.CARD_RECOGNIZED && h != null) {
				if (ctxHandle != null && ifdName != null) {
					if (ctxHandle.contentEquals(h.getContextHandle()) && ifdName == h.getIFDName()) {
						return cardTypes.contains(h.getRecognitionInfo().getCardType())
					}
				} else {
					return cardTypes.contains(h.getRecognitionInfo().getCardType())
				}
			}
			return false
		}
	}
}
