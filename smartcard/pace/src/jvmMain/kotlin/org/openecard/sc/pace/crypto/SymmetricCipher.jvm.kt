package org.openecard.sc.pace.crypto

import dev.whyoleg.cryptography.DelicateCryptographyApi
import org.openecard.sc.pace.crypto.whyoleg.aesCbcKey
import org.openecard.sc.pace.crypto.whyoleg.aesEcbKey
import org.openecard.sc.pace.crypto.whyoleg.crypto

actual fun aesEcbKey(key: ByteArray): AesKey<SymmetricCipherPlain> = crypto.aesEcbKey(key)

@OptIn(DelicateCryptographyApi::class)
actual fun aesCbcKey(key: ByteArray): AesKey<SymmetricCipherIv> = crypto.aesCbcKey(key)
