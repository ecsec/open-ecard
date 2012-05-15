/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.common.util;

import java.security.SecureRandom;
import java.util.UUID;


/**
 * Implements convenience methods to generates random values.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ValueGenerators {

    /**
     * Generates a new pre-shared key (PSK).
     *
     * @return PSK
     */
    public static String generatePSK() {
	return generatePSK(64);
    }

    /**
     * Generates a new pre-shared key (PSK).
     *
     * @param bitLength Length of the PSK
     * @return PSK
     */
    public static String generatePSK(int bitLength) {
	return generateRandomHex(bitLength);
    }

    /**
     * Generates a secure session identifier.
     *
     * @return Session identifier
     */
    public static String generateSessionID() {
	return generateSessionID(32);
    }

    /**
     * Generates a secure session identifier.
     *
     * @param bitLength Length of the session identifier
     * @return Session identifier
     */
    public static String generateSessionID(int bitLength) {
	return generateRandomHex(bitLength);
    }

    /**
     * Generates a UUID.
     * Using Java UUID and adds the prefix 'urn:uuid:'.
     *
     * @return UUID
     */
    public static String generateUUID() {
	String uuid = UUID.randomUUID().toString();
	return "urn:uuid:" + uuid;
    }

    /**
     * Generates a secure random hex string.
     *
     * @param bitLength Length of the random
     * @return Secure random hex string
     */
    public static String generateRandomHex(int bitLength) {
	return ByteUtils.toHexString(generateRandom(bitLength));
    }

    /**
     * Generates a secure random value.
     * Using 'java.security.SecureRandom'.
     *
     * @param length Length of the random
     * @return Secure random value
     */
    public static byte[] generateRandom(int bitLength) {
	if (bitLength < 1) {
	    return null;
	}

	bitLength = (bitLength / 2 + bitLength % 2);

	SecureRandom rand = new SecureRandom();
	byte[] randomBytes = new byte[bitLength];
	rand.nextBytes(randomBytes);

	return randomBytes;
    }

}
