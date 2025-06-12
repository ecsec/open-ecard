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

import org.openecard.utils.serialization.PrintableUByteArray
import org.openecard.utils.serialization.toPrintable

sealed interface Tlv {
	val tag: Tag
	val sibling: Tlv?

	val asConstructed: TlvConstructed? get() = this as? TlvConstructed
	val asPrimitive: TlvPrimitive? get() = this as? TlvPrimitive

	@OptIn(ExperimentalUnsignedTypes::class)
	val contentAsBytesBer: UByteArray

	/**
	 * Copy the node and set sibling to the given value.
	 */
	fun setSibling(newSibling: Tlv?): Tlv

	fun hasNext(): Boolean = sibling != null

	fun asList(): List<Tlv> {
		val result = mutableListOf<Tlv>()

		var nextTag: Tlv? = this
		while (nextTag != null) {
			val toAdd = nextTag.setSibling(null)
			result.add(toAdd)

			nextTag = nextTag.sibling
		}

		return result
	}

	fun findNextTags(num: ULong): List<Tlv> = asList().filter { it.tag.tagNumWithClass == num }

	fun findNextTags(tag: Tag): List<Tlv> = asList().filter { it.tag == tag }

	@OptIn(ExperimentalUnsignedTypes::class)
	fun toBer(withSuccessors: Boolean = false): UByteArray =
		buildList {
			when (this@Tlv) {
				is TlvPrimitive -> {
					addAll(tagLengthValue.toBer())
				}
				is TlvConstructed -> {
					val childData = child?.toBer(true) ?: ubyteArrayOf()
					val tlvIntermediate = TagLengthValue(tag, childData.toPrintable())
					addAll(tlvIntermediate.toBer())
				}
			}

			if (withSuccessors) {
				sibling?.let { addAll(it.toBer(withSuccessors)) }
			}
		}.toUByteArray()

	@OptIn(ExperimentalUnsignedTypes::class)
	fun toCompact(withSuccessors: Boolean = false): UByteArray =
		buildList {
			when (this@Tlv) {
				is TlvPrimitive -> {
					addAll(tagLengthValue.toCompact())
				}
				else -> throw IllegalArgumentException(
					"The given TLV structure contains constructed objects which can not be serialized as compact TLV.",
				)
			}

			if (withSuccessors) {
				sibling?.let { addAll(it.toCompact(withSuccessors)) }
			}
		}.toUByteArray()

	@OptIn(ExperimentalUnsignedTypes::class)
	fun toSimple(withSuccessors: Boolean = false): UByteArray =
		buildList {
			when (this@Tlv) {
				is TlvPrimitive -> {
					addAll(tagLengthValue.toSimple())
				}
				else -> throw IllegalArgumentException(
					"The given TLV structure contains constructed objects which can not be serialized as simple TLV.",
				)
			}

			if (withSuccessors) {
				sibling?.let { addAll(it.toSimple(withSuccessors)) }
			}
		}.toUByteArray()
}

data class TlvPrimitive
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val tagLengthValue: TagLengthValue,
		override val sibling: Tlv? = null,
	) : Tlv {
		override val tag: Tag = tagLengthValue.tag

		@OptIn(ExperimentalUnsignedTypes::class)
		val value: UByteArray = tagLengthValue.value.v

		@OptIn(ExperimentalUnsignedTypes::class)
		override val contentAsBytesBer: UByteArray = value

		init {
			require(tag.primitive) { "Building TLV primitive value with a constructed tag value." }
		}

		override fun setSibling(newSibling: Tlv?): Tlv = this.copy(sibling = newSibling)

		@OptIn(ExperimentalUnsignedTypes::class)
		constructor(
			tag: Tag,
			value: PrintableUByteArray,
			sibling: Tlv? = null,
		) : this(TagLengthValue(tag, value), sibling)
	}

data class TlvConstructed(
	override val tag: Tag,
	val child: Tlv?,
	override val sibling: Tlv? = null,
) : Tlv {
	init {
		require(!tag.primitive) { "Building TLV constructed value with a primitive tag value." }
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override val contentAsBytesBer: UByteArray by lazy {
		child?.toBer(withSuccessors = true) ?: ubyteArrayOf()
	}

	override fun setSibling(newSibling: Tlv?): Tlv = this.copy(sibling = newSibling)

	fun hasChild(): Boolean = child != null

	fun findChildTags(num: ULong): List<Tlv> = child?.findNextTags(num) ?: listOf()

	fun findChildTags(tag: Tag): List<Tlv> = child?.findNextTags(tag) ?: listOf()

	fun childList(): List<Tlv> = child?.asList() ?: listOf()
}

fun buildTlv(
	tag: Tag,
	init: TlvConstructedBuilder.() -> Unit,
): TlvConstructed {
	val builder = TlvConstructedBuilder(tag)
	builder.init()
	return builder.build()
}

class TlvConstructedBuilder internal constructor(
	val tag: Tag,
) {
	private val children = mutableListOf<Tlv>()

	internal fun build(): TlvConstructed {
		// link children
		val child =
			children.reduceRightOrNull { next, previous ->
				next.setSibling(previous)
			}
		return TlvConstructed(tag, child)
	}

	fun constructed(
		tag: Tag,
		init: TlvConstructedBuilder.() -> Unit,
	) {
		val builder = TlvConstructedBuilder(tag)
		builder.init()
		val child = builder.build()
		children.add(child)
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	fun primitive(
		tag: Tag,
		data: UByteArray,
	) {
		val child = TlvPrimitive(tag, data.toPrintable())
		children.add(child)
	}

	fun generic(child: Tlv) {
		children.add(child)
	}
}

@OptIn(ExperimentalUnsignedTypes::class)
@Throws(TlvException::class)
fun UByteArray.toTlvBer(): ParsedTlv {
	var rest = this
	val result = mutableListOf<Tlv>()

	try {
		while (rest.isNotEmpty()) {
			val (tlvInternal, numOctets, remaining) = TagLengthValue.fromBer(rest)
			val tlv =
				if (tlvInternal.tag.primitive) {
					TlvPrimitive(tlvInternal)
				} else {
					val child = parseChild(tlvInternal.value.v)
					TlvConstructed(tlvInternal.tag, child)
				}
			result.add(tlv)

			rest = remaining.v
		}
	} catch (ex: TlvException) {
		// end of TLV data reached
	}

	val root =
		result.reduceRightOrNull { next, previous ->
			next.setSibling(previous)
		} ?: throw TlvException("No TLV data found in provided data.")
	return ParsedTlv(root, rest.toPrintable())
}

@OptIn(ExperimentalUnsignedTypes::class)
@Throws(TlvException::class)
fun UByteArray.toTlvCompact(): ParsedTlv {
	var rest = this
	val result = mutableListOf<Tlv>()

	try {
		while (rest.isNotEmpty()) {
			val (tlvInternal, numOctets, remaining) = TagLengthValue.fromCompact(rest)
			val tlv = TlvPrimitive(tlvInternal)
			result.add(tlv)

			rest = remaining.v
		}
	} catch (ex: TlvException) {
		// end of TLV data reached
	}

	val root =
		result.reduceRightOrNull { next, previous ->
			next.setSibling(previous)
		} ?: throw TlvException("No TLV data found in provided data.")
	return ParsedTlv(root, rest.toPrintable())
}

@OptIn(ExperimentalUnsignedTypes::class)
@Throws(TlvException::class)
fun UByteArray.toTlvSimple(): ParsedTlv {
	var rest = this
	val result = mutableListOf<Tlv>()

	try {
		while (rest.isNotEmpty()) {
			val (tlvInternal, numOctets, remaining) = TagLengthValue.fromSimple(rest)
			val tlv = TlvPrimitive(tlvInternal)
			result.add(tlv)

			rest = remaining.v
		}
	} catch (ex: TlvException) {
		// end of TLV data reached
	}

	val root =
		result.reduceRightOrNull { next, previous ->
			next.setSibling(previous)
		} ?: throw TlvException("No TLV data found in provided data.")
	return ParsedTlv(root, rest.toPrintable())
}

@OptIn(ExperimentalUnsignedTypes::class)
internal fun parseChild(data: UByteArray): Tlv? {
	var rest = data
	val result = mutableListOf<Tlv>()

	while (rest.isNotEmpty()) {
		val (tlvInternal, numOctets, remaining) = TagLengthValue.fromBer(rest)
		val tlv =
			if (tlvInternal.tag.primitive) {
				TlvPrimitive(tlvInternal)
			} else {
				val child = parseChild(tlvInternal.value.v)
				TlvConstructed(tlvInternal.tag, child)
			}
		result.add(tlv)

		rest = remaining.v
	}

	val root =
		result.reduceRightOrNull { next, previous ->
			next.setSibling(previous)
		}
	return root
}

data class ParsedTlv(
	val tlv: Tlv,
	val rest: PrintableUByteArray,
)
