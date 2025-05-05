/****************************************************************************
 * Copyright (C) 2012-2019 HS Coburg.
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

package org.openecard.addons.status

import iso.std.iso_iec._24727.tech.schema.CardInfoType
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import org.openecard.addon.Context
import org.openecard.addon.EventHandler
import org.openecard.addon.bind.BindingResult
import org.openecard.common.AppVersion
import org.openecard.common.ECardConstants
import org.openecard.ws.marshal.WSMarshallerException
import org.openecard.ws.schema.Status
import org.openecard.ws.schema.StatusType
import java.math.BigInteger
import javax.annotation.Nonnull

/**
 * Handles the status request.
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
class StatusHandler(
	ctx: Context,
) {
	private val eventHandler: EventHandler = ctx.eventHandler
	private val rec = ctx.recognition
	private val salStateView = ctx.salStateView

	private val protocols =
		ctx.manager.registry
			.listAddons()
			.flatMap {
				it.salActions.map { proto ->
					proto.uri
				}
			}

	/**
	 * Handles a Status-Request by returning a status message describing the capabilities if the App.
	 *
	 * @param statusRequest Status Request possibly containing a session identifier for event registration.
	 * @return Status message.
	 * @throws WSMarshallerException
	 */
	@Throws(WSMarshallerException::class)
	fun handleRequest(statusRequest: StatusRequest): BindingResult {
		val status = Status()

		// user agent
		status.userAgent =
			StatusType.UserAgent().apply {
				name = AppVersion.name
				versionMajor = BigInteger.valueOf(AppVersion.major.toLong())
				versionMinor = BigInteger.valueOf(AppVersion.minor.toLong())
				versionSubminor = BigInteger.valueOf(AppVersion.patch.toLong())
			}

		// API versions
		status.supportedAPIVersions.add(
			StatusType.SupportedAPIVersions().apply {
				name = "http://www.bsi.bund.de/ecard/api"
				versionMajor = ECardConstants.ECARD_API_VERSION_MAJOR
				versionMinor = ECardConstants.ECARD_API_VERSION_MINOR
				versionSubminor = ECardConstants.ECARD_API_VERSION_SUBMINOR
			},
		)
		// supported cards
		val cifs: MutableList<CardInfoType> = rec.cardInfos.toMutableList()
		status.supportedCards.addAll(
			getSupportedCards(protocols, cifs),
		)

		// supported DID protocols
		status.supportedDIDProtocols.addAll(protocols)

		// TODO: additional features

		// add available cards
		status.connectionHandle.addAll(cardHandles)

		// register session for wait for change
		if (statusRequest.hasSessionIdentifier) {
			val sessionIdentifier = statusRequest.sessionIdentifier
			eventHandler.addQueue(sessionIdentifier)
		}

		return StatusResponseBodyFactory().createStatusResponse(status)
	}

	private val cardHandles: MutableList<ConnectionHandleType>
		// TODO: reimplement according to redesign.
		// TODO: verify done

		// 	ConnectionHandleType handle = new ConnectionHandleType();
		// 	Set<CardStateEntry> entries = cardStates.getMatchingEntries(handle, false);
		//
		// 	ArrayList<ConnectionHandleType> result = new ArrayList<>(entries.size());
		// 	for (CardStateEntry entry : entries) {
		// 	    result.add(entry.handleCopy());
		// 	}
		//
		// 	return result;
		get() = this.salStateView.listCardHandles()

	companion object {
		@Nonnull
		private fun getSupportedCards(
			protocols: List<String>,
			cifs: List<CardInfoType>,
		): List<StatusType.SupportedCards> =
			cifs.map { cif ->
				StatusType.SupportedCards().apply {
					cardType = cif.cardType.objectIdentifier
					didProtocols.addAll(
						cif.applicationCapabilities.cardApplication.flatMap { cardApplication ->
							cardApplication
								.getDIDInfo()
								.map { it.differentialIdentity.didProtocol }
								.filter { protocols.contains(it) }
								.filter { !didProtocols.contains(it) }
						},
					)
				}
			}
	}
}
