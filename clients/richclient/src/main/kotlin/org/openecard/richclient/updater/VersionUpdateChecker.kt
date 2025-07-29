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

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.z4kn4fein.semver.Version
import org.openecard.build.BuildInfo
import org.openecard.releases.ArtifactType
import org.openecard.releases.ReleaseInfo
import org.openecard.releases.UpdateAdvice
import org.openecard.releases.VersionData
import org.openecard.releases.checkVersion
import org.openecard.releases.getUpdateData

private val log = KotlinLogging.logger {}

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
 * The content of the update elements is defined in [VersionUpdateLoader].<br></br>
 * The update-list location is taken from the built-in property: `release-info.location`
 *
 * @author Tobias Wich
 * @author Sebastian Schuberth
 */
class VersionUpdateChecker internal constructor(
	val installedVersion: Version,
	private val list: Pair<ReleaseInfo, ArtifactType>,
) {
	val updateAdvice = list.first.checkVersion(installedVersion).getOrDefault(UpdateAdvice.NO_UPDATE)

	fun getUpdateInfo(): Pair<VersionData, UpdateAdvice>? =
		when (updateAdvice) {
			UpdateAdvice.NO_UPDATE, UpdateAdvice.MAINTAINED_NO_UPDATE -> null
			else -> list.first.getUpdateData(installedVersion) to updateAdvice
		}

	fun getArtifactUpdateUrl(): String? =
		getUpdateInfo()?.let { (data, _) ->
			data.artifacts
				.find { it.type == list.second }
				?.url
		}

	companion object {
		fun loadCurrentVersionList(): VersionUpdateChecker? {
			val loader: VersionUpdateLoader = VersionUpdateLoader.createWithDefaults()
			val list = loader.loadVersionUpdateList().getOrThrow()
			return list?.let { VersionUpdateChecker(BuildInfo.version, list) }
		}
	}
}
