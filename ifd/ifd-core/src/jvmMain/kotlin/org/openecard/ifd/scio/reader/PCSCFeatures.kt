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

import org.openecard.common.util.ByteUtils
import java.util.Locale

/**
 *
 * @author Tobias Wich
 */
object PCSCFeatures {
	const val VERIFY_PIN_START: Int = 0x01
	const val VERIFY_PIN_FINISH: Int = 0x02
	const val MODIFY_PIN_START: Int = 0x03
	const val MODIFY_PIN_FINISH: Int = 0x04
	const val GET_KEY_PRESSED: Int = 0x05
	const val VERIFY_PIN_DIRECT: Int = 0x06
	const val MODIFY_PIN_DIRECT: Int = 0x07
	const val MCT_READER_DIRECT: Int = 0x08
	const val MCT_UNIVERSAL: Int = 0x09
	const val IFD_PIN_PROPERTIES: Int = 0x0A
	const val ABORT: Int = 0x0B
	const val SET_SPE_MESSAGE: Int = 0x0C
	const val VERIFY_PIN_DIRECT_APP_ID: Int = 0x0D
	const val MODIFY_PIN_DIRECT_APP_ID: Int = 0x0E
	const val WRITE_DISPLAY: Int = 0x0F
	const val GET_KEY: Int = 0x10
	const val IFD_DISPLAY_PROPERTIES: Int = 0x11
	const val GET_TLV_PROPERTIES: Int = 0x12
	const val CCID_ESC_COMMAND: Int = 0x13
	const val EXECUTE_PACE: Int = 0x20

	fun getFeatureRequestCtlCode(): Int = PCSCFeatures.scardCtlCode(3400)

	private fun scardCtlCode(code: Int): Int =
		if (PCSCFeatures.isWindows) {
			(0x31 shl 16 or (code shl 2))
		} else {
			0x42000000 + code
		}

	private val isWindows: Boolean
		get() {
			val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
			return (osName.contains("windows"))
		}

	fun featureMapFromRequest(featureResponse: ByteArray): Map<Int, Int> {
		val result = mutableMapOf<Int, Int>()

		if ((featureResponse.size % 6) == 0) {
			var i = 0
			while (i < featureResponse.size) {
				val nextChunk = featureResponse.copyOfRange(i, i + 6)
				if (nextChunk.size == 6 && nextChunk[1].toInt() == 4) {
					val tag = nextChunk[0]
					val codeData = nextChunk.copyOfRange(2, 6)
					val code = ByteUtils.toInteger(codeData)
					result.put(tag.toInt(), code)
				}
				i += 6
			}
		}

		return result
	}
}
