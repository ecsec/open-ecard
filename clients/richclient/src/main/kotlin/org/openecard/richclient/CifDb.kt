package org.openecard.richclient

import org.openecard.cif.bundled.EgkCifDefinitions
import org.openecard.cif.bundled.HbaDefinitions
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.i18n.I18N
import java.io.ByteArrayInputStream
import java.io.InputStream

class CifDb(
	val supportedCardTypes: Set<String>,
) {
	fun resolveCardType(cardType: String): String =
		when (cardType) {
			NpaDefinitions.cardType -> I18N.strings.richclient_status_npa.localized()

			EgkCifDefinitions.cardType -> I18N.strings.richclient_status_egk.localized()

			HbaDefinitions.cardType -> I18N.strings.richclient_status_hba.localized()

			else -> I18N.strings.richclient_status_unknowncard.localized()
		}

	fun getCardType(cardType: String?): String {
		if (cardType != null) {
			return resolveCardType(cardType)
		} else {
			return I18N.strings.richclient_status_nocard.localized()
		}
	}

	fun getCardImage(cardType: String?): InputStream {
		val imageBytes =
			when (cardType) {
				NpaDefinitions.cardType -> I18N.images.npa

				EgkCifDefinitions.cardType -> I18N.images.egk

				HbaDefinitions.cardType -> I18N.images.hba

				else -> I18N.images.unknown_card
			}

		val filePath = imageBytes.filePath

		return filePath.let {
			val inputStream = CifDb::class.java.classLoader?.getResourceAsStream(it)
			inputStream ?: ByteArrayInputStream(ByteArray(0))
		}
	}
}
