package org.openecard.sc.iface.info

import org.openecard.sc.iface.Atr
import org.openecard.sc.iface.CardCapabilities

data class SmartcardInfo(
	val atr: Atr,
	val efAtr: EfAtr?,
	val efDir: EfDir?,
) {
	val capabilities: CardCapabilities? by lazy {
		// EF.ATR is preferred, as it likely contains the correct data
		efAtr?.historicalBytes?.cardCapabilities ?: atr.historicalBytes?.cardCapabilities
	}
}
