/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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
package org.openecard.addons.tr03124.gui

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import org.openecard.common.interfaces.CardRecognition
import org.openecard.gui.definition.BoxItem
import org.openecard.gui.definition.Radiobox
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.i18n.I18N

/**
 * Step implementation which represents a card selection dialog in case the are multiple valid cards available.
 *
 * @author Hans-Martin Haase
 */
class CardSelectionStep(
	title: String,
	availableCards: MutableList<ConnectionHandleType>,
	rec: CardRecognition,
) : Step(ID, title) {
	private val avCardWithName = mutableMapOf<String, ConnectionHandleType>()
	private val rec: CardRecognition

	/**
	 * Creates a new CardSelectionStep from the given title, the available cards and the card recognition.
	 *
	 * @param title Title of this step.
	 * @param availableCards List of [ConnectionHandleType] objects representing the available credentials.
	 * @param rec [CardRecognition] object used to translate cardTypeNames into human understandable strings.
	 */
	init {
		isReversible = false

		for (conHandle in availableCards) {
			avCardWithName.put(rec.getTranslatedCardName(conHandle.getRecognitionInfo().getCardType()), conHandle)
		}
		this.rec = rec
		addElements()
	}

	/**
	 * Add the UI elements to the step.
	 */
	private fun addElements() {
		val description = Text()
		description.text = I18N.strings.tr03112_card_selection_message.localized()
		val radioBox = Radiobox("credentialSelectionBox")
		radioBox.groupText = "Available Credentials"
		for (cardName in avCardWithName.keys) {
			val item = BoxItem()
			item.name = avCardWithName[cardName]!!.getRecognitionInfo().getCardType()
			item.text = cardName
			radioBox.boxItems.add(item)
		}

		inputInfoUnits.add(description)
		inputInfoUnits.add(radioBox)
	}

	/**
	 * Update the step with a new list of connection handles.
	 *
	 * @param availableCards List of available cards represented by connection handles.
	 */
	fun update(availableCards: MutableList<ConnectionHandleType>) {
		this.avCardWithName.clear()
		for (handle in availableCards) {
			avCardWithName.put(rec.getTranslatedCardName(handle.getRecognitionInfo().getCardType()), handle)
		}

		val task = backgroundTask as CardMonitorTask?
		if (task != null) {
			val handle = task.result!!
			if (handle.getRecognitionInfo() != null && handle.getRecognitionInfo().getCardType() != null) {
				avCardWithName.put(rec.getTranslatedCardName(handle.getRecognitionInfo().getCardType()), handle)
			}
		}

		inputInfoUnits.clear()
		addElements()
	}

	companion object {
		private const val ID = "CredentialSelection"
	}
}
