/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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
 ***************************************************************************/
package org.openecard.gui.swing.steplayout

import org.openecard.gui.definition.*
import org.openecard.gui.swing.ScrollPanel
import org.openecard.gui.swing.components.AbstractInput
import org.openecard.gui.swing.components.Radiobutton
import org.openecard.gui.swing.components.StepComponent
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * Default layouter for the Swing GUI.
 * This layouter provides a decent look and feel for most user consent tasks. If you need a specialised version,
 * create another layouter and register it for the respective URI.
 *
 * @author Tobias Wich
 * @author Florian Feldmann
 */
class DefaultStepLayouter(
	infoUnits: List<InputInfoUnit>,
	stepName: String,
) : StepLayouter() {
	override val components: MutableList<StepComponent> = mutableListOf()
	override val panel: JPanel = JPanel(BorderLayout())

	init {
		// Add a panel containing step title and separator
		val pageStart = JPanel(BorderLayout())
		val title = JLabel("<html><h3>$stepName</h3></html>")
		// add a space of 20 on top and 3 below to match with the logo separator
		title.setBorder(EmptyBorder(20, 0, 6, 0))
		pageStart.add(title, BorderLayout.PAGE_START)
		val sep = JSeparator(SwingConstants.HORIZONTAL)
		pageStart.add(sep, BorderLayout.CENTER)
		// add a space of 15 before the actual step content
		pageStart.setBorder(EmptyBorder(0, 0, 15, 0))
		panel.add(pageStart, BorderLayout.PAGE_START)

		val contentPanel = ScrollPanel()
		contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20))
		contentPanel.setLayout(BoxLayout(contentPanel, BoxLayout.Y_AXIS))

		// Create content
		for (next in infoUnits) {
			val nextComponent =
				when (next.type()) {
					InfoUnitElementType.CHECK_BOX ->
						org.openecard.gui.swing.components
							.Checkbox(next as Checkbox)
					InfoUnitElementType.HYPERLINK ->
						org.openecard.gui.swing.components
							.Hyperlink(next as Hyperlink)
					InfoUnitElementType.IMAGE_BOX ->
						org.openecard.gui.swing.components
							.ImageBox(next as ImageBox)
					InfoUnitElementType.PASSWORD_FIELD -> AbstractInput(next as PasswordField)
					InfoUnitElementType.RADIO_BOX -> Radiobutton(next as Radiobox)
					InfoUnitElementType.SIGNAUTRE_FIELD -> throw UnsupportedOperationException("Not implemented yet.")
					InfoUnitElementType.TEXT ->
						org.openecard.gui.swing.components
							.Text(next as Text)
					InfoUnitElementType.TEXT_FIELD -> AbstractInput(next as TextField)
					InfoUnitElementType.TOGGLE_TEXT ->
						org.openecard.gui.swing.components
							.ToggleText(next as ToggleText)
				}
			components.add(nextComponent)
			contentPanel.add(nextComponent.component)
			contentPanel.add(Box.createRigidArea(Dimension(0, 6)))
		}

		val scrollPane = JScrollPane(contentPanel)
		scrollPane.setBorder(BorderFactory.createEmptyBorder())
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)

		panel.add(scrollPane, BorderLayout.CENTER)
	}
}
