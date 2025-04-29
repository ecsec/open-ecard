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
package org.openecard.common.tlv

import org.openecard.common.tlv.TLVException
import org.openecard.common.util.*

/**
 *
 * @author Tobias Wich
 */
class Tag @JvmOverloads constructor(// char strings 18-22, 25-30
    //public static final Tag UTCTimeTag = new Tag(TagClass.UNIVERSAL, true, 23);
    //public static final Tag GeneralizedTimeTag = new Tag(TagClass.UNIVERSAL, true, 24);
    //public static final Tag DateTag = new Tag(TagClass.UNIVERSAL, true, 31);
    //public static final Tag TimeOfDayTag = new Tag(TagClass.UNIVERSAL, true, 32);
    //public static final Tag DateTimeTag = new Tag(TagClass.UNIVERSAL, true, 33);
    //public static final Tag DurationTag = new Tag(TagClass.UNIVERSAL, true, 34);
    //public static final Tag IntOIDTag = new Tag(TagClass.UNIVERSAL, true, 35);
    //public static final Tag IntOIDReferenceTag = new Tag(TagClass.UNIVERSAL, true, 36);
    private var tagClass: TagClass? = TagClass.UNIVERSAL,
    private var primitive: Boolean = true,
    private var tagNum: Long = 0
) {
    private var tagNumWithClass: Long = 0

    /** Set when created from BER. This is needed when creating a TagLengthValue instance from BER  */
    var numOctets: Int = 0


    constructor(tag: Tag) : this(tag.getTagClass(), tag.isPrimitive(), tag.getTagNum())

    init {
        calculateTagNumWithClass()
    }

    fun getTagClass(): TagClass? {
        return tagClass
    }

    fun setTagClass(tagClass: TagClass?) {
        this.tagClass = tagClass
        calculateTagNumWithClass()
    }

    fun isPrimitive(): Boolean {
        return this.primitive
    }

    fun setPrimitive(primitive: Boolean) {
        this.primitive = primitive
        calculateTagNumWithClass()
    }

    fun getTagNum(): Long {
        return this.tagNum
    }

    fun setTagNum(tagNum: Long) {
        this.tagNum = tagNum
        calculateTagNumWithClass()
    }

    fun getTagNumWithClass(): Long {
        return this.tagNumWithClass
    }

    @Throws(TLVException::class)
    fun setTagNumWithClass(tagNumWithClass: Long) {
        val newTag = fromBER(toByteArray(tagNumWithClass))
        this.tagClass = newTag.tagClass
        this.primitive = newTag.primitive
        this.tagNum = newTag.tagNum
        this.tagNumWithClass = tagNumWithClass
    }


    private fun calculateTagNumWithClass() {
        var leading = tagClass!!.num
        leading = ((leading.toInt() shl 1) or (if (this.primitive) 0 else 1)).toByte()

        val rest: ByteArray
        if (this.tagNum >= 31) {
            // long
            leading = ((leading.toInt() shl 5) or 0x1F).toByte()
            rest = toByteArray(this.tagNum, 7)
            for (i in 0..<rest.size - 1) {
                rest[i] = (rest[i].toInt() or 0x80).toByte()
            }
        } else {
            // short
            leading = ((leading.toInt() shl 5).toLong() or this.tagNum).toByte()
            rest = ByteArray(0)
        }

        val resultBytes = ByteUtils.concatenate(leading, rest)
        this.tagNumWithClass = toLong(resultBytes)
    }


    fun toBER(): ByteArray {
        return toByteArray(tagNumWithClass)
    }

    override fun toString(): String {
        return "[" + tagClass.toString() + " " +
                (if (primitive) "prim " else "cons ") +
                tagNum + " (0x" + java.lang.Long.toHexString(tagNumWithClass) + ")]"
    }

    override fun equals(obj: Any?): Boolean {
        if (obj is Tag) {
            return this.getTagNumWithClass() == obj.getTagNumWithClass()
        } else {
            return false
        }
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 79 * hash + (this.tagNumWithClass xor (this.tagNumWithClass ushr 32)).toInt()
        return hash
    }

    companion object {
        /**
         * Builtin standard tags
         */
        val BOOLEAN_TAG: Tag = Tag(TagClass.UNIVERSAL, true, 1)
        val INTEGER_TAG: Tag = Tag(TagClass.UNIVERSAL, true, 2)
        val BITSTRING_TAG: Tag = Tag(TagClass.UNIVERSAL, true, 3) // primitive or constructed
        val OCTETSTRING_TAG: Tag = Tag(TagClass.UNIVERSAL, true, 4) // primitive or constructed
        val NULL_TAG: Tag = Tag(TagClass.UNIVERSAL, true, 5)
        val OID_TAG: Tag = Tag(TagClass.UNIVERSAL, true, 6)

        //public static final Tag ObjectDescriptorTag = new Tag(TagClass.UNIVERSAL, true, 7);
        //public static final Tag ExternalTypeTag = new Tag(TagClass.UNIVERSAL, true, 8);
        val REAL_TAG: Tag = Tag(TagClass.UNIVERSAL, true, 9)
        val ENUMERATED_TAG: Tag = Tag(TagClass.UNIVERSAL, true, 10)
        val EMBEDDED_PDV_TAG: Tag = Tag(TagClass.UNIVERSAL, false, 11)

        //public static final Tag UTF8Tag = new Tag(TagClass.UNIVERSAL, true, 12);
        val RELATIVE_OID_TAG: Tag = Tag(TagClass.UNIVERSAL, true, 13)

        //public static final Tag TimeTag = new Tag(TagClass.UNIVERSAL, true, 14);
        // 15 is reserved for future recommendations
        val SEQUENCE_TAG: Tag = Tag(TagClass.UNIVERSAL, false, 16)
        val SET_TAG: Tag = Tag(TagClass.UNIVERSAL, false, 17)


        @Throws(TLVException::class)
        fun fromBER(data: ByteArray): Tag {
            // how many octets made up this tag?
            var numOctets = 1
            // get common values independed from encoding type
            val tagClass: TagClass = TagClass.Companion.getTagClass(data[0])
            val primitive = ((data[0].toInt() shr 5) and 0x01) == 0x00

            // get value so it can be seen if short or long form is present
            var tagNum: Long = 0
            val tmpTagNum = (data[0].toInt() and 0x1F).toByte()
            if (tmpTagNum <= 30) {
                // short form
                tagNum = tmpTagNum.toLong()
            } else {
                // long form
                var next: Byte
                // read as long as it is not the last octet
                do {
                    // terminate if there are no bytes left or the number is larger than 64 bits
                    if (numOctets * 7 > 64) {
                        throw TLVException("Tag number doesn't fit into a 64 bit word.")
                    } else if (data.size < numOctets) {
                        throw TLVException("Not enough bytes in input bytes to build TLV tag.")
                    }
                    // get next number
                    next = data[numOctets]
                    numOctets++
                    // get next bytes and merge them into result
                    val nextValue = (next.toInt() and 0x7F).toByte()
                    tagNum = (tagNum shl 7) or nextValue.toLong()
                } while (((next.toInt() shr 7) and 0x01) == 0x01)
            }

            val resultTag = Tag(tagClass, primitive, tagNum)
            resultTag.numOctets = numOctets
            return resultTag
        }
    }
}
