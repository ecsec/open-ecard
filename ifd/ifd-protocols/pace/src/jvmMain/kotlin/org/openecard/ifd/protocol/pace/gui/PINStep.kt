/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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
 ****************************************************************************/
package org.openecard.ifd.protocol.pace.gui

import dev.icerock.moko.resources.format
import org.openecard.gui.definition.PasswordField
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.gui.executor.ExecutionResults
import org.openecard.i18n.I18N.strings.pace_can
import org.openecard.i18n.I18N.strings.pace_mrz
import org.openecard.i18n.I18N.strings.pace_pin
import org.openecard.i18n.I18N.strings.pace_puk
import org.openecard.i18n.I18N.strings.pace_step_pace_description
import org.openecard.i18n.I18N.strings.pace_step_pace_step_description
import org.openecard.i18n.I18N.strings.pace_step_pace_title
import org.openecard.ifd.protocol.pace.common.PasswordID
import org.openecard.ifd.protocol.pace.gui.GUIContentMap.ELEMENT
import java.util.Locale

/**
 * Implements a GUI user consent step for the PIN.
 *
 * @author Moritz Horsch
 */
class PINStep(
	private val content: GUIContentMap,
) {
	/**
	 * Returns the generated step.
	 *
	 * @return Step
	 */
	@JvmField
	val step: Step
	private val passwordType: String = PasswordID.parse(((content.get(ELEMENT.PIN_ID)) as Byte?)!!)!!.name

	/**
	 * Creates a new GUI user consent step for the PIN.
	 *
	 * @param content GUI content
	 */
	init {
		this.step =
			Step(
				STEP_ID,
				pace_step_pace_title.format(passwordType).localized(),
			)
		initialize()
	}

	private fun initialize() {
		step.description = pace_step_pace_step_description.format(passwordType).localized()

		val decriptionText = pace_step_pace_description.format(passwordType).localized()

		val description = Text()
		description.text = decriptionText
		step.inputInfoUnits.add(description)

		val pinInputField = PasswordField(passwordType)
		pinInputField.description =
			when (passwordType) {
				"MRZ" -> pace_mrz.localized(Locale.getDefault())
				"CAN" -> pace_can.localized(Locale.getDefault())
				"PIN" -> pace_pin.localized(Locale.getDefault())
				"PUK" -> pace_puk.localized(Locale.getDefault())
				else -> ""
			}

		step.inputInfoUnits.add(pinInputField)
	}

	/**
	 * Processes the results of step.
	 *
	 * @param results Results
	 */
	fun processResult(results: MutableMap<String?, ExecutionResults?>) {
		val executionResults = results.get(step.id)

		if (executionResults == null) {
			return
		}

		val p = executionResults.getResult(passwordType) as PasswordField?
		content.add(ELEMENT.PIN, p!!.value)
	}

	companion object {
		// GUI translation constants
		private const val STEP_ID = "PROTOCOL_PACE_GUI_STEP_PIN"
	}
}
