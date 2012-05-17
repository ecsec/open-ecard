/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openecard.client.gui.swing.steplayout;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.openecard.client.gui.definition.InputInfoUnit;
import org.openecard.client.gui.definition.PasswordField;
import org.openecard.client.gui.definition.Radiobox;
import org.openecard.client.gui.definition.Textfield;
import org.openecard.client.gui.swing.components.AbstractInput;
import org.openecard.client.gui.swing.components.Checkbox;
import org.openecard.client.gui.swing.components.Hyperlink;
import org.openecard.client.gui.swing.components.Radiobutton;
import org.openecard.client.gui.swing.components.StepComponent;
import org.openecard.client.gui.swing.components.Text;
import org.openecard.client.gui.swing.components.ToggleText;

/**
 *
 * @author John
 */
public class NPAStepLayouter extends StepLayouter {

    private final ArrayList<StepComponent> components;
    private final JPanel rootPanel;

    protected NPAStepLayouter(List<InputInfoUnit> infoUnits, String stepName) {
	components = new ArrayList<StepComponent>(infoUnits.size());

	// using GridBagLayout over GridLayout gives much more control of
	// components' position and layout
	//
	// basically, all components are positioned into the first column -
	// multicolumn layout should only be used for specific forms
	GridBagLayout layout = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();

	rootPanel = new JPanel(new BorderLayout());
	rootPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

	// create title tag from stepName, formatting can be done in HTML
	JLabel title = new JLabel("<html><h3>" + stepName + "</h3></html>");
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.anchor = GridBagConstraints.WEST;
	gbc.weightx = 0.5;
	gbc.anchor = GridBagConstraints.WEST;
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	JPanel headerPanel = new JPanel(layout);
	JPanel contentPanel = new JPanel(layout);
	contentPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
	headerPanel.add(title, gbc);
	rootPanel.add(headerPanel, BorderLayout.PAGE_START);

	// create content
	for (InputInfoUnit next : infoUnits) {
	    StepComponent nextComponent = null;

	    switch (next.type()) {
		case CHECK_BOX:
		    nextComponent = new Checkbox((org.openecard.client.gui.definition.Checkbox) next);
		    break;
		case HYPERLINK:
		    nextComponent = new Hyperlink((org.openecard.client.gui.definition.Hyperlink) next);
		    break;
		case PASSWORD_FIELD:
		    nextComponent = new AbstractInput((PasswordField) next);
		    break;
		case RADIO_BOX:
		    nextComponent = new Radiobutton((Radiobox) next);
		    break;
		case SIGNAUTRE_FIELD:
		    throw new UnsupportedOperationException("Not implemented yet.");
		case TEXT:
		    nextComponent = new Text((org.openecard.client.gui.definition.Text) next);
		    break;
		case TEXT_FIELD:
		    nextComponent = new AbstractInput((Textfield) next);
		    break;
		case ToggleText:
		    nextComponent = new ToggleText((org.openecard.client.gui.definition.ToggleText) next);
		    break;
	    }
	    if (nextComponent != null) {
		components.add(nextComponent);
		contentPanel.add(nextComponent.getComponent(), gbc);
	    }
	}

//		rootPanel.add(new JScrollPane(contentPanel), gbc);


	// Add empty dummy element
	gbc.weighty = 1.0;
	contentPanel.add(new JLabel(), gbc);
	JScrollPane pane = new JScrollPane(contentPanel);
	pane.setBorder(BorderFactory.createEmptyBorder());
	pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	rootPanel.add(pane, BorderLayout.CENTER);
	rootPanel.add(new JLabel(), BorderLayout.PAGE_END);
    }

    @Override
    public List<StepComponent> getComponents() {
	return components;
    }

    @Override
    public Container getPanel() {
	return rootPanel;
    }
}
