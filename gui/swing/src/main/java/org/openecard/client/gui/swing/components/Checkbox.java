/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.client.gui.swing.components;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.openecard.client.gui.definition.BoxItem;
import org.openecard.client.gui.definition.OutputInfoUnit;
import org.openecard.client.gui.swing.StepFrame;


/**
 * Implementation of a checkbox group for use in a {@link StepFrame}.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Florian Feldmann <florian.feldmann@rub.de>
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class Checkbox implements StepComponent {

    private final org.openecard.client.gui.definition.Checkbox result;
    private final ArrayList<JCheckBox> boxButtons;
    private final List<BoxItem> defaultCheckbox;
    private final JPanel panel;
    JPanel contentPanel = new JPanel();

    public Checkbox(org.openecard.client.gui.definition.Checkbox checkbox) {
	result = new org.openecard.client.gui.definition.Checkbox(checkbox.getID()); // copy of checkbox, so result is pre assembled
	panel = new JPanel(new BorderLayout());

//	contentPanel.setBorder(new LineBorder(Color.GRAY));
	panel.setBorder(new EmptyBorder(10, 10, 10, 10));

	GridLayout layout = new GridLayout(0, 2);
//	GridBagLayout layout = new GridBagLayout();
	contentPanel.setLayout(layout);
	GridBagConstraints c = new GridBagConstraints();
	int alternate = 0;

	// create buttons, item copies and add to panel
	boxButtons = new ArrayList<JCheckBox>(checkbox.getBoxItems().size());
	defaultCheckbox = checkbox.getBoxItems();
	for (BoxItem next : checkbox.getBoxItems()) {
	    // copy box item
	    BoxItem copy = new BoxItem();
	    result.getBoxItems().add(copy);
	    copy.setName(next.getName());
	    copy.setText(next.getText());
	    copy.setDisabled(next.isDisabled());
	    // create checkbox
	    CheckBoxItem component = new CheckBoxItem((next.getText() == null) ? "" : next.getText(), next.isChecked());
	    if (next.isDisabled()) {
		component.setEnabled(false);
	    }
	    if (next.isChecked()) {
		component.setSelected(true);
	    }

	    component.setBackground(Color.WHITE);

	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.gridx = alternate;
	    c.gridy = GridBagConstraints.RELATIVE;
	    alternate = alternate ^ 1; // alternate checkboxes left and right -
	    // disable for vertical layout only

	    contentPanel.add(component);
//	    panel.add(component, c);
	    boxButtons.add(component);
	}
	panel.add(contentPanel, BorderLayout.CENTER);
    }

    public void resetSelection() {
	for (int i = 0; i < contentPanel.getComponentCount(); i++) {
	    JCheckBox b = (JCheckBox) contentPanel.getComponent(i);
	    for (int j = 0; j < defaultCheckbox.size(); j++) {
		if (b.getName().equals(defaultCheckbox.get(j).getName())) {
		    b.setSelected(defaultCheckbox.get(j).isChecked());
		}
	    }
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
	for (int i = 0; i < boxButtons.size(); i++) {
	    JCheckBox component = boxButtons.get(i);
	    this.result.getBoxItems().get(i).setChecked(component.isSelected());
	}
	// prepare result
	return result;
    }
}
