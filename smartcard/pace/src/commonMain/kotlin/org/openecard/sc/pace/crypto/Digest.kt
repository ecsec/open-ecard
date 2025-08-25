package org.openecard.sc.pace.crypto

interface Digest {
	fun update(data: ByteArray)

	fun digest(): ByteArray

	enum class Algorithms {
		SHA1,
		SHA256,
	}
}

expect fun digest(algo: Digest.Algorithms): Digest
