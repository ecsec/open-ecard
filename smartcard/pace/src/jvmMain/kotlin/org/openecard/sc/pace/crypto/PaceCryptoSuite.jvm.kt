package org.openecard.sc.pace.crypto

import org.openecard.sc.pace.asn1.EfCardAccess

actual fun cryptoSuite(
	paceInfos: EfCardAccess.PaceInfos,
	password: String,
): PaceCryptoSuite = JvmPaceCryptoSuite(paceInfos, password)
