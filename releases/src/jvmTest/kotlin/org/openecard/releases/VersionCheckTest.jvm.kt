/****************************************************************************
 * Copyright (C) 2024 ecsec GmbH.
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

package org.openecard.releases

import io.github.z4kn4fein.semver.constraints.toConstraint
import io.github.z4kn4fein.semver.toVersion
import kotlin.test.Test
import kotlin.test.assertEquals

class VersionCheckTest {

	private val releaseInfo = ReleaseInfo(
		version = "2.2.4".toVersion(),
		latestVersion = VersionData("2.2.4".toVersion(), listOf()),
		maintenanceVersions = listOf(VersionData("2.1.4".toVersion(), listOf())),
		artifacts = listOf(),
		versionStatus = VersionStatus(
			maintained = listOf("~2.1.0".toConstraint()),
			security = listOf(">=2.1.0 <=2.1.2".toConstraint()),
		),
	)

	@Test
	fun `test no update`() {
		assertEquals(UpdateAdvice.NO_UPDATE, releaseInfo.checkVersion("2.2.4").getOrThrow())
	}

	@Test
	fun `test unreleased version`() {
		assertEquals(UpdateAdvice.NO_UPDATE, releaseInfo.checkVersion("2.2.5").getOrThrow())
	}

	@Test
	fun `test update major`() {
		assertEquals(UpdateAdvice.UPDATE, releaseInfo.checkVersion("1.5.10").getOrThrow())
	}

	@Test
	fun `test update minor`() {
		assertEquals(UpdateAdvice.UPDATE, releaseInfo.checkVersion("2.0.0").getOrThrow())
	}

	@Test
	fun `test update patch`() {
		assertEquals(UpdateAdvice.UPDATE, releaseInfo.checkVersion("2.2.1").getOrThrow())
	}

	@Test
	fun `test sec update`() {
		assertEquals(UpdateAdvice.SECURITY_UPDATE, releaseInfo.checkVersion("2.1.1").getOrThrow())
	}

	@Test
	fun `test maintenance update`() {
		assertEquals(UpdateAdvice.MAINTAINED_UPDATE, releaseInfo.checkVersion("2.1.3").getOrThrow())
	}

	@Test
	fun `test maintenance no update`() {
		assertEquals(UpdateAdvice.MAINTAINED_NO_UPDATE, releaseInfo.checkVersion("2.1.4").getOrThrow())
	}
}
