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

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.openecard.common.AppVersion;
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
 * @author Sebastian Schuberth
 */
public class VersionUpdateChecker {

    private static final Logger LOG = LoggerFactory.getLogger(VersionUpdateChecker.class);

    private final List<VersionUpdate> updates;
    private final SemanticVersion installedVersion;
    private final URL downloadPage;

    VersionUpdateChecker(SemanticVersion installedVersion, VersionUpdateList list) {
	this.updates = list.getVersionUpdates();
	this.installedVersion = installedVersion;
	this.downloadPage = list.getDownloadPage();
    }

    public static VersionUpdateChecker loadCurrentVersionList() {
	VersionUpdateLoader loader = VersionUpdateLoader.createWithDefaults();
	VersionUpdateList list = loader.loadVersionUpdateList();
	return new VersionUpdateChecker(AppVersion.getVersion(), list);
    }

    public URL getDownloadPage() {
	return this.downloadPage;
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

	Iterator<VersionUpdate> i = copy.iterator();
	while (i.hasNext()) {
	    VersionUpdate next = i.next();
	    if (installedVersion.getMajor() >= next.getVersion().getMajor()) {
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
    public VersionUpdate getMinorUpgrade() {
	ArrayList<VersionUpdate> copy = new ArrayList<>(updates);

	// remove all versions having a different major and smaller minor version
	Iterator<VersionUpdate> i = copy.iterator();
	while (i.hasNext()) {
	    VersionUpdate next = i.next();
	    if (installedVersion.getMajor() != next.getVersion().getMajor()) {
		i.remove();
	    } else if (installedVersion.getMinor() >= next.getVersion().getMinor()) {
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
