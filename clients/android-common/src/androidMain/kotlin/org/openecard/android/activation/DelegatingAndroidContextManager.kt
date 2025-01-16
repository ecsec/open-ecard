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

package org.openecard.android.activation

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import java.io.IOException
import org.openecard.mobile.activation.ContextManager
import org.openecard.mobile.activation.StartServiceHandler
import org.openecard.mobile.activation.StopServiceHandler
import org.openecard.mobile.ex.ApduExtLengthNotSupported
import org.openecard.mobile.ex.NFCTagNotSupported
import org.openecard.mobile.ex.NfcDisabled
import org.openecard.mobile.ex.NfcUnavailable
import org.openecard.mobile.ex.UnableToInitialize
import org.openecard.scio.AndroidNFCFactory
import org.openecard.scio.CachingTerminalFactoryBuilder


/**
 *
 * @author Neil Crossley
 */
class DelegatingAndroidContextManager(
	private val contextManager: ContextManager,
	builder: CachingTerminalFactoryBuilder<AndroidNFCFactory?>,
) : AndroidContextManager {

	private val builder: CachingTerminalFactoryBuilder<AndroidNFCFactory?> = builder

	@Throws(ApduExtLengthNotSupported::class, NFCTagNotSupported::class, IOException::class)
	override fun onNewIntent(intent: Intent?) {
		val tagFromIntent: Tag? = intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG)
		if (tagFromIntent != null) {
			this.onNewIntent(tagFromIntent)
		}
	}

	@Throws(ApduExtLengthNotSupported::class, NFCTagNotSupported::class, IOException::class)
	override fun onNewIntent(intent: Tag?) {
		val nfcFactory: AndroidNFCFactory? = builder.getPreviousInstance()
		if (nfcFactory != null && intent != null) {
			val isoDep: IsoDep = IsoDep.get(intent)
			if (isoDep != null) {
				if (isoDep.isExtendedLengthApduSupported()) {
					// set nfc tag with timeout of five seconds
					nfcFactory.setNFCTag(isoDep, 5000)
				} else {
					throw ApduExtLengthNotSupported("APDU Extended Length is not supported.")
				}
			} else {
				throw NFCTagNotSupported("The tag is not supported")
			}
		}
	}

	override fun initializeContext(handler: StartServiceHandler) {
		contextManager.initializeContext(handler)
	}

	override fun terminateContext(handler: StopServiceHandler) {
		contextManager.terminateContext(handler)
	}
}
