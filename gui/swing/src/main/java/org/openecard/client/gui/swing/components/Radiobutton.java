package org.openecard.client.gui.swing.components;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.openecard.client.gui.definition.BoxItem;
import org.openecard.client.gui.definition.OutputInfoUnit;
import org.openecard.client.gui.definition.Radiobox;


/**
 * Implementation of a radio button group for use in a {@link StepFrame}.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Radiobutton implements StepComponent {

    private final Radiobox result;
    private final ArrayList<JRadioButton> buttons;
    private final JPanel panel;

    public Radiobutton(Radiobox radio) {
	result = new Radiobox(); // copy of radio, so result is pre assembled
	panel = new JPanel();
	GridLayout layout = new GridLayout(0, 1);
	panel.setLayout(layout);

	// create buttons and add to label, also copy items to result
	ButtonGroup bg = new ButtonGroup();
	buttons = new ArrayList<JRadioButton>(radio.getBoxItems().size());
	for (BoxItem next : radio.getBoxItems()) {
	    // copy box item
	    BoxItem copy = new BoxItem();
	    result.getBoxItems().add(copy);
	    copy.setName(next.getName());
	    copy.setText(next.getText());
	    copy.setDisabled(next.isDisabled());
	    // create checkbox
	    JRadioButton component = new JRadioButton((next.getText()==null) ? "" : next.getText(), next.isChecked());
	    bg.add(component);
	    if (next.isDisabled()) {
		component.setEnabled(false);
	    }
	    if (next.isChecked()) {
		component.setSelected(true);
	    }
	    panel.add(component);
	    buttons.add(component);
	}
    }


    @Override
    public Component getComponent() {
	return panel;
    }

    @Override
    public boolean validate() {
	// only valid if exactly one button is selected
	int numSelected = 0;
	for (JRadioButton next : buttons) {
	    if (next.isSelected()) {
		numSelected++;
	    }
	}
	return numSelected == 1;
    }

    @Override
    public boolean isValueType() {
	return true;
    }

    @Override
    public OutputInfoUnit getValue() {
	// loop over checkboxes and set checked values in result
	for (int i=0; i < buttons.size(); i++) {
	    JRadioButton component = buttons.get(i);
	    result.getBoxItems().get(i).setChecked(component.isSelected());
	}
	return result;
    }

}
