package org.openecard.richclient.pinmanagement

interface PinManagementUI {
	fun show()

	/**
	 * Aborts or finalizes the process of the pin management.
	 * This is the place to clean up state such as card connections.
	 * The method is called whenever the card specific process is closed or aborted.
	 * It may be called multiple times, so it should be robust against that.
	 */
	fun closeProcess()
}
