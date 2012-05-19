package org.openecard.client.gui.swing.components;

import java.awt.Dimension;
import javax.swing.Icon;
import javax.swing.JLabel;
import org.openecard.client.gui.swing.common.GUIDefaults;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class ToggleTextIndicator extends JLabel {

//    private String openedIndicator = " ▼";
    private Icon openedIndicator = GUIDefaults.getImage("ToggleText.selectedIcon");
//    private String closedIndicator = " ▲";
    private Icon closedIndicator = GUIDefaults.getImage("ToggleText.icon");

    /**
     * Creates a new ToggleTextIndicator.
     */
    public ToggleTextIndicator() {
	super(" ");
	int height = Math.max(openedIndicator.getIconHeight(), closedIndicator.getIconHeight());
	setPreferredSize(new Dimension(1, height));

    }

    /**
     * Creates a new ToggleTextIndicator.
     *
     * @param collapsed
     */
    public ToggleTextIndicator(boolean collapsed) {
	setCollapsed(collapsed);
    }

    /**
     * Sets the indicator.
     *
     * @param openedIndicator
     */
    public void setOpenedIndicator(String openedIndicator) {
//	this.openedIndicator = openedIndicator;
    }

    /**
     * Sets the indicator.
     *
     * @param closedIndicator
     */
    public void setClosedIndicator(String closedIndicator) {
//	this.closedIndicator = closedIndicator;
    }

    /**
     * Sets the toggle status of the indicator.
     *
     * @param collapsed Collapsed
     */
    public void setCollapsed(boolean collapsed) {
	if (collapsed) {
//	    setText(openedIndicator);
	    setIcon(openedIndicator);
	} else {
//	    setText(closedIndicator);
	    setIcon(closedIndicator);
	}
    }
}
