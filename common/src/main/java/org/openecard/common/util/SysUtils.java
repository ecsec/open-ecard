/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;


/**
 * Utility class for system related tasks.
 *
 * @author Tobias Wich
 */
public class SysUtils {

    private static String getName() {
	return System.getProperty("os.name").toLowerCase();
    }

    public static boolean isWin() {
	return getName().contains("win");
    }

    public static boolean isMacOSX() {
	return getName().contains("mac");
    }

    public static boolean isUnix() {
	String os = getName();
	return os.contains("nux") || os.contains("nix") || os.contains("aix");
    }


    private static final Pattern CURLY_VAR_ENV = Pattern.compile("(\\\\)?(\\$\\{([A-Za-z0-9_]+)\\})");
    private static final Pattern PLAIN_VAR_ENV = Pattern.compile("(\\\\)?(\\$([A-Za-z0-9_]+))");
    private static final Pattern CURLY_VAR_SYS = Pattern.compile("(\\\\)?(\\$\\{([A-Za-z0-9_\\.]+)\\})");

    /**
     * Expands environment variables in the given text.
     * The environment variables can be in the form of $VAR or ${VAR}. Unset variables will be replaced with the empty
     * string. To prevent the evaluation of a variable it must be prepended with a \ (e.g. \${VAR}). The \ gets removed
     * leaving only the text after it in place.
     *
     * @param s The text in which the variables should be expanded.
     * @return The expanded text.
     */
    @Nonnull
    public static String expandEnvVars(String s) {
	// expand and clear unset vars
	s = expandVars(s, System.getenv(), CURLY_VAR_ENV, true);
	s = expandVars(s, System.getenv(), PLAIN_VAR_ENV, true);
	// remove quote symbols
	s = removeQuote(s, CURLY_VAR_ENV);
	s = removeQuote(s, PLAIN_VAR_ENV);
	return s;
    }

    /**
     * Expands system properties in the given text.
     * The system property can be in the form of ${VAR}. Unset variables will be replaced with the empty
     * string. To prevent the evaluation of a variable it must be prepended with a \ (e.g. \${VAR}). The \ gets removed
     * leaving only the text after it in place.
     *
     * @param s The text in which the variables should be expanded.
     * @return The expanded text.
     */
    @Nonnull
    public static String expandSysProps(@Nonnull String s) {
	// expand and clear unset vars
	s = expandVars(s, System.getProperties(), CURLY_VAR_SYS, true);
	// remove quote symbols
	s = removeQuote(s, CURLY_VAR_SYS);
	return s;
    }

    /**
     * Expands environment variables and system properties in the given text.
     * The environment variables can be in the form of $VAR or ${VAR} and the system properties can be in the form of
     * ${VAR}. Unset variables will be replaced with the empty string. To prevent the evaluation of a variable it must
     * be prepended with a \ (e.g. \${VAR}). The \ gets removed leaving only the text after it in place.<br>
     * The evaluation order is:
     * <ol>
     * <li>Environment variables</li>
     * <li>System Properties</li>
     * </ol>
     *
     * @param s The text in which the variables should be expanded.
     * @return The expanded text.
     */
    @Nonnull
    public static String expandEnvVarsAndSysProps(@Nonnull String s) {
	// expand curly env without clearing vars so system properties have a chance to set values too
	s = expandVars(s, System.getenv(), CURLY_VAR_ENV, false);
	s = expandVars(s, System.getenv(), PLAIN_VAR_ENV, true);
	s = expandVars(s, System.getProperties(), CURLY_VAR_SYS, true);
	// remove quote symbols
	s = removeQuote(s, CURLY_VAR_ENV);
	s = removeQuote(s, PLAIN_VAR_ENV);
	s = removeQuote(s, CURLY_VAR_SYS);
	return s;
    }


    private static String expandVars(String s, Map<? extends Object, ? extends Object> env, Pattern p, boolean clear) {
	Matcher m = p.matcher(s);

	// Look up all variables in the string
	while (m.find()) {
	    boolean quote = "\\".equals(m.group(1));
	    String varName = m.group(3);
	    Object envValueObj = env.get(varName);
	    String replacement;
	    if (envValueObj != null) {
		replacement = envValueObj.toString();
	    } else {
		if (clear) {
		    replacement = "";
		} else {
		    replacement = null;
		}
	    }

	    // perform the replace (variable -> value) only if there is a value
	    if (! quote && replacement != null) {
		String target = m.group(2);
		s = s.replace(target, replacement);
	    }
	}

	return s;
    }

    private static String removeQuote(String s, Pattern p) {
	Matcher m = p.matcher(s);
	s = m.replaceAll("$2");
	return s;
    }

}
