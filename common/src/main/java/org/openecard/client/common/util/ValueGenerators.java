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
 * Alternatively, this file may be used in accordance with the terms and
 * conditions contained in a signed written agreement between you and ecsec.
 *
 ***************************************************************************/

package org.openecard.client.common.util;

import java.security.SecureRandom;
import java.util.Random;
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
	return generatePSK(32);
    }

    /**
     * Generates a new pre-shared key (PSK).
     *
     * @param length Length of the PSK
     * @return PSK
     */
    public static String generatePSK(int length) {
	return generateSecureRandomHex(length);
    }

    /**
     * Generates an session identifier.
     *
     * @return Session identifier.
     */
    public static String generateSessionID() {
	return generateSessionID(28);
    }

    /**
     * Generates a session identifier.
     *
     * @param length Length of the session identifier
     * @return Session identifier
     */
    public static String generateSessionID(int length) {
	return generateRandomHex(28);
    }

    /**
     * Generates a secure session identifier.
     *
     * @return Session identifier
     */
    public static String generateSecureSessionID() {
	return generateSecureRandomHex(28);
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
     * Generates a random hex string.
     *
     * @param length Length of the random
     * @return Random hex string
     */
    public static String generateRandomHex(int length) {
	return ByteUtils.toHexString(generateRandom(length));
    }

    /**
     * Generates a random value.
     * Using 'java.util.Random'.
     *
     * @param length Length of the random
     * @return Secure random value
     */
    public static byte[] generateRandom(int length) {
	if (length < 1) {
	    return null;
	}

	Random rand = new Random();
	byte[] randomBytes = new byte[length];
	rand.nextBytes(randomBytes);

	return randomBytes;
    }

    /**
     * Generates a secure random hex string.
     *
     * @param length Length of the random
     * @return Secure random hex string
     */
    public static String generateSecureRandomHex(int length) {
	return ByteUtils.toHexString(generateSecureRandom(length));
    }

    /**
     * Generates a secure random value.
     * Using 'java.security.SecureRandom'.
     *
     * @param length Length of the random
     * @return Secure random value
     */
    public static byte[] generateSecureRandom(int length) {
	if (length < 1) {
	    return null;
	}

	SecureRandom rand = new SecureRandom();
	byte[] randomBytes = new byte[length];
	rand.nextBytes(randomBytes);

	return randomBytes;
    }

}
