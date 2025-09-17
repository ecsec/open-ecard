package org.openecard.sc.pace.crypto.whyoleg

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.providers.openssl3.Openssl3

internal actual val crypto: CryptographyProvider = CryptographyProvider.Openssl3
