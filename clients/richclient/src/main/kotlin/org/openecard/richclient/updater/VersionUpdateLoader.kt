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
import org.jose4j.json.internal.json_simple.JSONArray
import org.jose4j.json.internal.json_simple.JSONObject
import org.jose4j.json.internal.json_simple.parser.JSONParser
import org.jose4j.json.internal.json_simple.parser.ParseException
import org.openecard.common.OpenecardProperties
import org.openecard.common.util.InvalidUpdateDefinition
import org.openecard.common.util.SysUtils
import org.openecard.crypto.tls.proxy.ProxySettings
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
class VersionUpdateLoader internal constructor(private val updateUrl: URL, private val pkgType: String) {
    @Throws(IllegalArgumentException::class)
    fun loadVersionUpdateList(): VersionUpdateList {
        try {
            // load proxy if one is available
            ProxySettings.default // make sure it is initialized
            val proxies = ProxySelector.getDefault().select(updateUrl.toURI())
            var p = Proxy.NO_PROXY
            for (next in proxies) {
                if (next.type() != Proxy.Type.DIRECT) {
					LOG.debug { "Found a proxy for the update connection." }
                    p = next
                    break
                }
            }

			LOG.info { "Trying to load version list." }
            val con = updateUrl.openConnection(p)
            con.connect()
            val `in` = con.getInputStream()
            val r: Reader = InputStreamReader(`in`, StandardCharsets.UTF_8)

            val rootObj = JSONParser().parse(r) as JSONObject

            // get package specific download page
            val downloadPageString = rootObj[pkgType + "_download_page"] as String

            // access package specific list
            val updatesRaw = rootObj[pkgType] as JSONArray

            val updates = ArrayList<VersionUpdate>()

            for (ur in updatesRaw) {
                try {
                    val next: VersionUpdate = VersionUpdate.Companion.fromJson(ur as JSONObject)
                    updates.add(next)
                } catch (ex: InvalidUpdateDefinition) {
					LOG.warn(ex) { "Invalid version info contained in update list." }
                    throw IllegalArgumentException("Invalid version info contained in update list.", ex)
                }
            }

            // make sure the versions are in the correct order
			updates.sort()

            val list = VersionUpdateList(updates, URL(downloadPageString))
			LOG.info { "Successfully retrieved version update list." }
            return list
        } catch (ex: IOException) {
			LOG.error(ex) { "Failed to retrieve update list from server." }
            throw IllegalArgumentException("Failed to retrieve update list from server.", ex)
        } catch (ex: NullPointerException) {
			LOG.warn { "Package type ${pkgType} not supported in update list." }
            throw IllegalArgumentException("Package type $pkgType not supported in update list.", ex)
        } catch (ex: URISyntaxException) {
            val msg = "Failed to convert Update URL to a URI."
			LOG.error(ex) { msg }
            throw IllegalArgumentException(msg, ex)
        } catch (ex: ParseException) {
            val msg = "Failed to deserialize JSON data."
			LOG.error(ex) { msg }
            throw IllegalArgumentException(msg, ex)
        }
    }

    companion object {
        @Throws(IllegalArgumentException::class)
        fun createWithDefaults(): VersionUpdateLoader {
            try {
                return VersionUpdateLoader(getUpdateUrl(), getPkgType())
            } catch (ex: MalformedURLException) {
                val msg = "Update URL value is not a valid URL."
				LOG.error(ex) { msg }
                throw IllegalArgumentException(msg, ex)
            }
        }

        @Throws(MalformedURLException::class)
        fun getUpdateUrl(): URL {
            val url = OpenecardProperties.getProperty("update-list.location")
            return URL(url)
        }

        fun getPkgType(): String {
            if (SysUtils.isWin() && SysUtils.is64bit()) {
                return "win64"
            } else if (SysUtils.isWin()) {
                return "win32"
            } else if (SysUtils.isDebianOrDerivate()) {
                return "deb"
            } else if (SysUtils.isRedhatOrDerivate()) {
                return "rpm"
            } else if (SysUtils.isSuSEOrDerivate()) {
                return "rpm"
            } else if (SysUtils.isMacOSX()) {
                return "osx"
            }

            return "UNKNOWN"
        }
    }
}
