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

import org.openecard.common.I18n
import org.openecard.gui.definition.Document
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.gui.definition.ToggleText
import org.openecard.sal.protocol.eac.EACData
import java.text.DateFormat
import java.text.SimpleDateFormat

private val LANG: I18n = I18n.getTranslation("eac")

// GUI translation constants
private const val TITLE = "step_cvc_title"
private const val STEP_DESCRIPTION = "step_cvc_step_description"
private const val DESCRIPTION = "step_cvc_description"
private const val SUBJECT_NAME = "cvc_subject_name"
private const val SUBJECT_URL = "cvc_subject_url"
private const val TERMS_OF_USAGE = "cvc_termsofusage"
private const val VALIDITY = "cvc_validity"
private const val VALIDITY_FORMAT = "cvc_validity_format"
private const val VALIDITY_FROM = "cvc_validity_from"
private const val VALIDITY_TO = "cvc_validity_to"
private const val ISSUER_NAME = "cvc_issuer_name"
private const val ISSUER_URL = "cvc_issuer_url"

/**
 * CVC GUI step for EAC.
 *
 * @author Tobias Wich
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 */
class CVCStep(
	private val eacData: EACData,
) : Step(STEP_ID, LANG.translationForKey(TITLE)) {
	init {
		description = LANG.translationForKey(STEP_DESCRIPTION)

		// create step elements
		addElements()
	}

	@Suppress("SimpleDateFormat")
	private fun addElements() {
		val description = Text()
		description.setText(LANG.translationForKey(DESCRIPTION))
		getInputInfoUnits().add(description)

		// SubjectName
		val subjectName = ToggleText()
		subjectName.id = "SubjectName"
		subjectName.title = LANG.translationForKey(SUBJECT_NAME)
		subjectName.setText(eacData.certificateDescription.subjectName)
		getInputInfoUnits().add(subjectName)

		// SubjectURL
		val subjectURL = ToggleText()
		subjectURL.id = "SubjectURL"
		subjectURL.title = LANG.translationForKey(SUBJECT_URL)
		if (eacData.certificateDescription.subjectURL != null) {
			subjectURL.setText(eacData.certificateDescription.subjectURL)
		} else {
			subjectURL.setText("")
		}
		getInputInfoUnits().add(subjectURL)

		// TermsOfUsage
		val termsOfUsage = ToggleText()
		termsOfUsage.id = "TermsOfUsage"
		termsOfUsage.title = LANG.translationForKey(TERMS_OF_USAGE)
		val doc = Document()
		doc.mimeType = eacData.certificateDescription.getTermsOfUsageMimeType()
		doc.value = eacData.certificateDescription.getTermsOfUsageBytes()
		termsOfUsage.document = doc
		termsOfUsage.isCollapsed = true
		getInputInfoUnits().add(termsOfUsage)

		// Validity
		val dateFormat: DateFormat =
			try {
				SimpleDateFormat(LANG.translationForKey(VALIDITY_FORMAT))
			} catch (_: IllegalArgumentException) {
				SimpleDateFormat()
			}
		val sb = StringBuilder(150)
		sb.append(LANG.translationForKey(VALIDITY_FROM))
		sb.append(" ")
		sb.append(dateFormat.format(eacData.certificate.getEffectiveDate().getTime()))
		sb.append(" ")
		sb.append(LANG.translationForKey(VALIDITY_TO))
		sb.append(" ")
		sb.append(dateFormat.format(eacData.certificate.getExpirationDate().getTime()))

		val validity = ToggleText()
		validity.id = "Validity"
		validity.title = LANG.translationForKey(VALIDITY)
		validity.setText(sb.toString())
		validity.isCollapsed = true
		getInputInfoUnits().add(validity)

		// IssuerName
		val issuerName = ToggleText()
		issuerName.id = "IssuerName"
		issuerName.title = LANG.translationForKey(ISSUER_NAME)
		issuerName.setText(eacData.certificateDescription.issuerName)
		issuerName.isCollapsed = true
		getInputInfoUnits().add(issuerName)

		// IssuerURL
		val issuerURL = ToggleText()
		issuerURL.id = "IssuerURL"
		issuerURL.title = LANG.translationForKey(ISSUER_URL)
		// issuer url is optional so perform a null check
		if (eacData.certificateDescription.issuerURL != null) {
			issuerURL.setText(eacData.certificateDescription.issuerURL)
		} else {
			issuerURL.setText("")
		}
		issuerURL.isCollapsed = true
		getInputInfoUnits().add(issuerURL)
	}

	companion object {
		const val STEP_ID: String = "PROTOCOL_EAC_GUI_STEP_CVC"
	}
}
