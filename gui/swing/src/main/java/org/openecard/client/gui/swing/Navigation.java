/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openecard.client.gui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.openecard.client.gui.definition.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class Navigation extends JPanel implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(Navigation.class);
    private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
    private JButton backButton, forwardButton, cancelButton;
    private List<Step> steps;
    private int stepPointer = 0;

    public Navigation(List<Step> steps) {
	this.steps = steps;

	initializeComponents();
	initializeLayout();
    }

    private void initializeComponents() {
	backButton = new JButton("Zurück");
	backButton.setActionCommand(GUIConstants.BUTTON_BACK);
	backButton.addActionListener(this);
	backButton.setVisible(false);

	forwardButton = new JButton("Weiter");
	forwardButton.setActionCommand(GUIConstants.BUTTON_NEXT);
	forwardButton.addActionListener(this);

	cancelButton = new JButton("Abbrechen");
	cancelButton.setActionCommand(GUIConstants.BUTTON_CANCEL);
	cancelButton.addActionListener(this);
    }

    private void initializeLayout() {
	GroupLayout layout = new GroupLayout(this);
	setLayout(layout);

	layout.setAutoCreateGaps(true);
	layout.setAutoCreateContainerGaps(true);

	layout.setHorizontalGroup(
		layout.createSequentialGroup().addGap(0, 0, Integer.MAX_VALUE).addComponent(backButton, 60, 60, 100).addComponent(forwardButton, 60, 60, 100).addGap(10).addComponent(cancelButton, 60, 60, 100));
	layout.setVerticalGroup(
		layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(backButton).addComponent(forwardButton).addComponent(cancelButton));

    }

    public void addActionListener(ActionListener actionListener) {
	listeners.add(actionListener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	logger.info("Navigation event: {} ", e.paramString());
	String command = e.getActionCommand();

	for (Iterator<ActionListener> it = listeners.iterator(); it.hasNext();) {
	    ActionListener actionListener = it.next();
	    actionListener.actionPerformed(
		    new ActionEvent(steps.get(stepPointer), ActionEvent.ACTION_PERFORMED, command));
	}

	if (command.equals(GUIConstants.BUTTON_NEXT)) {
	    stepPointer++;
	} else if (command.equals(GUIConstants.BUTTON_BACK)) {
	    stepPointer--;
	}

	if (stepPointer == 0) {
	    backButton.setVisible(false);
	} else {
	    backButton.setVisible(!false);
	}
    }
}
