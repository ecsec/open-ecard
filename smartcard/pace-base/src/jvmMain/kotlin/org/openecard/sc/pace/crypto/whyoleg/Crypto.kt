package org.openecard.sc.pace.crypto.whyoleg

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.providers.jdk.JDK
import org.bouncycastle.jce.provider.BouncyCastleProvider

internal actual val crypto = CryptographyProvider.Companion.JDK(BouncyCastleProvider())
