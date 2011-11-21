package org.openecard.client.gui.swing.components;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.openecard.ws.gui.v1.BoxItem;
import org.openecard.ws.gui.v1.CheckBox;
import org.openecard.ws.gui.v1.InfoUnitType;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Checkbox implements StepComponent {

    private final CheckBox result;
    private final ArrayList<JCheckBox> boxButtons;
    private final JPanel panel;

    public Checkbox(CheckBox checkbox) {
        this.result = new CheckBox();
        this.panel = new JPanel();
        GridLayout layout = new GridLayout(0, 1);
        this.panel.setLayout(layout);

        // create buttons, item copies and add to panel
        boxButtons = new ArrayList<JCheckBox>(checkbox.getBoxItem().size());
        List<BoxItem> boxItems = this.result.getBoxItem();
        for (BoxItem next : checkbox.getBoxItem()) {
            // copy box item
            BoxItem copy = new BoxItem();
            boxItems.add(copy);
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
            this.panel.add(component);
            this.boxButtons.add(component);
        }
    }


    @Override
    public Component getComponent() {
        return this.panel;
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
    public InfoUnitType getValue() {
        // loop over checkboxes and set checked values in result
        for (int i=0; i < this.boxButtons.size(); i++) {
            JCheckBox component = this.boxButtons.get(i);
            this.result.getBoxItem().get(i).setChecked(component.isSelected());
        }
        // prepare result
        InfoUnitType unit = new InfoUnitType();
        unit.setCheckBox(result);
        return unit;
    }

}
