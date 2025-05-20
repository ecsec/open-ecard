/****************************************************************************
 * Copyright (C) 2012-2017 ecsec GmbH.
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

import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.format
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.crypto.common.asn1.cvc.CHAT
import org.openecard.crypto.common.asn1.cvc.stringResource
import org.openecard.gui.definition.BoxItem
import org.openecard.gui.definition.Checkbox
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.gui.definition.ToggleText
import org.openecard.i18n.I18N
import org.openecard.sal.protocol.eac.EACData
import java.util.Calendar
import java.util.Locale

private val logger = KotlinLogging.logger { }

/**
 * CHAT GUI step for EAC.
 *
 * @author Tobias Wich
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
class CHATStep(
	private val eacData: EACData,
) : Step(
		STEP_ID,
		I18N.strings.eac_step_chat_title.localized(),
	) {
	init {
		description = I18N.strings.eac_step_chat_step_description.localized()
		// create step elements
		addElements()
	}

	private fun addElements() {
		val description = Text()
		val descriptionText =
			I18N.strings.eac_step_chat_description.localized(
				Locale.getDefault(),
				eacData.certificateDescription.subjectName as Any,
			)
		
		description.text = descriptionText
		inputInfoUnits.add(description)

		// process read access and special functions
		val readAccessCheckBox = Checkbox(READ_CHAT_BOXES)
		var displayReadAccessCheckBox = false
		readAccessCheckBox.groupText = I18N.strings.eac_step_chat_read_access_description.localized()
		val requiredReadAccess: Map<CHAT.DataGroup, Boolean> = eacData.requiredCHAT.getReadAccess()
		val optionalReadAccess: Map<CHAT.DataGroup, Boolean> = eacData.optionalCHAT.getReadAccess()

		val requiredSpecialFunctions: Map<CHAT.SpecialFunction, Boolean> =
			eacData.requiredCHAT.getSpecialFunctions()
		val optionalSpecialFunctions: Map<CHAT.SpecialFunction, Boolean?> =
			eacData.optionalCHAT.getSpecialFunctions()

		// iterate over all 22 eID application data groups
		for (dataGroup in requiredReadAccess.keys) {
			if (TR03119RightsFilter.isTr03119ConformReadRight(dataGroup)) {
				if (requiredReadAccess[dataGroup] == true) {
					displayReadAccessCheckBox = true
					readAccessCheckBox.boxItems.add(makeBoxItem(dataGroup, checked = true, disabled = true))
				} else if (optionalReadAccess[dataGroup] == true) {
					displayReadAccessCheckBox = true
					readAccessCheckBox.boxItems.add(makeBoxItem(dataGroup, checked = true, disabled = false))
				}
			} else {
				eacData.selectedCHAT.setReadAccess(dataGroup, false)
			}
		}

		// iterate over all 8 special functions
		for (specialFunction in requiredSpecialFunctions.keys) {
			// determine if extra data is necessary
			var textData = arrayOf<Any>()
			if (CHAT.SpecialFunction.AGE_VERIFICATION == specialFunction) {
				val c = eacData.aad.ageVerificationData
				if (c != null) {
					val yearDiff: Int = getYearDifference(c)
					textData = arrayOf(yearDiff)
				} else {
					logger.warn { "Removing age verification because of missing or invalid AAD." }
					// disable this function as no working reference value is given
					eacData.selectedCHAT.setSpecialFunctions(specialFunction, false)
					continue
				}
			} else if (CHAT.SpecialFunction.COMMUNITY_ID_VERIFICATION == specialFunction) {
				if (eacData.aad.communityIDVerificationData == null) {
					logger.warn { "Removing community ID verification because of missing AAD." }
					// disable this function as no working reference value is given
					eacData.selectedCHAT.setSpecialFunctions(specialFunction, false)
					continue
				}
			}

			if (TR03119RightsFilter.isTr03119ConformSpecialFunction(specialFunction)) {
				if (requiredSpecialFunctions[specialFunction] == true) {
					displayReadAccessCheckBox = true
					readAccessCheckBox.boxItems.add(
						makeBoxItem(
							specialFunction,
							true,
							disabled = true,
							textData = textData,
						),
					)
				} else if (optionalSpecialFunctions[specialFunction] == true) {
					displayReadAccessCheckBox = true
					readAccessCheckBox.boxItems.add(
						makeBoxItem(
							specialFunction,
							true,
							disabled = false,
							textData = textData,
						),
					)
				}
			} else {
				eacData.selectedCHAT.setSpecialFunctions(specialFunction, false)
			}
		}

		if (displayReadAccessCheckBox) {
			inputInfoUnits.add(readAccessCheckBox)
		}

		// process write access
		val writeAccessCheckBox = Checkbox(WRITE_CHAT_BOXES)
		var displayWriteAccessCheckBox = false
		writeAccessCheckBox.groupText = I18N.strings.eac_step_chat_write_access_description.localized()
		val requiredWriteAccess: Map<CHAT.DataGroup, Boolean?> = eacData.requiredCHAT.getWriteAccess()
		val optionalWriteAccess: Map<CHAT.DataGroup, Boolean?> = eacData.optionalCHAT.getWriteAccess()

		// iterate over DG17-DG21 of the eID application data groups
		for (dataGroup in requiredWriteAccess.keys) {
			if (TR03119RightsFilter.isTr03119ConformWriteRight(dataGroup)) {
				if (requiredWriteAccess[dataGroup] == true) {
					displayWriteAccessCheckBox = true
					writeAccessCheckBox.boxItems.add(makeBoxItem(dataGroup, true, true))
				} else if (optionalWriteAccess[dataGroup] == true) {
					displayWriteAccessCheckBox = true
					writeAccessCheckBox.boxItems.add(makeBoxItem(dataGroup, true, false))
				}
			} else {
				eacData.selectedCHAT.setWriteAccess(dataGroup, false)
			}
		}

		if (displayWriteAccessCheckBox) {
			inputInfoUnits.add(writeAccessCheckBox)
		}

		val requestedDataDescription =
			ToggleText().apply {
				title = I18N.strings.eac_step_chat_note.localized()
				text = I18N.strings.eac_step_chat_note_content.localized()
				isCollapsed = false
			}
		inputInfoUnits.add(requestedDataDescription)
	}

	private fun makeBoxItem(
		value: StringResource,
		checked: Boolean,
		disabled: Boolean,
		vararg textData: Any?,
	): BoxItem =
		BoxItem().apply {
			name = value.localized()
			isChecked = checked
			isDisabled = disabled
			this.text = value.format(textData).localized()
		}

	private fun makeBoxItem(
		value: CHAT.SpecialFunction,
		checked: Boolean,
		disabled: Boolean,
		vararg textData: Any?,
	) = makeBoxItem(value.stringResource(), checked, disabled, textData)

	private fun makeBoxItem(
		value: CHAT.DataGroup,
		checked: Boolean,
		disabled: Boolean,
		vararg textData: Any?,
	) = makeBoxItem(value.stringResource(), checked, disabled, textData)

	companion object {
		// step id
		const val STEP_ID: String = "PROTOCOL_EAC_GUI_STEP_CHAT"

		// GUI element IDs
		const val READ_CHAT_BOXES: String = "ReadCHATCheckBoxes"
		const val WRITE_CHAT_BOXES: String = "WriteCHATCheckBoxes"

		private fun getYearDifference(c: Calendar): Int =
			Calendar
				.getInstance()
				.apply {
					add(Calendar.DAY_OF_MONTH, -1 * c.get(Calendar.DAY_OF_MONTH))
					add(Calendar.DAY_OF_MONTH, 1)
					add(Calendar.MONTH, -1 * c.get(Calendar.MONTH))
					add(Calendar.MONTH, 1)
					add(Calendar.YEAR, -1 * c.get(Calendar.YEAR))
				}.get(Calendar.YEAR)
	}
}
