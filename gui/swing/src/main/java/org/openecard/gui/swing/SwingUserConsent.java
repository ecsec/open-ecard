/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.openecard.common.I18n;
import org.openecard.gui.FileDialog;
import org.openecard.gui.MessageDialog;
import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.swing.common.GUIConstants;


/**
 * Swing implementation of the UserConsent interface.
 *
 * @author Tobias Wich
 * @author Florian Feldmann
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 */
public class SwingUserConsent implements UserConsent {

    private final DialogWrapper dialogWrapper;
    private final I18n lang = I18n.getTranslation("addon");

    /**
     * Instantiate SwingUserConsent.
     * The implementation encapsulates a DialogWrapper which is needed to supply a root pane for all draw operations.
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
	JPanel sideBar = new JPanel();

	StepBar stepBar = new StepBar(steps);
	final NavigationBar navigationBar = new NavigationBar(steps.size());

	Logo l = new Logo();
	initializeSidePanel(sideBar, l, stepBar);

	final SwingNavigator navigator = new SwingNavigator(dialogWrapper, dialogType, steps, stepPanel, navigationBar, stepBar);
	navigationBar.registerEvents(navigator);

	// Add global key listener
	EventQueue eventQueue = new EventQueue() {
	    ActionEvent e = new ActionEvent(navigationBar, ActionEvent.ACTION_PERFORMED, GUIConstants.BUTTON_NEXT);
	    @Override
	    protected void dispatchEvent(AWTEvent event) {
		if (event instanceof KeyEvent) {
		    KeyEvent keyEvent = (KeyEvent) event;
		    if (KeyEvent.KEY_RELEASED == keyEvent.getID() && KeyEvent.VK_ENTER == keyEvent.getKeyCode()) {
			// If the enter is pressed when perform a next step event
			if (! navigationBar.hasFocus()) {
			    if (navigationBar.isNextButtonAccessible()) {
				navigator.actionPerformed(e);
			    }
			}
		    }
		} else if (event instanceof WindowEvent) {
		    WindowEvent windowEvent = (WindowEvent) event;
		    if (windowEvent.getID() == WindowEvent.WINDOW_CLOSING) {
			// The user has closed the window by pressing the x of the window manager handle this event as
			// cancelation. This is necessary to unlock the app in case of a running authentication.
			Object source = event.getSource();
			if (event.getSource() instanceof JFrame) {
			    JFrame sourceFrame = (JFrame) source;
			    if (! sourceFrame.getTitle().equals(lang.translationForKey("addon.title"))) {
				ActionEvent ev = new ActionEvent(navigationBar, ActionEvent.ACTION_PERFORMED,
					GUIConstants.BUTTON_CANCEL);
				navigator.actionPerformed(ev);
			    }
			}
		    }
		}


		super.dispatchEvent(event);
	    }
	};
	Toolkit.getDefaultToolkit().getSystemEventQueue().push(eventQueue);

	// Config layout
	GroupLayout layout = new GroupLayout(rootPanel);
	rootPanel.setLayout(layout);

	layout.setAutoCreateGaps(false);
	layout.setAutoCreateContainerGaps(true);

	layout.setHorizontalGroup(
		layout.createSequentialGroup()
		.addComponent(sideBar, 200, 200, 200)
		.addGroup(layout.createParallelGroup()
		.addComponent(stepPanel)
		.addComponent(navigationBar)));
	layout.setVerticalGroup(
		layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		.addComponent(sideBar)
		.addGroup(layout.createSequentialGroup()
		.addComponent(stepPanel)
		.addComponent(navigationBar)));

	rootPanel.validate();
	rootPanel.repaint();

	return navigator;
    }

    @Override
    public FileDialog obtainFileDialog() {
	return new SwingFileDialog();
    }

    @Override
    public MessageDialog obtainMessageDialog() {
	return new SwingMessageDialog();
    }

    private void initializeSidePanel(JPanel panel, JComponent... components) {
	panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
	for (JComponent c : components) {
	    c.setAlignmentX(Component.LEFT_ALIGNMENT);
	    panel.add(c);
	}
    }
    
}
