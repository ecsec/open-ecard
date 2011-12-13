package org.openecard.client.gui.definition;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Signaturefield implements InputInfoUnit, OutputInfoUnit {

    private String name;
    private String text;
    private byte[] value;

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
    public byte[] getValue() {
	return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(byte[] value) {
	this.value = value;
    }


    @Override
    public InfoUnitElementType type() {
	return InfoUnitElementType.Signaturefield;
    }

}
