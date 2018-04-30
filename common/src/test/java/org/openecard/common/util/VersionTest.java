/****************************************************************************
 * Copyright (C) 2018 ecsec GmbH.
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

package org.openecard.common.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import mockit.Expectations;
import mockit.Mocked;
import org.openecard.common.AppVersion;
import org.openecard.common.SemanticVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/**
 *
 * @author Sebastian Schuberth
 */
public class VersionTest {

    private static final Logger LOG = LoggerFactory.getLogger(VersionTest.class);

    private final String url = "http://www.google.de";
    private final SemanticVersion currentVersion = new SemanticVersion("1.2.0");
    private List<VersionUpdate> updates;

    @Mocked
    VersionUpdateLoader loader;
    @Mocked
    AppVersion appVersion;

    @BeforeMethod
    public void init() {
	updates = new ArrayList<>();
    }

    @Test(enabled = true)
    public void checkNoUpdate() {
	SemanticVersion version = currentVersion;

	VersionUpdate minor = newVersionUpdate(version);
	updates.add(newVersionUpdate(currentVersion));
	updates.add(minor);

	new Expectations() {{
	    loader.loadVersions(); result = updates;
	    AppVersion.getVersion(); result = currentVersion;
	}};

	VersionUpdateChecker result = VersionUpdateChecker.loadCurrentVersionList();

	Assert.assertTrue(result.isCurrentMaintained());
	Assert.assertNull(result.getSecurityUpgrade());
	Assert.assertNull(result.getMajorUpgrade());
	Assert.assertNull(result.getMinorUpgrade());
	Assert.assertFalse(result.needsUpdate());
    }

    @Test(enabled = true)
    public void checkUpdateMajorVersion() {
	SemanticVersion version = incrementPatch(incrementMajor(currentVersion));

	VersionUpdate major = newVersionUpdate(version);
	updates.add(newVersionUpdate(currentVersion));
	updates.add(major);

	new Expectations() {{
	    loader.loadVersions(); result = updates;
	    AppVersion.getVersion(); result = currentVersion;
	}};

	VersionUpdateChecker result = VersionUpdateChecker.loadCurrentVersionList();

	Assert.assertTrue(result.isCurrentMaintained());
	Assert.assertNull(result.getSecurityUpgrade());
	Assert.assertNull(result.getMinorUpgrade());
	Assert.assertEquals(major, result.getMajorUpgrade());
	Assert.assertTrue(result.needsUpdate());
    }

    @Test(enabled = true)
    public void checkUpdateSecurityVersion() {
	SemanticVersion version = incrementPatch(incrementPatch(currentVersion));

	VersionUpdate patch = newVersionUpdate(version);
	updates.add(newVersionUpdate(currentVersion));
	updates.add(patch);

	new Expectations() {{
	    loader.loadVersions(); result = updates;
	    AppVersion.getVersion(); result = currentVersion;
	}};

	VersionUpdateChecker result = VersionUpdateChecker.loadCurrentVersionList();

	Assert.assertTrue(result.isCurrentMaintained());
	Assert.assertTrue(result.needsUpdate());
	Assert.assertNull(result.getMinorUpgrade());
	Assert.assertNull(result.getMajorUpgrade());
	Assert.assertEquals(patch, result.getSecurityUpgrade());
    }

    @Test(enabled = true)
    public void checkUpdateMinorVersion() {
	SemanticVersion version = incrementPatch(incrementMinor(currentVersion));

	VersionUpdate minor = newVersionUpdate(version);
	updates.add(newVersionUpdate(currentVersion));
	updates.add(minor);

	new Expectations() {{
	    loader.loadVersions();
	    result = updates;
	    AppVersion.getVersion();
	    result = currentVersion;
	}};

	VersionUpdateChecker result = VersionUpdateChecker.loadCurrentVersionList();

	Assert.assertTrue(result.isCurrentMaintained());
	Assert.assertNull(result.getSecurityUpgrade());
	Assert.assertNull(result.getMajorUpgrade());
	Assert.assertEquals(minor, result.getMinorUpgrade());
	Assert.assertTrue(result.needsUpdate());
    }

    private VersionUpdate newVersionUpdate(SemanticVersion version) {
	try {
	    URL downloadUrl = new URL(url);
	    return new VersionUpdate(version, downloadUrl, downloadUrl, VersionUpdate.Status.MAINTAINED);
	} catch (MalformedURLException ex) {
	    LOG.error(ex.getMessage(), ex);
	}
	return null;
    }

    private SemanticVersion incrementMajor(SemanticVersion currentVersion) {
	return createVersion(currentVersion.getMajor()+1, currentVersion.getMinor(), currentVersion.getPatch());
    }

    private SemanticVersion incrementMinor(SemanticVersion currentVersion) {
	return createVersion(currentVersion.getMajor(), currentVersion.getMinor()+1, currentVersion.getPatch());
    }

    private SemanticVersion incrementPatch(SemanticVersion currentVersion) {
	return createVersion(currentVersion.getMajor(), currentVersion.getMinor(), currentVersion.getPatch() + 1);
    }

    private SemanticVersion createVersion(final int major, final int minor, final int patch) {
	String incremented = String.format("%d.%d.%d", major, minor, patch);
	return new SemanticVersion(incremented);
    }

}
