package org.openecard.client.gui.definition;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public abstract class AbstractTextfield implements InputInfoUnit, OutputInfoUnit {

    private String name;
    private String text;
    private String value;
    private int minLength = 0;
    private int maxLength = Integer.MAX_VALUE;

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
     * @return the value
     */
    public String getValue() {
	return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
	this.value = value;
    }

    /**
     * @return the minLength
     */
    public int getMinLength() {
	return minLength;
    }

    /**
     * @param minLength the minLength to set
     */
    public void setMinLength(int minLength) {
	this.minLength = minLength;
    }

    /**
     * @return the maxLength
     */
    public int getMaxLength() {
	return maxLength;
    }

    /**
     * @param maxLength the maxLength to set
     */
    public void setMaxLength(int maxLength) {
	this.maxLength = maxLength;
    }

}
