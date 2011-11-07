package org.openecard.client.transport.paos;

import java.util.UUID;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class MessageGenerator {

    private static String otherMsg = null;
    private static String myMsg = null;

    public static String getRemoteId() {
	return otherMsg;
    }

    public static boolean setRemoteId(String newId) {
	if (myMsg != null && newId.equals(myMsg)) {
	    // messages don't fit together
	    return false;
	}
	otherMsg = newId;
	return true;
    }

    public static String createNewId() {
	myMsg = UUID.randomUUID().toString();
	return myMsg;
    }

}
