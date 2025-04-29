/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

/**
 * Internal class representing one entry with a TLV definition.
 *
 * @author Tobias Wich
 */
internal class TagLengthValue private constructor(
    /**
     * Get number of the bytes from which this TLV was created.<br></br>
     * Only makes sense if created from bytes.
     * @return
     */
    val rawLength: Int, private var tag: Tag, @JvmField var value: ByteArray
) {
    private constructor(numOctets: Int, tagClass: TagClass, primitive: Boolean, tagNum: Long, value: ByteArray) : this(
        numOctets,
        Tag(tagClass, primitive, tagNum),
        value
    )

    private constructor(tag: Tag, value: ByteArray) : this(0, tag, value)

    constructor(tagClass: TagClass, primitive: Boolean, tagNum: Long, value: ByteArray) : this(
        0,
        tagClass,
        primitive,
        tagNum,
        value
    )

    constructor() : this(0, Tag(), byteArrayOf())


    fun getTag(): Tag {
        return Tag(this.tag)
    }

    fun setTag(tag: Tag) {
        this.tag = Tag(tag)
    }

    var tagClass: TagClass?
        get() = tag.tagClass
        set(tagClass) {
            tag.tagClass = tagClass
        }

    var isPrimitive: Boolean
        get() = tag.isPrimitive
        set(primitive) {
            tag.isPrimitive = primitive
        }

    var tagNum: Long
        get() = tag.tagNum
        set(tagNum) {
            tag.tagNum = tagNum
        }

    @set:Throws(TLVException::class)
    var tagNumWithClass: Long
        get() = tag.tagNumWithClass
        set(tagNumWithClass) {
            tag.tagNumWithClass = tagNumWithClass
        }

    @Throws(TLVException::class)
    fun setTagNumWithClass(tagNumWithClass: ByteArray) {
        this.tag = Tag.Companion.fromBER(tagNumWithClass)
    }

    val valueLength: Int
        get() = value.size


    /**
     * When fed with a large input stream, cut off the portion which makes up this TLV.
     * @param inputWithThisTag
     * @return
     */
    @Throws(TLVException::class)
    fun extractRest(inputWithThisTag: ByteArray): ByteArray {
        try {
            val from = rawLength
            val to = inputWithThisTag.size
            return Arrays.copyOfRange(inputWithThisTag, from, to)
        } catch (ex: ArrayIndexOutOfBoundsException) {
            throw TLVException("Data length and claimed length do not match.", ex)
        } catch (ex: IllegalArgumentException) {
            throw TLVException("Data length and claimed length do not match.", ex)
        }
    }


    fun toBER(): ByteArray {
        try {
            val out = ByteArrayOutputStream()

            val tagBytes = tag.toBER()
            out.write(tagBytes)

            // calculate length according to input data
            val len = valueLength
            if (len <= 127) {
                // short form
                out.write(len.toByte().toInt())
            } else {
                val lenBytes: ByteArray = toByteArray(len)
                val lenHeader = (0x80 or lenBytes.size).toByte()
                out.write(lenHeader.toInt())
                out.write(lenBytes)
            }

            // write actual data
            out.write(value)

            return out.toByteArray()
        } catch (ex: IOException) {
            // IOException depends solely on the stream. The only thing that can happen here is OOM.
            throw RuntimeException(ex)
        }
    }

    companion object {
        @JvmStatic
        @Throws(TLVException::class)
        fun fromBER(data: ByteArray): TagLengthValue {
            val tag: Tag = Tag.Companion.fromBER(data)
            // how many octets made up this tag?
            var numOctets = tag.numOctets

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
                            throw TLVException("Not enough bytes in input to read TLV length.")
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
                            throw TLVException("Length doesn't fit into a 32 bit word.")
                        } else if (data.size < numOctets + i + 1) {
                            throw TLVException("Not enough bytes in input to read TLV length.")
                        }
                        dataLength = if (data[numOctets + i] < 0) {
                            // correct bytes wich are interpreted as negative numbers by java
                            (dataLength shl 8) or (256 + data[numOctets + i])
                        } else {
                            (dataLength shl 8) or data[numOctets + i].toInt()
                        }
                        i++
                    }
                    numOctets += i
                }
            }

            // extract data based on calculated length
            val dataField = Arrays.copyOfRange(data, numOctets, numOctets + dataLength)

            // recalculate total length of datablock
            numOctets = numOctets + dataLength
            if (endOfLine) {
                numOctets += 2
            }

            // we have all values, build Tag object and return
            val result = TagLengthValue(numOctets, tag, dataField)
            return result
        }
    }
}
