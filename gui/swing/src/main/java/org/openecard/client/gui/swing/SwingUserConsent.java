package org.openecard.client.gui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.definition.UserConsentDescription;


/**
 * Swing implementation of the UserConsent interface.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @editor Florian Feldmann <florian.feldmann@rub.de>
 */
public class SwingUserConsent implements UserConsent {

    private final DialogWrapper dialogWrapper;

    /**
     * Instantiate SwingUserConsent. The implementation encapsulates a DialogWrapper which is needed to supply a root pane for all draw operations.
     * @param dialogWrapper
     */
    public SwingUserConsent(DialogWrapper dialogWrapper) {
	this.dialogWrapper = dialogWrapper;
    }


    @Override
    public UserConsentNavigator obtainNavigator(UserConsentDescription parameters) {
	dialogWrapper.setTitle(parameters.getTitle());
	// prepare root panel for display
	Container rootPanel = dialogWrapper.getRootPanel();
	rootPanel.removeAll();
        BorderLayout layout = new BorderLayout();
	rootPanel.setLayout(layout);
	JPanel stepContainer = new JPanel();
	JScrollPane scrollpane = new JScrollPane(stepContainer, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	JPanel sidebarPanel = new JPanel();
	stepContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	sidebarPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	rootPanel.add(scrollpane, BorderLayout.CENTER);
        
        // FF: Sidebar should be left to be similar to AusweisApp
	//rootPanel.add(sidebarPanel, BorderLayout.EAST);
        rootPanel.add(sidebarPanel, BorderLayout.LINE_START);

	// create the step container and display UC
	String dialogType = parameters.getDialogType();
	dialogType = dialogType==null ? "" : dialogType;
	List<Step> steps = parameters.getSteps();
	Sidebar sidebar = new Sidebar(sidebarPanel, stepNames(steps));
	SwingNavigator navigator = new SwingNavigator(dialogWrapper, dialogType, steps, stepContainer, sidebar);
	rootPanel.validate();
	rootPanel.repaint();

	return navigator;
    }


    /**
     * Create a list of step names. This is a helper function to factor out code from the actual call.
     * @param steps Step list as contained in ObtainUserConsent.
     * @return Array containing the names of all steps.
     */
    private static String[] stepNames(List<Step> steps) {
	ArrayList<String> stepNames = new ArrayList<String>(steps.size());
	for (Step s : steps) {
            // steps are added in HTML format for layout -
            // <br/><br/> adds a new line between entries in sidebar
	    stepNames.add("<html>"+s.getName()+"<br/><br/></html>");
	}
	return stepNames.toArray(new String[steps.size()]);
    }

}
