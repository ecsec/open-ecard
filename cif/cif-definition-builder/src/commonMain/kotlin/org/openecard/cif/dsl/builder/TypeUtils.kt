package org.openecard.cif.dsl.builder

import org.openecard.utils.common.hex
import org.openecard.utils.serialization.PrintableUByteArray
import org.openecard.utils.serialization.toPrintable

@OptIn(ExperimentalUnsignedTypes::class)
operator fun String.unaryPlus(): PrintableUByteArray = hex(this).toPrintable()
