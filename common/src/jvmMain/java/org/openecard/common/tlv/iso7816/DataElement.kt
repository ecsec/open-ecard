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

import org.openecard.common.util.ByteUtils.toInteger
import java.util.Arrays

/**
 * See ISO/IEC 7816-4 p.20 tab. 12, Tag 82
 *
 * @author Tobias Wich
 */
class DataElement(
	data: ByteArray,
) {
	val fileDescriptorByte: FileDescriptorByte = FileDescriptorByte(data[0])
	var dataCodingByte: DataCodingByte? = null
	var maxRecordSize: Int = 0
	var numRecords: Int = 0

	init {
		if (data.size >= 2) {
			dataCodingByte = DataCodingByte(data[1])
		} else {
			dataCodingByte = null
		}
		if (data.size >= 3) {
			maxRecordSize = toInteger(Arrays.copyOfRange(data, 2, 4))
			if (data.size > 4) {
				numRecords = toInteger(Arrays.copyOfRange(data, 4, 6))
			} else {
				numRecords = -1
			}
		} else {
			maxRecordSize = -1
			numRecords = -1
		}
	}

	fun hasDataCodingByte(): Boolean = dataCodingByte != null

	fun toString(prefix: String): String {
		val b = StringBuilder(1024)
		b.append(prefix)
		b.append("DataElement:\n")
		b.append(fileDescriptorByte.toString("$prefix "))
		b.append("\n")
		if (dataCodingByte != null) {
			b.append(dataCodingByte!!.toString("$prefix "))
			b.append("\n")
		}
		b.append(prefix)
		b.append(" ")
		b.append("max-record-size=")
		b.append(maxRecordSize)
		b.append(" num-records=")
		b.append(numRecords)

		return b.toString()
	}

	override fun toString(): String = toString("")
}
