/****************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
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

import org.openecard.common.ifd.scio.NoSuchTerminal
import org.openecard.common.ifd.scio.SCIOException
import org.openecard.common.ifd.scio.SCIOTerminal
import org.openecard.common.ifd.scio.SCIOTerminals
import org.openecard.common.ifd.scio.TerminalWatcher

/**
 * NFC implementation of smartcardio's CardTerminals interface.
 *
 * @author Dirk Petrautzki
 * @author Daniel Nemmert
 * @author Mike Prechtl
 */
class NFCCardTerminals(
	private val nfcTerminal: NFCCardTerminal<*>,
) : SCIOTerminals {
	@Throws(SCIOException::class)
	override fun prepareDevices(): Boolean = this.nfcTerminal.prepareDevices()

	override fun powerDownDevices(): Boolean = this.nfcTerminal.powerDownDevices()

	@Throws(SCIOException::class)
	override fun list(state: SCIOTerminals.State): List<SCIOTerminal> {
		when (state) {
			SCIOTerminals.State.ALL -> return listOf(this.nfcTerminal)
			SCIOTerminals.State.CARD_ABSENT ->
				if (!nfcTerminal.isCardPresent) {
					return listOf(this.nfcTerminal)
				}

			SCIOTerminals.State.CARD_PRESENT ->
				if (nfcTerminal.isCardPresent) {
					return listOf(this.nfcTerminal)
				}
		}
		return listOf()
	}

	@Throws(SCIOException::class)
	override fun list(): List<SCIOTerminal> = list(SCIOTerminals.State.ALL)

	@Throws(NoSuchTerminal::class)
	override fun getTerminal(name: String): SCIOTerminal {
		if (nfcTerminal.name == name) {
			return this.nfcTerminal
		}
		val errorMsg = String.format("There is no terminal with the name '%s' available.", name)
		throw NoSuchTerminal(errorMsg)
	}

	@get:Throws(SCIOException::class)
	override val watcher: TerminalWatcher
		get() = NFCCardWatcher(this, nfcTerminal)
}
