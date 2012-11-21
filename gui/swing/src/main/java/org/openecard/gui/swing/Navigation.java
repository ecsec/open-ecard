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

package org.openecard.gui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.openecard.common.I18n;
import org.openecard.gui.definition.Step;
import org.openecard.gui.swing.common.GUIConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class Navigation extends JPanel implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(Navigation.class);
    private static final long serialVersionUID = 1L;

    private final I18n lang = I18n.getTranslation("gui");
    private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
    private JButton backButton, nextButton, cancelButton;
    private List<Step> steps;
    private int stepPointer = 0;

    public Navigation(List<Step> steps) {
	this.steps = steps;

	initializeComponents();
	initializeLayout();
    }

    private void initializeComponents() {
	backButton = new JButton(lang.translationForKey(GUIConstants.BUTTON_BACK));
	backButton.setActionCommand(GUIConstants.BUTTON_BACK);
	backButton.addActionListener(this);
	backButton.setVisible(false);

	nextButton = new JButton(lang.translationForKey(GUIConstants.BUTTON_NEXT));
	nextButton.setActionCommand(GUIConstants.BUTTON_NEXT);
	nextButton.addActionListener(this);

	cancelButton = new JButton(lang.translationForKey(GUIConstants.BUTTON_CANCEL));
	cancelButton.setActionCommand(GUIConstants.BUTTON_CANCEL);
	cancelButton.addActionListener(this);
    }

    private void initializeLayout() {
	GroupLayout layout = new GroupLayout(this);
	setLayout(layout);

	layout.setAutoCreateGaps(true);
	layout.setAutoCreateContainerGaps(true);

	GroupLayout.SequentialGroup hg = layout.createSequentialGroup();
	hg.addGap(0, 0, Integer.MAX_VALUE);
	hg.addComponent(backButton, 60, 60, 150);
	hg.addComponent(nextButton, 60, 60, 150);
	hg.addGap(10);
	hg.addComponent(cancelButton, 60, 60, 150);
	layout.setHorizontalGroup(hg);

	GroupLayout.ParallelGroup vg = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
	vg.addComponent(backButton);
	vg.addComponent(nextButton);
	vg.addComponent(cancelButton);
	layout.setVerticalGroup(vg);
    }

    public void addActionListener(ActionListener actionListener) {
	listeners.add(actionListener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	logger.debug("Navigation event: {} ", e.paramString());

	String command = e.getActionCommand();

	for (Iterator<ActionListener> it = listeners.iterator(); it.hasNext();) {
	    ActionListener actionListener = it.next();
	    actionListener.actionPerformed(new ActionEvent(steps.get(stepPointer), ActionEvent.ACTION_PERFORMED, command));
	}

	if (command.equals(GUIConstants.BUTTON_NEXT)) {
	    stepPointer++;
	} else if (command.equals(GUIConstants.BUTTON_BACK)) {
	    stepPointer--;
	}

	// Dont show the back button on the first step
	if (stepPointer == 0) {
	    backButton.setVisible(false);
	} else {
	    backButton.setVisible(!false);
	}

	// Change the forward button on the last step to "finished"
	if (stepPointer == steps.size() - 1) {
	    nextButton.setText(lang.translationForKey(GUIConstants.BUTTON_FINISH));
	} else {
	    nextButton.setText(lang.translationForKey(GUIConstants.BUTTON_NEXT));
	}
    }

    @Override
    public boolean hasFocus() {
	return backButton.hasFocus() || nextButton.hasFocus() || cancelButton.hasFocus();
    }

}
