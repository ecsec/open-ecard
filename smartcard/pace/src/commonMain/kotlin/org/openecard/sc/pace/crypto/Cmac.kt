package org.openecard.sc.pace.crypto

import org.openecard.sc.pace.crypto.whyoleg.cmacKey
import org.openecard.sc.pace.crypto.whyoleg.crypto

interface CmacKey {
	fun signer(): Cmac
}

interface Cmac {
	fun update(data: ByteArray)

	fun sign(): ByteArray
}

fun cmacKey(key: ByteArray): CmacKey = crypto.cmacKey(key)
