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
package org.openecard.client.sal.protocol.eac.apdu;

import org.openecard.client.common.apdu.PerformSecurityOperation;


/**
 * Implements a PSO:VerifyCertificate APDU for Terminal Authentication.
 * See BSI-TR-03110, version 2.10, part 3, section B.11.5.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PSOVerifyCertificate extends PerformSecurityOperation {

    /**
     * Creates a new PSO:VerifyCertificate APDU.
     *
     * @param certificate Certificate
     */
    public PSOVerifyCertificate(byte[] certificate) {
	setP2((byte) 0xBE);
	setData(certificate);
    }

}
