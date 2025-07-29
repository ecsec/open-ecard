package org.openecard.sc.pace.crypto.whyoleg

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.SHA1
import dev.whyoleg.cryptography.algorithms.SHA256
import org.openecard.sc.pace.crypto.Digest

@OptIn(DelicateCryptographyApi::class)
fun CryptographyProvider.digest(algo: Digest.Algorithms): Digest {
	val digest =
		when (algo) {
			Digest.Algorithms.SHA1 -> this.get(SHA1)
			Digest.Algorithms.SHA256 -> this.get(SHA256)
		}.hasher().createHashFunction()
	return object : Digest {
		override fun update(data: ByteArray) {
			digest.update(data)
		}

		override fun digest(): ByteArray = digest.hashToByteArray()
	}
}
