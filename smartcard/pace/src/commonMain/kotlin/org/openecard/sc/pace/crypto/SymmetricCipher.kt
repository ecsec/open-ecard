package org.openecard.sc.pace.crypto

import org.openecard.sc.pace.crypto.whyoleg.aesCbcKey
import org.openecard.sc.pace.crypto.whyoleg.aesEcbKey
import org.openecard.sc.pace.crypto.whyoleg.crypto

interface AesKey<C : SymmetricCipher> {
	fun cipher(padding: Boolean): C
}

sealed interface SymmetricCipher

interface SymmetricCipherPlain : SymmetricCipher {
	fun encrypt(plainData: ByteArray): ByteArray

	fun decrypt(encData: ByteArray): ByteArray
}

interface SymmetricCipherIv : SymmetricCipher {
	fun encryptWithIv(
		iv: ByteArray,
		plainData: ByteArray,
	): ByteArray

	fun decryptWithIv(
		iv: ByteArray,
		encData: ByteArray,
	): ByteArray
}

fun aesEcbKey(key: ByteArray): AesKey<SymmetricCipherPlain> = crypto.aesEcbKey(key)

fun aesCbcKey(key: ByteArray): AesKey<SymmetricCipherIv> = crypto.aesCbcKey(key)
