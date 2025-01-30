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

package org.openecard.richclient.gui.update

import org.openecard.common.AppVersion.name
import org.openecard.common.I18n
import org.openecard.richclient.updater.VersionUpdate
import org.openecard.richclient.updater.VersionUpdateChecker

/**
 *
 * @author Sebastian Schuberth
 */
class UpdateMessageCreator {
    private val lang: I18n = I18n.getTranslation("update")

    fun getMessage(updateChecker: VersionUpdateChecker): String {
        val currentVersion: VersionUpdate? = updateChecker.currentVersion

        val majUpdate: VersionUpdate? = updateChecker.majorUpgrade
        val updateVersions: ArrayList<String> = ArrayList()

        if (majUpdate != null) {
            val maj: String = majUpdate.version.toString() + " (major update)"
            updateVersions.add(maj)
        }
        val minUpdate: VersionUpdate? = updateChecker.minorUpgrade

        if (minUpdate != null) {
            val min: String = minUpdate.version.toString() + " (minor update)"
            updateVersions.add(min)
        }
        val secUpdate: VersionUpdate? = updateChecker.securityUpgrade

        if (secUpdate != null) {
            val sec: String = secUpdate.version.toString() + " (security update)"
            updateVersions.add(sec)
        }

        val numberOfVersions: Int = updateVersions.size

        var msg: String = ""
        if (numberOfVersions == 1) {
            msg = lang.translationForKey("new_version_msg", name, updateVersions.get(0))
        } else if (numberOfVersions > 1) {
            val sb: StringBuilder = StringBuilder()

            for (i in 0 until numberOfVersions) {
                sb.append(updateVersions[i])
                if (i < numberOfVersions - 1) {
                    sb.append(", ")
                }
            }

            msg = lang.translationForKey("new_versions_msg", name, sb.toString())
        }

        if (!updateChecker.isCurrentMaintained) {
			if (currentVersion != null) {
				msg = lang.translationForKey(
					"version_not_maintained",
					currentVersion.version.toString(),
					updateVersions[0]
				)
			}
        }

        return msg
    }
}
