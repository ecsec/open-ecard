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

import dev.icerock.moko.resources.format
import org.openecard.common.AppVersion.name
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.i18n.I18N

/**
 * Step with instructing the user to wait for the authentication to finish.
 *
 * @author Tobias Wich
 */
class ProcessingStep :
	Step(
		STEP_ID,
		I18N.strings.eac_step_processing_title.localized(),
	) {
	init {
		description = I18N.strings.eac_step_processing_step_description.localized()

		isInstantReturn = true
		isReversible = false

		val desc = Text()
		desc.text =
			I18N.strings.eac_step_processing_description
				.format(name)
				.localized()
		inputInfoUnits.add(desc)
	}

	companion object {
		// step id
		const val STEP_ID: String = "PROTOCOL_GUI_STEP_PROCESSING"
	}
}
