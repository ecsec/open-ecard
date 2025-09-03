package org.openecard.richclient

import org.openecard.cif.bundled.EgkCifDefinitions
import org.openecard.cif.bundled.HbaDefinitions
import org.openecard.cif.bundled.NpaDefinitions
import java.awt.image.BufferedImage

class CifDb(
	val supportedCardTypes: Set<String>,
) {
	fun getCardType(cardType: String): String =
		when (cardType) {
			NO_TERMINAL -> MR.strings.status_no_terminal.localized()
			NO_CARD -> MR.strings.status_no_card.localized()
			NpaDefinitions.cardType -> MR.strings.status_npa.localized()
			EgkCifDefinitions.cardType -> MR.strings.status_egk.localized()
			HbaDefinitions.cardType -> MR.strings.status_hba.localized()
			else -> MR.strings.status_unknown_card.localized()
		}

	fun getCardImage(cardType: String): BufferedImage =
		when (cardType) {
			NO_TERMINAL -> MR.images.no_terminal
			NO_CARD -> MR.images.no_card
			NpaDefinitions.cardType -> MR.images.npa
			EgkCifDefinitions.cardType -> MR.images.egk
			HbaDefinitions.cardType -> MR.images.hba
			else -> MR.images.unknown_card
		}.image

	companion object {
		const val NO_TERMINAL = "http://openecard.org/cif/no-terminal"
		const val NO_CARD = "http://openecard.org/cif/no-card"
	}
}
