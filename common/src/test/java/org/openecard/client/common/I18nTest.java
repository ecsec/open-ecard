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
	//FIXME funktioniert nur mit EN!
//	Assert.assertEquals("Enter secret '%s'", result);
    }

    @Test
    public void testTemplate() {
	I18n i = I18n.getTranslation("ifd");
	String result = i.translationForKey("pin_title", "PIN");
	//FIXME funktioniert nur mit EN!
//	Assert.assertEquals("Enter secret 'PIN'", result);
    }
}
