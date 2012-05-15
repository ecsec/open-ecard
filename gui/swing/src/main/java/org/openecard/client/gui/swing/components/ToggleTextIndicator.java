package org.openecard.client.gui.swing.components;

import javax.swing.JLabel;

/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class ToggleTextIndicator extends JLabel {

    private String openedIndicator = " ▼";
    private String closedIndicator = " ▲";

    /**
     * Creates a new ToggleTextIndicator.
     */
    public ToggleTextIndicator() {
	super(" ");
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
	this.openedIndicator = openedIndicator;
    }

    /**
     * Sets the indicator.
     *
     * @param closedIndicator
     */
    public void setClosedIndicator(String closedIndicator) {
	this.closedIndicator = closedIndicator;
    }

    /**
     * Sets the toggle status of the indicator.
     *
     * @param collapsed Collapsed
     */
    public void setCollapsed(boolean collapsed) {
	if (collapsed) {
	    setText(openedIndicator);
	} else {
	    setText(closedIndicator);
	}
    }
}
