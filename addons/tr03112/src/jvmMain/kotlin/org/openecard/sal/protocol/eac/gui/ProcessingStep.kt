/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

import org.openecard.common.AppVersion.name
import org.openecard.common.I18n
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text

/**
 * Step with instructing the user to wait for the authentication to finish.
 *
 * @author Tobias Wich
 */
class ProcessingStep : Step(STEP_ID, lang.translationForKey(TITLE)) {
    init {
        setDescription(lang.translationForKey(STEP_DESCRIPTION))
        setInstantReturn(true)
        setReversible(false)

        val desc = Text()
        desc.setText(lang.translationForKey(DESCRIPTION, name))
        getInputInfoUnits().add(desc)
    }

    companion object {
        private val lang: I18n = I18n.getTranslation("eac")

        // step id
        const val STEP_ID: String = "PROTOCOL_GUI_STEP_PROCESSING"

        // GUI translation constants
        private const val TITLE = "step_processing_title"
        private const val STEP_DESCRIPTION = "step_processing_step_description"
        private const val DESCRIPTION = "step_processing_description"
    }
}
