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

package org.openecard.common;

import java.util.Scanner;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author Tobias Wich
 */
public class AppVersionTest {

    @Test
    public void testVersion() {
	// read app name from file
	Scanner nameScan = new Scanner(AppVersionTest.class.getResourceAsStream("/openecard/APPNAME"), "UTF-8");
	String refName = nameScan.useDelimiter("\\A").next().trim();
	Assert.assertFalse(refName.isEmpty());

	String name = AppVersion.getName();
	Assert.assertEquals(name, refName);

	String version = AppVersion.getVersion();
	int major = AppVersion.getMajor();
	int minor = AppVersion.getMinor();
	int patch = AppVersion.getPatch();
	String buildId = AppVersion.getBuildId();
	String reconstructedVersion = major + "." + minor + "." + patch;
	if (buildId != null) {
	    reconstructedVersion += "-" + buildId;
	}

	Assert.assertEquals(reconstructedVersion, version);
    }

    @Test void compareVersions() {
	Version old = new Version(null, "1.0.0", null, null);
	Version new1 = new Version(null, "1.1.0", null, null);
	Version new2 = new Version(null, "1.1.0", null, null);
	Version snap = new Version(null, "1.1.0-rc1", null, null);

	Assert.assertTrue(old.isOlder(new1));
	Assert.assertFalse(new1.isOlder(old));

	Assert.assertFalse(new1.isNewer(new2));
	Assert.assertFalse(new2.isNewer(new1));
	Assert.assertFalse(new1.isOlder(new2));
	Assert.assertFalse(new2.isOlder(new1));
	Assert.assertTrue(new1.isSame(new2));

	Assert.assertFalse(new1.isSame(snap));
	Assert.assertTrue(new1.isNewer(snap));
    }

}
