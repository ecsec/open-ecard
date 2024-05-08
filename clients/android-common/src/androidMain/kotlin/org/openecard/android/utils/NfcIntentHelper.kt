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
 */

package org.openecard.android.utils

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.provider.Settings
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * Provides methods to enable/disable the nfc dispatch or to jump to the nfc settings.
 *
 * @author Mike Prechtl
 * @author Neil Crossley
 */
class NfcIntentHelper(innerHelper: NfcCapabilityHelper<Activity>) {

	private val capabilityHelper: NfcCapabilityHelper<Activity> = innerHelper

	/**
	 * This method opens the nfc settings on the corresponding device where the user can enable nfc.
	 */
	fun goToNFCSettings() {
		val intent: Intent = Intent(Settings.ACTION_NFC_SETTINGS)
		capabilityHelper.context.startActivityForResult(intent, 0)
	}

	fun enableNFCDispatch() {
		if (capabilityHelper.isNFCEnabled) {
			LOG.debug("Enable NFC foreground dispatch...")
			val activity: Activity = capabilityHelper.context
			val activityIntent: Intent =
				Intent(activity, activity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
			val flags = if (android.os.Build.VERSION.SDK_INT >= 31) android.app.PendingIntent.FLAG_MUTABLE else 0
			val pendingIntent: PendingIntent = PendingIntent.getActivity(activity, 0, activityIntent, flags)
			// enable dispatch of messages with nfc tag
			capabilityHelper.getNfcAdapter()?.enableForegroundDispatch(activity, pendingIntent, null, null)
		}
	}

	fun disableNFCDispatch() {
		if (capabilityHelper.isNFCEnabled) {
			LOG.debug("Disable NFC foreground dispatch...")
			// disable dispatch of messages with nfc tag
			capabilityHelper.getNfcAdapter()?.disableForegroundDispatch(capabilityHelper.context)
		}
	}

	companion object {
		var LOG: Logger = LoggerFactory.getLogger(NfcIntentHelper::class.java)

		fun create(activity: Activity): NfcIntentHelper {
			requireNotNull(activity) { "activity cannot be null" }

			val innerHelper: NfcCapabilityHelper<Activity> = NfcCapabilityHelper.create<Activity>(activity)

			return NfcIntentHelper(innerHelper)
		}
	}
}
