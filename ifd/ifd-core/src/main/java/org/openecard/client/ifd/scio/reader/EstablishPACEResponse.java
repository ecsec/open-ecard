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
        // read card access
        efCardAccessLength = (short) (response[2] + (response[3] << 8));
        if (efCardAccessLength > 0) {
            efCardAccess = Arrays.copyOfRange(response, idx, idx + efCardAccessLength);
            idx += efCardAccessLength;
        } else {
            efCardAccess = new byte[0];
        }
        // read car
        if (dataLen > idx + 1) {
            currentCARLength = response[idx];
            idx++;
            if (currentCARLength > 0) {
                currentCAR = Arrays.copyOfRange(response, idx, idx + currentCARLength);
                idx += currentCARLength;
            }
        }
        // read car prev
        if (dataLen > idx + 1) {
            previousCARLength = response[idx];
            idx++;
            if (previousCARLength > 0) {
                previousCAR = Arrays.copyOfRange(response, idx, idx + previousCARLength);
                idx += previousCARLength;
            }
        }
        // read id icc
        if (dataLen > idx + 2) {
            idiccLength = (short) (response[idx] + (response[idx + 1] << 8));
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
