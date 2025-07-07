package org.openecard.sc.tlv

import org.openecard.utils.common.mergeToArray
import org.openecard.utils.common.toSparseUByteArray
import org.openecard.utils.serialization.toPrintable
import kotlin.jvm.JvmInline

@JvmInline
value class ObjectIdentifier(
	val value: String,
)

@OptIn(ExperimentalUnsignedTypes::class)
val ObjectIdentifier.tlvStandard: Tlv get() {
	return tlvCustom(Tag.OID_TAG)
}

@OptIn(ExperimentalUnsignedTypes::class)
fun ObjectIdentifier.tlvCustom(tag: Tag): Tlv = TlvPrimitive(tag, this.valueBytes.toPrintable())

@OptIn(ExperimentalUnsignedTypes::class)
val ObjectIdentifier.valueBytes: UByteArray get() {
	val parts = this.value.split(".")

	// correct root node, as it combine two values in one sequence
	val rootValue =
		run {
			val v1 = parts[0].toUInt()
			val v2 = parts[1].toUInt()
			require(v1 < 3u) { "OID root identifier is too big" }
			if (v1 < 2u) {
				require(v2 < 40u) { "OID sub-root identifier is too big" }
			}
			val v = (v1 * 40u) + v2
			v.toString(10)
		}
	val partsCorrected = listOf(rootValue) + parts.drop(2)

	val octets =
		partsCorrected.map {
			val num = it.toULong()
			// use only 7 bits per byte to represent the number
			val sparse = num.toSparseUByteArray(7)
			// set highest bit in all upper bytes
			for (i in 0 until sparse.size - 1) {
				sparse[i] = sparse[i] or 0x80u
			}
			// return sparse array
			sparse
		}
	return octets.mergeToArray()
}

private val oidRe = """^[0-9]+(\.[0-9]+)+$""".toRegex()

fun String.toObjectIdentifier(): ObjectIdentifier {
	require(oidRe.matches(this)) { "OID=$this is not a valid OID" }
	return ObjectIdentifier(this)
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toObjectIdentifier(): ObjectIdentifier {
	val groups = mutableListOf<ULong>()
	var slice = this
	var isFirst = true
	while (slice.isNotEmpty()) {
		val preceding = slice.takeWhile { (it and 0x80u) > 0u }.map { it and 0x7Fu }
		val last = slice[preceding.size]
		// update slice
		slice = slice.sliceArray(preceding.size + 1 until slice.size)

		var result = last.toULong()
		for (i in 0 until preceding.size) {
			// shift each octet to the right by a multiple of 7
			val shiftedOctet = preceding[preceding.size - i - 1].toULong() shl ((i + 1) * 7)
			result += shiftedOctet
		}

		if (isFirst) {
			// the first sequence represents two values
			when {
				result < 40u -> {
					groups.add(0u)
					groups.add(result)
				}
				result < 80u -> {
					groups.add(1u)
					groups.add(result - 40u)
				} else -> {
					groups.add(2u)
					groups.add(result - 80u)
				}
			}
			isFirst = false
		} else {
			groups.add(result)
		}
	}

	val oidStr = groups.joinToString(".")
	return ObjectIdentifier(oidStr)
}

@OptIn(ExperimentalUnsignedTypes::class)
fun Tlv.toObjectIdentifier(tag: Tag = Tag.OID_TAG): ObjectIdentifier {
	require(this.tag == tag) { "The tag of the TLV ($tag) is not the expected tag." }
	return when (this) {
		is TlvPrimitive -> this.value.toObjectIdentifier()
		else -> throw IllegalArgumentException("Object Identifier TLV is not primitive")
	}
}
