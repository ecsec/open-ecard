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
	private var _tag: TagLengthValue
	var tag: Tag
		get() = _tag.getTag()
		set(value) = _tag.setTag(value)

	// protected TLV parent = null;
	var next: TLV? = null
	var child: TLV? = null

	constructor() {
		_tag = TagLengthValue()
	}

	constructor(obj: TLV) {
		this._tag = obj._tag
		this.next = if (obj.next != null) TLV(obj.next!!) else null
		this.child = if (obj.child != null) TLV(obj.child!!) else null
	}

	/**
	 * deferred setters for TLV container
	 */

	var tagClass: TagClass?
		get() = _tag.tagClass
		set(tagClass) {
			_tag.tagClass = tagClass
		}

	var isPrimitive: Boolean
		get() = _tag.isPrimitive
		set(primitive) {
			_tag.isPrimitive = primitive
		}

	val tagNum: Long
		get() = _tag.tagNum

	fun setTagNum(tagNum: Byte) {
		setTagNum((tagNum.toInt() and 0xFF).toLong())
	}

	fun setTagNum(tagNum: Long) {
		_tag.tagNum = tagNum
	}

	open var tagNumWithClass: Long
		get() {
			return _tag.tagNumWithClass
		}
		set(value) {
			_tag.tagNumWithClass = value
		}

	@Throws(TLVException::class)
	fun setTagNumWithClass(tagNumWithClass: Byte) {
		_tag.tagNumWithClass = (tagNumWithClass.toInt() and 0xFF).toLong()
	}

	fun setTagNumWithClass(tagNumWithClass: ByteArray) {
		_tag.setTagNumWithClass(tagNumWithClass)
	}

	val valueLength: Int
		get() = _tag.valueLength

	var value: ByteArray
		get() = _tag.value
		set(value) {
			_tag.value = value
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
			_tag.isPrimitive = false
			_tag.value = childBytes
		} else {
			_tag.isPrimitive = true
		}
		// write child to output stream
		out.write(_tag.toBER())

		if (withSuccessors && next != null) {
			next!!.toBER(out, withSuccessors)
		}
	}

	override fun toString(): String = toString("")

	@OptIn(ExperimentalStdlibApi::class)
	fun toString(prefix: String): String {
		var result = prefix + tagNumWithClass.toUShort().toHexString()

		result +=
			if (!hasChild()) {
				" " + _tag.valueLength + " " + ByteUtils.toHexString(_tag.value)
			} else {
				child!!.toString("$prefix  ")
			}

		if (hasNext()) {
			result += next!!.toString(prefix)
		}

		return "[TLV $result]"
	}

	companion object {
		/**
		 * TLV construction from and to different encodings
		 */
		@JvmStatic
		@Throws(TLVException::class)
		fun fromBER(input: ByteArray): TLV {
			var rest = input

			val first = TLV()
			var isFirst = true
			var last = first
			// build as long as there is input left
			while (rest.isNotEmpty()) {
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
				next._tag = TagLengthValue.fromBER(rest)
				// if constructed build child structure
				if (!next._tag.isPrimitive && next._tag.valueLength > 0) {
					next.child = fromBER(next._tag.value)
				}

				// set next as sibling in last
				if (isFirst) {
					isFirst = false
				} else {
					last.next = next
				}
				last = next

				// get rest of the bytes for next iteration
				rest = last._tag.extractRest(rest)
			}

			return first
		}
	}
}
