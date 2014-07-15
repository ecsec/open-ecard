/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
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

import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A set of utility functions used in connection with the TC Token.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class TR03112Utils {

    private static final Logger logger = LoggerFactory.getLogger(TR03112Utils.class.getName());

    private static final String SHA256 = "SHA-256";

    /**
     * Check if the two given URLs comply the Same-Origin-Policy.
     * 
     * @param url1 the first URL
     * @param url2 the second URL
     * @return {@code true} if the Same-Origin-Policy has been complied with, {@code false} otherwise
     */
    public static boolean checkSameOriginPolicy(URL url1, URL url2) {
	logger.debug("Checking SOP for {} and {}.", url1, url2);
	String endpointProtocol = url1.getProtocol();
	String subjectProtocol = url2.getProtocol();
	if (! endpointProtocol.equalsIgnoreCase(subjectProtocol)) {
	    logger.error("SOP violated; the protocols do not match.");
	    return false;
	}

	String endpointHost = url1.getHost();
	String subjectHost = url2.getHost();
	if (! endpointHost.equalsIgnoreCase(subjectHost)) {
	    logger.error("SOP violated; the hosts do not match.");
	    return false;
	}

	int endpointPort = url1.getPort();
	if (endpointPort == -1) {
	    endpointPort = url1.getDefaultPort();
	}

	int subjectPort = url2.getPort();
	if (subjectPort == -1) {
	    subjectPort = url2.getDefaultPort();
	}

	if (! (endpointPort == subjectPort)) {
	    logger.error("SOP violated; the ports do not match");
	    return false;
	}

	return true;
    }

    /**
     * Check if the hash of the retrieved server certificate is contained in the CommCertificates of the
     * CertificateDescription extension of the eService certificate.
     * 
     * @param serverCertificate the retrieved server certificate
     * @param commCertificates List of hashes of the communication certificates as obtained from the
     *     CertificateDescription
     * @return {@code true} if the hash is contained; {@code false} otherwise
     */
    public static boolean isInCommCertificates(Certificate serverCertificate, List<byte[]> commCertificates) {
	try {
	    // calculate hash of first certificate in chain
	    MessageDigest md = MessageDigest.getInstance(SHA256);
	    md.update(serverCertificate.getCertificateAt(0).getEncoded());
	    byte[] hash = md.digest();

	    if (logger.isDebugEnabled()) {
		logger.debug("Hash of the retrieved server certificate: {}", ByteUtils.toHexString(hash));
	    }

	    // finally check if contained in the CommCertificates
	    for (byte[] commCertificate : commCertificates) {
		logger.debug("CommCertificate: {}", ByteUtils.toHexString(commCertificate));
		if (ByteUtils.compare(commCertificate, hash)) {
		    return true;
		}
	    }
	} catch (NoSuchAlgorithmException e) {
	    logger.error("SHA-256 digest algorithm is not available.");
	    return false;
	} catch (IOException e) {
	    logger.error("Server certificate couldn't be encoded.");
	    return false;
	}
	return false;
    }

    /**
     * Check if the given status code indicates a redirect (301, 302, 303, 307).
     *
     * @param statusCode the status code to check
     * @return {@code true} if the status code indicates a redirect, {@code false} otherwise
     */
    public static boolean isRedirectStatusCode(int statusCode) {
	switch (statusCode) {
		// TODO check why 301 is not mentioned in TR
	    case 301: // fall through
	    case 302: // fall through
	    case 303: // fall through
	    case 307:
		return true;

	    default:
		return false;
	}
    }

}
