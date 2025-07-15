package org.openecard.sc.pace.asn1

import dev.whyoleg.cryptography.serialization.asn1.Der
import dev.whyoleg.cryptography.serialization.asn1.ObjectIdentifier
import kotlinx.serialization.decodeFromByteArray
import org.openecard.sc.tlv.Tlv

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
			val algorithm: ObjectIdentifier = Der.decodeFromByteArray(childTags[0].toBer().toByteArray())
			val parameters = childTags.getOrNull(1)

			// TODO: distinguish between types
			return Unknown(algorithm, parameters)
		}
	}
}
