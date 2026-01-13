package org.openecard.addons.tr03124.transport

import org.openecard.sc.pace.crypto.Digest
import org.openecard.sc.pace.crypto.digest

internal fun ByteArray.sha256(): ByteArray =
	digest(Digest.Algorithms.SHA256).let {
		it.update(this)
		it.digest()
	}
