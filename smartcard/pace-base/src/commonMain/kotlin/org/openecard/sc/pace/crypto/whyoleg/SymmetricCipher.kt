package org.openecard.sc.pace.crypto.whyoleg

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import org.openecard.sc.pace.crypto.AesKey
import org.openecard.sc.pace.crypto.SymmetricCipherIv
import org.openecard.sc.pace.crypto.SymmetricCipherPlain

@OptIn(DelicateCryptographyApi::class)
fun CryptographyProvider.aesEcbKey(key: ByteArray): AesKey<SymmetricCipherPlain> {
	val aesKey = this.get(AES.ECB).keyDecoder().decodeFromByteArrayBlocking(AES.Key.Format.RAW, key)
	return object : AesKey<SymmetricCipherPlain> {
		override fun cipher(padding: Boolean): SymmetricCipherPlain =
			object : SymmetricCipherPlain {
				override fun encrypt(plainData: ByteArray) = aesKey.cipher(padding).encryptBlocking(plainData)

				override fun decrypt(encData: ByteArray): ByteArray = aesKey.cipher(padding).decryptBlocking(encData)
			}
	}
}

@OptIn(DelicateCryptographyApi::class)
fun CryptographyProvider.aesCbcKey(key: ByteArray): AesKey<SymmetricCipherIv> {
	val aesKey = this.get(AES.CBC).keyDecoder().decodeFromByteArrayBlocking(AES.Key.Format.RAW, key)
	return object : AesKey<SymmetricCipherIv> {
		override fun cipher(padding: Boolean): SymmetricCipherIv =
			object : SymmetricCipherIv {
				override fun encryptWithIv(
					iv: ByteArray,
					plainData: ByteArray,
				) = aesKey.cipher(padding).encryptWithIvBlocking(iv, plainData)

				override fun decryptWithIv(
					iv: ByteArray,
					encData: ByteArray,
				): ByteArray = aesKey.cipher(padding).decryptWithIvBlocking(iv, encData)
			}
	}
}
