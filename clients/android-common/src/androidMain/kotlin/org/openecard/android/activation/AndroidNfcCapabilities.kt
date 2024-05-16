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

package org.openecard.android.activation

import android.content.Context
import org.openecard.android.utils.NfcCapabilityHelper
import org.openecard.mobile.activation.NFCCapabilities
import org.openecard.mobile.activation.NfcCapabilityResult


/**
 *
 * @author Neil Crossley
 */
class AndroidNfcCapabilities internal constructor(capabilityHelper: NfcCapabilityHelper<Context?>) : NFCCapabilities {

	private val capabilityHelper: NfcCapabilityHelper<Context?> = capabilityHelper

	override fun isAvailable(): Boolean {
		return capabilityHelper.isNFCAvailable
	}

	override fun isEnabled(): Boolean {
		return capabilityHelper.isNFCEnabled
	}

	override fun checkExtendedLength(): NfcCapabilityResult {
		return capabilityHelper.checkExtendedLength()
	}

	companion object {
		fun create(context: Context?): AndroidNfcCapabilities {
			return AndroidNfcCapabilities(NfcCapabilityHelper.create(context))
		}
	}
}
