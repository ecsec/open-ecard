package org.openecard.client.gui.definition;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Text implements InputInfoUnit {

    private String text;

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


    @Override
    public InfoUnitElementType type() {
	return InfoUnitElementType.Text;
    }

}
