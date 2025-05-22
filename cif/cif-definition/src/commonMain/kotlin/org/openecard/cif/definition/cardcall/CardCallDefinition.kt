package org.openecard.cif.definition.cardcall

import org.openecard.utils.serialization.PrintableUByteArray

interface CardCallDefinition

data class ApduCallDefinition(
	val command: PrintableUByteArray,
	val responses: Set<ResponseApduDefinition>,
) : CardCallDefinition

interface ApiCallDefinition : CardCallDefinition
