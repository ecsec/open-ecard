package org.openecard.sc.pcsc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.openecard.sc.iface.withContext

class PcscCardWatcher(
	val callbacks: PcscCardWatcherCallbacks,
	val context: CoroutineScope,
) {
	var job: Job? = null

	fun start() {
		job =
			context.launch {
				PcscTerminalFactory.instance.load().withContext { ctx ->
					var currentTerminals = ctx.list()
				}
			}
	}
}
