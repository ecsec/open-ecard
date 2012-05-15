package org.openecard.client.gui.definition;

/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ToggleText implements InputInfoUnit {

    private String title;
    private String text;
    private boolean collapsed;

    public boolean isCollapsed() {
	return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
	this.collapsed = collapsed;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public String getText() {
	return text;
    }

    public void setText(String text) {
	this.text = text;
    }

    @Override
    public InfoUnitElementType type() {
	return InfoUnitElementType.ToggleText;
    }
}
