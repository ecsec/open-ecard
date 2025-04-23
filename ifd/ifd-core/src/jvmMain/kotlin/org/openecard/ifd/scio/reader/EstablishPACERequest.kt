/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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
package org.openecard.ifd.scio.reader

import org.openecard.common.ifd.PACECapabilities
import org.openecard.common.util.ShortUtils
import java.io.ByteArrayOutputStream

/**
 *
 * @author Tobias Wich
 */
class EstablishPACERequest(
	private val passwordType: Byte,
	chat: ByteArray?,
	password: ByteArray?,
	certDesc: ByteArray?,
) {
	private var chatLength: Byte = 0
	private var chat: ByteArray? = null
	private var passwordLength: Byte = 0
	private var password: ByteArray? = null
	private var certDescLength: Short = 0
	private var certDesc: ByteArray? = null

	init {
		if (chat != null) {
			this.chatLength = chat.size.toByte()
			this.chat = chat
		}
		if (password != null) {
			this.passwordLength = password.size.toByte()
			this.password = password
		}
		if (certDesc != null) {
			this.certDescLength = certDesc.size.toShort()
			this.certDesc = certDesc
		}
	}

	fun isSupportedType(capabilities: List<PACECapabilities.PACECapability>): Boolean {
		// perform sanity check of the request according to BSI-TR-03119_V1
		// Für eine Durchführung von PACE in der Rolle
		// + eines nicht-authentisierten Terminals (Capability PACE) ist nur die Position 1 vorhanden
		// + in der Rolle Authentisierungsterminal (Capability eID) sind alle Positionen anzugeben
		// + in der Rolle Signaturterminal (Capability QES) sind die Positionen 1-3 und ggfs. 4-5 (für
		//     Passwort CAN, sofern dieses nicht am Leser eingegeben wird) anzugeben.
		return if (chat == null && certDesc == null) {
			capabilities.contains(PACECapabilities.PACECapability.GenericPACE)
		} else if (chat != null) {
			if (certDesc != null) {
				capabilities.contains(PACECapabilities.PACECapability.GermanEID)
			} else {
				capabilities.contains(PACECapabilities.PACECapability.QES)
			}
		} else {
			false
		}
	}

	fun toBytes(): ByteArray {
		val o = ByteArrayOutputStream()
		o.write(passwordType.toInt())
		// the following elements are only present if PACE is followed by TA v2
		if (chatLength > 0) {
			o.write(chatLength.toInt())
			if (chatLength > 0) {
				o.write(chat!!, 0, chat!!.size)
			}
			o.write(passwordLength.toInt())
			if (passwordLength > 0) {
				o.write(password!!, 0, password!!.size)
			}
			// optional application specific data (only certs possible at the moment)
			if (certDescLength > 0) {
				// write data length
				val dataLengthBytes = ShortUtils.toByteArray(certDescLength)
				for (i in dataLengthBytes.indices.reversed()) {
					o.write(dataLengthBytes[i].toInt())
				}
				// write missing bytes to length field
				for (i in dataLengthBytes.size..1) {
					o.write(0)
				}
				// write data
				o.write(certDesc!!, 0, certDesc!!.size)
			}
		} else {
			// PACE is not followed by TA v2, e.g. PIN Management
			// fill up to a total length of five
			o.write(0x00)
			o.write(0x00)
			o.write(0x00)
			o.write(0x00)
		}

		return o.toByteArray()
	}
}
