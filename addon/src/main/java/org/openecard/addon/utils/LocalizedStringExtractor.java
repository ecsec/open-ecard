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
 ***************************************************************************/

package org.openecard.addon.utils;

import java.util.List;
import org.openecard.addon.manifest.LocalizedString;


/**
 * Utility class which provides operations related to lists containing LocalizedString objects.
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class LocalizedStringExtractor {

    /**
     * Get a string in a specific language.
     *
     * @param translations A list containing {@link LocalizedString} objects.
     * @param langCode The language code of the language to extract.
     * @return A string in the language {@code langCode} or an empty string if no string in the specific language is
     * available.
     */
    public static String getLocalizedString(List<LocalizedString> translations, String langCode) {
	String fallback = "";
	for (LocalizedString s : translations) {
	    if (s.getLang().equalsIgnoreCase(langCode)) {
		return s.getValue();
	    } else if (s.getLang().equalsIgnoreCase("EN")) {
		fallback = s.getValue();
	    }
	}
	return fallback;
    }
}
