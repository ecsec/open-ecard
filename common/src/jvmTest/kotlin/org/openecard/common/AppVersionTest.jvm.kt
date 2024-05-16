/****************************************************************************
 * Copyright (C) 2012-2024 ecsec GmbH.
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

package org.openecard.common

import org.openecard.common.AppVersion.buildId
import org.openecard.common.AppVersion.major
import org.openecard.common.AppVersion.minor
import org.openecard.common.AppVersion.name
import org.openecard.common.AppVersion.patch
import org.openecard.common.AppVersion.versionString
import org.testng.Assert
import org.testng.annotations.Test
import java.util.*

/**
 *
 * @author Tobias Wich
 */
class AppVersionTest {
    @Test
    fun testVersion() {
        // read app name from file
        val nameScan = Scanner(AppVersionTest::class.java.getResourceAsStream("/openecard/APPNAME")!!, "UTF-8")
        val refName = nameScan.useDelimiter("\\A").next().trim { it <= ' ' }
        Assert.assertFalse(refName.isEmpty())

        val name = name
        Assert.assertEquals(name, refName)

        val version = versionString
        val major = major
        val minor = minor
        val patch = patch
        val buildId = buildId
        var reconstructedVersion = "$major.$minor.$patch"
        if (buildId != null) {
            reconstructedVersion += "-$buildId"
        }

        Assert.assertEquals(reconstructedVersion, version)
    }

    @Test
    fun compareVersions() {
        val old = Version(null, "1.0.0", null, null)
        val new1 = Version(null, "1.1.0", null, null)
        val new2 = Version(null, "1.1.0", null, null)
        val snap = Version(null, "1.1.0-rc1", null, null)

        Assert.assertTrue(old.version.isOlder(new1.version))
        Assert.assertFalse(new1.version.isOlder(old.version))

        Assert.assertFalse(new1.version.isNewer(new2.version))
        Assert.assertFalse(new2.version.isNewer(new1.version))
        Assert.assertFalse(new1.version.isOlder(new2.version))
        Assert.assertFalse(new2.version.isOlder(new1.version))
        Assert.assertTrue(new1.version.isSame(new2.version))

        Assert.assertFalse(new1.version.isSame(snap.version))
        Assert.assertTrue(new1.version.isNewer(snap.version))
    }
}
