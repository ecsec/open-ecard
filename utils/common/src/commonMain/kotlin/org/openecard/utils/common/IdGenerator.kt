package org.openecard.utils.common

import kotlin.io.encoding.Base64
import kotlin.random.Random

var sessionRandomBytes: Int = 16

@OptIn(ExperimentalStdlibApi::class)
fun Random.generateSessionId(numRandomBytes: Int = sessionRandomBytes): String = generateSessionIdBase16(numRandomBytes)

@OptIn(ExperimentalStdlibApi::class)
fun Random.generateSessionIdBase64(
	numRandomBytes: Int = sessionRandomBytes,
	urlSafe: Boolean = true,
	withPadding: Boolean = false,
): String {
	val padOption =
		if (withPadding) {
			Base64.PaddingOption.PRESENT
		} else {
			Base64.PaddingOption.ABSENT
		}
	val b64 =
		if (urlSafe) {
			Base64.UrlSafe
		} else {
			Base64.Default
		}.withPadding(padOption)
	return b64.encode(this.nextBytes(numRandomBytes))
}

@OptIn(ExperimentalStdlibApi::class)
fun Random.generateSessionIdBase16(numRandomBytes: Int = sessionRandomBytes): String =
	this.nextBytes(numRandomBytes).toHexString()
