package org.openecard.sc.pace.crypto

class Kdf(
	val keyLength: Int,
) {
	private val digestAlg =
		when (keyLength) {
			16 -> Digest.Algorithms.SHA1
			32 -> Digest.Algorithms.SHA256
			else -> throw IllegalArgumentException("Invalid key length ($keyLength) specified")
		}

	/**
	 * Derive key for encryption.
	 *
	 * @param secret Secret
	 * @return Key for message en/decryption (Key_PI)
	 */
	fun derivePi(secret: ByteArray): ByteArray = derive(secret, 3.toByte(), null)

	/**
	 * Derive key for message authentication.
	 *
	 * @param secret Secret
	 * @param nonce Nonce
	 * @return Key for message authentication (Key_MAC)
	 */
	fun deriveMac(
		secret: ByteArray,
		nonce: ByteArray? = null,
	): ByteArray = derive(secret, 2.toByte(), nonce)

	/**
	 * Derive key for message encryption.
	 *
	 * @param secret Secret
	 * @param nonce Nonce
	 * @return Key for message encryption (Key_ENC)
	 */
	fun deriveEnc(
		secret: ByteArray,
		nonce: ByteArray? = null,
	): ByteArray = derive(secret, 1.toByte(), nonce)

	private fun derive(
		secret: ByteArray,
		counter: Byte,
		nonce: ByteArray?,
	): ByteArray {
		val c = byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x00.toByte(), counter)

		val h = digest(digestAlg)
		h.update(secret)
		if (nonce != null) {
			h.update(nonce)
		}
		h.update(c)

		val hash = h.digest()
		return hash.copyOf(keyLength)
	}
}
