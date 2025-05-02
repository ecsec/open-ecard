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

/**
 * CVC GUI step for EAC.
 *
 * @author Tobias Wich
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 */
class CVCStep(private val eacData: EACData) : Step(STEP_ID, LANG.translationForKey(TITLE)) {
    init {
        setDescription(LANG.translationForKey(STEP_DESCRIPTION))

        // create step elements
        addElements()
    }

    private fun addElements() {
        val description = Text()
        description.setText(LANG.translationForKey(DESCRIPTION))
        getInputInfoUnits().add(description)

        // SubjectName
        val subjectName = ToggleText()
        subjectName.setID("SubjectName")
        subjectName.setTitle(LANG.translationForKey(SUBJECT_NAME))
        subjectName.setText(eacData.certificateDescription.subjectName)
        getInputInfoUnits().add(subjectName)

        // SubjectURL
        val subjectURL = ToggleText()
        subjectURL.setID("SubjectURL")
        subjectURL.setTitle(LANG.translationForKey(SUBJECT_URL))
        if (eacData.certificateDescription.subjectURL != null) {
            subjectURL.setText(eacData.certificateDescription.subjectURL)
        } else {
            subjectURL.setText("")
        }
        getInputInfoUnits().add(subjectURL)

        // TermsOfUsage
        val termsOfUsage = ToggleText()
        termsOfUsage.setID("TermsOfUsage")
        termsOfUsage.setTitle(LANG.translationForKey(TERMS_OF_USAGE))
        val doc = Document()
        doc.setMimeType(eacData.certificateDescription.getTermsOfUsageMimeType())
        doc.setValue(eacData.certificateDescription.getTermsOfUsageBytes())
        termsOfUsage.setDocument(doc)
        termsOfUsage.setCollapsed(true)
        getInputInfoUnits().add(termsOfUsage)

        // Validity
        var dateFormat: DateFormat?
        try {
            dateFormat = SimpleDateFormat(LANG.translationForKey(VALIDITY_FORMAT))
        } catch (e: IllegalArgumentException) {
            dateFormat = SimpleDateFormat()
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
        validity.setID("Validity")
        validity.setTitle(LANG.translationForKey(VALIDITY))
        validity.setText(sb.toString())
        validity.setCollapsed(true)
        getInputInfoUnits().add(validity)

        // IssuerName
        val issuerName = ToggleText()
        issuerName.setID("IssuerName")
        issuerName.setTitle(LANG.translationForKey(ISSUER_NAME))
        issuerName.setText(eacData.certificateDescription.issuerName)
        issuerName.setCollapsed(true)
        getInputInfoUnits().add(issuerName)

        // IssuerURL
        val issuerURL = ToggleText()
        issuerURL.setID("IssuerURL")
        issuerURL.setTitle(LANG.translationForKey(ISSUER_URL))
        // issuer url is optional so perform a null check
        if (eacData.certificateDescription.issuerURL != null) {
            issuerURL.setText(eacData.certificateDescription.issuerURL)
        } else {
            issuerURL.setText("")
        }
        issuerURL.setCollapsed(true)
        getInputInfoUnits().add(issuerURL)
    }

    companion object {
        private val LANG: I18n = I18n.getTranslation("eac")

        // step id
        const val STEP_ID: String = "PROTOCOL_EAC_GUI_STEP_CVC"

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

        fun createDummy(): Step {
            val s = Step(STEP_ID)
            s.setTitle(LANG.translationForKey(TITLE))
            s.setDescription(LANG.translationForKey(STEP_DESCRIPTION))
            return s
        }
    }
}
