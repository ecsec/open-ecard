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

package org.openecard.richclient.updater;

import org.openecard.richclient.updater.VersionUpdate;
import org.openecard.richclient.updater.VersionUpdateList;
import org.openecard.richclient.updater.VersionUpdateChecker;
import org.openecard.richclient.updater.VersionUpdateLoader;
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
import org.testng.annotations.Test;


/**
 *
 * @author Sebastian Schuberth
 */
public class TestMockedVersionUpdate {

    private static final Logger LOG = LoggerFactory.getLogger(TestMockedVersionUpdate.class);

    private final String url = "http://www.google.de";
    private final SemanticVersion currentVersion = new SemanticVersion("1.2.0");

    @Mocked
    AppVersion appVersion;
    @Mocked
    VersionUpdateLoader loader;

    @Test(enabled = true)
    public void testNoUpdateAvailable() throws MalformedURLException {
	expectUpdateInVersions(loader);

	VersionUpdateChecker result = VersionUpdateChecker.loadCurrentVersionList();

	Assert.assertTrue(result.isCurrentMaintained());
	Assert.assertNull(result.getSecurityUpgrade());
	Assert.assertNull(result.getMajorUpgrade());
	Assert.assertNull(result.getMinorUpgrade());
	Assert.assertFalse(result.needsUpdate());
    }

    @Test(enabled = true)
    public void testUpdateMajorVersionAvailable() throws MalformedURLException {
	VersionUpdate nextMajorUpdate = newVersionUpdate(incrementPatch(incrementMajor(currentVersion)));

	expectUpdateInVersions(loader, nextMajorUpdate);

	VersionUpdateChecker result = VersionUpdateChecker.loadCurrentVersionList();

	Assert.assertTrue(result.isCurrentMaintained());
	Assert.assertNull(result.getSecurityUpgrade());
	Assert.assertNull(result.getMinorUpgrade());
	Assert.assertEquals(nextMajorUpdate, result.getMajorUpgrade());
	Assert.assertTrue(result.needsUpdate());
    }

    @Test(enabled = true)
    public void testUpdateSecurityVersionAvailable() throws MalformedURLException {
	VersionUpdate nextPatchUpdate = newVersionUpdate(incrementPatch(incrementPatch(currentVersion)));

	expectUpdateInVersions(loader, nextPatchUpdate);

	VersionUpdateChecker result = VersionUpdateChecker.loadCurrentVersionList();

	Assert.assertTrue(result.isCurrentMaintained());
	Assert.assertTrue(result.needsUpdate());
	Assert.assertNull(result.getMinorUpgrade());
	Assert.assertNull(result.getMajorUpgrade());
	Assert.assertEquals(nextPatchUpdate, result.getSecurityUpgrade());
    }

    @Test(enabled = true)
    public void testUpdateMinorVersionAvailable() throws MalformedURLException {
	VersionUpdate nextMinorUpdate = newVersionUpdate(incrementPatch(incrementMinor(currentVersion)));

	expectUpdateInVersions(loader, nextMinorUpdate);

	VersionUpdateChecker result = VersionUpdateChecker.loadCurrentVersionList();

	Assert.assertTrue(result.isCurrentMaintained());
	Assert.assertNull(result.getSecurityUpgrade());
	Assert.assertNull(result.getMajorUpgrade());
	Assert.assertEquals(nextMinorUpdate, result.getMinorUpgrade());
	Assert.assertTrue(result.needsUpdate());
    }
    private void expectUpdateInVersions(final VersionUpdateLoader loader) {
	final VersionUpdateList updateList = createInput();

	expectUpdateInVersions(loader, updateList);
    }

    private void expectUpdateInVersions(final VersionUpdateLoader loader, VersionUpdate nextMajorUpdate) {
	final VersionUpdateList updateList = createInput(nextMajorUpdate);

	expectUpdateInVersions(loader, updateList);
    }

    private void expectUpdateInVersions(final VersionUpdateLoader loader, final VersionUpdateList updateList) {
	new Expectations() {
	    {
		VersionUpdateLoader.createWithDefaults();
		result = loader;
		loader.loadVersionUpdateList();
		result = updateList;
		AppVersion.getVersion();
		result = currentVersion;
	    }
	};
    }

    private SemanticVersion incrementMajor(SemanticVersion currentVersion) {
	return createVersion(currentVersion.getMajor() + 1, currentVersion.getMinor(), currentVersion.getPatch());
    }

    private SemanticVersion incrementMinor(SemanticVersion currentVersion) {
	return createVersion(currentVersion.getMajor(), currentVersion.getMinor() + 1, currentVersion.getPatch());
    }

    private SemanticVersion incrementPatch(SemanticVersion currentVersion) {
	return createVersion(currentVersion.getMajor(), currentVersion.getMinor(), currentVersion.getPatch() + 1);
    }

    private SemanticVersion createVersion(final int major, final int minor, final int patch) {
	String incremented = String.format("%d.%d.%d", major, minor, patch);
	return new SemanticVersion(incremented);
    }


    private VersionUpdateList createInput() {
	return createInput(newVersionUpdate(currentVersion));
    }

    private VersionUpdateList createInput(VersionUpdate update) {

	List<VersionUpdate> updates = new ArrayList<>();
	updates.add(newVersionUpdate(currentVersion));
	updates.add(update);

	URL downloadPage;
	try {
	    downloadPage = new URL(url + "/downloadpage");
	} catch (MalformedURLException ex) {
	    throw new IllegalArgumentException("Wrong url", ex);
	}
	final VersionUpdateList updateList = new VersionUpdateList(updates, downloadPage);
	return updateList;
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


}
