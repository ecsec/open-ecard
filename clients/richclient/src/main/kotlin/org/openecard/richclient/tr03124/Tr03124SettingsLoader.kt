package org.openecard.richclient.tr03124

import org.openecard.addons.tr03124.Tr03124Config
import org.openecard.common.OpenecardProperties

object Tr03124SettingsLoader {
	const val REMOVE_CARD_DIALOG = "notification.omit_show_remove_card"
	const val USE_NON_BSI_CIPHERS = "tls.use_non_bsi_ciphers"
	const val DISABLE_KEY_SIZE_CHECK = "tls.disable_key_size_check"

	fun loadFromProperties() {
		Tr03124Config.nonBsiApprovedCiphers = OpenecardProperties.getProperty(USE_NON_BSI_CIPHERS).toBoolean()
		Tr03124Config.disableKeySizeCheck = OpenecardProperties.getProperty(DISABLE_KEY_SIZE_CHECK).toBoolean()
	}
}
