/*
 * Copyright 2012 Tobias Wich ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
