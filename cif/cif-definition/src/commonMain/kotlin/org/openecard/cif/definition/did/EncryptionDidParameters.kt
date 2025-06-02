package org.openecard.cif.definition.did

import org.openecard.utils.serialization.PrintableUByteArray

data class EncryptionDidParameters(
	override val key: KeyRefDefinition,
	override val certificates: List<String>,
	/**
	 * Name of the [JCA Cipher Algorithm](https://docs.oracle.com/en/java/javase/21/docs/specs/security/standard-names.html#signature-algorithms).
	 */
	val encryptionAlgorithm: String,
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
) : GenericCryptoDidParameters
