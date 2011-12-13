package org.openecard.client.gui.definition;

import java.net.MalformedURLException;
import java.net.URL;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Hyperlink implements InputInfoUnit {

    private String text;
    private URL href;

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
     * @return the href
     */
    public URL getHref() {
	return href;
    }

    /**
     * @param href the href to set
     */
    public void setHref(URL href) {
	this.href = href;
    }
    /**
     * @param href the href to set
     */
    public void setHref(String href) throws MalformedURLException {
	this.href = new URL(href);
    }


    @Override
    public InfoUnitElementType type() {
	return InfoUnitElementType.Hyperlink;
    }

}
