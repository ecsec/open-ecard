package org.openecard.client.gui.swing.components;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.openecard.ws.gui.v1.BoxItem;
import org.openecard.ws.gui.v1.InfoUnitType;
import org.openecard.ws.gui.v1.Radio;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Radiobutton implements StepComponent {

    private final Radio result;
    private final ArrayList<JRadioButton> buttons;
    private final JPanel panel;

    public Radiobutton(Radio radio) {
        this.result = new Radio();
        this.panel = new JPanel();
        GridLayout layout = new GridLayout(0, 1);
        this.panel.setLayout(layout);

        // create buttons, item copies and add to panel
        ButtonGroup bg = new ButtonGroup();
        buttons = new ArrayList<JRadioButton>(radio.getBoxItem().size());
        List<BoxItem> boxItems = this.result.getBoxItem();
        for (BoxItem next : radio.getBoxItem()) {
            // copy box item
            BoxItem copy = new BoxItem();
            boxItems.add(copy);
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
            this.panel.add(component);
            this.buttons.add(component);
        }
    }


    @Override
    public Component getComponent() {
        return this.panel;
    }

    @Override
    public boolean validate() {
        // only valid if exactly one button is selected
        int numSelected = 0;
        for (JRadioButton next : this.buttons) {
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
    public InfoUnitType getValue() {
        // loop over checkboxes and set checked values in result
        for (int i=0; i < this.buttons.size(); i++) {
            JRadioButton component = this.buttons.get(i);
            this.result.getBoxItem().get(i).setChecked(component.isSelected());
        }
        // prepare result
        InfoUnitType unit = new InfoUnitType();
        unit.setRadio(result);
        return unit;
    }

}
