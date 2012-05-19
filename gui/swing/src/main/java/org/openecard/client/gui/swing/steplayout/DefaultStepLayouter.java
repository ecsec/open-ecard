package org.openecard.client.gui.swing.steplayout;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
 * Updated Default layouter. Should be fine for most generic forms
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @editor Florian Feldmann <florian.feldmann@rub.de>
 */
public class DefaultStepLayouter extends StepLayouter {

    private final ArrayList<StepComponent> components;
    private final JPanel rootPanel;

    protected DefaultStepLayouter(List<InputInfoUnit> infoUnits, String stepName) {
	components = new ArrayList<StepComponent>(infoUnits.size());
	rootPanel = new JPanel(new BorderLayout());

	// Add step title
	JLabel title = new JLabel("<html><h3>" + stepName + "</h3></html>");
	rootPanel.add(title, BorderLayout.PAGE_START);

	GridBagLayout layout = new GridBagLayout();
	GridBagConstraints gbc = new GridBagConstraints();

	JPanel contentPanel = new JPanel();
	contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
	contentPanel.setLayout(layout);

	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.anchor = GridBagConstraints.WEST;
	gbc.weightx = 0.5;
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbc.insets = new Insets(0, 0, 10, 0);

	// Create content
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

	// Add empty dummy element
	gbc.weighty = 1.0;
	contentPanel.add(new JLabel(), gbc);

	JScrollPane pane = new JScrollPane(contentPanel);
	pane.setBorder(BorderFactory.createEmptyBorder());
	pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	layout.setConstraints(pane, gbc);

	rootPanel.add(pane, BorderLayout.CENTER);
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
