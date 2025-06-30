package org.openecard.sc.pace.oid

import org.openecard.sc.pace.oid.EacObjectIdentifier.ID_PK
import org.openecard.sc.pace.oid.EacObjectIdentifier.ID_PS

object PsaObjectIdentifier {
	/**
	 * Pseudonymous Signature Authentication
	 *
	 * `id-PSA OBJECT IDENTIFIER ::= { id-PS 1 }`
	 */
	const val ID_PSA = "$ID_PS.1"

	/**
	 * Pseudonymous Signature Public Key
	 *
	 * `id-PS-PK OBJECT IDENTIFIER ::= { bsi-de protocols(2) smartcards(2) PK(1) 3 }`
	 */
	const val ID_PS_PK = "$ID_PK.3"

	const val ID_PSA_ECDH_ECSCHNORR = "$ID_PSA.2"
	const val ID_PSA_ECDH_ECSCHNORR_SHA256 = "$ID_PSA_ECDH_ECSCHNORR.3"
	const val ID_PSA_ECDH_ECSCHNORR_SHA384 = "$ID_PSA_ECDH_ECSCHNORR.4"
	const val ID_PSA_ECDH_ECSCHNORR_SHA512 = "$ID_PSA_ECDH_ECSCHNORR.5"

	const val ID_PS_PK_ECDH_ECSCHNORR = "$ID_PS_PK.2"
}
