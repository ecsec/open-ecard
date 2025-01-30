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

import org.openecard.common.I18n
import org.openecard.gui.definition.Step
import org.openecard.gui.swing.common.GUIConstants
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.event.ActionListener
import javax.swing.GroupLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JRootPane

/**
 * Component of the Swing GUI with navigation buttons.
 * Depending on whether the first, the last or an intermediate step is displayed, the visible buttons are:
 *
 *  * Back
 *  * Next
 *  * Finish
 *  * Cancel
 *
 *
 * @author Tobias Wich
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 */
class NavigationBar(private val numSteps: Int) : JPanel() {

    private val lang: I18n = I18n.getTranslation("gui")

    private var backButton = JButton(lang.translationForKey(GUIConstants.BUTTON_BACK))
    private var nextButton = JButton(lang.translationForKey(GUIConstants.BUTTON_NEXT))
    private var cancelButton = JButton(lang.translationForKey(GUIConstants.BUTTON_CANCEL))

    /**
     * Create and initialize the navigation panel for the given number of steps.
     * The step number is important, because the panel needs to know when it is finished.
     *
     * @param numSteps Number of steps in this user consent.
     */
    init {
        initializeComponents()
        initializeLayout()
    }

    /**
     * Register the provided listener for all navigation (button) events.
     *
     * @param eventSink Listener for button events.
     */
    fun registerEvents(eventSink: ActionListener?) {
        backButton.addActionListener(eventSink)
        nextButton.addActionListener(eventSink)
        cancelButton.addActionListener(eventSink)
    }

    fun setDefaultButton(rootPane: JRootPane) {
        rootPane.setDefaultButton(nextButton)
    }

    private fun initializeComponents() {
		backButton.actionCommand = GUIConstants.BUTTON_BACK
		backButton.isVisible = false

		nextButton.actionCommand = GUIConstants.BUTTON_NEXT
        // if there is only one step set next button to finished
        if (numSteps == 1) {
            nextButton.setText(lang.translationForKey(GUIConstants.BUTTON_FINISH))
        }

		cancelButton.actionCommand = GUIConstants.BUTTON_CANCEL
    }

    private fun initializeLayout() {
        val layout = GroupLayout(this)
        setLayout(layout)

		layout.autoCreateGaps = true
		layout.autoCreateContainerGaps = false

        val hg = layout.createSequentialGroup()
        hg.addComponent(backButton, 60, 60, 150)
        hg.addComponent(nextButton, 60, 60, 150)
        hg.addComponent(cancelButton, 60, 60, 150)
        layout.setHorizontalGroup(hg)

        val vg = layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        vg.addComponent(backButton)
        vg.addComponent(nextButton)
        vg.addComponent(cancelButton)
        layout.setVerticalGroup(vg)
    }

    /**
     * Locks buttons except the cancel button.
     */
    fun lockControls() {
        // lock buttons
        backButton.setEnabled(false)
        nextButton.setEnabled(false)
    }

    /**
     * Unlocks all buttons.
     */
    fun unlockControls() {
        // unlock buttons
        backButton.setEnabled(true)
        nextButton.setEnabled(true)
    }

    /**
     * Updates the buttons according to the position of the user consent.
     *
     * @param nextIdx Index of the step that is to be displayed.
     */
    fun selectIdx(nextIdx: Int, nextStep: Step?) {
        // Don't show the back button on the first step
        if (nextIdx == 0) {
			backButton.isVisible = false
        } else {
			backButton.isVisible = true
        }

        // Change the forward button on the last step to "finished"
        if (nextIdx == (numSteps - 1)) {
            nextButton.setText(lang.translationForKey(GUIConstants.BUTTON_FINISH))
        } else {
            nextButton.setText(lang.translationForKey(GUIConstants.BUTTON_NEXT))
        }
    }

    override fun hasFocus(): Boolean {
        return backButton.hasFocus() || nextButton.hasFocus() || cancelButton.hasFocus()
    }

    /**
     * Makes the step reversible or not.
     * This actually enables or disables the back button.
     *
     * @param reversible `true` if the button should be enabled, `false` otherwise.
     */
    fun setReversible(reversible: Boolean) {
        backButton.setEnabled(reversible)
    }

    val isNextButtonAccessible: Boolean
        /**
         * Indicates whether there is a visible next button which may be controlled by the enter key.
         *
         * @return `TRUE` if there is a visible next button on the gui which does not have the focus. In all other
         * cases `FALSE` is returned.
         */
        get() = nextButton.isEnabled && !nextButton.hasFocus()

    companion object {
        private const val serialVersionUID = 1L
    }
}
