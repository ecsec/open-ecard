/****************************************************************************
 * Copyright (C) 2019-2024 ecsec GmbH.
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
import android.nfc.NfcManager
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.mobile.activation.NfcCapabilityResult

private val LOG = KotlinLogging.logger { }

/**
 *
 * @author Neil Crossley
 */
class NfcCapabilityHelper<T : Context?> internal constructor(
	activity: T?,
	nfcAdapter: NfcAdapter?,
) {
	val context: T
	private val nfcAdapter: NfcAdapter?

	init {
		requireNotNull(activity) { "activity cannot be null" }
		this.context = activity
		this.nfcAdapter = nfcAdapter
	}

	fun getNfcAdapter(): NfcAdapter? = nfcAdapter

	val isNFCAvailable: Boolean
		/*
		 * Check if NFC is available on the corresponding device.
		 *
		 * @return true if nfc is available, otherwise false
		 */
		get() = nfcAdapter != null

	val isNFCEnabled: Boolean
		/**
		 * Proof if NFC is enabled on the corresponding device. If this method return `false` nfc should be activated
		 * in the device settings.
		 *
		 * @return true if nfc is enabled, otherwise false
		 */
		get() = nfcAdapter != null && nfcAdapter.isEnabled

	fun checkExtendedLength(): NfcCapabilityResult = NfcExtendedHelper.checkExtendedLength(context)

	companion object {
		fun <T : Context> create(activity: T): NfcCapabilityHelper<T> {
			requireNotNull(activity) { "activity cannot be null" }
			val nfcManager: NfcManager = activity.getSystemService(Context.NFC_SERVICE) as NfcManager
			val adapter: NfcAdapter = nfcManager.defaultAdapter

			return NfcCapabilityHelper(activity, adapter)
		}
	}
}
