/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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
 */
package org.openecard.addon.utils

import org.openecard.addon.manifest.LocalizedString

/**
 * Utility class which provides operations related to lists containing LocalizedString objects.
 *
 * @author Hans-Martin Haase
 */
object LocalizedStringExtractor {
	/**
	 * Get a string in a specific language.
	 *
	 * @param translations A list containing [LocalizedString] objects.
	 * @param langCode The language code of the language to extract.
	 * @return A string in the language `langCode` or an empty string if no string in the specific language is
	 * available.
	 */
	fun getLocalizedString(
		translations: MutableList<LocalizedString>,
		langCode: String,
	): String {
		var fallback = ""
		for (s in translations) {
			if (s.lang.equals(langCode, ignoreCase = true)) {
				return s.value ?: fallback
			} else if (s.lang.equals("EN", ignoreCase = true)) {
				fallback = s.value!!
			}
		}
		return fallback
	}
}
