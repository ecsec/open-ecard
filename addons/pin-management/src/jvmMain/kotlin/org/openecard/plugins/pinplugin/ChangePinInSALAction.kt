/****************************************************************************
 * Copyright (C) 2016-2025 ecsec GmbH.
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
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.DIDGet
import iso.std.iso_iec._24727.tech.schema.DIDGetResponse
import iso.std.iso_iec._24727.tech.schema.DIDList
import iso.std.iso_iec._24727.tech.schema.DIDListResponse
import iso.std.iso_iec._24727.tech.schema.DIDUpdate
import iso.std.iso_iec._24727.tech.schema.PinCompareDIDUpdateDataType
import org.openecard.addon.ActionInitializationException
import org.openecard.addon.Context
import org.openecard.common.ECardConstants
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.WSHelper.createException
import org.openecard.common.WSHelper.makeResultError

private val logger = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
class ChangePinInSALAction : AbstractPINAction() {
	override fun execute() {
		var connectedCards: List<ConnectionHandleType>? = null
		try {
			connectedCards = connectCards()

			// if (connectedCards.isEmpty()) {
			// 	// TODO: show no card inserted dialog
			// }

			for (nextCard in connectedCards) {
				// pick first card, find pin DID and call didupdate
				var dn: String
				try {
					dn = getPinDid(nextCard)
				} catch (ex: WSHelper.WSException) {
					logger.info { "Skipping card, because it has no PIN DID." }
					continue
				}

				val updateReq =
					DIDUpdate().apply {
						connectionHandle = nextCard
						didName = dn
						didUpdateData =
							PinCompareDIDUpdateDataType().apply {
								protocol = "urn:oid:1.3.162.15480.3.0.9"
							}
					}

				dispatcher.safeDeliver(updateReq)
			}
		} catch (_: WSHelper.WSException) {
		} finally {
			connectedCards?.let {
				for (nextHandle in it) {
					val dr =
						CardApplicationDisconnect().apply {
							connectionHandle = nextHandle
							action = ActionType.RESET
						}
					dispatcher.safeDeliver(dr)
				}
			}
		}
	}

	@Throws(ActionInitializationException::class)
	override fun init(aCtx: Context) {
		dispatcher = aCtx.dispatcher
		gui = aCtx.userConsent
		recognition = aCtx.recognition
		evDispatcher = aCtx.eventDispatcher
		salStateView = aCtx.salStateView
	}

	// ignore
	override fun destroy(force: Boolean) = Unit

	@Throws(WSHelper.WSException::class)
	private fun connectCards(): List<ConnectionHandleType> {
		// get all cards in the system
		val pathReq =
			CardApplicationPath().apply {
				cardAppPathRequest = CardApplicationPathType()
			}

		val pathRes = dispatcher.safeDeliver(pathReq) as CardApplicationPathResponse
		checkResult<CardApplicationPathResponse>(pathRes)

		// connect every card in the set
		val connectedCards = mutableListOf<ConnectionHandleType>()
		for (path in pathRes.cardAppPathResultSet.cardApplicationPathResult) {
			try {
				val conReq =
					CardApplicationConnect().apply {
						cardApplicationPath = path
						isExclusiveUse = false
					}

				val conRes = dispatcher.safeDeliver(conReq) as CardApplicationConnectResponse
				checkResult<CardApplicationConnectResponse>(conRes)
				connectedCards.add(conRes.connectionHandle)
			} catch (ex: WSHelper.WSException) {
				logger.error(ex) { "Failed to connect card, skipping this entry." }
			}
		}

		return connectedCards
	}

	@Throws(WSHelper.WSException::class)
	private fun getPinDid(handle: ConnectionHandleType?): String {
		// get all DIDs
		val listReq =
			DIDList().apply {
				connectionHandle = handle
			}
		val listRes = dispatcher.safeDeliver(listReq) as DIDListResponse
		checkResult<DIDListResponse>(listRes)

		// find pin did
		for (didName in listRes.didNameList.didName) {
			val getReq =
				DIDGet().apply {
					setConnectionHandle(handle)
					setDIDName(didName)
				}
			val getRes = dispatcher.safeDeliver(getReq) as DIDGetResponse
			// don't check result, just see if we have a response
			getRes.didStructure?.let {
				if ("urn:oid:1.3.162.15480.3.0.9" == it.didMarker.protocol) {
					return didName
				}
			}
		}
		throw createException(
			makeResultError(ECardConstants.Minor.SAL.INAPPROPRIATE_PROTOCOL_FOR_ACTION, "No PIN DID found."),
		)
	}
}
