package org.openecard.sc.pace.crypto.whyoleg

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import org.openecard.sc.pace.crypto.Cmac
import org.openecard.sc.pace.crypto.CmacKey

fun CryptographyProvider.cmacKey(key: ByteArray): CmacKey {
	val key = this.get(AES.CMAC).keyDecoder().decodeFromByteArrayBlocking(AES.Key.Format.RAW, key)
	return object : CmacKey {
		override fun signer(): Cmac {
			val cmac = key.signatureGenerator().createSignFunction()
			return object : Cmac {
				override fun update(data: ByteArray) {
					cmac.update(data)
				}

				override fun sign(): ByteArray = cmac.signToByteArray()
			}
		}
	}
}
