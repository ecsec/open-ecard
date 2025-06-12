package org.openecard.sc.tlv

fun List<Tlv>.filterPrimitive(num: ULong): List<TlvPrimitive> = filterPrimitive(Tag.forTagNumWithClass(num))

fun List<Tlv>.filterPrimitive(tag: Tag): List<TlvPrimitive> = filter { it.tag == tag }.filterIsInstance<TlvPrimitive>()

fun List<Tlv>.findPrimitive(num: ULong): TlvPrimitive? = filterPrimitive(num).firstOrNull()

fun List<Tlv>.findPrimitive(tag: Tag): TlvPrimitive? = filterPrimitive(tag).firstOrNull()

fun List<Tlv>.filterConstructed(num: ULong): List<TlvConstructed> = filterConstructed(Tag.forTagNumWithClass(num))

fun List<Tlv>.filterConstructed(tag: Tag): List<TlvConstructed> =
	filter {
		it.tag == tag
	}.filterIsInstance<TlvConstructed>()

fun List<Tlv>.findConstructed(num: ULong): TlvConstructed? = filterConstructed(num).firstOrNull()

fun List<Tlv>.findConstructed(tag: Tag): TlvConstructed? = filterConstructed(tag).firstOrNull()

fun List<Tlv>.filterTlv(num: ULong): List<Tlv> = filterTlv(Tag.forTagNumWithClass(num))

fun List<Tlv>.filterTlv(tag: Tag): List<Tlv> = filter { it.tag == tag }

fun List<Tlv>.findTlv(num: ULong): Tlv? = filterTlv(num).firstOrNull()

fun List<Tlv>.findTlv(tag: Tag): Tlv? = filterTlv(tag).firstOrNull()
