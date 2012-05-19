package org.openecard.client.gui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.definition.UserConsentDescription;


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
	Navigation navigationPanel = new Navigation(steps);

	Logo l = new Logo();
	initializeSidePanel(sideBar, l, stepBar);

	SwingNavigator navigator = new SwingNavigator(dialogWrapper, dialogType, steps, stepPanel);
	navigator.addPropertyChangeListener(stepBar);
	for(StepFrame frame : navigator.getStepFrames()){
	    navigationPanel.addActionListener(frame);
	}

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
