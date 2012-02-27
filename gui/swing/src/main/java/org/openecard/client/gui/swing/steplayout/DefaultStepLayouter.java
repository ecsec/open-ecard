package org.openecard.client.gui.swing.steplayout;

import java.awt.Container;
import java.awt.GridBagConstraints;
//import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
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
 * Updated Default layouter. Should be fine for most generic forms
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @editor Florian Feldmann <florian.feldmann@rub.de>
 */
public class DefaultStepLayouter extends StepLayouter {

    private final ArrayList<StepComponent> components;
    private final JPanel contentPanel;

    protected DefaultStepLayouter(List<InputInfoUnit> infoUnits, String stepName) {
	components = new ArrayList<StepComponent>(infoUnits.size());
        
        // using GridBagLayout over GridLayout gives much more control of
        // components' position and layout
        //
        // basically, all components are positioned into the first column -
        // multicolumn layout should only be used for specific forms
	GridBagLayout contentLayout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        //GridLayout contentLayout = new GridLayout(0, 1);
	
        contentPanel = new JPanel(contentLayout);
        
        // create title tag from stepName, formatting can be done in HTML
        JLabel title = new JLabel("<html><h2>"+stepName+"</h2></html>");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0.2;
        c.weightx = 0.2;
        c.anchor =GridBagConstraints.FIRST_LINE_START;
        c.gridx = 0; // place in top-left grid element (x=0, y=0)
        c.gridy = 0;
        contentPanel.add(title, c);

	// create content
	for (InputInfoUnit next : infoUnits) {
	    StepComponent nextComponent = null;
            c = new GridBagConstraints();
            int x = 0;
	    switch (next.type()) {
		case Checkbox:
                    nextComponent      = new Checkbox((org.openecard.client.gui.definition.Checkbox)next);
                    //c.fill = GridBagConstraints.HORIZONTAL; // fill is already done in Checkbox definition
                    c.anchor = GridBagConstraints.WEST;
                    c.weighty = 0.2;
                    c.gridx = 0;
                    c.gridy = GridBagConstraints.RELATIVE;
                    break;
		case Hyperlink:
                    nextComponent     = new Hyperlink((org.openecard.client.gui.definition.Hyperlink)next);
                    c.fill = GridBagConstraints.HORIZONTAL;
                    c.anchor = GridBagConstraints.WEST;
                    c.weighty = 0.2;
                    c.gridx = 0;
                    c.gridy = GridBagConstraints.RELATIVE;
                    break;
		case Passwordfield:
                    nextComponent = new AbstractInput((Passwordfield)next);
                    c.fill = GridBagConstraints.HORIZONTAL;
                    c.anchor = GridBagConstraints.WEST;
                    c.weighty = 0.2;
                    c.gridx = 0;
                    c.gridy = GridBagConstraints.RELATIVE;
                    break;
		case Radiobox:
                    nextComponent      = new Radiobutton((Radiobox)next);
                    c.fill = GridBagConstraints.HORIZONTAL;
                    c.anchor = GridBagConstraints.WEST;
                    c.weighty = 0.2;
                    c.gridx = 0;
                    c.gridy = GridBagConstraints.RELATIVE;
                    break;
		case Signaturefield:
                    throw new UnsupportedOperationException("Not implemented yet.");
		case Text:
                    nextComponent          = new Text((org.openecard.client.gui.definition.Text)next);
                    c.fill = GridBagConstraints.HORIZONTAL;
                    c.anchor = GridBagConstraints.WEST;
                    c.weighty = 0.2;
                    c.gridx = 0;
                    c.gridy = GridBagConstraints.RELATIVE;
                    break;
		case Textfield:
                    nextComponent     = new AbstractInput((Textfield)next); 
                    c.fill = GridBagConstraints.HORIZONTAL;
                    c.anchor = GridBagConstraints.WEST;
                    c.weighty = 0.2;
                    c.gridx = 0;
                    c.gridy = GridBagConstraints.RELATIVE;
                    break;
	    }

	    // add to list panel
	    components.add(nextComponent);
            //contentPanel.add(nextComponent.getComponent());
	    contentPanel.add(nextComponent.getComponent(), c);
	}
        
        // add empty dummy JLabel for positioning - 
        // by setting weights to 100%, all available free space will be stuffed
        // to the end of the dialog contents
        //
        // this MUST be the last element to be added to the contentPanel!
        JLabel dummy = new JLabel();
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 1.0;
        c.weightx = 1.0;
        c.anchor =GridBagConstraints.FIRST_LINE_START;
        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        contentPanel.add(dummy, c);
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
