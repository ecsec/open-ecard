package org.openecard.client.gui.swing.steplayout;

import java.awt.Container;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.openecard.client.gui.definition.InputInfoUnit;
import org.openecard.client.gui.definition.Passwordfield;
import org.openecard.client.gui.definition.Radiobox;
import org.openecard.client.gui.definition.Textfield;
import org.openecard.client.gui.swing.components.AbstractInput;
import org.openecard.client.gui.swing.components.Checkbox;
import org.openecard.client.gui.swing.components.Hyperlink;
import org.openecard.client.gui.swing.components.Radiobutton;
import org.openecard.client.gui.swing.components.StepComponent;
import org.openecard.client.gui.swing.components.Text;


/**
 * Default layouter. Ugly but works in any case.
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class DefaultStepLayouter extends StepLayouter {

    private final ArrayList<StepComponent> components;
    private final JPanel contentPanel;

    protected DefaultStepLayouter(List<InputInfoUnit> infoUnits) {
	components = new ArrayList<StepComponent>(infoUnits.size());
	GridLayout contentLayout = new GridLayout(0, 1);
	contentPanel = new JPanel(contentLayout);

	// create content
	for (InputInfoUnit next : infoUnits) {
	    StepComponent nextComponent = null;
	    switch (next.type()) {
		case Checkbox: nextComponent      = new Checkbox((org.openecard.client.gui.definition.Checkbox)next); break;
		case Hyperlink: nextComponent     = new Hyperlink((org.openecard.client.gui.definition.Hyperlink)next); break;
		case Passwordfield: nextComponent = new AbstractInput((Passwordfield)next); break;
		case Radiobox: nextComponent      = new Radiobutton((Radiobox)next); break;
		case Signaturefield: throw new UnsupportedOperationException("Not implemented yet.");
		case Text: nextComponent          = new Text((org.openecard.client.gui.definition.Text)next); break;
		case Textfield: nextComponent     = new AbstractInput((Textfield)next); break;
	    }

	    // add to list panel
	    components.add(nextComponent);
	    contentPanel.add(nextComponent.getComponent());
	}
    }


    @Override
    public List<StepComponent> getComponents() {
	return components;
    }

    @Override
    public Container getPanel() {
	return contentPanel;
    }

}
