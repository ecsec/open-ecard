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

package org.openecard.common.util;

import java.security.SecureRandom;
import java.util.UUID;


/**
 * Implements convenience methods to generates random values.
 *
 * @author Tobias Wich
 */
public class ValueGenerators {

    private static final SecureRandom rand;
    private static long counter;

    static {
	rand = new SecureRandom();
	rand.setSeed(rand.generateSeed(32));
	counter = 0;
    }

    private static void reseed() {
	counter++;
	rand.setSeed(counter);
	rand.setSeed(System.nanoTime());
    }

    /**
     * Generates a new pre-shared key (PSK).
     *
     * @return PSK in hex notation.
     */
    public static String generatePSK() {
	return generatePSK(64);
    }

    /**
     * Generates a new pre-shared key (PSK).
     *
     * @param nibbleLength Length of the PSK in nibbles.
     * @return PSK in hex notation.
     */
    public static String generatePSK(int nibbleLength) {
	return generateRandomHex(nibbleLength);
    }

    /**
     * Generates a secure session identifier encoded as web safe base 64
     *
     * @return Session identifier.
     */
    public static String genBase64Session() {
	return genBase64Session(32);
    }

    /**
     * Generates a secure session identifier encoded as web safe base 64
     *
     * @param nibbleLength Length of the session identifier in nibbles.
     * @return Session identifier.
     */
    public static String genBase64Session(int nibbleLength) {
	byte[] random = generateRandom(nibbleLength);
	return ByteUtils.toWebSafeBase64String(random);
    }

    /**
     * Generates a secure session identifier in hex format.
     *
     * @return Session identifier.
     */
    public static String genHexSession() {
	return genHexSession(32);
    }

    /**
     * Generates a secure session identifier in hex format.
     *
     * @param nibbleLength Length of the session identifier in nibbles.
     * @return Session identifier.
     */
    public static String genHexSession(int nibbleLength) {
	return generateRandomHex(nibbleLength);
    }


    /**
     * Generates a secure session identifier.
     *
     * @return Session identifier.
     * @deprecated Replaced by {@link #genHexSession()}
     */
    @Deprecated
    public static String generateSessionID() {
	return genHexSession();
    }

    /**
     * Generates a secure session identifier.
     *
     * @param nibbleLength Length of the session identifier in nibbles.
     * @return Session identifier.
     * @deprecated Replaced by {@link #genHexSession(int)}
     */
    @Deprecated
    public static String generateSessionID(int nibbleLength) {
	return genHexSession(nibbleLength);
    }

    /**
     * Generates a UUID.
     * Using Java UUID and adds the prefix 'urn:uuid:'.
     *
     * @return UUID urn.
     */
    public static String generateUUID() {
	String uuid = UUID.randomUUID().toString();
	return "urn:uuid:" + uuid;
    }

    /**
     * Generates a secure random hex string.
     *
     * @param nibbleLength Length of the random in nibbles.
     * @return Secure random hex string.
     */
    public static String generateRandomHex(int nibbleLength) {
	return ByteUtils.toHexString(generateRandom(nibbleLength));
    }

    /**
     * Generates a secure random value.
     * Using 'java.security.SecureRandom'. The random instance is reseeded with a counter and the current system time in
     * order to provide better random numbers.
     *
     * @param nibbleLength Length of the random in nibbles
     * @return Secure random value
     */
    public static byte[] generateRandom(int nibbleLength) {
	if (nibbleLength < 1) {
	    return null;
	}

	nibbleLength = (nibbleLength / 2 + nibbleLength % 2);

	byte[] randomBytes = new byte[nibbleLength];
	reseed();
	rand.nextBytes(randomBytes);

	return randomBytes;
    }

}
