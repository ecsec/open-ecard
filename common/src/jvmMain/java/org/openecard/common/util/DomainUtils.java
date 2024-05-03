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

import java.net.IDN;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class DomainUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DomainUtils.class);

    /**
     * Compares a reference host against an actual host.
     * <p>Wildcard comparisons are not supported by this function.</p>
     * <p>International domain names are supported.</p>
     *
     * @param referenceHost Reference host name.
     * @param actualHost Actual host name which is checked against the reference host name.
     * @return {@code true} if the actual host matches the reference, {@code false} otherwise.
     * @throws IllegalArgumentException Thrown in case any of the given host names is invalid.
     * @see #checkHostName(String, String, boolean)
     */
    public static boolean checkHostName(@Nonnull String referenceHost, @Nonnull String actualHost)
	    throws IllegalArgumentException {
	return checkHostName(referenceHost, actualHost, false);
    }

    /**
     * Compares a reference host against an actual host.
     * <p>The reference host may be in wildcard form according to RFC 6125, Sec. 6.4.3, in case the boolean flag is
     * set.</p>
     * <p>International domain names are supported.</p>
     *
     * @param referenceHost Reference host name. May be a wildcard host name.
     * @param actualHost Actual host name which is checked against the reference host name.
     * @return {@code true} if the actual host matches the reference, {@code false} otherwise.
     * @throws IllegalArgumentException Thrown in case any of the given host names is invalid.
     * @see #checkHostName(String, String, boolean)
     */
    public static boolean checkWildcardHostName(@Nonnull String referenceHost, @Nonnull String actualHost)
	    throws IllegalArgumentException {
	return checkHostName(referenceHost, actualHost, true);
    }

    /**
     * Compares a reference host against an actual host.
     * <p>The reference host may be in wildcard form according to RFC 6125, Sec. 6.4.3, in case the boolean flag is
     * set.</p>
     * <p>International domain names are supported.</p>
     *
     * @param referenceHost Reference host name. May be a wildcard host name.
     * @param actualHost Actual host name which is checked against the reference host name.
     * @param allowWildcard {@code true} if wildcard reference host names are allowed, {@code false} otherwise.
     * @return {@code true} if the actual host matches the reference, {@code false} otherwise.
     * @throws IllegalArgumentException Thrown in case any of the given host names is invalid.
     */
    public static boolean checkHostName(@Nonnull String referenceHost, @Nonnull String actualHost,
	    boolean allowWildcard) throws IllegalArgumentException {
	try {
	    // check if wildcard host is used and allowed
	    if (! allowWildcard && referenceHost.startsWith("*")) {
		return false;
	    }

	    String[] actualToken = IDN.toASCII(actualHost).split("\\.");
	    String[] refToken = IDN.toASCII(referenceHost).split("\\.");

	    // error if number of token is different
	    if (actualToken.length != refToken.length) {
		return false;
	    }

	    // compare entries in backwards order
	    for (int i = actualToken.length - 1; i >= 0; i--) {
		// wildcard may only be at the last position
		if (i == 0 && refToken[i].equals("*")) {
		    continue;
		}
		// compare in case insensitive way
		if (! actualToken[i].equalsIgnoreCase(refToken[i])) {
		    return false;
		}
	    }

	    // each part processed and no error -> success
	    return true;
	} catch (IllegalArgumentException ex) {
	    String msg = "Error normalizing international domain name.";
	    LOG.warn(msg, ex);
	    throw new IllegalArgumentException(msg, ex);
	}
    }

}
