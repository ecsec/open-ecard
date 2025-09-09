package org.openecard.sc.pace.crypto

import org.openecard.sc.pace.crypto.whyoleg.crypto
import org.openecard.sc.pace.crypto.whyoleg.digest

interface Digest {
	fun update(data: ByteArray)

	fun digest(): ByteArray

	enum class Algorithms {
		SHA1,
		SHA256,
	}
}

fun digest(algo: Digest.Algorithms): Digest = crypto.digest(algo)
