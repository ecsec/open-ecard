package org.openecard.cif.dsl.api.did

import org.openecard.utils.serialization.PrintableUByteArray

interface EncryptionDidParametersScope : GenericCryptoDidParametersScope {
	var encryptionAlgorithm: String
	var cardAlgRef: PrintableUByteArray?
	var hashAlgRef: PrintableUByteArray?
}
