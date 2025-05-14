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
 */
package org.openecard.common.tlv.iso7816

import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLV.Companion.fromBER
import java.io.ByteArrayOutputStream

/**
 *
 * @author Tobias Wich
 */
object ReadHelper {
	fun readCIAFile(name: String): TLV {
		val path = "/df.cia/$name"
		val ins = ReadHelper::class.java.getResourceAsStream(path)
		val outs = ByteArrayOutputStream(ins!!.available())

		var next: Int
		while ((ins.read().also { next = it }) != -1) {
			outs.write(next.toByte().toInt())
		}

		val resultBytes = outs.toByteArray()
		val result = fromBER(resultBytes)
		return result
	}
}
