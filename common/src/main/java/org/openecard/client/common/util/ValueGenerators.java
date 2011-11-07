package org.openecard.client.common.util;

import java.util.Random;
import java.util.UUID;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ValueGenerators {

    public static String generatePSK() {
	String psk = generateRandomHex(32);
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

    public static String generateUUID() {
        String uuid = UUID.randomUUID().toString();
        return "urn:uuid:" + uuid;
    }

}
