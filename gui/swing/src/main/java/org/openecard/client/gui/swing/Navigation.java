package org.openecard.client.gui.swing;

import org.openecard.client.gui.swing.common.GUIConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.openecard.client.common.I18n;
import org.openecard.client.gui.definition.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class Navigation extends JPanel implements ActionListener {

    private static final Logger logger = LoggerFactory.getLogger(Navigation.class);
    private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
    private JButton backButton, nextButton, cancelButton;
    private I18n lang = I18n.getTranslation("gui");
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

	layout.setHorizontalGroup(
		layout.createSequentialGroup().addGap(0, 0, Integer.MAX_VALUE).addComponent(backButton, 60, 60, 100).addComponent(nextButton, 60, 60, 100).addGap(10).addComponent(cancelButton, 60, 60, 100));
	layout.setVerticalGroup(
		layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(backButton).addComponent(nextButton).addComponent(cancelButton));

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
}
