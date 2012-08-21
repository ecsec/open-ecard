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
 ***************************************************************************/
package org.openecard.client.gui.swing;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.definition.UserConsentDescription;
import org.openecard.client.gui.swing.common.GUIConstants;


/**
 * Swing implementation of the UserConsent interface.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Florian Feldmann <florian.feldmann@rub.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class SwingUserConsent implements UserConsent {

    private final DialogWrapper dialogWrapper;

    /**
     * Instantiate SwingUserConsent. The implementation encapsulates a DialogWrapper which
     * is needed to supply a root pane for all draw operations.
     *
     * @param dialogWrapper
     */
    public SwingUserConsent(DialogWrapper dialogWrapper) {
	this.dialogWrapper = dialogWrapper;
    }

    @Override
    public UserConsentNavigator obtainNavigator(UserConsentDescription parameters) {
	dialogWrapper.setTitle(parameters.getTitle());

	Container rootPanel = dialogWrapper.getContentPane();
	rootPanel.removeAll();

	String dialogType = parameters.getDialogType();
	List<Step> steps = parameters.getSteps();

	// Set up panels
	JPanel stepPanel = new JPanel(new BorderLayout());
	JPanel sideBar = new JPanel(new BorderLayout());

	StepBar stepBar = new StepBar(steps);
	final Navigation navigationPanel = new Navigation(steps);

	Logo l = new Logo();
	initializeSidePanel(sideBar, l, stepBar);

	SwingNavigator navigator = new SwingNavigator(dialogWrapper, dialogType, steps, stepPanel);
	navigator.addPropertyChangeListener(stepBar);
	for (StepFrame frame : navigator.getStepFrames()) {
	    navigationPanel.addActionListener(frame);
	}

	// Add global key listener
	Toolkit.getDefaultToolkit().getSystemEventQueue().push(
		new EventQueue() {
		    @Override
		    protected void dispatchEvent(AWTEvent event) {
			if (event instanceof KeyEvent) {
			    KeyEvent keyEvent = (KeyEvent) event;
			    if (KeyEvent.KEY_RELEASED == keyEvent.getID() && KeyEvent.VK_ENTER == keyEvent.getKeyCode()) {
				// If the enter is pressed when perform a next step event
				if (!navigationPanel.hasFocus()) {
				    navigationPanel.actionPerformed(
					    new ActionEvent(navigationPanel, ActionEvent.ACTION_PERFORMED, GUIConstants.BUTTON_NEXT));
				}
			    }
			}
			super.dispatchEvent(event);
		    }

		});

	// Config layout
	GroupLayout layout = new GroupLayout(rootPanel);
	rootPanel.setLayout(layout);

	layout.setAutoCreateGaps(true);
	layout.setAutoCreateContainerGaps(true);

	layout.setHorizontalGroup(
		layout.createSequentialGroup()
		.addComponent(sideBar, 150, 150, 150)
		.addGroup(layout.createParallelGroup()
		.addComponent(stepPanel)
		.addGap(10)
		.addComponent(navigationPanel)));
	layout.setVerticalGroup(
		layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		.addComponent(sideBar)
		.addGroup(layout.createSequentialGroup()
		.addComponent(stepPanel)
		.addComponent(navigationPanel)));

	rootPanel.validate();
	rootPanel.repaint();

	return navigator;
    }

    private void initializeSidePanel(JPanel panel, JComponent... components) {
	panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
	for (JComponent c : components) {
	    panel.add(c);
	}
    }

}
