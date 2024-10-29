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

package org.openecard.richclient.updater

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.jose4j.json.internal.json_simple.JSONArray
import org.jose4j.json.internal.json_simple.JSONObject
import org.jose4j.json.internal.json_simple.parser.JSONParser
import org.jose4j.json.internal.json_simple.parser.ParseException
import org.openecard.common.OpenecardProperties
import org.openecard.common.util.InvalidUpdateDefinition
import org.openecard.common.util.SysUtils
import org.openecard.crypto.tls.proxy.ProxySettings
import org.openecard.releases.ArtifactType
import org.openecard.releases.ReleaseInfo
import org.openecard.releases.loadReleaseInfo
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.net.*
import java.nio.charset.StandardCharsets
import java.util.*

private val LOG = KotlinLogging.logger {}

/**
 * Class loading the updates file.
 *
 * @author Tobias Wich
 * @author Sebastian Schuberth
 */
class VersionUpdateLoader internal constructor(private val updateUrl: String, private val pkgType: ArtifactType?) {
    fun loadVersionUpdateList(): Result<Pair<ReleaseInfo, ArtifactType>?> = runBlocking {
		// TODO: add local caching of the release info jwt, for offline display of the update
		pkgType?.let {
			loadReleaseInfo(updateUrl)
				.map { it.releaseInfo }
				.map { it to pkgType }
		} ?: Result.success(null)
    }

    companion object {
        fun createWithDefaults(): VersionUpdateLoader {
			return VersionUpdateLoader(getUpdateUrl(), getPkgType())
        }

        @Throws(MalformedURLException::class)
        private fun getUpdateUrl(): String {
            val url = OpenecardProperties.getProperty("release-info.location")
            return url
        }

        private fun getPkgType(): ArtifactType? {
            return if (SysUtils.isWin() && SysUtils.is64bit()) {
                ArtifactType.EXE
            } else if (SysUtils.isWin()) {
				// win 32 is not supported anymore
                null
            } else if (SysUtils.isDebianOrDerivate()) {
                ArtifactType.DEB
            } else if (SysUtils.isRedhatOrDerivate()) {
                ArtifactType.RPM
            } else if (SysUtils.isSuSEOrDerivate()) {
                ArtifactType.RPM
            } else if (SysUtils.isMacOSX()) {
                ArtifactType.DMG
            } else {
				null
			}
        }
    }
}
