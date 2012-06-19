/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.client.common;

import org.testng.annotations.Test;


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
