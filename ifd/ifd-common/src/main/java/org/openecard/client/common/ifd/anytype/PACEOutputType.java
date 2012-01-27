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

package org.openecard.client.common.ifd.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import org.openecard.client.common.util.ByteUtils;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PACEOutputType {

    private final AuthDataMap authMap;
    private byte[] statusBytes;
    private byte[] efCardAccess;
    private byte[] currentCAR;
    private byte[] previousCAR;
    private byte[] idpicc;

    protected PACEOutputType(AuthDataMap authMap) {
        this.authMap = authMap;
    }

    public void setStatusbytes(byte[] statusBytes) {
        this.statusBytes = statusBytes;
    }

    public void setEFCardAccess(byte[] efCardAccess) {
        this.efCardAccess = efCardAccess;
    }

    public void setCurrentCAR(byte[] car) {
        this.currentCAR = car;
    }

    public void setPreviousCAR(byte[] car) {
        this.previousCAR = car;
    }

    public void setIDICC(byte[] idicc) {
        this.idpicc = idicc;
    }

    public DIDAuthenticationDataType getAuthDataType() {
        AuthDataResponse authResponse = authMap.createResponse(new iso.std.iso_iec._24727.tech.schema.PACEOutputType());
        authResponse.addElement("StatusBytes", ByteUtils.toHexString(statusBytes));
        authResponse.addElement("EF_CardAccess", ByteUtils.toHexString(efCardAccess));
        if (currentCAR != null) {
            authResponse.addElement("CARcurr", ByteUtils.toHexString(currentCAR));
        }
        if (previousCAR != null) {
            authResponse.addElement("CARprev", ByteUtils.toHexString(previousCAR));
        }
        if (idpicc != null) {
            authResponse.addElement("IDicc", ByteUtils.toHexString(idpicc));
        }
        return authResponse.getResponse();
    }

}
