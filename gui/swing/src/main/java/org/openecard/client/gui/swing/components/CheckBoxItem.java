package org.openecard.client.gui.swing.components;

import javax.swing.JCheckBox;
import org.openecard.client.gui.swing.common.GUIDefaults;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CheckBoxItem extends JCheckBox {

    public CheckBoxItem() {
    }

    public CheckBoxItem(String text) {
	this(text, true);
    }

    public CheckBoxItem(String text, boolean selected) {
	super(text, selected);
	setIcon(GUIDefaults.getImage("CheckBox.icon"));
	setDisabledIcon(GUIDefaults.getImage("CheckBox.disabledIcon"));
	setDisabledSelectedIcon(GUIDefaults.getImage("CheckBox.disabledSelectedIcon"));
	setSelectedIcon(GUIDefaults.getImage("CheckBox.selectedIcon"));
    }
}
