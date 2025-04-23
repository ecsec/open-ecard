/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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

package org.openecard.scio

import android.nfc.tech.IsoDep
import java.io.IOException

/**
 *
 * @author Neil Crossley
 */
class AndroidNFCCardTerminal : NFCCardTerminal<AndroidNFCCard>() {
	override fun prepareDevices(): Boolean {
		val card = AndroidNFCCard(this)
		return this.setNFCCard(card)
	}

	override val isCardPresent: Boolean
		get() {
			synchronized(cardLock) {
				val currentCard: AndroidNFCCard? = this.nFCCard
				return currentCard != null && currentCard.isTagPresent
			}
		}

	@Throws(IOException::class)
	fun setNFCTag(
		tag: IsoDep,
		timeout: Int,
	) {
		kotlin.synchronized(cardLock) {
			val card = nFCCard
			if (card == null) {
				throw IOException("The NFC stack was not initialized and cannot prematurely accept the NFC tag.")
			}
			card.setTag(tag, timeout)
		}
		this.notifyCardPresent()
	}
}
