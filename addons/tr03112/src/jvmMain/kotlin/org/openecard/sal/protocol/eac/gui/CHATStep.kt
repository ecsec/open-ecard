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

import org.openecard.common.I18n
import org.openecard.crypto.common.asn1.cvc.CHAT
import org.openecard.gui.definition.*
import org.openecard.sal.protocol.eac.EACData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

/**
 * CHAT GUI step for EAC.
 *
 * @author Tobias Wich
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
class CHATStep(private val eacData: EACData) : Step(STEP_ID, LANG.translationForKey(TITLE)) {
    init {
        setDescription(LANG.translationForKey(STEP_DESCRIPTION))

        // create step elements
        addElements()
    }

    private fun addElements() {
        val decription = Text()
        val decriptionText: String? = LANG.translationForKey(DESCRIPTION, eacData.certificateDescription.subjectName)
        decription.setText(decriptionText)
        getInputInfoUnits().add(decription)

        // process read access and special functions
        val readAccessCheckBox = Checkbox(READ_CHAT_BOXES)
        var displayReadAccessCheckBox = false
        readAccessCheckBox.setGroupText(LANG.translationForKey(READ_ACCESS_DESC))
        val requiredReadAccess: MutableMap<CHAT.DataGroup, Boolean?> = eacData.requiredCHAT.getReadAccess()
        val optionalReadAccess: MutableMap<CHAT.DataGroup, Boolean?> = eacData.optionalCHAT.getReadAccess()
        val requiredSpecialFunctions: MutableMap<CHAT.SpecialFunction, Boolean?> =
            eacData.requiredCHAT.getSpecialFunctions()
        val optionalSpecialFunctions: MutableMap<CHAT.SpecialFunction, Boolean?> =
            eacData.optionalCHAT.getSpecialFunctions()

        // iterate over all 22 eID application data groups
        for (dataGroup in requiredReadAccess.keys) {
            if (TR03119RightsFilter.isTr03119ConformReadRight(dataGroup)) {
                if (requiredReadAccess.get(dataGroup)) {
                    displayReadAccessCheckBox = true
                    readAccessCheckBox.getBoxItems().add(makeBoxItem(dataGroup, true, true))
                } else if (optionalReadAccess.get(dataGroup)) {
                    displayReadAccessCheckBox = true
                    readAccessCheckBox.getBoxItems().add(makeBoxItem(dataGroup, true, false))
                }
            } else {
                eacData.selectedCHAT.setReadAccess(dataGroup, false)
            }
        }

        // iterate over all 8 special functions
        for (specialFunction in requiredSpecialFunctions.keys) {
            // determine if extra data is necessary
            var textData = arrayOfNulls<Any>(0)
            if (CHAT.SpecialFunction.AGE_VERIFICATION == specialFunction) {
                val c = eacData.aad.ageVerificationData
                if (c != null) {
                    val yearDiff: Int = getYearDifference(c)
                    textData = arrayOf<Any>(yearDiff)
                } else {
                    LOG.warn("Removing age verification because of missing or invalid AAD.")
                    // disable this function as no working reference value is given
                    eacData.selectedCHAT.setSpecialFunctions(specialFunction, false)
                    continue
                }
            } else if (CHAT.SpecialFunction.COMMUNITY_ID_VERIFICATION == specialFunction) {
                if (eacData.aad.communityIDVerificationData == null) {
                    LOG.warn("Removing community ID verification because of missing AAD.")
                    // disable this function as no working reference value is given
                    eacData.selectedCHAT.setSpecialFunctions(specialFunction, false)
                    continue
                }
            }

            if (TR03119RightsFilter.isTr03119ConformSpecialFunction(specialFunction)) {
                if (requiredSpecialFunctions.get(specialFunction)) {
                    displayReadAccessCheckBox = true
                    readAccessCheckBox.getBoxItems().add(makeBoxItem(specialFunction, true, true, *textData))
                } else if (optionalSpecialFunctions.get(specialFunction)) {
                    displayReadAccessCheckBox = true
                    readAccessCheckBox.getBoxItems().add(makeBoxItem(specialFunction, true, false, *textData))
                }
            } else {
                eacData.selectedCHAT.setSpecialFunctions(specialFunction, false)
            }
        }

        if (displayReadAccessCheckBox) {
            getInputInfoUnits().add(readAccessCheckBox)
        }

        // process write access
        val writeAccessCheckBox = Checkbox(WRITE_CHAT_BOXES)
        var displayWriteAccessCheckBox = false
        writeAccessCheckBox.setGroupText(LANG.translationForKey(WRITE_ACCESS_DESC))
        val requiredWriteAccess: MutableMap<CHAT.DataGroup, Boolean?> = eacData.requiredCHAT.getWriteAccess()
        val optionalWriteAccess: MutableMap<CHAT.DataGroup, Boolean?> = eacData.optionalCHAT.getWriteAccess()

        // iterate over DG17-DG21 of the eID application data groups
        for (dataGroup in requiredWriteAccess.keys) {
            if (TR03119RightsFilter.isTr03119ConformWriteRight(dataGroup)) {
                if (requiredWriteAccess.get(dataGroup)) {
                    displayWriteAccessCheckBox = true
                    writeAccessCheckBox.getBoxItems().add(makeBoxItem(dataGroup, true, true))
                } else if (optionalWriteAccess.get(dataGroup)) {
                    displayWriteAccessCheckBox = true
                    writeAccessCheckBox.getBoxItems().add(makeBoxItem(dataGroup, true, false))
                }
            } else {
                eacData.selectedCHAT.setWriteAccess(dataGroup, false)
            }
        }

        if (displayWriteAccessCheckBox) {
            getInputInfoUnits().add(writeAccessCheckBox)
        }

        val requestedDataDescription = ToggleText()
        requestedDataDescription.setTitle(LANG.translationForKey(NOTE))
        requestedDataDescription.setText(LANG.translationForKey(NOTE_CONTENT))
        requestedDataDescription.setCollapsed(!true)
        getInputInfoUnits().add(requestedDataDescription)
    }

    private fun makeBoxItem(value: Enum<*>, checked: Boolean, disabled: Boolean, vararg textData: Any?): BoxItem {
        val item = BoxItem()

        item.setName(value.name)
        item.setChecked(checked)
        item.setDisabled(disabled)
        item.setText(LANG.translationForKey(value.name, *textData))

        return item
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(CHATStep::class.java)

        private val LANG: I18n = I18n.getTranslation("eac")

        // step id
        const val STEP_ID: String = "PROTOCOL_EAC_GUI_STEP_CHAT"

        // GUI translation constants
        const val TITLE: String = "step_chat_title"
        const val STEP_DESCRIPTION: String = "step_chat_step_description"
        const val DESCRIPTION: String = "step_chat_description"
        const val NOTE: String = "step_chat_note"
        const val NOTE_CONTENT: String = "step_chat_note_content"
        const val READ_ACCESS_DESC: String = "step_chat_read_access_description"
        const val WRITE_ACCESS_DESC: String = "step_chat_write_access_description"

        // GUI element IDs
        const val READ_CHAT_BOXES: String = "ReadCHATCheckBoxes"
        const val WRITE_CHAT_BOXES: String = "WriteCHATCheckBoxes"

        private fun getYearDifference(c: Calendar): Int {
            val now = Calendar.getInstance()
            now.add(Calendar.DAY_OF_MONTH, -1 * c.get(Calendar.DAY_OF_MONTH))
            now.add(Calendar.DAY_OF_MONTH, 1)
            now.add(Calendar.MONTH, -1 * c.get(Calendar.MONTH))
            now.add(Calendar.MONTH, 1)
            now.add(Calendar.YEAR, -1 * c.get(Calendar.YEAR))
            return now.get(Calendar.YEAR)
        }
    }
}
