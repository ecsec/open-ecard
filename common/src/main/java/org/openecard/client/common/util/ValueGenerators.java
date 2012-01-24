package org.openecard.client.common.util;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ValueGenerators {

    public static String generatePSK() {
	String psk = generateSecureRandomHex(32);
	return psk;
    }

    public static String generateSessionID() {
	String session = generateRandomHex(28);
	return session;
    }

    public static String generateRandomHex(int len) {
	StringBuilder result = new StringBuilder(len);
	Random rand = new Random();
	for (int i = 0; i < len; i++) {
	    int num = Math.abs(rand.nextInt()) % 16;
	    char c = Character.forDigit(num, 16);
	    result.append(c);
	}
	return result.toString();
    }

    public static String generateSecureSessionID() {
	String session = generateSecureRandomHex(28);
	return session;
    }

    public static String generateSecureRandomHex(int len) {
	if (len <= 0) {
	    return "";
	}

	byte[] randomBytes = new byte[len];
	SecureRandom rand = new SecureRandom();

	rand.nextBytes(randomBytes);
	return Helper.convByteArrayToString(randomBytes);
    }

    public static String generateUUID() {
        String uuid = UUID.randomUUID().toString();
        return "urn:uuid:" + uuid;
    }

}
