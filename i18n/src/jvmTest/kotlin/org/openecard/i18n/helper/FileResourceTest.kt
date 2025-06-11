/****************************************************************************
 * Copyright (C) 2012-2025 ecsec GmbH.
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
package org.openecard.i18n.helper

import org.openecard.i18n.I18N
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertContains

class FileResourceTest {
	@Test
	fun html_in_xml() {
		assertContains(
			I18N.strings.about_html.localized(),
			"<title>About</title>",
		)
	}

	@Test
	fun html_in_xml_de() {
		assertContains(
			I18N.strings.about_html.localized(Locale.GERMAN),
			"<title>Über</title>",
		)
	}

	@Test
	fun html_in_xml_license() {
		assertContains(
			I18N.strings.about_license_html.localized(),
			"<title>GNU General Public License v3.0 - GNU Project - Free Software Foundation (FSF)</title>",
		)
	}

	@Test
	fun html_in_xml_license_de_fallback_en() {
		assertContains(
			I18N.strings.about_license_html.localized(Locale.GERMAN),
			"<title>GNU General Public License v3.0 - GNU Project - Free Software Foundation (FSF)</title>",
		)
	}
}
