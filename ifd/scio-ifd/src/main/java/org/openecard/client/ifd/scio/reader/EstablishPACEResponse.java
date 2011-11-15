package org.openecard.client.ifd.scio.reader;

import java.util.Arrays;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class EstablishPACEResponse {

    private byte[] statusBytes;
    private short efCardAccessLength;
    private byte[] efCardAccess;
    // eID attributes
    private byte carLength;
    private byte[] car;
    private byte carPrevLength;
    private byte[] carPrev;
    private short IDiccLength;
    private byte[] IDicc;


    public EstablishPACEResponse(byte[] response) {
	int dataLen = response.length;
	int idx = 4;
	// read status
	statusBytes = new byte[] {response[0], response[1]};
	// read card access
	efCardAccessLength = (short) (response[2] + (response[3] << 8));
	if (efCardAccessLength > 0) {
	    efCardAccess = Arrays.copyOfRange(response, idx, idx+efCardAccessLength);
	    idx += efCardAccessLength;
	} else {
            efCardAccess = new byte[0];
        }
	// read car
	if (dataLen > idx+1) {
	    carLength = response[idx];
	    idx++;
	    if (carLength > 0) {
		car = Arrays.copyOfRange(response, idx, idx+carLength);
		idx += carLength;
	    }
	}
	// read car prev
	if (dataLen > idx+1) {
	    carPrevLength = response[idx];
	    idx++;
	    if (carPrevLength > 0) {
		carPrev = Arrays.copyOfRange(response, idx, idx+carPrevLength);
		idx += carPrevLength;
	    }
	}
	// read id icc
	if (dataLen > idx+2) {
	    IDiccLength = (short) (response[idx] + (response[idx+1] << 8));
	    idx += 2;
	    if (IDiccLength > 0) {
		IDicc = Arrays.copyOfRange(response, idx, idx+IDiccLength);
		idx += IDiccLength;
	    }
	}
    }


    public byte[] getStatus() {
	return this.statusBytes;
    }

    public boolean hasCardAccess() {
	return efCardAccessLength > 0;
    }
    public byte[] getCardAccess() {
	return this.efCardAccess;
    }

    public boolean hasCar() {
	return carLength > 0;
    }
    public byte[] getCar() {
	return this.car;
    }

    public boolean hasCarPrev() {
	return carPrevLength > 0;
    }
    public byte[] getCarPrev() {
	return carPrev;
    }

    public boolean hasIDicc() {
	return IDiccLength > 0;
    }
    public byte[] getIDicc() {
	return IDicc;
    }

}
