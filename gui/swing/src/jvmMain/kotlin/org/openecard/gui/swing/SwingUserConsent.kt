/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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
package org.openecard.gui.swing

import org.openecard.gui.FileDialog
import org.openecard.gui.MessageDialog
import org.openecard.gui.UserConsent
import org.openecard.gui.UserConsentNavigator
import org.openecard.gui.definition.UserConsentDescription
import org.openecard.gui.swing.common.GUIConstants
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ActionEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.BoxLayout
import javax.swing.GroupLayout
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Swing implementation of the UserConsent interface.
 * The implementation encapsulates a DialogWrapper which is needed to supply a root pane for all draw operations.
 *
 * @author Tobias Wich
 * @author Florian Feldmann
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 */
class SwingUserConsent
/**
 * Instantiate SwingUserConsent.
 *
 * @param baseDialogWrapper
 */
(
	private val baseDialogWrapper: SwingDialogWrapper,
) : UserConsent {
	override fun obtainNavigator(parameters: UserConsentDescription): UserConsentNavigator {
		val dialogWrapper = baseDialogWrapper.derive()
		dialogWrapper.setTitle(parameters.title)

		val rootPanel = dialogWrapper.getContentPane
		rootPanel.removeAll()

		val isPinEntryDialog = parameters.dialogType == "pin_entry_dialog"
		val isPinChangeDialog = parameters.dialogType == "pin_change_dialog"
		val isUpdateDialog = parameters.dialogType == "update_dialog"

		// set different size when special dialog type is requested
		if (isPinEntryDialog) {
			dialogWrapper.setSize(350, 284)
		} else if (isPinChangeDialog) {
			dialogWrapper.setSize(570, 430)
		} else if (isUpdateDialog) {
			dialogWrapper.setSize(480, 330)
		}

		val dialogType = parameters.dialogType
		val steps = parameters.getSteps()

		// Set up panels
		val stepPanel = JPanel(BorderLayout())
		val sideBar = JPanel()

		val stepBar = StepBar(steps)
		val navBar = NavigationBar(steps.size)

		val l = Logo()
		initializeSidePanel(sideBar, l, stepBar)

		val navigator = SwingNavigator(dialogWrapper, dialogType, steps, stepPanel, navBar, stepBar)
		navBar.registerEvents(navigator)
		navBar.setDefaultButton(dialogWrapper.rootPane)

		dialogWrapper.dialog.addWindowListener(
			object : WindowAdapter() {
				override fun windowClosing(event: WindowEvent?) {
					// The user has closed the window by pressing the x of the window manager handle this event as
					// cancelation. This is necessary to unlock the app in case of a running authentication.
					val e = ActionEvent(navBar, ActionEvent.ACTION_PERFORMED, GUIConstants.BUTTON_CANCEL)
					navigator.actionPerformed(e)
				}
			},
		)

		// Config layout
		val layout = GroupLayout(rootPanel)
		rootPanel.setLayout(layout)

		layout.autoCreateGaps = false
		layout.autoCreateContainerGaps = true

		if (isPinEntryDialog || isPinChangeDialog || isUpdateDialog) {
			layout.setHorizontalGroup(
				layout
					.createSequentialGroup()
					.addGroup(
						layout
							.createParallelGroup()
							.addComponent(stepPanel)
							.addComponent(navBar),
					),
			)
			layout.setVerticalGroup(
				layout
					.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addGroup(
						layout
							.createSequentialGroup()
							.addComponent(stepPanel)
							.addComponent(navBar),
					),
			)
		} else {
			layout.setHorizontalGroup(
				layout
					.createSequentialGroup()
					.addComponent(sideBar, 200, 200, 200)
					.addGroup(
						layout
							.createParallelGroup()
							.addComponent(stepPanel)
							.addComponent(navBar),
					),
			)
			layout.setVerticalGroup(
				layout
					.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(sideBar)
					.addGroup(
						layout
							.createSequentialGroup()
							.addComponent(stepPanel)
							.addComponent(navBar),
					),
			)
		}

		rootPanel.validate()
		rootPanel.repaint()

		return navigator
	}

	override fun obtainFileDialog(): FileDialog = SwingFileDialog()

	override fun obtainMessageDialog(): MessageDialog = SwingMessageDialog()

	private fun initializeSidePanel(
		panel: JPanel,
		vararg components: JComponent,
	) {
		panel.setLayout(BoxLayout(panel, BoxLayout.PAGE_AXIS))
		for (c in components) {
			c.setAlignmentX(Component.LEFT_ALIGNMENT)
			panel.add(c)
		}
	}
}
