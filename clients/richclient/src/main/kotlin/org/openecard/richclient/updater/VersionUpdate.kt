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

package org.openecard.richclient.updater

import org.jose4j.json.internal.json_simple.JSONObject
import org.openecard.common.SemanticVersion
import org.openecard.common.util.InvalidUpdateDefinition
import java.net.MalformedURLException
import java.net.URL

/**
 * Data class representing an update version.
 *
 * @author Tobias Wich
 */
class VersionUpdate(val version: SemanticVersion, val downloadPage: URL, val downloadLink: URL, val status: Status) :
    Comparable<VersionUpdate> {
    enum class Status {
        MAINTAINED,
        END_OF_LIFE,
        UNKNOWN
    }

    override fun compareTo(o: VersionUpdate): Int {
        return this.version.compareTo(o.version)
    }

    override fun toString(): String {
        return String.format(
            "{version: '%s',%n dl_page: '%s',%n dl_link: '%s',%n status: '%s'}",
            this.version, downloadPage, downloadLink, status
        )
    }

    companion object {
        @Throws(InvalidUpdateDefinition::class)
        fun fromJson(jsonObject: JSONObject): VersionUpdate {
            try {
                val version = SemanticVersion(jsonObject["version"] as String?)
                val dlPage = URL(jsonObject["download_page"] as String?)
                val dlUrl = URL(jsonObject["download_url"] as String?)
                var status = try {
                    Status.valueOf((jsonObject["status"] as String?)!!)
                } catch (ex: IllegalArgumentException) {
                    Status.UNKNOWN
                }

                if (version.major == 0 && version.minor == 0 && version.patch == 0) {
                    throw InvalidUpdateDefinition("Invalid version specified.")
                }
                if (!"http".equals(dlPage.protocol, ignoreCase = true) && !"https".equals(
                        dlPage.protocol,
                        ignoreCase = true
                    )
                ) {
                    throw InvalidUpdateDefinition("Download Page URL is not an http URL.")
                }
                if (!"http".equals(dlUrl.protocol, ignoreCase = true) && !"https".equals(
                        dlUrl.protocol,
                        ignoreCase = true
                    )
                ) {
                    throw InvalidUpdateDefinition("Download URL is not an http URL.")
                }

                return VersionUpdate(version, dlPage, dlUrl, status)
            } catch (ex: MalformedURLException) {
                throw InvalidUpdateDefinition("Incomplete JSON data received.", ex)
            } catch (ex: NullPointerException) {
                throw InvalidUpdateDefinition("Incomplete JSON data received.", ex)
            }
        }
    }
}
