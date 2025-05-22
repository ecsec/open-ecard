/****************************************************************************
 * Copyright (C) 2012-2025 ecsec GmbH.
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

package org.openecard.sc.tlv

import org.openecard.utils.common.toSparseUByteArray
import org.openecard.utils.common.toUByteArray
import org.openecard.utils.common.toUShort
import org.openecard.utils.serialization.PrintableUByteArray
import org.openecard.utils.serialization.toPrintable

/**
 * Internal class representing one entry with a TLV definition.
 *
 * @author Tobias Wich
 */
data class TagLengthValue
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val tag: Tag,
		var value: PrintableUByteArray,
	) {
		@OptIn(ExperimentalUnsignedTypes::class)
		val valueLength: Int by lazy { value.v.size }

		@OptIn(ExperimentalUnsignedTypes::class)
		fun toBer(): UByteArray =
			buildList<UByte> {
				addAll(tag.toBer())

				// calculate length according to input data
				val len = valueLength
				if (len <= 127) {
					// short form
					add(len.toUByte())
				} else {
					val lenBytes = len.toULong().toSparseUByteArray()
					val lenHeader = (0x80 or lenBytes.size).toUByte()
					add(lenHeader)
					addAll(lenBytes)
				}

				// write actual data
				addAll(value.v)
			}.toUByteArray()

		@OptIn(ExperimentalUnsignedTypes::class)
		fun toCompact(): UByteArray =
			buildList {
				add(tag.toCompact(valueLength))
				addAll(value.v)
			}.toUByteArray()

		@OptIn(ExperimentalUnsignedTypes::class)
		fun toSimple(): UByteArray =
			buildList {
				add(tag.toSimple())
				if (valueLength >= 0xFF) {
					add(0xFFu)
					addAll(valueLength.toUShort().toUByteArray())
				} else {
					add(valueLength.toUByte())
				}
				addAll(value.v)
			}.toUByteArray()

		companion object {
			@Throws(TlvException::class)
			@OptIn(ExperimentalUnsignedTypes::class)
			fun fromBer(data: UByteArray): ParsedTagLengthValue {
				var (tag, numOctets) = Tag.fromBer(data)

				// get length
				var dataLength = 0
				var endOfLine = false
				if (((data[numOctets].toInt() shr 7) and 0x01) == 0) {
					// short form
					dataLength = data[numOctets].toInt()
					numOctets++
				} else {
					// has end-of-line octets
					if ((data[numOctets].toInt() and 0x7F) == 0x00) {
						endOfLine = true
						numOctets++
						// loop through content to find termination point
						var i = 0
						var endFound = false
						var zeroFound = false
						do {
							if (data.size <= numOctets + i) {
								throw TlvException("Not enough bytes in input to read TLV length.")
							}
							val next = data[numOctets + i]
							if (next.toInt() == 0x00) {
								if (zeroFound) {
									endFound = true
								} else {
									zeroFound = true
								}
							} else {
								zeroFound = false
							}
							i++
						} while (!endFound)
						// calculate data length
						dataLength = i - 2
					} else {
						// long form
						// first byte indicates number of length bytes
						val numLengthBytes = data[numOctets].toInt() and 0x7F
						numOctets++

						var i = 0
						while (i < numLengthBytes) {
							if (i * 8 > 32) {
								throw TlvException("Length doesn't fit into a 32 bit word.")
							} else if (data.size < numOctets + i + 1) {
								throw TlvException("Not enough bytes in input to read TLV length.")
							}
							dataLength = (dataLength shl 8) or data[numOctets + i].toInt()
							i++
						}
						numOctets += i
					}
				}

				val dataField: UByteArray
				val rest: UByteArray
				try {
					// extract data based on calculated length
					dataField = data.copyOfRange(numOctets, numOctets + dataLength)
					rest = data.copyOfRange(numOctets + dataLength, data.size)
				} catch (ex: IndexOutOfBoundsException) {
					throw TlvException("Data length and claimed length do not match.", ex)
				}

				// recalculate total length of datablock
				numOctets = numOctets + dataLength
				if (endOfLine) {
					numOctets += 2
				}

				// we have all values, build Tag object and return
				val result = TagLengthValue(tag, dataField.toPrintable())
				return ParsedTagLengthValue(result, numOctets, rest.toPrintable())
			}

			@Throws(TlvException::class)
			@OptIn(ExperimentalUnsignedTypes::class)
			fun fromCompact(data: UByteArray): ParsedTagLengthValue {
				val num = data[0].toUInt() shr 4
				val len = (data[0] and 0x0Fu).toInt()
				val tag = Tag.fromBer(ubyteArrayOf((0x40u or num).toUByte()))
				val tlv = TagLengthValue(tag.tag, data.sliceArray(1 until 1 + len).toPrintable())
				val rest = data.sliceArray(1 + len until data.size)
				return ParsedTagLengthValue(tlv, 1 + len, rest.toPrintable())
			}

			@OptIn(ExperimentalUnsignedTypes::class)
			fun fromSimple(data: UByteArray): ParsedTagLengthValue {
				val tag = Tag.fromSimple(data)
				val l1 = data[1]
				val (len, offset) =
					if (l1 == 0xFFu.toUByte()) {
						data.toUShort(2).toInt() to 4
					} else {
						l1.toInt() to 2
					}
				val tlv = TagLengthValue(tag.tag, data.sliceArray(offset until offset + len).toPrintable())
				val rest = data.sliceArray(offset + len until data.size)
				return ParsedTagLengthValue(tlv, offset + len, rest.toPrintable())
			}
		}
	}

data class ParsedTagLengthValue
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val tagLengthValue: TagLengthValue,
		val numOctets: Int,
		val rest: PrintableUByteArray,
	)
