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

package org.openecard.gui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.openecard.gui.FileDialog;
import org.openecard.gui.MessageDialog;
import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.swing.common.GUIConstants;


/**
 * Swing implementation of the UserConsent interface.
 * The implementation encapsulates a DialogWrapper which is needed to supply a root pane for all draw operations.
 *
 * @author Tobias Wich
 * @author Florian Feldmann
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 */
public class SwingUserConsent implements UserConsent {

    private final SwingDialogWrapper baseDialogWrapper;

    /**
     * Instantiate SwingUserConsent.
     *
     * @param dialogWrapper
     */
    public SwingUserConsent(SwingDialogWrapper dialogWrapper) {
	this.baseDialogWrapper = dialogWrapper;
    }

    @Override
    public UserConsentNavigator obtainNavigator(UserConsentDescription parameters) {
	SwingDialogWrapper dialogWrapper = baseDialogWrapper.derive();
	dialogWrapper.setTitle(parameters.getTitle());

	Container rootPanel = dialogWrapper.getContentPane();
	rootPanel.removeAll();

	boolean isPinEntryDialog = parameters.getDialogType().equals("pin_entry_dialog");
	boolean isPinChangeDialog = parameters.getDialogType().equals("pin_change_dialog");
	boolean isUpdateDialog = parameters.getDialogType().equals("update_dialog");

	// set different size when special dialog type is requested
	if (isPinEntryDialog) {
	    dialogWrapper.setSize(350, 284);
	} else if (isPinChangeDialog) {
	    dialogWrapper.setSize(570, 430);
	} else if (isUpdateDialog) {
	    dialogWrapper.setSize(480, 330);
	}

	String dialogType = parameters.getDialogType();
	List<Step> steps = parameters.getSteps();

	// Set up panels
	JPanel stepPanel = new JPanel(new BorderLayout());
	JPanel sideBar = new JPanel();

	StepBar stepBar = new StepBar(steps);
	final NavigationBar navBar = new NavigationBar(steps.size());

	Logo l = new Logo();
	initializeSidePanel(sideBar, l, stepBar);

	final SwingNavigator navigator = new SwingNavigator(dialogWrapper, dialogType, steps, stepPanel, navBar, stepBar);
	navBar.registerEvents(navigator);
	navBar.setDefaultButton(dialogWrapper.getRootPane());

	dialogWrapper.getDialog().addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent event) {
		// The user has closed the window by pressing the x of the window manager handle this event as
		// cancelation. This is necessary to unlock the app in case of a running authentication.
		ActionEvent e = new ActionEvent(navBar, ActionEvent.ACTION_PERFORMED, GUIConstants.BUTTON_CANCEL);
		navigator.actionPerformed(e);
	    }
	});

	// Config layout
	GroupLayout layout = new GroupLayout(rootPanel);
	rootPanel.setLayout(layout);

	layout.setAutoCreateGaps(false);
	layout.setAutoCreateContainerGaps(true);

	if (isPinEntryDialog || isPinChangeDialog || isUpdateDialog) {
	    layout.setHorizontalGroup(
		    layout.createSequentialGroup()
		    .addGroup(layout.createParallelGroup()
			    .addComponent(stepPanel)
			    .addComponent(navBar)));
	    layout.setVerticalGroup(
		    layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    .addGroup(layout.createSequentialGroup()
			    .addComponent(stepPanel)
			    .addComponent(navBar)));
	} else {
	    layout.setHorizontalGroup(
		    layout.createSequentialGroup()
		    .addComponent(sideBar, 200, 200, 200)
		    .addGroup(layout.createParallelGroup()
			    .addComponent(stepPanel)
			    .addComponent(navBar)));
	    layout.setVerticalGroup(
		    layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		    .addComponent(sideBar)
		    .addGroup(layout.createSequentialGroup()
			    .addComponent(stepPanel)
			    .addComponent(navBar)));
	}

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
