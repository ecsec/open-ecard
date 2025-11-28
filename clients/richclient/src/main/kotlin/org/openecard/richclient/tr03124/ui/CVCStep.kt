/*
 * Copyright (C) 2025 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 */

package org.openecard.richclient.tr03124.ui

import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import org.openecard.addons.tr03124.eac.EacUiData
import org.openecard.i18n.I18N
import org.openecard.richclient.processui.definition.Document
import org.openecard.richclient.processui.definition.Step
import org.openecard.richclient.processui.definition.Text
import org.openecard.richclient.processui.definition.ToggleText
import org.openecard.sc.pace.cvc.CvcDate.Companion.toLocalDate
import org.openecard.sc.pace.cvc.TermsOfUse

/**
 * CVC GUI step for EAC.
 *
 * @author Tobias Wich
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 */
class CVCStep(
	private val eacData: EacUiData,
) : Step(id = STEP_ID, title = I18N.strings.eac_step_cvc_title.localized()) {
	init {
		description = I18N.strings.eac_step_cvc_description.localized()

		// create step elements
		addElements()
	}

	@OptIn(FormatStringsInDatetimeFormats::class)
	private fun addElements() {
		val description = Text()
		description.text = I18N.strings.eac_step_cvc_description.localized()
		inputInfoUnits.add(description)

		// SubjectName
		val subjectName = ToggleText()
		subjectName.id = "SubjectName"
		subjectName.title = I18N.strings.eac_cvc_subject_name.localized()
		subjectName.text = eacData.certificateDescription.subjectName
		inputInfoUnits.add(subjectName)

		// SubjectURL
		val subjectURL = ToggleText()
		subjectURL.id = "SubjectURL"
		subjectURL.title = I18N.strings.eac_cvc_subject_url.localized()
		subjectURL.text = eacData.certificateDescription.subjectUrl ?: ""
		inputInfoUnits.add(subjectURL)

		// TermsOfUsage
		val termsOfUsage = ToggleText()
		termsOfUsage.id = "TermsOfUsage"
		termsOfUsage.title = I18N.strings.eac_cvc_termsofusage.localized()
		termsOfUsage.document =
			when (val tou = eacData.certificateDescription.termsOfUse) {
				is TermsOfUse.Html -> Document("text/html", tou.html.encodeToByteArray())
				is TermsOfUse.Pdf -> Document("application/pdf", tou.pdf)
				is TermsOfUse.PlainText -> Document("text/plain", tou.text.encodeToByteArray())
			}
		termsOfUsage.isCollapsed = true
		inputInfoUnits.add(termsOfUsage)

		// Validity
		val dateFormat = LocalDate.Format { this.byUnicodePattern(I18N.strings.eac_cvc_validity_format.localized()) }
		val sb = StringBuilder(150)
		sb.append(I18N.strings.eac_cvc_validity_from.localized())
		sb.append(" ")
		sb.append(
			eacData.terminalCert.validFrom
				.toLocalDate()
				.format(dateFormat),
		)
		sb.append(" ")
		sb.append(I18N.strings.eac_cvc_validity_to.localized())
		sb.append(" ")
		sb.append(
			eacData.terminalCert.validUntil
				.toLocalDate()
				.format(dateFormat),
		)

		val validity = ToggleText()
		validity.id = "Validity"
		validity.title = I18N.strings.eac_cvc_validity.localized()
		validity.text = sb.toString()
		validity.isCollapsed = true
		inputInfoUnits.add(validity)

		// IssuerName
		val issuerName = ToggleText()
		issuerName.id = "IssuerName"
		issuerName.title = I18N.strings.eac_cvc_issuer_name.localized()
		issuerName.text = eacData.certificateDescription.issuerName
		issuerName.isCollapsed = true
		inputInfoUnits.add(issuerName)

		// IssuerURL
		val issuerURL = ToggleText()
		issuerURL.id = "IssuerURL"
		issuerURL.title = I18N.strings.eac_cvc_issuer_url.localized()
		// issuer url is optional so perform a null check
		issuerURL.text = eacData.certificateDescription.issuerUrl ?: ""
		issuerURL.isCollapsed = true
		inputInfoUnits.add(issuerURL)
	}

	companion object {
		const val STEP_ID: String = "PROTOCOL_EAC_GUI_STEP_CVC"
	}
}
