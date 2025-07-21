package org.openecard.sc.pcsc

import android.nfc.TagLostException
import org.openecard.sc.iface.InternalSystemError
import org.openecard.sc.iface.RemovedCard
import java.io.IOException

fun <R> mapScioError(block: () -> R): R {
	try {
		return block()
	} catch (ex: Throwable) {
		when (ex) {
			is TagLostException,
			is SecurityException,
			is IOException,
			-> {
				throw RemovedCard(cause = ex)
			}
			else -> throw InternalSystemError(cause = ex)
		}
	}
}
