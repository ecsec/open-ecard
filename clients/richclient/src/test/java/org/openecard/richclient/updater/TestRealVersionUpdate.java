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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import mockit.Expectations;
import mockit.Mocked;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.openecard.common.AppVersion;
import org.openecard.common.OpenecardProperties;
import org.openecard.common.SemanticVersion;
import org.openecard.common.util.InvalidUpdateDefinition;
import org.openecard.richclient.updater.VersionUpdate.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author Sebastian Schuberth
 */
public class TestRealVersionUpdate {

    private static final Logger LOG = LoggerFactory.getLogger(TestRealVersionUpdate.class);

    private final String url = "http://www.google.de";
    private final SemanticVersion currentVersion = new SemanticVersion("1.2.0");

    @Mocked
    AppVersion appVersion;
    @Mocked
    OpenecardProperties props;

    @Test(enabled = true)
    public void foo() {
	Assert.assertTrue(true);
    }

    @Test(enabled = true)
    public void validDefaultCreationOfVersionUpdateLoader() throws MalformedURLException {
	final String updateListUrl = url;
	final String sysPkg = "deb";

	new Expectations(VersionUpdateLoader.class) {
	    {
		OpenecardProperties.getProperty("update-list.location");
		result = updateListUrl;
		VersionUpdateLoader.getPkgType();
		result = sysPkg;
	    }
	};

	VersionUpdateLoader result = VersionUpdateLoader.createWithDefaults();
	Assert.assertNotNull(result);
    }

    @Test(enabled = false)
    public void defaultCreationOfVersionUpdateLoaderWithBadURL() throws MalformedURLException {
	final String updateListUrl = "test";

	new Expectations(VersionUpdateLoader.class) {
	    {
		OpenecardProperties.getProperty("update-list.location");
		result = updateListUrl;
	    }
	};

	try {
	    VersionUpdateLoader result = VersionUpdateLoader.createWithDefaults();
	    Assert.fail();
	} catch (IllegalArgumentException ex) {
	    Assert.assertEquals(ex.getMessage(), "Update URL value is not a valid URL.");
	}
    }

    @Test(enabled = true)
    public void testEmptyUpdateList() throws MalformedURLException {
	VersionUpdateList updateList = createEmptyInput();

	VersionUpdateChecker result = new VersionUpdateChecker(currentVersion, updateList);

	Assert.assertNull(result.getCurrentVersion());
	Assert.assertFalse(result.isCurrentMaintained());
	Assert.assertNull(result.getMajorUpgrade());
	Assert.assertNull(result.getSecurityUpgrade());
	Assert.assertNull(result.getMinorUpgrade());
	Assert.assertEquals(result.getDownloadPage(), new URL(url + "/downloadpage"));
    }

    @Test(enabled = true)
    public void loadValidUpdateList() throws MalformedURLException, IOException {
	URL downloadListAddress = TestRealVersionUpdate.class.getResource("/updatelist.json");
	String systemPkg = "deb";
	URL expectedDownloadPage = new URL("https://www.openecard.org/downloads_" + systemPkg);
	VersionUpdateLoader updateLoader = new VersionUpdateLoader(downloadListAddress, systemPkg);

	VersionUpdateList result = updateLoader.loadVersionUpdateList();
	Assert.assertFalse(result.getVersionUpdates().isEmpty());
	Assert.assertEquals(result.getDownloadPage(), expectedDownloadPage);
    }

    @Test(enabled = true)
    public void loadUpdateListForNonExistingSystemPkg() throws MalformedURLException, IOException {
	URL downloadListAddress = TestRealVersionUpdate.class.getResource("/updatelist.json");
	String systemPkg = "nonexisting";

	VersionUpdateLoader updateLoader = new VersionUpdateLoader(downloadListAddress, systemPkg);
	try {
	    VersionUpdateList result = updateLoader.loadVersionUpdateList();
	    Assert.fail(); // Exception expected
	} catch (IllegalArgumentException ex) {
	    Assert.assertEquals(ex.getMessage(), "Package type " + systemPkg + " not supported in update list.");
	}
    }

    @Test(enabled = true)
    public void loadUpdateListWithInvalidVersion() throws MalformedURLException, IOException {
	URL downloadListAddress = TestRealVersionUpdate.class.getResource("/invalidupdatelist.json");
	String systemPkg = "deb";

	VersionUpdateLoader updateLoader = new VersionUpdateLoader(downloadListAddress, systemPkg);
	try {
	    VersionUpdateList result = updateLoader.loadVersionUpdateList();
	    Assert.fail(); // Exception expected
	} catch (IllegalArgumentException ex) {
	    Assert.assertEquals(ex.getMessage(), "Invalid version info contained in update list.");
	}
    }

    @Test(enabled = true)
    public void loadUpdateListFromNonExistingURL() throws MalformedURLException, IOException {
	URL downloadListAddress = new URL("http://thisisaboguspage.com");
	String systemPkg = "deb";

	VersionUpdateLoader updateLoader = new VersionUpdateLoader(downloadListAddress, systemPkg);
	try {
	    VersionUpdateList result = updateLoader.loadVersionUpdateList();
	    Assert.fail(); // Exception expected
	} catch (IllegalArgumentException ex) {
	    // fine, end the test
	}
    }

    @Test(enabled = true)
    public void validVersionUpdate() throws InvalidUpdateDefinition, MalformedURLException {
	// Same values as the default values of the JSONObjectBuilder
	String downloadPage = "http://www.google.de/downloadpage";
	String downloadUrl = "http://www.google.de/downloadurl";
	String version = "1.3.0";
	String status = Status.MAINTAINED.name();

	JSONObject obj = new JSONObjectBuilder().build();
	VersionUpdate update = VersionUpdate.fromJson(obj);

	Assert.assertEquals(update.getDownloadPage(), new URL(downloadPage));
	Assert.assertEquals(update.getDownloadLink(), new URL(downloadUrl));
	Assert.assertEquals(update.getVersion().compareTo(new SemanticVersion(version)), 0);
	Assert.assertEquals(update.getStatus(), Status.valueOf(status));

	VersionUpdate newerVersion = new VersionUpdate(new SemanticVersion("1.3.1"), new URL(downloadPage), new URL(downloadUrl), Status.MAINTAINED);
	Assert.assertEquals(update.compareTo(newerVersion), -1);
	Assert.assertEquals(newerVersion.compareTo(update), 1);
    }

    @Test(enabled = true)
    public void versionUpdateUnknownStatus() throws InvalidUpdateDefinition {
	JSONObject obj = new JSONObjectBuilder().status("test").build();
	VersionUpdate update = VersionUpdate.fromJson(obj);
	Assert.assertEquals(update.getStatus(), Status.UNKNOWN);
    }

    @Test(enabled = true)
    public void invalidJSONObject() {
	try {
	    JSONObject obj = new JSONObject();
	    VersionUpdate update = VersionUpdate.fromJson(obj);
	    Assert.fail(); // Exception expected
	} catch (InvalidUpdateDefinition ex) {
	    Assert.assertEquals(ex.getMessage(), "Incomplete JSON data received.");
	}
    }

    @Test(enabled = true)
    public void versionUpdateinvalidSemanticVersionSpecified() {
	try {
	    String invalidVersion = "0.0.0";
	    JSONObject obj = new JSONObjectBuilder().version(invalidVersion).build();
	    VersionUpdate update = VersionUpdate.fromJson(obj);
	    Assert.fail(); // Exception expected
	} catch (InvalidUpdateDefinition ex) {
	    Assert.assertEquals(ex.getMessage(), "Invalid version specified.");
	}
    }

    @Test(enabled = true)
    public void versionUpdateinvalidDownloadURL() {
	try {
	    String invalidDownloadURL = "test";
	    JSONObject obj = new JSONObjectBuilder().downloadUrl(invalidDownloadURL).build();
	    VersionUpdate update = VersionUpdate.fromJson(obj);
	    Assert.fail(); // Exception expected
	} catch (InvalidUpdateDefinition ex) {
	    Assert.assertEquals(ex.getMessage(), "Incomplete JSON data received.");
	}
    }

    @Test(enabled = true)
    public void versionUpdateDownloadPageNotHTTP() {
	try {
	    String invalidDownloadPage = "file://test";
	    JSONObject obj = new JSONObjectBuilder().downloadPage(invalidDownloadPage).build();
	    VersionUpdate update = VersionUpdate.fromJson(obj);
	    Assert.fail(); // Exception expected
	} catch (InvalidUpdateDefinition ex) {
	    Assert.assertEquals(ex.getMessage(), "Download Page URL is not an http URL.");
	}
    }

    @Test(enabled = true)
    public void versionUpdateDownloadUrlNotHTTP() {
	try {
	    String invalidDownloadURL = "file://test";
	    JSONObject obj = new JSONObjectBuilder().downloadUrl(invalidDownloadURL).build();
	    VersionUpdate update = VersionUpdate.fromJson(obj);
	    Assert.fail(); // Exception expected
	} catch (InvalidUpdateDefinition ex) {
	    Assert.assertEquals(ex.getMessage(), "Download URL is not an http URL.");
	}
    }

    private VersionUpdateList createEmptyInput() {
	List<VersionUpdate> updates = new ArrayList<>();

	URL downloadPage;
	try {
	    downloadPage = new URL(url + "/downloadpage");
	} catch (MalformedURLException ex) {
	    throw new IllegalArgumentException("Wrong url", ex);
	}
	final VersionUpdateList updateList = new VersionUpdateList(updates, downloadPage);
	return updateList;
    }


    public static class JSONObjectBuilder {

	private String version;
	private String downloadPage;
	private String downloadUrl;
	private String status;

	public JSONObjectBuilder() {

	    this.version = "1.3.0";
	    this.downloadPage = "http://www.google.de/downloadpage";
	    this.downloadUrl = "http://www.google.de/downloadurl";
	    this.status = Status.MAINTAINED.name();
	}

	public JSONObjectBuilder version(String version) {
	    this.version = version;
	    return this;
	}

	public JSONObjectBuilder downloadPage(String downloadPage) {
	    this.downloadPage = downloadPage;
	    return this;
	}

	public JSONObjectBuilder downloadUrl(String downloadUrl) {
	    this.downloadUrl = downloadUrl;
	    return this;
	}

	public JSONObjectBuilder status(String status) {
	    this.status = status;
	    return this;
	}

	public JSONObject build() {
	    JSONObject obj = new JSONObject();
	    obj.put("version", version);
	    obj.put("download_page", downloadPage);
	    obj.put("download_url", downloadUrl);
	    obj.put("status", status);
	    return obj;
	}
    }

}
