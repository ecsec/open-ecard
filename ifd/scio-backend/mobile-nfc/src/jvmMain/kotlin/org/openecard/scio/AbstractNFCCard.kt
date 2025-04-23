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

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.ifd.scio.SCIOATR
import org.openecard.common.ifd.scio.SCIOCard
import org.openecard.common.ifd.scio.SCIOChannel
import org.openecard.common.ifd.scio.SCIOErrorCode
import org.openecard.common.ifd.scio.SCIOException
import org.openecard.common.ifd.scio.SCIOProtocol
import java.io.IOException

private val LOG = KotlinLogging.logger { }

/**
 * NFC implementation of SCIO API card interface.
 *
 * @author Dirk Petrautzki
 */
abstract class AbstractNFCCard(
	override val terminal: NFCCardTerminal<*>,
) : SCIOCard {
	protected val nfcCardChannel: NFCCardChannel = NFCCardChannel(this)

	abstract val isTagPresent: Boolean

	abstract fun tagWasPresent(): Boolean

	@Throws(SCIOException::class)
	abstract fun terminateTag(): Boolean

	@Throws(SCIOException::class)
	override fun beginExclusive() {
		LOG.warn { "beginExclusive not supported" }
	}

	@Throws(SCIOException::class)
	override fun endExclusive() {
		LOG.warn { "endExclusive not supported" }
	}

	@Throws(SCIOException::class)
	override fun disconnect(reset: Boolean) {
	}

	abstract override val aTR: SCIOATR

	override val basicChannel: SCIOChannel = this.nfcCardChannel

	// NFC is contactless
	override val protocol: SCIOProtocol = SCIOProtocol.TCL

	override val isContactless: Boolean = true

	@Throws(SCIOException::class)
	override fun openLogicalChannel(): SCIOChannel =
		throw SCIOException("Logical channels are not supported.", SCIOErrorCode.SCARD_E_UNSUPPORTED_FEATURE)

	@Throws(SCIOException::class)
	override fun transmitControlCommand(
		controlCode: Int,
		command: ByteArray,
	): ByteArray {
		if (controlCode == (0x42000000 + 3400)) {
			// GET_FEATURE_REQUEST_CTLCODE
			return ByteArray(0)
		} else {
			val msg = "Control command not supported."
			throw SCIOException(msg, SCIOErrorCode.SCARD_E_INVALID_PARAMETER)
		}
	}

	@Throws(IOException::class)
	abstract fun transceive(apdu: ByteArray): ByteArray

	open fun setDialogMsg(msg: String): Unit = throw UnsupportedOperationException("Not supported.")
}
