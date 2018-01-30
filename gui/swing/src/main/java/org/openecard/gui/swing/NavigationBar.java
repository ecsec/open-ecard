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

package org.openecard.gui.swing;

import java.awt.event.ActionListener;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import org.openecard.common.I18n;
import org.openecard.gui.definition.Step;
import org.openecard.gui.swing.common.GUIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Component of the Swing GUI with navigation buttons.
 * Depending on whether the first, the last or an intermediate step is displayed, the visible buttons are:
 * <ul>
 *   <li>Back</li>
 *   <li>Next</li>
 *   <li>Finish</li>
 *   <li>Cancel</li>
 * </ul>
 *
 * @author Tobias Wich
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 */
public class NavigationBar extends JPanel {

    private static final Logger LOG = LoggerFactory.getLogger(NavigationBar.class);
    private static final long serialVersionUID = 1L;

    private final I18n lang = I18n.getTranslation("gui");
    private final int numSteps;
    
    private JButton backButton;
    private JButton nextButton;
    private JButton cancelButton;

    /**
     * Create and initialize the navigation panel for the given number of steps.
     * The step number is important, because the panel needs to know when it is finished.
     *
     * @param numSteps Number of steps in this user consent.
     */
    public NavigationBar(int numSteps) {
	this.numSteps = numSteps;
	initializeComponents();
	initializeLayout();
    }

    /**
     * Register the provided listener for all navigation (button) events.
     *
     * @param eventSink Listener for button events.
     */
    public void registerEvents(ActionListener eventSink) {
	backButton.addActionListener(eventSink);
	nextButton.addActionListener(eventSink);
	cancelButton.addActionListener(eventSink);
    }

    public void setDefaultButton(JRootPane rootPane) {
	rootPane.setDefaultButton(nextButton);
    }

    private void initializeComponents() {
	backButton = new JButton(lang.translationForKey(GUIConstants.BUTTON_BACK));
	backButton.setActionCommand(GUIConstants.BUTTON_BACK);
	backButton.setVisible(false);

	nextButton = new JButton(lang.translationForKey(GUIConstants.BUTTON_NEXT));
	nextButton.setActionCommand(GUIConstants.BUTTON_NEXT);
	// if there is only one step set next button to finished
	if (numSteps == 1) {
	    nextButton.setText(lang.translationForKey(GUIConstants.BUTTON_FINISH));
	}

	cancelButton = new JButton(lang.translationForKey(GUIConstants.BUTTON_CANCEL));
	cancelButton.setActionCommand(GUIConstants.BUTTON_CANCEL);
    }

    private void initializeLayout() {
	GroupLayout layout = new GroupLayout(this);
	setLayout(layout);

	layout.setAutoCreateGaps(true);
	layout.setAutoCreateContainerGaps(false);

	GroupLayout.SequentialGroup hg = layout.createSequentialGroup();
	hg.addComponent(backButton, 60, 60, 150);
	hg.addComponent(nextButton, 60, 60, 150);
	hg.addComponent(cancelButton, 60, 60, 150);
	layout.setHorizontalGroup(hg);

	GroupLayout.ParallelGroup vg = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
	vg.addComponent(backButton);
	vg.addComponent(nextButton);
	vg.addComponent(cancelButton);
	layout.setVerticalGroup(vg);
    }

    /**
     * Locks buttons except the cancel button.
     */
    public void lockControls() {
	// lock buttons
	backButton.setEnabled(false);
	nextButton.setEnabled(false);
    }

    /**
     * Unlocks all buttons.
     */
    public void unlockControls() {
	// unlock buttons
	backButton.setEnabled(true);
	nextButton.setEnabled(true);
    }

    /**
     * Updates the buttons according to the position of the user consent.
     *
     * @param nextIdx Index of the step that is to be displayed.
     * @param nextStep Step that will be displayed next.
     */
    public void selectIdx(int nextIdx, Step nextStep) {
	// Don't show the back button on the first step
	if (nextIdx == 0) {
	    backButton.setVisible(false);
	} else {
	    backButton.setVisible(true);
	}

	// Change the forward button on the last step to "finished"
	if (nextIdx == (numSteps - 1)) {
	    nextButton.setText(lang.translationForKey(GUIConstants.BUTTON_FINISH));
	} else {
	    nextButton.setText(lang.translationForKey(GUIConstants.BUTTON_NEXT));
	}
    }

    @Override
    public boolean hasFocus() {
	return backButton.hasFocus() || nextButton.hasFocus() || cancelButton.hasFocus();
    }

    /**
     * Makes the step reversible or not.
     * This actually enables or disables the back button.
     *
     * @param reversible {@code true} if the button should be enabled, {@code false} otherwise.
     */
    void setReversible(boolean reversible) {
	backButton.setEnabled(reversible);
    }

    /**
     * Indicates whether there is a visible next button which may be controlled by the enter key.
     *
     * @return {@code TRUE} if there is a visible next button on the gui which does not have the focus. In all other
     * cases {@code FALSE} is returned.
     */
    boolean isNextButtonAccessible() {
	return nextButton.isEnabled() && ! nextButton.hasFocus();
    }

}
