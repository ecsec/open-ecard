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
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.ifd.scio.reader;

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
    private byte currentCARLength;
    private byte[] currentCAR;
    private byte previousCARLength;
    private byte[] previousCAR;
    private short idiccLength;
    private byte[] idicc;

    public EstablishPACEResponse(byte[] response) {
	int dataLen = response.length;
	int idx = 4;
	// read status
	statusBytes = new byte[]{response[0], response[1]};
	// read card access (&0xFF produces unsigned numbers)
	efCardAccessLength = (short) ((response[2]&0xFF) + ((response[3]&0xFF) << 8));
	if (efCardAccessLength > 0) {
	    efCardAccess = Arrays.copyOfRange(response, idx, idx + efCardAccessLength);
	    idx += efCardAccessLength;
	} else {
	    efCardAccess = new byte[0];
	}
	// read car
	if (dataLen > idx + 1) {
	    currentCARLength = (byte) (response[idx]&0xFF);
	    idx++;
	    if (currentCARLength > 0) {
		currentCAR = Arrays.copyOfRange(response, idx, idx + currentCARLength);
		idx += currentCARLength;
	    }
	}
	// read car prev
	if (dataLen > idx + 1) {
	    previousCARLength = (byte) (response[idx]&0xFF);
	    idx++;
	    if (previousCARLength > 0) {
		previousCAR = Arrays.copyOfRange(response, idx, idx + previousCARLength);
		idx += previousCARLength;
	    }
	}
	// read id icc
	if (dataLen > idx + 2) {
	    idiccLength = (short) ((response[idx]&0xFF) + ((response[idx + 1]&0xFF) << 8));
	    idx += 2;
	    if (idiccLength > 0) {
		idicc = Arrays.copyOfRange(response, idx, idx + idiccLength);
		idx += idiccLength;
	    }
	}
    }

    public byte[] getStatus() {
	return this.statusBytes;
    }

    public byte getRetryCounter() {
	// TODO: verify that retry counter is extracted from 63CX statusword
	if (statusBytes[0] == 0x63 && (statusBytes[1] & 0xF0) == 0xC0) {
	    return (byte) (statusBytes[1] & 0x0F);
	} else {
	    // TODO: check if 3 is ok as default and if any other statuswords must be considered here
	    // default 3 seems to make sense
	    return 3;
	}
    }

    public boolean hasEFCardAccess() {
	return efCardAccessLength > 0;
    }
    public byte[] getEFCardAccess() {
	return this.efCardAccess;
    }

    public boolean hasCurrentCAR() {
	return currentCARLength > 0;
    }
    public byte[] getCurrentCAR() {
	return this.currentCAR;
    }

    public boolean hasPreviousCAR() {
	return previousCARLength > 0;
    }
    public byte[] getPreviousCAR() {
	return previousCAR;
    }

    public boolean hasIDICC() {
	return idiccLength > 0;
    }
    public byte[] getIDICC() {
	return idicc;
    }

}
