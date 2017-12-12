/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.openecard.common.AppVersion;
import org.openecard.common.OpenecardProperties;
import org.openecard.common.SemanticVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Update checker for the Open eCard App.
 * <p>
 * The data structure returned from the server is as follows:
 * <pre>{
 *   win: [&lt;update1&gt;, &lt;update2&gt;, ...]
 *   deb: [&lt;update1&gt;, &lt;update2&gt;, ...]
 *   rpm: [&lt;update1&gt;, &lt;update2&gt;, ...]
 *}</pre>
 * The content of the update elements is defined in {@link VersionUpdate}.
 * </p>
 * <p>The update-list location is taken from the built in property: <tt>update-list.location</tt></p>
 *
 * @author Tobias Wich
 */
public class VersionUpdateChecker {

    private static final Logger LOG = LoggerFactory.getLogger(VersionUpdateChecker.class);

    private final List<VersionUpdate> updates;
    private final SemanticVersion installedVersion;

    private VersionUpdateChecker(List<VersionUpdate> updates) {
	this.updates = updates;
	this.installedVersion = AppVersion.getVersion();
    }

    public static VersionUpdateChecker loadCurrentVersionList() {
	String pkgType = getSystemPackageType();
	try {
	    // load list data
	    URL updateUrl = getUpdateUrl();
	    URLConnection con = updateUrl.openConnection();
	    con.connect();
	    InputStream in = con.getInputStream();
	    JSONObject obj = new JSONObject(new JSONTokener(in));

	    // access package specific list
	    JSONArray updatesRaw = obj.getJSONArray(pkgType);
	    ArrayList<VersionUpdate> updates = new ArrayList<>();
	    for (int i = 0; i < updatesRaw.length(); i++) {
		try {
		    VersionUpdate next = VersionUpdate.fromJson(updatesRaw.getJSONObject(i));
		    updates.add(next);
		} catch (InvalidUpdateDefinition ex) {
		    LOG.warn("Invalid version info contained in update list.", ex);
		}
	    }

	    // make sure the versions are in the correct order
	    Collections.sort(updates);

	    return new VersionUpdateChecker(updates);
	} catch (MalformedURLException ex) {
	    LOG.error("Failed to get URL for update list.");
	} catch (IOException ex) {
	    LOG.error("Failed to retrieve update list from server.", ex);
	} catch (JSONException ex) {
	    LOG.warn("Package type {} not supported in update list.", pkgType);
	}

	LOG.info("Using no update list.");
	return new VersionUpdateChecker(Collections.EMPTY_LIST);
    }

    private static URL getUpdateUrl() throws MalformedURLException {
	String url = OpenecardProperties.getProperty("update-list.location");
	return new URL(url);
    }


    private static String getSystemPackageType() {
	if (SysUtils.isWin()) {
	    return "win";
	} else if (SysUtils.isDebianOrDerivate()) {
	    return "deb";
	} else if (SysUtils.isRedhatOrDerivate()) {
	    // not supported yet
	    //return "rpm";
	} else if (SysUtils.isSuSEOrDerivate()) {
	    // not supported yet
	    //return "rpm";
	}

	return "UNKNOWN";
    }

    public boolean needsUpdate() {
	VersionUpdate major = getMajorUpgrade();
	VersionUpdate minor = getMinorUpgrade();
	VersionUpdate sec = getSecurityUpgrade();

	return major != null || minor != null || sec != null || ! isCurrentMaintained();
    }

    public boolean isCurrentMaintained() {
	VersionUpdate cur = getCurrentVersion();
	if (cur != null) {
	    return cur.getStatus() == VersionUpdate.Status.MAINTAINED;
	}
	// version not in list means not maintained
	return false;
    }

    @Nullable
    public VersionUpdate getMajorUpgrade() {
	ArrayList<VersionUpdate> copy = new ArrayList<>(updates);

	// just compare last version as it will be the most current one
	if (! copy.isEmpty()) {
	    VersionUpdate last = copy.get(copy.size() - 1);
	    if (last.getVersion().isNewer(installedVersion)) {
		return last;
	    }
	}

	// no newer version available
	return null;
    }

    @Nullable
    public VersionUpdate getMinorUpgrade() {
	ArrayList<VersionUpdate> copy = new ArrayList<>(updates);

	// remove all versions having a different major version
	Iterator<VersionUpdate> i = copy.iterator();
	while (i.hasNext()) {
	    VersionUpdate next = i.next();
	    if (installedVersion.getMajor() != next.getVersion().getMajor()) {
		i.remove();
	    }
	}

	// just compare last version as it will be the most current one
	if (! copy.isEmpty()) {
	    VersionUpdate last = copy.get(copy.size() - 1);
	    if (last.getVersion().isNewer(installedVersion)) {
		return last;
	    }
	}

	// no newer version available
	return null;
    }

    @Nullable
    public VersionUpdate getSecurityUpgrade() {
	ArrayList<VersionUpdate> copy = new ArrayList<>(updates);

	// remove all versions having a different major and minor version
	Iterator<VersionUpdate> i = copy.iterator();
	while (i.hasNext()) {
	    VersionUpdate next = i.next();
	    if (installedVersion.getMajor() != next.getVersion().getMajor()) {
		i.remove();
	    } else if (installedVersion.getMinor() != next.getVersion().getMinor()) {
		i.remove();
	    }
	}

	// just compare last version as it will be the most current one
	if (! copy.isEmpty()) {
	    VersionUpdate last = copy.get(copy.size() - 1);
	    if (last.getVersion().isNewer(installedVersion)) {
		return last;
	    }
	}

	// no newer version available
	return null;
    }

    @Nullable
    public VersionUpdate getCurrentVersion() {
	for (VersionUpdate next : updates) {
	    if (installedVersion.isSame(next.getVersion())) {
		return next;
	    }
	}

	return null;
    }

}
