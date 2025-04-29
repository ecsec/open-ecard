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
 */
package org.openecard.common.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.IDN
import javax.annotation.Nonnull

/**
 *
 * @author Tobias Wich
 */
object DomainUtils {
    private val LOG: Logger = LoggerFactory.getLogger(DomainUtils::class.java)

    /**
     * Compares a reference host against an actual host.
     *
     * The reference host may be in wildcard form according to RFC 6125, Sec. 6.4.3, in case the boolean flag is
     * set.
     *
     * International domain names are supported.
     *
     * @param referenceHost Reference host name. May be a wildcard host name.
     * @param actualHost Actual host name which is checked against the reference host name.
     * @return `true` if the actual host matches the reference, `false` otherwise.
     * @throws IllegalArgumentException Thrown in case any of the given host names is invalid.
     * @see .checkHostName
     */
    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun checkWildcardHostName(@Nonnull referenceHost: String, @Nonnull actualHost: String?): Boolean {
        return checkHostName(referenceHost, actualHost, true)
    }

    /**
     * Compares a reference host against an actual host.
     *
     * The reference host may be in wildcard form according to RFC 6125, Sec. 6.4.3, in case the boolean flag is
     * set.
     *
     * International domain names are supported.
     *
     * @param referenceHost Reference host name. May be a wildcard host name.
     * @param actualHost Actual host name which is checked against the reference host name.
     * @param allowWildcard `true` if wildcard reference host names are allowed, `false` otherwise.
     * @return `true` if the actual host matches the reference, `false` otherwise.
     * @throws IllegalArgumentException Thrown in case any of the given host names is invalid.
     */
    /**
     * Compares a reference host against an actual host.
     *
     * Wildcard comparisons are not supported by this function.
     *
     * International domain names are supported.
     *
     * @param referenceHost Reference host name.
     * @param actualHost Actual host name which is checked against the reference host name.
     * @return `true` if the actual host matches the reference, `false` otherwise.
     * @throws IllegalArgumentException Thrown in case any of the given host names is invalid.
     * @see .checkHostName
     */
    @JvmOverloads
    @Throws(IllegalArgumentException::class)
    fun checkHostName(
        @Nonnull referenceHost: String, @Nonnull actualHost: String?,
        allowWildcard: Boolean = false
    ): Boolean {
        try {
            // check if wildcard host is used and allowed
            if (!allowWildcard && referenceHost.startsWith("*")) {
                return false
            }

            val actualToken =
                IDN.toASCII(actualHost).split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val refToken =
                IDN.toASCII(referenceHost).split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            // error if number of token is different
            if (actualToken.size != refToken.size) {
                return false
            }

            // compare entries in backwards order
            for (i in actualToken.indices.reversed()) {
                // wildcard may only be at the last position
                if (i == 0 && refToken[i] == "*") {
                    continue
                }
                // compare in case insensitive way
                if (!actualToken[i].equals(refToken[i], ignoreCase = true)) {
                    return false
                }
            }

            // each part processed and no error -> success
            return true
        } catch (ex: IllegalArgumentException) {
            val msg = "Error normalizing international domain name."
            LOG.warn(msg, ex)
            throw IllegalArgumentException(msg, ex)
        }
    }
}
