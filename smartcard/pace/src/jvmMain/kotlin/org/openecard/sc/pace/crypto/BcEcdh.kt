package org.openecard.sc.pace.crypto

import org.bouncycastle.crypto.agreement.ECDHBasicAgreement

object BcEcdh {
	@OptIn(ExperimentalUnsignedTypes::class)
	fun generateSharedSecret(
		sk: BcEcKeyPair.BcEcKeyPrivate,
		pk: BcEcKeyPair.BcEcKeyPublic,
	): UByteArray {
		val ecdh = ECDHBasicAgreement()
		ecdh.init(sk.key)
		val sharedSecret = ecdh.calculateAgreement(pk.key)
		return sharedSecret.toByteArray().toUByteArray()
	}
}
