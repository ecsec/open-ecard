package org.openecard.cif.definition.did

import kotlinx.serialization.Serializable
import org.openecard.cif.definition.cardcall.TemplateApduCallDefinition
import org.openecard.utils.serialization.PrintableUByteArray

@Serializable
data class SignatureDidParameters(
	override val key: KeyRefDefinition,
	override val certificates: List<String>,
	/**
	 * Name of the [JCA Signature Algorithm](https://docs.oracle.com/en/java/javase/21/docs/specs/security/standard-names.html#signature-algorithms).
	 */
	val signatureAlgorithm: String,
	val sigGen: SignatureGenerationInfo,
) : GenericCryptoDidParameters

sealed interface SignatureGenerationInfo {
	data class StandardInfo(
		val info: List<SignatureGenerationInfoType>,
		val hashGenInfo: HashGenerationInfoType?,
		/**
		 * MAY contain the card-specific cryptographic mechanism reference according to [ISO7816-4] (Table 33) and hence the
		 * content of the CardAlgRef-element MUST be used in an MSE-command with Tag ‘80’.
		 */
		val cardAlgRef: PrintableUByteArray?,
		/**
		 * MAY contain the card-specific reference for a hash algorithm, if the present AlgorithmInfo-element refers to a
		 * signature algorithm and CardAlgRef does not implicitly specify the hash algorithm, which is to be used for the
		 * signature generation.
		 */
		val hashAlgRef: PrintableUByteArray?,
	) : SignatureGenerationInfo

	data class TemplateInfo(
		val info: List<TemplateApduCallDefinition>,
	) : SignatureGenerationInfo
}

enum class SignatureGenerationInfoType {
	MSE_RESTORE,
	MSE_HASH,
	PSO_HASH,
	MSE_KEY,
	MSE_DS,
	MSE_KEY_DS,
	PSO_CDS,
	INT_AUTH,
}

enum class HashGenerationInfoType {
	NOT_ON_CARD,
	COMPLETELY_ON_CARD,
	LAST_ROUND_ON_CARD,
}
