package org.openecard.client.common.ifd;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class VirtualPinResult {

    private final VirtualPinResultType type;
    private final String pin;

    public VirtualPinResult(VirtualPinResultType type) {
	this.type = type;
	this.pin = null;
    }

    public VirtualPinResult(String pin) {
	this.type = null;
	this.pin = pin;
    }

    public String getPin() {
	return pin;
    }

    public boolean isTimeout() {
	return (type == null) ? false : type.equals(VirtualPinResultType.TIMEOUT);
    }

    public boolean isCancelled() {
	return (type == null) ? false : type.equals(VirtualPinResultType.CANCELLED);
    }

}
