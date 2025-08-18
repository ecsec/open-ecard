package org.openecard.sc.pace.crypto

import org.openecard.sc.pace.crypto.whyoleg.cmacKey
import org.openecard.sc.pace.crypto.whyoleg.crypto

actual fun cmacKey(key: ByteArray): CmacKey = crypto.cmacKey(key)
