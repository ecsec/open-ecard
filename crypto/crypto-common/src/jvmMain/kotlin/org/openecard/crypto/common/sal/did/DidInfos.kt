/****************************************************************************
 * Copyright (C) 2016-2018 ecsec GmbH.
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
package org.openecard.crypto.common.sal.did

import iso.std.iso_iec._24727.tech.schema.*
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.util.ByteComparator
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.HandlerUtils
import java.util.*

/**
 *
 * @author Tobias Wich
 */
class DidInfos(
	val dispatcher: Dispatcher,
	pin: CharArray?,
	handle: ConnectionHandleType,
) {
	private var pin: CharArray?

	private val handle: ConnectionHandleType

	private val _applications: List<ByteArray> by lazy {
		val req = CardApplicationList()
		req.setConnectionHandle(getHandle())

		val res = dispatcher.safeDeliver(req) as CardApplicationListResponse
		checkResult<CardApplicationListResponse>(res)
			.cardApplicationNameList
			?.cardApplicationName ?: emptyList()
	}

	val didNames: Map<ByteArray, MutableList<String>> by lazy {
		val tmpDidNames = TreeMap<ByteArray, MutableList<String>>(ByteComparator())
		// check out all applications
		for (application in this._applications) {
			try {
				val req = DIDList()
				req.setConnectionHandle(getHandle())
				val filter = DIDQualifierType()
				filter.setApplicationIdentifier(application)
				req.setFilter(filter)

				val res = dispatcher.safeDeliver(req) as DIDListResponse
				checkResult<DIDListResponse>(
					res,
				)

				if (res.getDIDNameList() != null) {
					tmpDidNames.put(
						application,
						res.getDIDNameList().getDIDName(),
					)
				}
			} catch (_: WSHelper.WSException) {
				// skip this application
			}
		}

		tmpDidNames
	}

	private val cachedDids: MutableMap<ByteArray, MutableMap<String, DidInfo>> = TreeMap(ByteComparator())
	private val cachedDataSets: MutableMap<ByteArray, MutableMap<String, DataSetInfo>> = TreeMap(ByteComparator())

	init {
		if (pin != null) {
			this.pin = pin.clone()
		} else {
			this.pin = null
		}

		this.handle = HandlerUtils.copyHandle(handle)
	}

	fun getDidCache(application: ByteArray): MutableMap<String, DidInfo> {
		var applicationCache = cachedDids[application]
		return if (applicationCache == null) {
			applicationCache = HashMap()
			cachedDids.put(application, applicationCache)
			applicationCache
		} else {
			applicationCache
		}
	}

	fun getDataSetCache(application: ByteArray): MutableMap<String, DataSetInfo> {
		var applicationCache = cachedDataSets.get(application)
		return if (applicationCache == null) {
			applicationCache = HashMap()
			cachedDataSets.put(application, applicationCache)
			applicationCache
		} else {
			applicationCache
		}
	}

	fun getHandle(): ConnectionHandleType = HandlerUtils.copyHandle(handle)

	fun getHandle(application: ByteArray?): ConnectionHandleType {
		val newHandle = HandlerUtils.copyHandle(handle)
		newHandle.setCardApplication(ByteUtils.clone(application))
		return newHandle
	}

	fun setPin(pin: CharArray?) {
		if (pin != null) {
			this.pin?.fill(' ')
		}
		this.pin = pin?.clone()
	}

	@Throws(WSHelper.WSException::class)
	fun getApplications(): List<ByteArray> {
		val result = ArrayList<ByteArray>()
		for (next in this._applications) {
			result.add(ByteUtils.clone(next))
		}
		return result.toList()
	}

	@Throws(WSHelper.WSException::class)
	fun getDidNames(application: ByteArray): List<String> {
		val result = this.didNames[application]
		return result ?: listOf<String>()
	}

	@Throws(WSHelper.WSException::class, NoSuchDid::class)
	fun getDidInfos(application: ByteArray): List<DidInfo> =
		getDidNames(application).map {
			getDidInfo(application, it)
		}

	@get:Throws(WSHelper.WSException::class, NoSuchDid::class)
	val didInfos: MutableList<DidInfo>
		get() {
			val result = ArrayList<DidInfo>()
			val didNames = HashSet<String>()

			// walk over all didnames and filter out duplicates
			for (entry in this.didNames.entries) {
				for (didName in entry.value) {
					if (!didNames.contains(didName)) {
						val didInfo = getDidInfo(entry.key, didName)
						didNames.add(didName)
						result.add(didInfo)
					}
				}
			}

			return result
		}

	val cryptoDidInfos: List<DidInfo> by lazy { didInfos.filter { it.isCryptoDid } }

	@Throws(NoSuchDid::class, WSHelper.WSException::class)
	fun getDidInfo(name: String): DidInfo {
		for (application in getApplications()) {
			try {
				return getDidInfo(application, name)
			} catch (_: NoSuchDid) {
				// try again
			}
		}
		throw NoSuchDid("The DID $name does not exist.")
	}

	@Throws(NoSuchDid::class, WSHelper.WSException::class)
	fun getDidInfo(
		application: ByteArray,
		name: String,
	): DidInfo {
		val appCache = getDidCache(application)
		var result = appCache[name]

		// in case there is no cached info object, try to find one
		if (result == null) {
			val names = getDidNames(application)
			for (next in names) {
				if (next == name) {
					result = DidInfo(this, application, name, pin)
					appCache.put(name, result)
					return result
				}
			}
			throw NoSuchDid("The DID $name does not exist.")
		}

		result.setPin(pin)
		return result
	}

	@Throws(WSHelper.WSException::class)
	fun getDataSetNames(application: ByteArray): List<String> {
		val req = DataSetList()
		req.setConnectionHandle(getHandle(application))

		val res = dispatcher.safeDeliver(req) as DataSetListResponse
		checkResult<DataSetListResponse>(res)

		val listWrapper = res.getDataSetNameList()
		val datasetNames = listWrapper?.dataSetName ?: listOf()

		return datasetNames
	}

	@Throws(NoSuchDataSet::class, WSHelper.WSException::class)
	fun getDataSetInfo(
		application: ByteArray,
		name: String,
	): DataSetInfo {
		val appCache = getDataSetCache(application)
		var result = appCache[name]

		// in case there is no cached info object, try to find one
		if (result == null) {
			val names = getDataSetNames(application)
			for (next in names) {
				if (name == next) {
					result = DataSetInfo(this, application, name)
					appCache.put(name, result)
					return result
				}
			}
			throw NoSuchDataSet("The DataSet $name does not exist.")
		}

		return result
	}

	@Throws(NoSuchDataSet::class, WSHelper.WSException::class)
	fun getDataSetInfo(name: String): DataSetInfo {
		var finalResult: DataSetInfo? = null

		for (application in getApplications()) {
			val appCache = getDataSetCache(application)
			var result = appCache[name]

			// in case there is no cached info object, try to find one
			if (result == null) {
				val names = getDataSetNames(application)
				for (next in names) {
					if (name == next) {
						result = DataSetInfo(this, application, name)
						appCache.put(name, result)
						finalResult = result
					}
				}
			} else {
				finalResult = result
			}
		}
		if (finalResult == null) {
			throw NoSuchDataSet("The DataSet $name does not exist.")
		}
		return finalResult
	}

	@Throws(WSHelper.WSException::class)
	fun connectApplication(application: ByteArray) {
		val req = CardApplicationSelect()
		req.setCardApplication(application)
		req.setSlotHandle(handle.getSlotHandle())

		val res = dispatcher.safeDeliver(req) as CardApplicationSelectResponse
		checkResult<CardApplicationSelectResponse>(res)
	}

	fun clearPin(slotHandle: ByteArray) {
		setPin(null)
		for (e1 in cachedDids.entries) {
			if (ByteUtils.compare(slotHandle, e1.key)) {
				for (e2 in e1.value.entries) {
					e2.value.setPin(null)
				}
			}
		}
	}
}
