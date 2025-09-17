package org.openecard.richclient.sc

import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.EgkCif
import org.openecard.cif.bundled.EgkCifDefinitions
import org.openecard.cif.bundled.HbaCif
import org.openecard.cif.bundled.HbaDefinitions
import org.openecard.cif.bundled.NpaCif
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.cif.definition.CardInfoDefinition
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.richclient.MR
import org.openecard.sal.sc.recognition.DirectCardRecognition
import java.awt.image.BufferedImage

interface CifDb {
	val supportedCardTypes: Set<String>
		get() = supportedCifs.map { it.metadata.id }.toSet()
	val supportedCifs: Set<CardInfoDefinition>

	fun getCardRecognition(): DirectCardRecognition {
		check(supportedCardTypes.isNotEmpty())
		return DirectCardRecognition(CompleteTree.calls.removeUnsupported(supportedCardTypes))
	}

	fun getCardType(cardType: String): String

	fun getCardImage(cardType: String): BufferedImage

	companion object {
		const val NO_TERMINAL = "http://openecard.org/cif/no-terminal"
		const val NO_CARD = "http://openecard.org/cif/no-card"

		object Bundled : CifDb {
			override val supportedCifs =
				setOf(
					NpaCif,
					EgkCif,
					HbaCif,
				)

			override fun getCardType(cardType: String): String =
				when (cardType) {
					NO_TERMINAL -> MR.strings.status_no_terminal.localized()
					NO_CARD -> MR.strings.status_no_card.localized()
					NpaDefinitions.cardType -> MR.strings.status_npa.localized()
					EgkCifDefinitions.cardType -> MR.strings.status_egk.localized()
					HbaDefinitions.cardType -> MR.strings.status_hba.localized()
					else -> MR.strings.status_unknown_card.localized()
				}

			override fun getCardImage(cardType: String): BufferedImage =
				when (cardType) {
					NO_TERMINAL -> MR.images.no_terminal
					NO_CARD -> MR.images.no_card
					NpaDefinitions.cardType -> MR.images.npa
					EgkCifDefinitions.cardType -> MR.images.egk
					HbaDefinitions.cardType -> MR.images.hba
					else -> MR.images.unknown_card
				}.image
		}
	}
}
