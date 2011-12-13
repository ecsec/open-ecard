package org.openecard.client.gui.definition;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class BoxItem {

    private String name;
    private String text;
    private boolean checked  = false;
    private boolean disabled = false;

    /**
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * @return the text
     */
    public String getText() {
	return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
	this.text = text;
    }

    /**
     * @return the checked
     */
    public boolean isChecked() {
	return checked;
    }

    /**
     * @param checked the checked to set
     */
    public void setChecked(boolean checked) {
	this.checked = checked;
    }

    /**
     * @return the disabled
     */
    public boolean isDisabled() {
	return disabled;
    }

    /**
     * @param disabled the disabled to set
     */
    public void setDisabled(boolean disabled) {
	this.disabled = disabled;
    }

}
