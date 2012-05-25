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

package org.openecard.client.sal.protocol.eac.anytype;

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import org.openecard.client.common.sal.anytype.AuthDataMap;
import org.openecard.client.common.sal.anytype.AuthDataResponse;
import org.openecard.client.common.util.ByteUtils;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PACEOutputType {

    public static final String RETRY_COUNTER = "RetryCounter";
    public static final String EF_CARD_ACCESS = "EFCardAccess";
    public static final String CURRENT_CAR = "CARcurr";
    public static final String PREVIOUS_CAR = "CARprev";
    public static final String ID_PICC = "IDPICC";

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
	authResponse.addElement(RETRY_COUNTER, String.valueOf(retryCounter));
	authResponse.addElement(EF_CARD_ACCESS, ByteUtils.toHexString(efCardAccess));
	if (currentCAR != null) {
	    authResponse.addElement(CURRENT_CAR, ByteUtils.toHexString(currentCAR));
	}
	if (previousCAR != null) {
	    authResponse.addElement(PREVIOUS_CAR, ByteUtils.toHexString(previousCAR));
	}
	if (idpicc != null) {
	    authResponse.addElement(ID_PICC, ByteUtils.toHexString(idpicc));
	}
	return authResponse.getResponse();
    }

}
