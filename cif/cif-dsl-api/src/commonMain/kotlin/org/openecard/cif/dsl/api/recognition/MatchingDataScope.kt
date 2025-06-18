package org.openecard.cif.dsl.api.recognition

import org.openecard.cif.definition.recognition.MatchRule
import org.openecard.cif.dsl.api.CifScope

interface MatchingDataScope : CifScope {
	@OptIn(ExperimentalUnsignedTypes::class)
	var value: UByteArray
	var offset: UInt
	var length: UInt?

	@OptIn(ExperimentalUnsignedTypes::class)
	var mask: UByteArray?
	var rule: MatchRule?
}
