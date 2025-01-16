/****************************************************************************
 * Copyright (C) 2017-2024 ecsec GmbH.
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

package org.openecard.android.utils

import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.tech.TagTechnology
import android.os.Build
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import org.openecard.mobile.activation.NfcCapabilityResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory


object NfcExtendedHelper {

	private val LOG: Logger = LoggerFactory.getLogger(NfcExtendedHelper::class.java)

	/**
	 * Checks the support of extended length APDUs of the system.
	 *
	 * This function uses non public API in order to work without a card insert intent. As this is forbidden since
	 * API 28, this function returns [NfcCapabilityResult.QUERY_NOT_ALLOWED] when the system runs on API level 28
	 * or higher.
	 *
	 * @param context
	 * @return Result of the extended length determination.
	 */
	fun checkExtendedLength(context: Context?): NfcCapabilityResult {
		if (canUseHiddenApi()) {
			val nfcAdapter: NfcAdapter = NfcAdapter.getDefaultAdapter(context)
			if (nfcAdapter != null) {
				val tagObj = getTagObject(nfcAdapter)
				if (tagObj != null) {
					val extSup = isExtendedLengthSupported(tagObj)
					val maxLen = getMaxTransceiveLength(tagObj)
					LOG.info("maxLen = {} ; extSup = {}", maxLen, extSup)
					if (extSup != null) {
						return if (extSup) NfcCapabilityResult.SUPPORTED else NfcCapabilityResult.NOT_SUPPORTED
					} else if (maxLen != null) {
						// This is roughly the size of the biggest APDU observed in EAC
						return if (maxLen > 370) NfcCapabilityResult.SUPPORTED else NfcCapabilityResult.NOT_SUPPORTED
					}
				}

				// not values determined, assume not supported for some other reason
				return NfcCapabilityResult.NOT_SUPPORTED
			} else {
				LOG.warn("NfcAdapter is not available.")
				return NfcCapabilityResult.NFC_SYSTEM_DISABLED
			}
		} else {
			return NfcCapabilityResult.QUERY_NOT_ALLOWED
		}
	}

	private fun canUseHiddenApi(): Boolean {
		// we can only request this API, when we are < Android 9 (API 28)
		return Build.VERSION.SDK_INT < 28
	}

	private fun getTagObject(nfcAdapter: NfcAdapter): Any? {
		try {
			val getTagFun: Method = nfcAdapter::class.java.getMethod("getTagService")
			val tagObj = getTagFun.invoke(nfcAdapter)
			return tagObj
		} catch (ex: NoSuchMethodException) {
			LOG.error("Error requesting TagService retrieval method.", ex)
		} catch (ex: SecurityException) {
			LOG.error("Requesting TagService object is not allowed.")
		} catch (ex: IllegalAccessException) {
			LOG.error("Requesting TagService object is not allowed.")
		} catch (ex: InvocationTargetException) {
			LOG.error("Error requesting TagService object.", ex)
		}

		return null
	}

	private fun getMaxTransceiveLength(tagObj: Any): Int? {
		var tech = 3 // taken from Android source and used as fallback if lookup fails
		try {
			val isoDep: Field = TagTechnology::class.java.getDeclaredField("ISO_DEP")
			tech = isoDep.getInt(null)
		} catch (ex: NoSuchFieldException) {
			LOG.error("Error requesting ISO_DEP field.", ex)
		} catch (ex: SecurityException) {
			LOG.error("Requesting ISO_DEP tech constant is not allowed.")
		} catch (ex: IllegalAccessException) {
			LOG.error("Requesting ISO_DEP tech constant is not allowed.")
		} catch (ex: NullPointerException) {
			LOG.error("Invalid parameters for requesting ISO_DEP tech constant.", ex)
		} catch (ex: IllegalArgumentException) {
			LOG.error("Invalid parameters for requesting ISO_DEP tech constant.", ex)
		}

		try {
			val tlenFun = tagObj.javaClass.getMethod("getMaxTransceiveLength", Int::class.javaPrimitiveType)
			val lenObj = tlenFun.invoke(tagObj, tech)
			LOG.debug("Transceive Length == {}", lenObj)
			if (lenObj is Int) {
				return lenObj
			}
		} catch (ex: NoSuchMethodException) {
			LOG.debug("Error requesting max transceive length retrieval method.", ex)
		} catch (ex: SecurityException) {
			LOG.debug("Requesting max transceive length is not allowed.")
		} catch (ex: IllegalAccessException) {
			LOG.debug("Requesting max transceive length is not allowed.")
		} catch (ex: InvocationTargetException) {
			LOG.debug("Error requesting max transceive length.", ex)
		}

		return null
	}

	private fun isExtendedLengthSupported(tagObj: Any): Boolean? {
		try {
			val extSupFun = tagObj.javaClass.getMethod("getExtendedLengthApdusSupported")
			val extSupObj = extSupFun.invoke(tagObj)
			LOG.debug("Extended Length Support == {}", extSupObj)
			if (extSupObj is Boolean) {
				return extSupObj
			}
		} catch (ex: NoSuchMethodException) {
			LOG.debug("Error requesting extended length support retrieval method.", ex)
		} catch (ex: SecurityException) {
			LOG.debug("Requesting extended length support is not allowed.")
		} catch (ex: IllegalAccessException) {
			LOG.debug("Requesting extended length support is not allowed.")
		} catch (ex: InvocationTargetException) {
			LOG.debug("Error requesting extended length support.", ex)
		}

		return null
	}
}
