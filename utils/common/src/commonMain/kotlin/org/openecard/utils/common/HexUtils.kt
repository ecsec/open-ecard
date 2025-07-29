package org.openecard.utils.common

@OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)
fun hex(value: String) = value.replace("""\s""".toRegex(), "").hexToUByteArray()
