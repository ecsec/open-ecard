package org.openecard.sc.tlv

import org.openecard.utils.common.toUByteArray
import org.openecard.utils.common.toUInt
import org.openecard.utils.common.toULong
import org.openecard.utils.serialization.toPrintable

@OptIn(ExperimentalUnsignedTypes::class)
fun ULong.toTlv(tag: Tag = Tag.INTEGER_TAG): TlvPrimitive {
	val value = this.toUByteArray()
	return TlvPrimitive(tag, value.toPrintable())
}

@OptIn(ExperimentalUnsignedTypes::class)
fun Tlv.toULong(tag: Tag = Tag.INTEGER_TAG): ULong {
	require(this.tag == tag) { "The tag of the TLV ($tag) is not the expected tag." }
	return when (this) {
		is TlvPrimitive -> this.value.toULong(0)
		else -> throw IllegalArgumentException("Integer TLV is not primitive")
	}
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UInt.toTlv(tag: Tag = Tag.INTEGER_TAG): TlvPrimitive {
	val value = this.toUByteArray()
	return TlvPrimitive(tag, value.toPrintable())
}

@OptIn(ExperimentalUnsignedTypes::class)
fun Tlv.toUInt(tag: Tag = Tag.INTEGER_TAG): UInt {
	require(this.tag == tag) { "The tag of the TLV ($tag) is not the expected tag." }
	return when (this) {
		is TlvPrimitive -> this.value.toUInt(0)
		else -> throw IllegalArgumentException("Integer TLV is not primitive")
	}
}
