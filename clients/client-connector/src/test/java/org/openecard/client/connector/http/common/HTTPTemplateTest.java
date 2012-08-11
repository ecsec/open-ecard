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

package org.openecard.client.connector.http.common;

import org.openecard.client.connector.common.DocumentRoot;
import org.openecard.client.connector.common.HTTPTemplate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class HTTPTemplateTest {

    static HTTPTemplate template;

    @BeforeClass
    public static void setUpClass() throws Exception {
	DocumentRoot documentRoot = new DocumentRoot("\\www");
	template = new HTTPTemplate(documentRoot, "\\templates\\error.html");
    }

    /**
     * Test of setProperty method, of class HTTPTemplate.
     */
    @Test(enabled = !true)
    public void testSetProperty() {
	template.setProperty("%%%TITLE%%%", "My header");
	template.setProperty("%%%HEADLINE%%%", "My headline");
	template.setProperty("%%%MESSAGE%%%", "My message");

	System.out.println(template.toString());
    }

}
