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
 * PERFORM SECURITY OPERATION Command
 * See ISO/IEC 7816-8 Section 11
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PerformSecurityOperation extends CardCommandAPDU {

    /**
     * PERFORM SECURITY OPERATION command instruction byte
     */
    private static final byte PERFORM_SECURITY_OPERATION_INS = (byte) 0x2A;

    /**
     * Creates a new PERFORM SECURITY OPERATION Command.
     */
    public PerformSecurityOperation() {
	super(x00, PERFORM_SECURITY_OPERATION_INS, x00, x00);
    }

}
