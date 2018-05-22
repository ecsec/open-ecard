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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.openecard.common.OpenecardProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class loading the updates file.
 *
 * @author Tobias Wich
 * @author Sebastian Schuberth
 */
public class VersionUpdateLoader {

    private static final Logger LOG = LoggerFactory.getLogger(VersionUpdateLoader.class);
    private final URL updateUrl;
    private final String pkgType;

    VersionUpdateLoader(URL updateUrl, String systemPackageType) {
	this.updateUrl = updateUrl;
	this.pkgType = systemPackageType;
    }

    public static VersionUpdateLoader createWithDefaults() throws IllegalArgumentException {	
	try {
	    return new VersionUpdateLoader(getUpdateUrl(), getPkgType());
	} catch (MalformedURLException ex) {
	    String msg = "Update URL value is not a valid URL.";
	    LOG.error(msg, ex);
	    throw new IllegalArgumentException(msg, ex);
	}
    }

    public VersionUpdateList loadVersionUpdateList() throws IllegalArgumentException {	
	try {	 
	    // load list data
	    LOG.info("Trying to load version list.");
	    URLConnection con = updateUrl.openConnection();
	    con.connect();
	    InputStream in = con.getInputStream();
	    JSONObject obj = new JSONObject(new JSONTokener(in));

	    // get package specific download page
	    String dowloadPageString = obj.getString(pkgType+"_download_page");

	    // access package specific list
	    JSONArray updatesRaw = obj.getJSONArray(pkgType);

	    ArrayList<VersionUpdate> updates = new ArrayList<>();

	    for (int i = 0; i < updatesRaw.length(); i++) {
		try {
		    VersionUpdate next = VersionUpdate.fromJson(updatesRaw.getJSONObject(i));
		    updates.add(next);
		} catch (InvalidUpdateDefinition ex) {
		    LOG.warn("Invalid version info contained in update list.", ex);
		    throw new IllegalArgumentException("Invalid version info contained in update list.", ex);
		}
	    }

	    // make sure the versions are in the correct order
	    Collections.sort(updates);

	    VersionUpdateList list =  new VersionUpdateList(updates, new URL(dowloadPageString));
	    LOG.info("Successfully got versionupdatelist!");
	    return list;
	} catch (MalformedURLException ex) {
	    LOG.error("Failed to get URL for update list.");
	    throw new IllegalArgumentException("Failed to get URL for update list.", ex);
	} catch (IOException ex) {
	    LOG.error("Failed to retrieve update list from server.", ex);
	    throw new IllegalArgumentException("Failed to retrieve update list from server.", ex);
	} catch (JSONException ex) {
	    LOG.warn("Package type {} not supported in update list.", pkgType);
	    throw new IllegalArgumentException("Package type "+pkgType+" not supported in update list.", ex);
	}
    }

    private static URL getUpdateUrl() throws MalformedURLException {
	String url = OpenecardProperties.getProperty("update-list.location");
	return new URL(url);
    }

    private static String getPkgType() {
	if (SysUtils.isWin() && SysUtils.is64bit()) {
	    return "win64";
	} else if (SysUtils.isWin()) {
	    return "win32";
	} else if (SysUtils.isDebianOrDerivate()) {
	    return "deb";
	} else if (SysUtils.isRedhatOrDerivate()) {
	    return "rpm";
	} else if (SysUtils.isSuSEOrDerivate()) {
	    return "rpm";
	}

	return "UNKNOWN";
    }

}
