package org.openecard.client.transport.paos;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import org.safehaus.uuid.EthernetAddress;
import org.safehaus.uuid.NativeInterfaces;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class MessageGenerator {

    private static EthernetAddress addr = null;

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
	myMsg = generateUUID();
	return myMsg;
    }

    private static String generateUUID() {
	// get mac if not yet present
	MessageGenerator.getMAC();
	UUIDGenerator gen = UUIDGenerator.getInstance();
	// do we have a net address?
	UUID uuid = (addr == null) ? gen.generateTimeBasedUUID() : gen.generateTimeBasedUUID(addr);
	String result = uuid.toString();
	// add urn prefix
	result = "urn:uuid:" + result;
	return result;
    }

    private static void getMAC() {
	if (addr == null) {
	    try {
		addr = NativeInterfaces.getPrimaryInterface();
	    } catch (Throwable t) {
		System.err.println("JUD couldn't retrieve MAC of primary network interface.");
	    }
	    // break out if a MAC is found
	    if (addr != null) {
		return;
	    }
	    try {
		Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface iface : Collections.list(ifaces)) {
		    if (iface.isUp() && !iface.isLoopback() && !iface.isVirtual()) {
			byte[] rawAddr = iface.getHardwareAddress();
			if (rawAddr == null) { // if address doesn't exist or isn't accessible
			    continue;
			}
			EthernetAddress newAddr = new EthernetAddress(rawAddr);
			addr = newAddr;
			return;
		    }
		}
	    } catch (SocketException ex) {
		System.err.println("Error while reading information about network interfaces.");
	    } catch (Throwable t) {
                // something went horribly wrong
            }
	    System.err.println("No network address based uuids available. Falling back to time-based uuids.");
	}
    }

}
