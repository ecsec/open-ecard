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
package org.openecard.addons.cg.impl

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.GetStatus
import iso.std.iso_iec._24727.tech.schema.GetStatusResponse
import org.openecard.addon.Context
import org.openecard.common.ECardConstants
import org.openecard.common.SecurityConditionUnsatisfiable
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.util.HandlerBuilder
import org.openecard.crypto.common.UnsupportedAlgorithmException
import org.openecard.crypto.common.sal.did.DidInfos
import org.openecard.crypto.common.sal.did.NoSuchDid
import org.openecard.ws.chipgateway.TokenInfoType
import java.util.TreeSet

private val LOG = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
class ListTokens(
	private val requestedTokens: MutableList<TokenInfoType>,
	private val ctx: Context,
	private val sessionId: String?,
) {
	private val dispatcher = ctx.dispatcher
	private val connectedSlots = mutableSetOf<ByteArray>()

	init {

		// if no filter is specified, add an empty filter
		if (this.requestedTokens.isEmpty()) {
			this.requestedTokens.add(TokenInfoType())
		}

		validateFilters()
	}

	fun getConnectedSlots() = connectedSlots.sortedWith(byteArComparator)

	@Throws(WSHelper.WSException::class)
	fun findTokens(): List<TokenInfoType> {
		val connected = connectCards()

		// save slots of connected cards
		connected.forEach { connectedSlots.add(it.slotHandle) }

		// convert handles to TokenInfo structure
		val allTokens = convertHandles(connected)
		// add unknown cards to the list
		allTokens.addAll(getUnknownCards(connected))

		// process handles for each requested filter
		val filteredLists = ArrayList<TokenInfoType>()
		for (filter in requestedTokens) {
			var filtered = filterTypes(filter, allTokens)
			filtered = filterTerminalFeatures(filter, filtered)
			filtered = filterTokenFeatures(filter, filtered)
			filtered = filterAlgorithms(filter, filtered)

			filteredLists.addAll(filtered)
		}

		val resultHandles = filteredLists.distinctBy { it.connectionHandle.slotHandle }
		return resultHandles
	}

	@Throws(WSHelper.WSException::class)
	private fun connectCards(): ArrayList<ConnectionHandleType> {
		// get all cards in the system
		val pathReq = CardApplicationPath().apply { cardAppPathRequest = CardApplicationPathType() }

		val pathRes = dispatcher.safeDeliver(pathReq) as CardApplicationPathResponse
		checkResult<CardApplicationPathResponse>(pathRes)

		// remove duplicates
		val paths =
			pathRes.cardAppPathResultSet.cardApplicationPathResult
				.distinctBy { "${it.ifdName}${it.slotIndex}" }

		// connect every card in the set
		val connectedCards = ArrayList<ConnectionHandleType>()
		for (path in paths) {
			try {
				val conReq =
					CardApplicationConnect().apply {
						cardApplicationPath = path
						isExclusiveUse = false
					}

				val conRes = dispatcher.safeDeliver(conReq) as CardApplicationConnectResponse
				checkResult(conRes)
				connectedCards.add(conRes.connectionHandle)
			} catch (ex: WSHelper.WSException) {
				LOG.error(ex) { "Failed to connect card, skipping this entry." }
			}
		}

		return connectedCards
	}

	private fun getUnknownCards(knownHandles: MutableList<ConnectionHandleType>): MutableList<TokenInfoType> {
		val result = ArrayList<TokenInfoType>()

		for (ifdCtx in ctx.ifdCtx) {
			try {
				// get all IFD names
				val gs = GetStatus().apply { contextHandle = ifdCtx }
				val gsr = dispatcher.safeDeliver(gs) as GetStatusResponse
				checkResult(gsr)

				for (istatus in gsr.ifdStatus) {
					for (sstatus in istatus.slotStatus) {
						// check if name is already in the list of known cards
						if (sstatus.isCardAvailable && !isInHandleList(istatus.ifdName, knownHandles)) {
							val ti = TokenInfoType()
							val conHandle =
								org.openecard.ws.chipgateway
									.ConnectionHandleType()
							conHandle.setCardType(ECardConstants.UNKNOWN_CARD)
							ti.connectionHandle = conHandle
							// add to handle list
							result.add(ti)
						}
					}
				}
			} catch (ex: WSHelper.WSException) {
				LOG.warn { "Failed to retrieve status info from IFD. Skipping unknown card entries." }
			}
		}

		return result
	}

	private fun isInHandleList(
		ifdName: String,
		handles: List<ConnectionHandleType>,
	) = handles.find { it.ifdName == ifdName } != null

	private fun convertHandles(handles: MutableList<ConnectionHandleType>): ArrayList<TokenInfoType> {
		val result = ArrayList<TokenInfoType>()
		for (next in handles) {
			val rec = next.recognitionInfo
			// create token type and copy available information about it
			val ti = TokenInfoType()
			val h =
				org.openecard.ws.chipgateway
					.ConnectionHandleType()
			h.slotHandle = next.slotHandle
			h.cardType = rec.cardType
			ti.setConnectionHandle(h)

			next.slotInfo?.let { ti.isHasProtectedAuthPath = it.isProtectedAuthPath }

			if (determineTokenFeatures(ti)) {
				// only add this token if there are no errors
				result.add(ti)
			}
		}

		return result
	}

	private fun determineTokenFeatures(next: TokenInfoType): Boolean {
		try {
			// request the missing information
			val h =
				HandlerBuilder
					.create()
					.setSlotHandle(next.getConnectionHandle().getSlotHandle())
					.setSessionId(sessionId)
					.buildConnectionHandle()

			val didInfos = DidInfos(dispatcher, null, h).didInfos

			var needsDidPin = false
			var needsCertPin = false
			val algorithms = TreeSet<String?>()

			// find out everything about the token
			for (didInfo in didInfos) {
				if (didInfo.isCryptoDid) {
					// only evaluate if we have no positive match yet
					if (!needsDidPin) {
						needsDidPin = didInfo.needsPin()
					}

					// only evaluate if we have no positive match yet
					if (!needsCertPin) {
						for (dataSetinfo in didInfo.relatedDataSets) {
							needsCertPin = needsCertPin || dataSetinfo.needsPin()
						}
					}

					// get the algorithm of the did
					val algInfo = didInfo.genericCryptoMarker.algorithmInfo
					val algId = algInfo!!.algorithmIdentifier
					val alg = algInfo.algorithm
					try {
						if (algId != null && algId.algorithm != null) {
							val jcaName = AllowedSignatureAlgorithms.algIdtoJcaName(algId.algorithm)
							algorithms.add(jcaName)
						}
					} catch (ex: UnsupportedAlgorithmException) {
						// ignore and fall back to Algorithm field
						if (alg != null && !alg.isEmpty() && AllowedSignatureAlgorithms.isKnownJcaAlgorithm(alg)) {
							algorithms.add(alg)
						}
					}
				}
			}
			next.isNeedsPinForCertAccess = needsCertPin
			next.isNeedsPinForPrivateKeyAccess = needsDidPin
			next.algorithm.addAll(algorithms)

			// finished evaluation everything successfully
			return true
		} catch (ex: NoSuchDid) {
			LOG.error(ex) { "Failed to evaluate DID." }
		} catch (ex: WSHelper.WSException) {
			LOG.error(ex) { "Failed to evaluate DID." }
		} catch (ex: SecurityConditionUnsatisfiable) {
			LOG.error(ex) { "Failed to evaluate DID." }
		}
		// there has been an error
		return false
	}

	private fun filterTypes(
		filter: TokenInfoType,
		tokens: List<TokenInfoType>,
	): List<TokenInfoType> {
		val type = filter.connectionHandle?.cardType
		return if (type == null) {
			tokens
		} else {
			tokens.filter { it.connectionHandle.cardType == type }
		}

		// add to result if it matches the desired type or if no type is given
	}

	private fun filterTerminalFeatures(
		filter: TokenInfoType,
		filtered: List<TokenInfoType>,
	): List<TokenInfoType> {
		val protectedAuthPath = filter.isHasProtectedAuthPath
		return if (protectedAuthPath == null) {
			filtered
		} else {
			filtered.filter { it.isHasProtectedAuthPath == protectedAuthPath }
		}
	}

	private fun filterTokenFeatures(
		filter: TokenInfoType,
		filtered: List<TokenInfoType>,
	): List<TokenInfoType> {
		val needsDidPinFilter = filter.isNeedsPinForPrivateKeyAccess
		val needsCertPinFilter = filter.isNeedsPinForCertAccess

		return if (needsDidPinFilter != null || needsCertPinFilter != null) {
			filtered.filter {
				needsDidPinFilter == it.isNeedsPinForPrivateKeyAccess ||
					needsCertPinFilter == it.isNeedsPinForCertAccess
			}
		} else {
			filtered
		}
	}

	private fun filterAlgorithms(
		filter: TokenInfoType,
		filtered: List<TokenInfoType>,
	): List<TokenInfoType> {
		val result = mutableListOf<TokenInfoType>()

		val refAlgs = filter.algorithm
		if (refAlgs.isNotEmpty()) {
			// loop over all elements to be filtered
			for (nextTokenInfo in filtered) {
				val tokenAlgs = nextTokenInfo.algorithm
				// check if the token has all algorithms contained in the reference list
				if (tokenAlgs.containsAll(refAlgs)) {
					result.add(nextTokenInfo)
				}
			}
		} else {
			result.addAll(filtered)
		}

		return result
	}

	@Throws(UnsupportedAlgorithmException::class)
	private fun validateFilters() {
		for (filter in requestedTokens) {
			// check algorithms
			val algIt = filter.algorithm.iterator()
			while (algIt.hasNext()) {
				val alg = algIt.next()
				if (!AllowedSignatureAlgorithms.isKnownJcaAlgorithm(alg)) {
					// remove invalid algortihm and check if there is anything left in the list
					algIt.remove()
					if (filter.getAlgorithm().isEmpty()) {
						val msg = String.format("Algorithm %s is not supported by the client.", alg)
						throw UnsupportedAlgorithmException(msg)
					}
				}
			}
		}
	}
}
