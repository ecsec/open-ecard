package org.openecard.sc.pace.asn1

import org.openecard.sc.tlv.ObjectIdentifier
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.toObjectIdentifier

sealed class AlgorithmIdentifier(
	val algorithm: ObjectIdentifier,
	val parametersRaw: Tlv?,
) {
	class Unknown(
		algorithm: ObjectIdentifier,
		parameters: Tlv?,
	) : AlgorithmIdentifier(algorithm, parameters)

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun Tlv.toAlgorithmIdentifier(): AlgorithmIdentifier {
			val childTags = requireNotNull(this.asConstructed?.child).asList()
			val algorithm: ObjectIdentifier = childTags[0].toObjectIdentifier()
			val parameters = childTags.getOrNull(1)

			// TODO: distinguish between types
			return Unknown(algorithm, parameters)
		}
	}
}
