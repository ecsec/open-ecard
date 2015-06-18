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

import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author Tobias Wich
 */
public class VersionTest {

    @Test
    public void testVersion() {
	String name = Version.getName();
	Assert.assertEquals(name, "Open eCard App");

	String version = Version.getVersion();
	int major = Version.getMajor();
	int minor = Version.getMinor();
	int patch = Version.getPatch();
	String buildId = Version.getBuildId();
	String reconstructedVersion = major + "." + minor + "." + patch;
	if (buildId != null) {
	    reconstructedVersion += "-" + buildId;
	}

	Assert.assertEquals(reconstructedVersion, version);
    }

}
