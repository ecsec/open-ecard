package org.openecard.sc.pace.crypto

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

expect fun aesEcbKey(key: ByteArray): AesKey<SymmetricCipherPlain>

expect fun aesCbcKey(key: ByteArray): AesKey<SymmetricCipherIv>
