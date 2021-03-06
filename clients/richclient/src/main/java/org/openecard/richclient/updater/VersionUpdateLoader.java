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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jose4j.json.internal.json_simple.JSONArray;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.json.internal.json_simple.parser.JSONParser;
import org.jose4j.json.internal.json_simple.parser.ParseException;
import org.openecard.common.OpenecardProperties;
import org.openecard.common.util.InvalidUpdateDefinition;
import org.openecard.common.util.SysUtils;
import org.openecard.crypto.tls.proxy.ProxySettings;
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
	    // load proxy if one is available
	    ProxySettings.getDefault(); // make sure it is initialized
	    List<Proxy> proxies = ProxySelector.getDefault().select(updateUrl.toURI());
	    Proxy p = Proxy.NO_PROXY;
	    for (Proxy next : proxies) {
		if (next.type() != Proxy.Type.DIRECT) {
		    LOG.debug("Found a proxy for the update connection.");
		    p = next;
		    break;
		}
	    }

	    LOG.info("Trying to load version list.");
	    URLConnection con = updateUrl.openConnection(p);
	    con.connect();
	    InputStream in = con.getInputStream();
	    Reader r = new InputStreamReader(in, StandardCharsets.UTF_8);

	    JSONObject rootObj = (JSONObject) new JSONParser().parse(r);

	    // get package specific download page
	    String downloadPageString = (String) rootObj.get(pkgType + "_download_page");

	    // access package specific list
	    JSONArray updatesRaw = (JSONArray) rootObj.get(pkgType);

	    ArrayList<VersionUpdate> updates = new ArrayList<>();

	    for (Object ur : updatesRaw) {
		try {
		    VersionUpdate next = VersionUpdate.fromJson((JSONObject) ur);
		    updates.add(next);
		} catch (InvalidUpdateDefinition ex) {
		    LOG.warn("Invalid version info contained in update list.", ex);
		    throw new IllegalArgumentException("Invalid version info contained in update list.", ex);
		}
	    }

	    // make sure the versions are in the correct order
	    Collections.sort(updates);

	    VersionUpdateList list =  new VersionUpdateList(updates, new URL(downloadPageString));
	    LOG.info("Successfully retrieved version update list.");
	    return list;
	} catch (IOException ex) {
	    LOG.error("Failed to retrieve update list from server.", ex);
	    throw new IllegalArgumentException("Failed to retrieve update list from server.", ex);
	} catch (NullPointerException ex) {
	    LOG.warn("Package type {} not supported in update list.", pkgType);
	    throw new IllegalArgumentException("Package type " + pkgType + " not supported in update list.", ex);
	} catch (URISyntaxException ex) {
	    String msg = "Failed to convert Update URL to a URI.";
	    LOG.error(msg, ex);
	    throw new IllegalArgumentException(msg, ex);
	} catch (ParseException ex) {
	    String msg = "Failed to deserialize JSON data.";
	    LOG.error(msg, ex);
	    throw new IllegalArgumentException(msg, ex);
	}
    }

    static URL getUpdateUrl() throws MalformedURLException {
	String url = OpenecardProperties.getProperty("update-list.location");
	return new URL(url);
    }

    static String getPkgType() {
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
	} else if (SysUtils.isMacOSX()) {
	    return "osx";
	}

	return "UNKNOWN";
    }

}
