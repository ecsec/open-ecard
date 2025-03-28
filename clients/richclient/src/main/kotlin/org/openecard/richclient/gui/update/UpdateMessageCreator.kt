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
import org.openecard.releases.UpdateAdvice
import org.openecard.richclient.updater.VersionUpdateChecker

/**
 *
 * @author Sebastian Schuberth
 */
class UpdateMessageCreator {
    private val lang: I18n = I18n.getTranslation("update")

    fun getMessage(updateChecker: VersionUpdateChecker): String? {
		updateChecker.getUpdateInfo()?.let {
			val (data, advice) = it
			val updateStr = when (advice) {
				UpdateAdvice.UNMAINTAINED -> return lang.translationForKey("version_not_maintained", updateChecker.installedVersion, data.version.toString() + " (major update)")
				UpdateAdvice.MAINTAINED_UPDATE -> data.version.toString() + " (minor update)"
				UpdateAdvice.UPDATE -> data.version.toString() + " (minor update)"
				UpdateAdvice.SECURITY_UPDATE -> data.version.toString() + " (security update)"
				else -> null
			}

			if (updateStr != null) {
				val msg = lang.translationForKey("new_version_msg", name, updateStr)
				return msg
			}
		}

		return null
    }
}
