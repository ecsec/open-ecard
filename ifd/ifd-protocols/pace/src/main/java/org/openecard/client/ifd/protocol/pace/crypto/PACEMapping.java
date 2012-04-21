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
package org.openecard.client.ifd.protocol.pace.crypto;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public abstract class PACEMapping {

    protected PACEDomainParameter pdp;

    /**
     * Creates an new mapping for PACE.
     *
     * @param pdp PACEDomainParameter
     */
    protected PACEMapping(PACEDomainParameter pdp) {
	this.pdp = pdp;
    }

    /**
     * Perform the PACE mapping.
     *
     * @param keyPICC Key from PICC
     * @param keyPCD Key from PCD
     * @return PACEDomainParameter
     */
    public abstract PACEDomainParameter map(byte[] keyPICC, byte[] keyPCD);

}
