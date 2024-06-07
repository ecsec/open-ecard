/****************************************************************************
 * Copyright (C) 2024 ecsec GmbH.
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

package org.openecard.common.sal.util

import iso.std.iso_iec._24727.tech.schema.*
import org.openecard.addon.Context
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.util.HandlerUtils
import org.openecard.common.util.SysUtils
import org.openecard.crypto.common.sal.CardConnectorUtil
import kotlin.String
import kotlin.Throws

open class InsertCardHelper(
	protected val ctx: Context,
	protected var conHandle: ConnectionHandleType
) {
	fun isConnected(): Boolean {
		val hasSlotHandle = conHandle.slotHandle != null
		return if (hasSlotHandle) {
			ctx.salStateView.hasConnectedCard(conHandle)
		} else {
			false
		}
	}

	@Throws(WSHelper.WSException::class, InterruptedException::class)
	fun connectCardIfNeeded(possibleCardTypes: Set<String>): ConnectionHandleType {
		// connect card and update handle of this instance
		if (isConnected()) {
			return conHandle
		} else {
			// signal cards to be activated
			val pdreq = PrepareDevices()
			pdreq.contextHandle = conHandle.contextHandle
			checkResult(ctx.dispatcher.safeDeliver(pdreq) as PrepareDevicesResponse)
			val sessionIdentifier: String = conHandle.channelHandle.sessionIdentifier

			// wait for eid card
			val connectorUtil = CardConnectorUtil(
				ctx.dispatcher, ctx.eventDispatcher, possibleCardTypes,
				sessionIdentifier, conHandle.contextHandle, conHandle.ifdName
			)
			val path = connectorUtil.waitForCard()
			var channelHandle = path.channelHandle
			if (channelHandle == null) {
				channelHandle = ChannelHandleType()
				path.channelHandle = channelHandle
			}
			if (channelHandle.sessionIdentifier == null) {
				channelHandle.sessionIdentifier = sessionIdentifier
			}

			// connect eid card
			val conReq = CardApplicationConnect()
			conReq.cardApplicationPath = path
			conReq.isExclusiveUse = true
			val conRes = ctx.dispatcher.safeDeliver(conReq) as CardApplicationConnectResponse
			checkResult(conRes)
			this.conHandle = conRes.connectionHandle
			return conHandle
		}
	}

	@Throws(WSHelper.WSException::class)
	fun getMobileReader(): ConnectionHandleType {
		// ListIFD should be fine for that
		val li = ListIFDs()
		val ctxHandle = ctx.ifdCtx[0]
		li.contextHandle = ctxHandle
		val lir = ctx.dispatcher.safeDeliver(li) as ListIFDsResponse
		checkResult(lir)
		val ifd = lir.ifdName[0]

		val newHandle = HandlerUtils.copyHandle(conHandle)
		newHandle.contextHandle = ctxHandle
		newHandle.ifdName = ifd
		return newHandle
	}

	fun useMobileReader(): ConnectionHandleType {
		val newHandle = getMobileReader()
		conHandle = newHandle
		return HandlerUtils.copyHandle(conHandle)
	}

	fun disconnectIfMobile() {
		if (SysUtils.isMobileDevice()) {
			val disc = CardApplicationDisconnect()
			disc.connectionHandle = conHandle
			val discr = ctx.dispatcher.safeDeliver(disc) as CardApplicationDisconnectResponse
		}
	}
}
