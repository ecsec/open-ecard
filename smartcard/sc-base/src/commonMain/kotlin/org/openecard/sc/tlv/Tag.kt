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

import org.openecard.utils.common.enlargeToLong
import org.openecard.utils.common.toSparseUByteArray
import org.openecard.utils.common.toULong

/**
 *
 * @author Tobias Wich
 */
@OptIn(ExperimentalUnsignedTypes::class)
data class Tag(
	val tagClass: TagClass,
	val primitive: Boolean,
	val tagNum: ULong,
) {
	val tagNumWithClass: ULong by lazy { calculateTagNumWithClass() }

	@OptIn(ExperimentalUnsignedTypes::class)
	private fun calculateTagNumWithClass(): ULong {
		var leading = tagClass.num
		leading = ((leading.toInt() shl 1) or (if (this.primitive) 0 else 1)).toUByte()

		val rest: UByteArray
		if (this.tagNum >= 31u) {
			// long
			leading = ((leading.toUInt() shl 5) or 0x1Fu).toUByte()
			rest = this.tagNum.toSparseUByteArray(numBits = 7)
			for (i in 0..<rest.size - 1) {
				rest[i] = (rest[i].toUInt() or 0x80u).toUByte()
			}
		} else {
			// short
			leading = ((leading.toUInt() shl 5).toULong() or this.tagNum).toUByte()
			rest = ubyteArrayOf()
		}

		val resultBytes = ubyteArrayOf(leading) + rest
		return resultBytes.enlargeToLong().toULong(0)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	fun toBer(): UByteArray = tagNumWithClass.toSparseUByteArray()

	fun toCompact(valueLen: Int): UByte = ((tagNum.toInt() shl 4) or (valueLen and 0xF)).toUByte()

	fun toSimple(): UByte = tagNum.toUByte()

	@OptIn(ExperimentalStdlibApi::class)
	override fun toString(): String {
		val numClass = tagNumWithClass.toSparseUByteArray().toHexString()
		return "Tag(numClass=0x$numClass, class=$tagClass, primitive=$primitive, num=$tagNum)"
	}

	companion object {
		/**
		 * Builtin standard tags
		 */
		val BOOLEAN_TAG: Tag = Tag(TagClass.UNIVERSAL, true, 1u)
		val INTEGER_TAG: Tag = Tag(TagClass.UNIVERSAL, true, 2u)
		val BITSTRING_TAG: Tag = Tag(TagClass.UNIVERSAL, true, 3u) // primitive or constructed
		val OCTETSTRING_TAG: Tag = Tag(TagClass.UNIVERSAL, true, 4u) // primitive or constructed
		val NULL_TAG: Tag = Tag(TagClass.UNIVERSAL, true, 5u)
		val OID_TAG: Tag = Tag(TagClass.UNIVERSAL, true, 6u)

		// public static final Tag ObjectDescriptorTag = new Tag(TagClass.UNIVERSAL, true, 7);
		// public static final Tag ExternalTypeTag = new Tag(TagClass.UNIVERSAL, true, 8);
		val REAL_TAG: Tag = Tag(TagClass.UNIVERSAL, true, 9u)
		val ENUMERATED_TAG: Tag = Tag(TagClass.UNIVERSAL, true, 10u)
		val EMBEDDED_PDV_TAG: Tag = Tag(TagClass.UNIVERSAL, false, 11u)

		// public static final Tag UTF8Tag = new Tag(TagClass.UNIVERSAL, true, 12);
		val RELATIVE_OID_TAG: Tag = Tag(TagClass.UNIVERSAL, true, 13u)

		// public static final Tag TimeTag = new Tag(TagClass.UNIVERSAL, true, 14);
		// 15 is reserved for future recommendations
		val SEQUENCE_TAG: Tag = Tag(TagClass.UNIVERSAL, false, 16u)
		val SET_TAG: Tag = Tag(TagClass.UNIVERSAL, false, 17u)

		// char strings 18-22, 25-30
		// public static final Tag UTCTimeTag = new Tag(TagClass.UNIVERSAL, true, 23);
		// public static final Tag GeneralizedTimeTag = new Tag(TagClass.UNIVERSAL, true, 24);
		// public static final Tag DateTag = new Tag(TagClass.UNIVERSAL, true, 31);
		// public static final Tag TimeOfDayTag = new Tag(TagClass.UNIVERSAL, true, 32);
		// public static final Tag DateTimeTag = new Tag(TagClass.UNIVERSAL, true, 33);
		// public static final Tag DurationTag = new Tag(TagClass.UNIVERSAL, true, 34);
		// public static final Tag IntOIDTag = new Tag(TagClass.UNIVERSAL, true, 35);
		// public static final Tag IntOIDReferenceTag = new Tag(TagClass.UNIVERSAL, true, 36);

		@OptIn(ExperimentalUnsignedTypes::class)
		@Throws(TlvException::class)
		internal fun fromBer(data: UByteArray): ParsedTag {
			// how many octets made up this tag?
			var numOctets = 1
			// get common values independed from encoding type
			val tagClass: TagClass = TagClass.getTagClass(data[0])
			val primitive = ((data[0].toUInt() shr 5) and 0x01u) == 0x00u

			// get value so it can be seen if short or long form is present
			var tagNum = 0uL
			val tmpTagNum = (data[0].toUInt() and 0x1Fu).toUByte()
			if (tmpTagNum <= 30u) {
				// short form
				tagNum = tmpTagNum.toULong()
			} else {
				// long form
				var next: UByte
				// read as long as it is not the last octet
				do {
					// terminate if there are no bytes left or the number is larger than 64 bits
					if (numOctets * 7 > 64) {
						throw TlvException("Tag number doesn't fit into a 64 bit word.")
					} else if (data.size < numOctets) {
						throw TlvException("Not enough bytes in input bytes to build TLV tag.")
					}
					// get next number
					next = data[numOctets]
					numOctets++
					// get next bytes and merge them into result
					val nextValue = (next.toUInt() and 0x7Fu).toUByte()
					tagNum = (tagNum shl 7) or nextValue.toULong()
				} while (((next.toInt() shr 7) and 0x01) == 0x01)
			}

			val resultTag = Tag(tagClass, primitive, tagNum)

			return ParsedTag(resultTag, numOctets)
		}

		internal fun fromSimple(data: UByteArray): ParsedTag {
			val num = data[0]
			return ParsedTag(Tag(TagClass.APPLICATION, true, num.toULong()), 1)
		}

		fun forTagNumWithClass(tagNumWithClass: ULong): Tag {
			val bytes = tagNumWithClass.toSparseUByteArray()
			return fromBer(bytes).tag
		}
	}
}

internal data class ParsedTag(
	val tag: Tag,
	val numOctets: Int,
)
