package org.openecard.sc.pace.crypto

interface CmacKey {
	fun signer(): Cmac
}

interface Cmac {
	fun update(data: ByteArray)

	fun sign(): ByteArray
}

expect fun cmacKey(key: ByteArray): CmacKey
