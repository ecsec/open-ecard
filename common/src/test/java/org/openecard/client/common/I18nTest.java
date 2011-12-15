package org.openecard.client.common;

import org.junit.Assert;
import org.junit.Test;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class I18nTest {

    @Test
    public void testTranslation() {
	I18n i = I18n.getTranslation("ifd");
	String result = i.translationForKey("pin_title");
	Assert.assertEquals("Enter secret '%s'", result);
    }

    @Test
    public void testTemplate() {
	I18n i = I18n.getTranslation("ifd");
	String result = i.translationForKey("pin_title", "PIN");
	Assert.assertEquals("Enter secret 'PIN'", result);
    }

}
