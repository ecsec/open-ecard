/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.Nullable;


/**
 * Class with utility functions for working with HTML values.
 * It contains encoding functions for strings to prevent injection attacks.
 *
 * @author Tobias Wich
 */
public class HTMLUtils {

    private static final Map<Character, String> HTML_ESCAPES = new HashMap<Character, String>() {
	{
	    put('&', "&amp;");
	    put('<', "&lt;");
	    put('>', "&gt;");
	    put('"', "&quot;");
	    put('\'', "&#x27;");
	    put('/', "&#x2F;");
	}
    };
    private static final Pattern NEEDS_ATTRIBUTE_ESCAPE = Pattern.compile("\\p{Alnum}");

    /**
     * Escape HTML entities in the input string.
     *
     * @param input String to encode, may be {@code null}.
     * @return The encoded string, or {@code null} if {@code null} was given as input.
     * @see <a href="https://www.owasp.org/index.php/XSS_%28Cross_Site_Scripting%29_Prevention_Cheat_Sheet#RULE_.231_-_HTML_Escape_Before_Inserting_Untrusted_Data_into_HTML_Element_Content">OWASP XSS Cheat Sheet</a>
     */
    @Nullable
    public static String escapeHtml(@Nullable String input) {
	// shortcut if the string does not contain anything interesting
	if (input == null || input.length() == 0) {
	    return input;
	}

	StringBuilder result = new StringBuilder(input.length());
	for (int i = 0; i < input.length(); i++) {
	    result.append(encodeChar(input.charAt(i), HTML_ESCAPES));
	}
	return result.toString();
    }

    /**
     * Escape the string for safe use in HTML attributes.
     *
     * @param input String to encode, may be {@code null}.
     * @return The encoded string, or {@code null} if {@code null} was given as input.
     * @see <a href="https://www.owasp.org/index.php/XSS_%28Cross_Site_Scripting%29_Prevention_Cheat_Sheet#RULE_.232_-_Attribute_Escape_Before_Inserting_Untrusted_Data_into_HTML_Common_Attributes">OWASP XSS Cheat Sheet</a>
     */
    public static String escapeAttribute(@Nullable String input) {
	// shortcut if the string does not contain anything interesting
	if (input == null || input.length() == 0) {
	    return input;
	}

	StringBuilder result = new StringBuilder(input.length());
	for (int i = 0; i < input.length(); i++) {
	    char c = input.charAt(i);
	    if (c < 256 && NEEDS_ATTRIBUTE_ESCAPE.matcher(String.valueOf(c)).matches()) {
		result.append("&#x");
		result.append(String.format("%02x", (byte) c));
		result.append(";");
	    } else {
		result.append(c);
	    }
	}
	return result.toString();
    }

    private static String encodeChar(Character c, Map<Character, String> mapping) {
	String encoded = mapping.get(c);
	if (encoded == null) {
	    return c.toString();
	} else {
	    return encoded;
	}
    }

}
