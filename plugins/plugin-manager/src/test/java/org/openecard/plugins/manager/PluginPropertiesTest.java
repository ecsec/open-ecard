/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.plugins.manager;

import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * This is the Test for the PluginProperties.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PluginPropertiesTest {

    /**
     * Test loading, setting and getting of properties.
     * 
     * @throws IOException if the home configuration directory coudn't be found or the properties file coudn't be loaded
     */
    @Test
    public void test() throws IOException {
	// Test loading
	PluginProperties.loadProperties();

	// Test default value for unknown properties to be false 
	boolean retValue = Boolean.parseBoolean(PluginProperties.getProperty("NonExistent"));
	Assert.assertFalse(retValue);

	// Test if set property returns the right values
	PluginProperties.setProperty("testProperty", "true");
	retValue = Boolean.parseBoolean(PluginProperties.getProperty("testProperty"));
	Assert.assertTrue(retValue);
	PluginProperties.setProperty("testProperty", "false");
	retValue = Boolean.parseBoolean(PluginProperties.getProperty("testProperty"));
	Assert.assertFalse(retValue);
    }

}
