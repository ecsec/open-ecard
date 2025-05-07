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

import org.openecard.common.util.ByteUtils
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.LinkedList

/**
 * Class representing a TLV object according to the ASN1 specification.
 * This class is capable of representing sequences and has features to navigation in an ASN1 tree.
 *
 * @author Tobias Wich
 */
open class TLV {
	private var tag: TagLengthValue

	// protected TLV parent = null;
	var next: TLV? = null
	var child: TLV? = null

	constructor() {
		tag = TagLengthValue()
	}

	constructor(obj: TLV) {
		this.tag = obj.tag
		this.next = if (obj.next != null) TLV(obj.next!!) else null
		this.child = if (obj.child != null) TLV(obj.child!!) else null
	}

	/**
	 * deferred setters for TLV container
	 */
	fun getTag(): Tag = tag.getTag()

	fun setTag(tag: Tag) {
		this.tag.setTag(tag)
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

	val tagNum: Long
		get() = tag.tagNum

	fun setTagNum(tagNum: Byte) {
		setTagNum((tagNum.toInt() and 0xFF).toLong())
	}

	fun setTagNum(tagNum: Long) {
		tag.tagNum = tagNum
	}

	open var tagNumWithClass: Long
		get() {
			return tag.tagNumWithClass
		}
		set(value) {
			tag.tagNumWithClass = value
		}

	fun setTagNumWithClass(tagNumWithClass: Byte) {
		tag.tagNumWithClass = (tagNumWithClass.toInt() and 0xFF).toLong()
	}

	fun setTagNumWithClass(tagNumWithClass: ByteArray) {
		tag.setTagNumWithClass(tagNumWithClass)
	}

	val valueLength: Int
		get() = tag.valueLength

	var value: ByteArray
		get() = tag.value
		set(value) {
			tag.value = value
		}

	/**
	 * modification functions
	 */
	fun addToEnd(sibling: TLV?) {
		if (next == null) {
			next = sibling
		} else {
			next!!.addToEnd(sibling)
		}
	}

	/**
	 * Remove next which is indicated by n.
	 * 0 means direct sibling.
	 * @param n Index of the sibling.
	 * @return Removed node.
	 */
	fun remove(n: Int): TLV? {
		if (n == 0) {
			val tmp = next
			next = null
			return tmp
		} else if (n > 0 && next != null) {
			return next!!.remove(n - 1)
		} else {
			return null
		}
	}

	fun removeNext(): TLV? = remove(0)

	fun hasChild(): Boolean = child != null

	fun hasNext(): Boolean = next != null

	fun asList(): List<TLV> {
		val result = LinkedList<TLV>()

		var nextTag: TLV? = this
		while (nextTag != null) {
			val toAdd = TLV(nextTag)
			toAdd.next = null // delete reference to next
			result.add(toAdd)

			nextTag = nextTag.next
		}

		return result
	}

	fun findNextTags(num: Long): List<TLV> {
		val all = asList()
		val result = LinkedList<TLV>()

		for (nextTLV in all) {
			if (nextTLV.tagNumWithClass == num) {
				result.add(nextTLV)
			}
		}

		return result
	}

	fun findChildTags(num: Long): List<TLV> =
		if (hasChild()) {
			child!!.findNextTags(num)
		} else {
			LinkedList()
		}

	@JvmOverloads
	fun toBER(withSuccessors: Boolean = false): ByteArray {
		try {
			val out = ByteArrayOutputStream()
			// value calculated from child if any
			toBER(out, withSuccessors)
			return out.toByteArray()
		} catch (ex: IOException) {
			// IOException depends solely on the stream. The only thing that can happen here is OOM.
			throw RuntimeException(ex)
		}
	}

	private fun toBER(
		out: ByteArrayOutputStream,
		withSuccessors: Boolean,
	) {
		if (child != null) {
			val childBytes = child!!.toBER(true)
			tag.isPrimitive = false
			tag.value = childBytes
		} else {
			tag.isPrimitive = true
		}
		// write child to output stream
		out.write(tag.toBER())

		if (withSuccessors && next != null) {
			next!!.toBER(out, withSuccessors)
		}
	}

	override fun toString(): String = toString("")

	fun toString(prefix: String): String {
		var result = prefix + String.format("%02X", tagNumWithClass)

		result +=
			if (!hasChild()) {
				" " + tag.valueLength + " " + ByteUtils.toHexString(tag.value)
			} else {
				"""
				
				${child!!.toString("$prefix  ")}
				""".trimIndent()
			}

		if (hasNext()) {
			result +=
				"""
				
				${next!!.toString(prefix)}
				""".trimIndent()
		}

		return result
	}

	companion object {
		/**
		 * TLV construction from and to different encodings
		 */
		@JvmStatic
		@Throws(TLVException::class)
		fun fromBER(input: ByteArray?): TLV {
			var rest = input

			val first = TLV()
			var isFirst = true
			var last = first
			// build as long as there is input left
			while (rest!!.isNotEmpty()) {
				val next: TLV
				next =
					if (isFirst) {
						first
					} else {
						TLV()
					}

				// break execution when 0 tag encountered
				if (rest[0] == 0.toByte()) {
					return first
				}
				// convert bytes to flat TLV data
				next.tag = TagLengthValue.Companion.fromBER(rest)
				// if constructed build child structure
				if (!next.tag.isPrimitive && next.tag.valueLength > 0) {
					next.child = fromBER(next.tag.value)
				}

				// set next as sibling in last
				if (isFirst) {
					isFirst = false
				} else {
					last.next = next
				}
				last = next

				// get rest of the bytes for next iteration
				rest = last.tag.extractRest(rest)
			}

			return first
		}
	}
}
