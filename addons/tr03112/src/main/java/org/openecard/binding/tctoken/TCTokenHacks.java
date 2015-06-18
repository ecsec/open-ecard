/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

package org.openecard.binding.tctoken;

import java.io.IOException;
import javax.annotation.Nonnull;
import org.openecard.common.util.Pair;


/**
 * Helper class to fixObjectTag common problems with TCTokens.
 * TCToken provider may handle the TCToken generation in sloppy way. According to the specification, it is up to the
 * client to be as forgiving as possible. This class has fixes for the problems we have seen in the past.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
public class TCTokenHacks {

    /**
     * Fixes PathSecurity-Parameters if the trailing s is missing.
     *
     * @param input Possibly errornous string containing the token.
     * @return Fixed data.
     */
    public static String fixPathSecurityParameters(String input) {
	if (! input.contains("PathSecurity-Parameters")) {
	    input = input.replace("PathSecurity-Parameter", "PathSecurity-Parameters");
	}
	if (input.contains("&lt;PSK&gt;")) {
	    input = input.replace("&lt;PSK&gt;", "<PSK>");
	}
	if (input.contains("&lt;/PSK&gt;")) {
	    input = input.replace("&lt;/PSK&gt;", "</PSK>");
	}
	return input;
    }

    /**
     * Converts an Object tag to a TCToken, if applicable.
     * If the parameter contains an object element it is converted to a TCTpkenType. If it is already a TCTokenType, the
     * string is returned as is.
     * .
     * @param input Possibly errornous string containing the token.
     * @return Fixed data
     */
    public static String fixObjectTag(String input) {
	int x = input.indexOf("<object");
	int y = input.indexOf("object", x + 7);

	// there is nothing to do here ... leave
	if (x == -1 || y == -1) {
	    return input;
	}

	String data = input.substring(x, y);

	StringBuilder out = new StringBuilder(2048);
	out.append("<TCTokenType>");
	try {
	    while (true) {
		Pair<String, String> result = convertParameter(data);
		out.append(result.p1);
		data = result.p2;
	    }
	} catch (Exception ignore) {
	}
	out.append("</TCTokenType>");

	return out.toString();
    }

    private static Pair<String, String> convertParameter(String data) throws IOException {
	String input = data;
	StringBuilder out = new StringBuilder(2048);

	int x = input.indexOf("<param name=");
	if (x == -1) {
	    throw new IOException();
	} else {
	    x += 13;
	}
	String element = input.substring(x, input.indexOf('"', x));

	int y = input.indexOf("value=", x) + 7;
	String value = input.substring(y, input.indexOf('"', y));

	out.append("<").append(element).append(">");
	out.append(value);
	out.append("</").append(element).append(">");

	data = input.substring(y + value.length(), input.length());

	return new Pair<>(out.toString(), data);
    }

    /**
     * Replace minor code for use in refresh URLs.
     * BSI TR-03124-1 defines non URL versions of the ResultMinor codes from BSI TR-03112.
     *
     * @param minor
     * @return Code part of the minor code URL.
     */
    @Nonnull
    public static String fixResultMinor(@Nonnull String minor) {
	// each URL should be in the form <prefix>#<code>. We must return only the code part
	int idx = minor.lastIndexOf("#");
	if (idx != -1) {
	    idx++;
	    if (idx < minor.length()) {
		minor = minor.substring(idx);
	    }
	}

	// TODO: the new version of the spec allows only certain types
//	switch (minor) {
//	    case ECardConstants.Minor.App.UNKNOWN_ERROR:
//		minor = "unknownError";
//		break;
//	    case ECardConstants.Minor.App.INT_ERROR:
//		minor = "internalError";
//		break;
//	    case ECardConstants.Minor.App.COMMUNICATION_ERROR:
//		minor = "communicationError";
//		break;
//	    case ECardConstants.Minor.App.INCORRECT_PARM:
//		minor = "incorrectParameter";
//		break;
//	    default:
//		minor = "unkownError";
//		// we're fine for things not mentioned in the spec
//	}

	return minor;
    }

}
