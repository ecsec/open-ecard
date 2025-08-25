package org.openecard.sc.pace.crypto

import org.openecard.sc.pace.crypto.whyoleg.crypto
import org.openecard.sc.pace.crypto.whyoleg.digest

actual fun digest(algo: Digest.Algorithms): Digest = crypto.digest(algo)
