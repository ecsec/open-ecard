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

import dev.icerock.moko.resources.format
import org.openecard.common.AppVersion.name
import org.openecard.i18n.I18N
import org.openecard.releases.UpdateAdvice
import org.openecard.richclient.updater.VersionUpdateChecker

/**
 *
 * @author Sebastian Schuberth
 */
class UpdateMessageCreator {
	fun getMessage(updateChecker: VersionUpdateChecker): String? {
		updateChecker.getUpdateInfo()?.let {
			val (data, advice) = it
			val updateStr =
				when (advice) {
					UpdateAdvice.UNMAINTAINED ->
						return I18N.strings.update_version_not_maintained
							.format(
								updateChecker.installedVersion,
								data.version.toString() + " (major update)",
							).localized()
					UpdateAdvice.MAINTAINED_UPDATE -> data.version.toString() + " (minor update)"
					UpdateAdvice.UPDATE -> data.version.toString() + " (minor update)"
					UpdateAdvice.SECURITY_UPDATE -> data.version.toString() + " (security update)"
					else -> null
				}

			if (updateStr != null) {
				return I18N.strings.update_new_version_msg
					.format(name, updateStr)
					.localized()
			}
		}

		return null
	}
}
