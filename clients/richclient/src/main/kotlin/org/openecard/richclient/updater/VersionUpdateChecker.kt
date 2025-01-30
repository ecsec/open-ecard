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

import org.openecard.common.AppVersion.version
import org.openecard.common.SemanticVersion
import java.net.URL

/**
 * Update checker for the Open eCard App.
 *
 * The data structure returned from the server is as follows:
 * <pre>{
 * win: [&lt;update1&gt;, &lt;update2&gt;, ...]
 * deb: [&lt;update1&gt;, &lt;update2&gt;, ...]
 * rpm: [&lt;update1&gt;, &lt;update2&gt;, ...]
 * }</pre>
 *
 * The content of the update elements is defined in [VersionUpdate].<br></br>
 * The update-list location is taken from the built in property: `update-list.location`
 *
 * @author Tobias Wich
 * @author Sebastian Schuberth
 */
class VersionUpdateChecker internal constructor(
    private val installedVersion: SemanticVersion,
    list: VersionUpdateList
) {
    private val updates: List<VersionUpdate> = list.versionUpdates
    val downloadPage: URL = list.downloadPage

    fun needsUpdate(): Boolean {
        val major = majorUpgrade
        val minor = minorUpgrade
        val sec = securityUpgrade

        // don't check for maintained version as this would trigger an update without any versions to update to
        return major != null || minor != null || sec != null /*|| ! isCurrentMaintained()*/
    }

    val isCurrentMaintained: Boolean
        get() {
            val cur = currentVersion
            if (cur != null) {
                return cur.status == VersionUpdate.Status.MAINTAINED
            }
            // version not in list means not maintained
            return false
        }

    val majorUpgrade: VersionUpdate?
        get() {
            val copy = ArrayList(updates)

            val i = copy.iterator()
            while (i.hasNext()) {
                val next = i.next()
                if (installedVersion.major >= next.version.major) {
                    i.remove()
                }
            }

            // just compare last version as it will be the most current one
            if (!copy.isEmpty()) {
                val last = copy[copy.size - 1]
                if (last.version.isNewer(installedVersion)) {
                    return last
                }
            }

            // no newer version available
            return null
        }

    val minorUpgrade: VersionUpdate?
        get() {
            val copy = ArrayList(updates)

            // remove all versions having a different major and smaller minor version
            val i = copy.iterator()
            while (i.hasNext()) {
                val next = i.next()
                if (installedVersion.major != next.version.major) {
                    i.remove()
                } else if (installedVersion.minor >= next.version.minor) {
                    i.remove()
                }
            }

            // just compare last version as it will be the most current one
            if (copy.isNotEmpty()) {
                val last = copy[copy.size - 1]
                if (last.version.isNewer(installedVersion)) {
                    return last
                }
            }

            // no newer version available
            return null
        }

    val securityUpgrade: VersionUpdate?
        get() {
            val copy = ArrayList(updates)

            // remove all versions having a different major and minor version
            val i = copy.iterator()
            while (i.hasNext()) {
                val next = i.next()
                if (installedVersion.major != next.version.major) {
                    i.remove()
                } else if (installedVersion.minor != next.version.minor) {
                    i.remove()
                }
            }

            // just compare last version as it will be the most current one
            if (!copy.isEmpty()) {
                val last = copy[copy.size - 1]
                if (last.version.isNewer(installedVersion)) {
                    return last
                }
            }

            // no newer version available
            return null
        }

    val currentVersion: VersionUpdate?
        get() {
            for (next in updates) {
                if (installedVersion.isSame(next.version)) {
                    return next
                }
            }

            return null
        }

    companion object {
        fun loadCurrentVersionList(): VersionUpdateChecker {
            val loader: VersionUpdateLoader = VersionUpdateLoader.Companion.createWithDefaults()
            val list = loader.loadVersionUpdateList()
            return VersionUpdateChecker(version, list)
        }
    }
}
