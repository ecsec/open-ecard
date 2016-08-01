/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.versioncheck;

import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author Tobias Wich
 */
public class VersionCheckTest {

    @Test
    public void testJavaVersion() {
	String[] versions = new String[] {"1.7.0", "1.7.0_50", "1.8.0", "9", "9.1.2+62", "9+100"};
	for (String version : versions) {
	    System.setProperty("java.version", version);
	    Assert.assertTrue(MainLoader.checkJavaVersion(1.6f));
	    Assert.assertTrue(MainLoader.checkJavaVersion(1.7f));
	    Assert.assertFalse(MainLoader.checkJavaVersion(100.0f));
	}
    }

}
