/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.common.apdu;

import org.openecard.client.common.apdu.common.CardCommandAPDU;


/**
 * MANAGE SECURITY ENVIRONMENT command.
 * See ISO/IEC 7816-4 Section 7.5.11.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ManageSecurityEnviroment extends CardCommandAPDU {

    /**
     * MANAGE SECURITY ENVIRONMENT command instruction byte
     */
    private static final byte COMMMAND_MSESet_AT = (byte) 0x22;

    /**
     * Creates a new MANAGE SECURITY ENVIRONMENT APDU.
     *
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     */
    public ManageSecurityEnviroment(byte p1, byte p2) {
	super(x00, COMMMAND_MSESet_AT, p1, p2);
    }

    /**
     * Creates a new MANAGE SECURITY ENVIRONMENT APDU.
     *
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     * @param data Data
     */
    public ManageSecurityEnviroment(byte p1, byte p2, byte[] data) {
	super(x00, COMMMAND_MSESet_AT, p1, p2);
	setData(data);
    }

}
