package org.openecard.sc.pace.crypto.whyoleg

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDH
import org.openecard.sc.pace.asn1.StandardizedDomainParameters
import org.openecard.sc.pace.crypto.KeyAgreement

fun CryptographyProvider.ecdhAgreement(curve: StandardizedDomainParameters.Curve): KeyAgreement {
	val curve = curve.toKotlinCrypto()

	val ecdh = this.get(ECDH)
	return object : KeyAgreement {
		override fun derive(
			privKey: ByteArray,
			pubKey: ByteArray,
		): ByteArray {
			val sk = ecdh.privateKeyDecoder(curve).decodeFromByteArrayBlocking(EC.PrivateKey.Format.RAW, privKey)
			val pk = ecdh.publicKeyDecoder(curve).decodeFromByteArrayBlocking(EC.PublicKey.Format.RAW, pubKey)
			return sk.sharedSecretGenerator().generateSharedSecretToByteArrayBlocking(pk)
		}
	}
}
