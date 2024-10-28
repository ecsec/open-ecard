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
import org.openecard.common.SemanticVersion
import java.net.MalformedURLException
import java.net.URL
import kotlin.test.*

val log = KotlinLogging.logger {}

/**
 *
 * @author Sebastian Schuberth
 */
class TestMockedVersionUpdate {
    private val url = "http://www.google.de"
    private val currentVersion = SemanticVersion("1.2.0")


    @Test
    fun testNoUpdateAvailable() {
        val updateList = createInput()
        val result = VersionUpdateChecker(currentVersion, updateList)

        assertTrue(result.isCurrentMaintained)
        assertNull(result.securityUpgrade)
        assertNull(result.majorUpgrade)
        assertNull(result.minorUpgrade)
        assertFalse(result.needsUpdate())
    }

    @Test
    fun testUpdateMajorVersionAvailable() {
        val nextMajorUpdate = newVersionUpdate(incrementPatch(incrementMajor(currentVersion)))

        val updateList = createInput(nextMajorUpdate)
        val result = VersionUpdateChecker(currentVersion, updateList)

        assertTrue(result.isCurrentMaintained)
        assertNull(result.securityUpgrade)
        assertNull(result.minorUpgrade)
        assertEquals(nextMajorUpdate, result.majorUpgrade)
        assertTrue(result.needsUpdate())
    }

    @Test
    fun testUpdateSecurityVersionAvailable() {
        val nextPatchUpdate = newVersionUpdate(incrementPatch(incrementPatch(currentVersion)))

        val updateList = createInput(nextPatchUpdate)
        val result = VersionUpdateChecker(currentVersion, updateList)

        assertTrue(result.isCurrentMaintained)
        assertTrue(result.needsUpdate())
        assertNull(result.minorUpgrade)
        assertNull(result.majorUpgrade)
        assertEquals(nextPatchUpdate, result.securityUpgrade)
    }

    @Test
    fun testUpdateMinorVersionAvailable() {
        val nextMinorUpdate = newVersionUpdate(incrementPatch(incrementMinor(currentVersion)))

        val updateList = createInput(nextMinorUpdate)
        val result = VersionUpdateChecker(currentVersion, updateList)

        assertTrue(result.isCurrentMaintained)
        assertNull(result.securityUpgrade)
        assertNull(result.majorUpgrade)
        assertEquals(nextMinorUpdate, result.minorUpgrade)
        assertTrue(result.needsUpdate())
    }

    private fun incrementMajor(currentVersion: SemanticVersion): SemanticVersion {
        return createVersion(currentVersion.major + 1, currentVersion.minor, currentVersion.patch)
    }

    private fun incrementMinor(currentVersion: SemanticVersion): SemanticVersion {
        return createVersion(currentVersion.major, currentVersion.minor + 1, currentVersion.patch)
    }

    private fun incrementPatch(currentVersion: SemanticVersion): SemanticVersion {
        return createVersion(currentVersion.major, currentVersion.minor, currentVersion.patch + 1)
    }

    private fun createVersion(major: Int, minor: Int, patch: Int): SemanticVersion {
        val incremented = String.format("%d.%d.%d", major, minor, patch)
        return SemanticVersion(incremented)
    }


    private fun createInput(update: VersionUpdate = newVersionUpdate(currentVersion)): VersionUpdateList {
        val updates: MutableList<VersionUpdate> = mutableListOf()
        updates.add(newVersionUpdate(currentVersion))
        updates.add(update)

        val downloadPage: URL
        try {
            downloadPage = URL("$url/downloadpage")
        } catch (ex: MalformedURLException) {
            throw IllegalArgumentException("Wrong url", ex)
        }
        val updateList = VersionUpdateList(updates, downloadPage)
        return updateList
    }

    private fun newVersionUpdate(version: SemanticVersion): VersionUpdate {
		val downloadUrl = URL(url)
		return VersionUpdate(version, downloadUrl, downloadUrl, VersionUpdate.Status.MAINTAINED)
    }

}
