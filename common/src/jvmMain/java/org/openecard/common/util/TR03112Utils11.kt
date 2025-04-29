/****************************************************************************
 * Copyright (C) 2013-2017 HS Coburg.
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

import org.openecard.bouncycastle.tls.TlsServerCertificate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * A set of utility functions used in connection with the TC Token.
 *
 * @author Dirk Petrautzki
 */
object TR03112Utils {
    private val LOG: Logger = LoggerFactory.getLogger(TR03112Utils::class.java.name)

    private const val SHA256 = "SHA-256"

    /**
     * This switch enables the developer mode described in TR-03124-1.
     * When enabled the Communication Certificate checks do not fail but issue a warning instead.
     */
    @JvmField
    var DEVELOPER_MODE: Boolean = false

    /**
     * Check if the two given URLs comply the Same-Origin-Policy.
     *
     * @param url1 the first URL
     * @param url2 the second URL
     * @return `true` if the Same-Origin-Policy has been complied with, `false` otherwise
     */
    @JvmStatic
    fun checkSameOriginPolicy(url1: URL, url2: URL): Boolean {
        LOG.debug("Checking SOP for {} and {}.", url1, url2)
        val endpointProtocol = url1.protocol
        val subjectProtocol = url2.protocol
        if (!endpointProtocol.equals(subjectProtocol, ignoreCase = true)) {
            LOG.error("SOP violated; the protocols do not match.")
            return false
        }

        val endpointHost = url1.host
        val subjectHost = url2.host
        if (!endpointHost.equals(subjectHost, ignoreCase = true)) {
            LOG.error("SOP violated; the hosts do not match.")
            return false
        }

        var endpointPort = url1.port
        if (endpointPort == -1) {
            endpointPort = url1.defaultPort
        }

        var subjectPort = url2.port
        if (subjectPort == -1) {
            subjectPort = url2.defaultPort
        }

        if (endpointPort != subjectPort) {
            LOG.error("SOP violated; the ports do not match")
            return false
        }

        return true
    }

    /**
     * Check if the hash of the retrieved server certificate is contained in the CommCertificates of the
     * CertificateDescription extension of the eService certificate.
     *
     * @param serverCertificate the retrieved server certificate
     * @param commCertificates List of hashes of the communication certificates as obtained from the
     * CertificateDescription
     * @param serverDesc Description of the server for which the certificate is checked. May be a hostname or just a
     * verbose description such as eService.
     * @return `true` if the hash is contained; `false` otherwise
     */
    fun isInCommCertificates(
        serverCertificate: TlsServerCertificate, commCertificates: List<ByteArray?>,
        serverDesc: String?
    ): Boolean {
        LOG.info("Checking certificate hash of {} against list of communication certificates.", serverDesc)
        try {
            // calculate hash of first certificate in chain
            val md = MessageDigest.getInstance(SHA256)
            md.update(serverCertificate.certificate.getCertificateAt(0).encoded)
            val hash = md.digest()

            if (LOG.isDebugEnabled) {
                LOG.debug("Hash of the retrieved server certificate: {}", ByteUtils.toHexString(hash))
            }

            // finally check if contained in the CommCertificates
            for (commCertificate in commCertificates) {
                LOG.debug("CommCertificate: {}", ByteUtils.toHexString(commCertificate))
                if (ByteUtils.compare(commCertificate, hash)) {
                    return true
                }
            }
        } catch (e: NoSuchAlgorithmException) {
            LOG.error("SHA-256 digest algorithm is not available.")
            return false
        } catch (e: IOException) {
            LOG.error("Server certificate couldn't be encoded.")
            return false
        }

        return false
    }

    /**
     * Check if the given status code indicates a redirect (301, 302, 303, 307).
     *
     * @param statusCode the status code to check
     * @return `true` if the status code indicates a redirect, `false` otherwise
     */
    @JvmStatic
    fun isRedirectStatusCode(statusCode: Int): Boolean {
        return when (statusCode) {
            301, 302, 303, 307 -> true

            else -> false
        }
    }
}
