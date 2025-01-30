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

import org.jose4j.json.internal.json_simple.JSONObject
import org.openecard.common.SemanticVersion
import org.openecard.common.util.InvalidUpdateDefinition
import java.net.MalformedURLException
import java.net.URL
import kotlin.test.*

/**
 *
 * @author Sebastian Schuberth
 */
class TestRealVersionUpdate {
    private val url = "http://www.google.de"
    private val currentVersion = SemanticVersion("1.2.0")

    @Test
    fun validDefaultCreationOfVersionUpdateLoader() {
        val updateListUrl = URL(url)
        val sysPkg = "deb"

        val result = VersionUpdateLoader(updateListUrl, sysPkg)
        assertNotNull(result)
    }

    @Test
    fun testEmptyUpdateList() {
        val updateList = createEmptyInput()

        val result = VersionUpdateChecker(currentVersion, updateList)

        assertNull(result.currentVersion)
        assertFalse(result.isCurrentMaintained)
        assertNull(result.majorUpgrade)
        assertNull(result.securityUpgrade)
        assertNull(result.minorUpgrade)
        assertEquals(result.downloadPage, URL("$url/downloadpage"))
    }

    @Test
    fun loadValidUpdateList() {
        val downloadListAddress = TestRealVersionUpdate::class.java.getResource("/updatelist.json")
        val systemPkg = "deb"
        val expectedDownloadPage = URL("https://www.openecard.org/downloads_$systemPkg")
        val updateLoader = VersionUpdateLoader(downloadListAddress, systemPkg)

        val result = updateLoader.loadVersionUpdateList()
        assertFalse(result.versionUpdates.isEmpty())
        assertEquals(result.downloadPage, expectedDownloadPage)
    }

    @Test
    fun loadUpdateListForNonExistingSystemPkg() {
        val downloadListAddress = TestRealVersionUpdate::class.java.getResource("/updatelist.json")
        val systemPkg = "nonexisting"

        val updateLoader = VersionUpdateLoader(downloadListAddress, systemPkg)
        try {
            val result = updateLoader.loadVersionUpdateList()
            fail() // Exception expected
        } catch (ex: IllegalArgumentException) {
            assertEquals(ex.message, "Package type $systemPkg not supported in update list.")
        }
    }

    @Test
    fun loadUpdateListWithInvalidVersion() {
        val downloadListAddress = TestRealVersionUpdate::class.java.getResource("/invalidupdatelist.json")
        val systemPkg = "deb"

        val updateLoader = VersionUpdateLoader(downloadListAddress, systemPkg)
        try {
            val result = updateLoader.loadVersionUpdateList()
            fail() // Exception expected
        } catch (ex: IllegalArgumentException) {
            assertEquals(ex.message, "Invalid version info contained in update list.")
        }
    }

    @Test
    fun loadUpdateListFromNonExistingURL() {
        val downloadListAddress = URL("http://thisisaboguspage.com")
        val systemPkg = "deb"

        val updateLoader = VersionUpdateLoader(downloadListAddress, systemPkg)
        try {
            val result = updateLoader.loadVersionUpdateList()
            fail() // Exception expected
        } catch (ex: IllegalArgumentException) {
            // fine, end the test
        }
    }

    @Test
    fun validVersionUpdate() {
        // Same values as the default values of the JSONObjectBuilder
        val downloadPage = "http://www.google.de/downloadpage"
        val downloadUrl = "http://www.google.de/downloadurl"
        val version = "1.3.0"
        val status = VersionUpdate.Status.MAINTAINED.name

        val obj = JSONObjectBuilder().build()
        val update = VersionUpdate.fromJson(obj)

        assertEquals(update.downloadPage, URL(downloadPage))
        assertEquals(update.downloadLink, URL(downloadUrl))
        assertEquals(update.version.compareTo(SemanticVersion(version)), 0)
        assertEquals(update.status, VersionUpdate.Status.valueOf(status))

        val newerVersion = VersionUpdate(
            SemanticVersion("1.3.1"),
            URL(downloadPage),
            URL(downloadUrl),
            VersionUpdate.Status.MAINTAINED
        )
        assertEquals(update.compareTo(newerVersion), -1)
        assertEquals(newerVersion.compareTo(update), 1)
    }

    @Test
    fun versionUpdateUnknownStatus() {
        val obj = JSONObjectBuilder().status("test").build()
        val update = VersionUpdate.fromJson(obj)
        assertEquals(update.status, VersionUpdate.Status.UNKNOWN)
    }

    @Test
    fun invalidJSONObject() {
        try {
            val obj = JSONObject()
            val update = VersionUpdate.fromJson(obj)
            fail() // Exception expected
        } catch (ex: InvalidUpdateDefinition) {
            assertEquals(ex.message, "Incomplete JSON data received.")
        }
    }

    @Test
    fun versionUpdateinvalidSemanticVersionSpecified() {
        try {
            val invalidVersion = "0.0.0"
            val obj = JSONObjectBuilder().version(invalidVersion).build()
            val update = VersionUpdate.fromJson(obj)
            fail() // Exception expected
        } catch (ex: InvalidUpdateDefinition) {
            assertEquals(ex.message, "Invalid version specified.")
        }
    }

    @Test
    fun versionUpdateinvalidDownloadURL() {
        try {
            val invalidDownloadURL = "test"
            val obj = JSONObjectBuilder().downloadUrl(invalidDownloadURL).build()
            val update = VersionUpdate.fromJson(obj)
            fail() // Exception expected
        } catch (ex: InvalidUpdateDefinition) {
            assertEquals(ex.message, "Incomplete JSON data received.")
        }
    }

    @Test
    fun versionUpdateDownloadPageNotHTTP() {
        try {
            val invalidDownloadPage = "file://test"
            val obj = JSONObjectBuilder().downloadPage(invalidDownloadPage).build()
            val update = VersionUpdate.fromJson(obj)
            fail() // Exception expected
        } catch (ex: InvalidUpdateDefinition) {
            assertEquals(ex.message, "Download Page URL is not an http URL.")
        }
    }

    @Test
    fun versionUpdateDownloadUrlNotHTTP() {
        try {
            val invalidDownloadURL = "file://test"
            val obj = JSONObjectBuilder().downloadUrl(invalidDownloadURL).build()
            val update = VersionUpdate.fromJson(obj)
            fail() // Exception expected
        } catch (ex: InvalidUpdateDefinition) {
            assertEquals(ex.message, "Download URL is not an http URL.")
        }
    }

    private fun createEmptyInput(): VersionUpdateList {
        val updates: List<VersionUpdate> = ArrayList()

        val downloadPage: URL
        try {
            downloadPage = URL("$url/downloadpage")
        } catch (ex: MalformedURLException) {
            throw IllegalArgumentException("Wrong url", ex)
        }
        val updateList = VersionUpdateList(updates, downloadPage)
        return updateList
    }


    class JSONObjectBuilder {
        private var version = "1.3.0"
        private var downloadPage = "http://www.google.de/downloadpage"
        private var downloadUrl = "http://www.google.de/downloadurl"
        private var status: String

        init {
            this.status = VersionUpdate.Status.MAINTAINED.name
        }

        fun version(version: String): JSONObjectBuilder {
            this.version = version
            return this
        }

        fun downloadPage(downloadPage: String): JSONObjectBuilder {
            this.downloadPage = downloadPage
            return this
        }

        fun downloadUrl(downloadUrl: String): JSONObjectBuilder {
            this.downloadUrl = downloadUrl
            return this
        }

        fun status(status: String): JSONObjectBuilder {
            this.status = status
            return this
        }

        fun build(): JSONObject {
            val obj = JSONObject()
            obj["version"] = version
            obj["download_page"] = downloadPage
            obj["download_url"] = downloadUrl
            obj["status"] = status
            return obj
        }
    }

}
