package org.openecard.client.gui.common;

import java.util.Random;
import org.openecard.client.common.util.ByteUtils;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class IDGenerator {

    private static Random random = new Random();
    private static byte[] randomBytes = new byte[20];

    public static String generateID() {
	random.nextBytes(randomBytes);
	return ByteUtils.toHexString(randomBytes);
    }
}
