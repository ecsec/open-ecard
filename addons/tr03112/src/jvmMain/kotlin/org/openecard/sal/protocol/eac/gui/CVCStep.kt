/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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
package org.openecard.sal.protocol.eac.gui

import org.openecard.gui.definition.Document
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.gui.definition.ToggleText
import org.openecard.i18n.I18N
import org.openecard.sal.protocol.eac.EACData
import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * CVC GUI step for EAC.
 *
 * @author Tobias Wich
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 */
class CVCStep(
	private val eacData: EACData,
) : Step(STEP_ID, I18N.strings.eac_step_cvc_title.localized()) {
	init {
		description = I18N.strings.eac_step_cvc_description.localized()

		// create step elements
		addElements()
	}

	@Suppress("SimpleDateFormat")
	private fun addElements() {
		val description = Text()
		description.text = I18N.strings.eac_step_cvc_description.localized()
		inputInfoUnits.add(description)

		// SubjectName
		val subjectName = ToggleText()
		subjectName.id = "SubjectName"
		subjectName.title = I18N.strings.eac_cvc_subject_name.localized()
		subjectName.text = eacData.certificateDescription.subjectName!!
		inputInfoUnits.add(subjectName)

		// SubjectURL
		val subjectURL = ToggleText()
		subjectURL.id = "SubjectURL"
		subjectURL.title = I18N.strings.eac_cvc_subject_url.localized()
		if (eacData.certificateDescription.subjectURL != null) {
			subjectURL.text = eacData.certificateDescription.subjectURL!!
		} else {
			subjectURL.text = ""
		}
		inputInfoUnits.add(subjectURL)

		// TermsOfUsage
		val termsOfUsage = ToggleText()
		termsOfUsage.id = "TermsOfUsage"
		termsOfUsage.title = I18N.strings.eac_cvc_termsofusage.localized()
		val doc = Document()
		doc.mimeType = eacData.certificateDescription.getTermsOfUsageMimeType()
		doc.value = eacData.certificateDescription.getTermsOfUsageBytes()
		termsOfUsage.document = doc
		termsOfUsage.isCollapsed = true
		inputInfoUnits.add(termsOfUsage)

		// Validity
		val dateFormat: DateFormat =
			try {
				SimpleDateFormat(
					I18N.strings.eac_cvc_validity_format.localized(),
				)
			} catch (_: IllegalArgumentException) {
				SimpleDateFormat()
			}
		val sb = StringBuilder(150)
		sb.append(I18N.strings.eac_cvc_validity_from.localized())
		sb.append(" ")
		sb.append(dateFormat.format(eacData.certificate.getEffectiveDate().getTime()))
		sb.append(" ")
		sb.append(I18N.strings.eac_cvc_validity_to.localized())
		sb.append(" ")
		sb.append(dateFormat.format(eacData.certificate.getExpirationDate().getTime()))

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
		issuerName.text = eacData.certificateDescription.issuerName!!
		issuerName.isCollapsed = true
		inputInfoUnits.add(issuerName)

		// IssuerURL
		val issuerURL = ToggleText()
		issuerURL.id = "IssuerURL"
		issuerURL.title = I18N.strings.eac_cvc_issuer_url.localized()
		// issuer url is optional so perform a null check
		if (eacData.certificateDescription.issuerURL != null) {
			issuerURL.text = eacData.certificateDescription.issuerURL!!
		} else {
			issuerURL.text = ""
		}
		issuerURL.isCollapsed = true
		inputInfoUnits.add(issuerURL)
	}

	companion object {
		const val STEP_ID: String = "PROTOCOL_EAC_GUI_STEP_CVC"
	}
}
