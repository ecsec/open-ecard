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

package org.openecard.richclient.updater;

import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import org.openecard.common.SemanticVersion;
import org.openecard.common.util.InvalidUpdateDefinition;


/**
 * Data class representing an update version.
 *
 * @author Tobias Wich
 */
public class VersionUpdate implements Comparable<VersionUpdate> {

    public static enum Status {
	MAINTAINED,
	END_OF_LIFE,
	UNKNOWN;
    }

    private final SemanticVersion version;
    private final URL downloadPage;
    private final URL downloadUrl;
    private final Status status;

    public VersionUpdate(SemanticVersion version, URL downloadPage, URL downloadUrl, Status status) {
	this.version = version;
	this.downloadPage = downloadPage;
	this.downloadUrl = downloadUrl;
	this.status = status;
    }

    public static VersionUpdate fromJson(JSONObject jsonObject) throws InvalidUpdateDefinition {
	try {
	    SemanticVersion version = new SemanticVersion(jsonObject.getString("version"));
	    URL dlPage = new URL(jsonObject.getString("download_page"));
	    URL dlUrl = new URL(jsonObject.getString("download_url"));
	    Status status;
	    try {
		status = Status.valueOf(jsonObject.getString("status"));
	    } catch (IllegalArgumentException ex) {
		status = Status.UNKNOWN;
	    }

	    if (version.getMajor() == 0 && version.getMinor() == 0 && version.getPatch() == 0) {
		throw new InvalidUpdateDefinition("Invalid version specified.");
	    }
	    if (! "http".equalsIgnoreCase(dlPage.getProtocol()) && ! "https".equalsIgnoreCase(dlPage.getProtocol())) {
		throw new InvalidUpdateDefinition("Download Page URL is not an http URL.");
	    }
	    if (! "http".equalsIgnoreCase(dlUrl.getProtocol()) && ! "https".equalsIgnoreCase(dlUrl.getProtocol())) {
		throw new InvalidUpdateDefinition("Download URL is not an http URL.");
	    }

	    return new VersionUpdate(version, dlPage, dlUrl, status);
	} catch (MalformedURLException ex) {
	    throw new InvalidUpdateDefinition("At least one of the download URLs is not a valid URL.", ex);
	} catch (JSONException ex) {
	    throw new InvalidUpdateDefinition("Incomplete JSON data received.", ex);
	}
    }

    public SemanticVersion getVersion() {
	return version;
    }

    public URL getDownloadPage() {
	return downloadPage;
    }

    public URL getDownloadLink() {
	return downloadUrl;
    }

    public Status getStatus() {
	return status;
    }

    @Override
    public int compareTo(VersionUpdate o) {
	return this.getVersion().compareTo(o.getVersion());
    }

    @Override
    public String toString() {
	return String.format("{version: '%s',%n dl_page: '%s',%n dl_link: '%s',%n status: '%s'}",
		getVersion(), getDownloadPage(), getDownloadLink(), getStatus());
    }

}
