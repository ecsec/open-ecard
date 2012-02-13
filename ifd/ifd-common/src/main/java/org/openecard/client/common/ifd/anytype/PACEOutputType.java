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
    private byte[] efCardAccess;
    private byte[] currentCAR;
    private byte[] previousCAR;
    private byte[] idpicc;
    private byte retryCounter;

    protected PACEOutputType(AuthDataMap authMap) {
        this.authMap = authMap;
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

    public void setIDPICC(byte[] idpicc) {
        this.idpicc = idpicc;
    }
    
    public void setRetryCounter(byte counter){
	this.retryCounter = counter;
    }

    public DIDAuthenticationDataType getAuthDataType() {
        AuthDataResponse authResponse = authMap.createResponse(new iso.std.iso_iec._24727.tech.schema.PACEOutputType());
        authResponse.addElement("RetryCounter", String.valueOf(retryCounter));
        authResponse.addElement("EFCardAccess", ByteUtils.toHexString(efCardAccess));
        if (currentCAR != null) {
            authResponse.addElement("CARcurr", ByteUtils.toHexString(currentCAR));
        }
        if (previousCAR != null) {
            authResponse.addElement("CARprev", ByteUtils.toHexString(previousCAR));
        }
        if (idpicc != null) {
            authResponse.addElement("IDPICC", ByteUtils.toHexString(idpicc));
        }
        return authResponse.getResponse();
    }

}
