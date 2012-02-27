package org.openecard.client.gui.swing.components;

import java.awt.Component;
import java.awt.GridBagConstraints;
//import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.openecard.client.gui.definition.BoxItem;
import org.openecard.client.gui.definition.OutputInfoUnit;
import org.openecard.client.gui.swing.StepFrame;


/**
 * Implementation of a checkbox group for use in a {@link StepFrame}.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @editor Florian Feldmann <florian.feldmann@rub.de>
 */
public class Checkbox implements StepComponent {

    private final org.openecard.client.gui.definition.Checkbox result;
    private final ArrayList<JCheckBox> boxButtons;
    private final JPanel panel;

    public Checkbox(org.openecard.client.gui.definition.Checkbox checkbox) {
	result = new org.openecard.client.gui.definition.Checkbox(); // copy of checkbox, so result is pre assembled
	panel = new JPanel();
        
	//GridLayout layout = new GridLayout(0, 1);
        GridBagLayout layout = new GridBagLayout();
	panel.setLayout(layout);
        GridBagConstraints c = new GridBagConstraints();
        int alternate = 0;

	// create buttons, item copies and add to panel
	boxButtons = new ArrayList<JCheckBox>(checkbox.getBoxItems().size());
	for (BoxItem next : checkbox.getBoxItems()) {
	    // copy box item
	    BoxItem copy = new BoxItem();
	    result.getBoxItems().add(copy);
	    copy.setName(next.getName());
	    copy.setText(next.getText());
	    copy.setDisabled(next.isDisabled());
	    // create checkbox
	    JCheckBox component = new JCheckBox((next.getText()==null) ? "" : next.getText(), next.isChecked());
	    if (next.isDisabled()) {
		component.setEnabled(false);
	    }
	    if (next.isChecked()) {
		component.setSelected(true);
	    }
            
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = alternate;
            c.gridy = GridBagConstraints.RELATIVE;
            alternate = alternate ^ 1; // alternate checkboxes left and right -
                                       // disable for vertical layout only
                    
	    //panel.add(component);
            panel.add(component, c);
	    boxButtons.add(component);
	}
    }


    @Override
    public Component getComponent() {
	return panel;
    }

    @Override
    public boolean validate() {
	return true;
    }

    @Override
    public boolean isValueType() {
	return true;
    }

    @Override
    public OutputInfoUnit getValue() {
	// loop over checkboxes and set checked values in result
	for (int i=0; i < boxButtons.size(); i++) {
	    JCheckBox component = boxButtons.get(i);
	    this.result.getBoxItems().get(i).setChecked(component.isSelected());
	}
	// prepare result
	return result;
    }

}
