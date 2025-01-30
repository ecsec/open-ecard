/****************************************************************************
 * Copyright (C) 2012-2017 HS Coburg.
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

import android.nfc.Tag
import android.nfc.tech.IsoDep
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.ifd.scio.SCIOTerminals
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException

private val LOG = KotlinLogging.logger { }

/**
 * NFC specific implementation of the TerminalFactory
 *
 * @author Dirk Petrautzki
 * @author Mike Prechtl
 */
class AndroidNFCFactory : org.openecard.common.ifd.scio.TerminalFactory {
	private val terminals: NFCCardTerminals
	private val terminal: AndroidNFCCardTerminal

	init {
		LOG.info { "Create new NFCFactory" }
		this.terminal = AndroidNFCCardTerminal()
		this.terminals = NFCCardTerminals(terminal)
	}


	override val type: String
		get() = ALGORITHM


	override fun terminals(): SCIOTerminals {
		return terminals
	}

	@Throws(IOException::class)
	fun setNFCTag(tag: Tag) {
		val isoTag: IsoDep = IsoDep.get(tag)

		val timeout: Int = isoTag.timeout
		terminal.setNFCTag(isoTag, timeout)
	}

	/**
	 * Set the nfc tag in the nfc card terminal.
	 *
	 * @param tag
	 * @param timeout current timeout for transceive(byte[]) in milliseconds.
	 */
	@Throws(IOException::class)
	fun setNFCTag(tag: IsoDep, timeout: Int) {
		tag.timeout = timeout
		terminal.setNFCTag(tag, timeout)
	}

}

private const val ALGORITHM = "AndroidNFC"
