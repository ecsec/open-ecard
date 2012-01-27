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

package org.openecard.client.common.ifd;

import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import org.openecard.client.gui.UserConsent;
import org.openecard.ws.IFD;

/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface Protocol {

    /**
     * Perform protocol and thereby set up a secure messaging channel.
     *
     * @param req Request data needed for the protocol
     * @param ifd IFD instance to perform commands on the terminal
     * @param gui UserConsent GUI which can be used to get secrets (e.g. PIN) from the user
     * @return Protocol response data
     */
    public EstablishChannelResponse establish(EstablishChannel req, IFD ifd, UserConsent gui);

    /**
     * Filter function to perform secure messaging after the protocol has been established.<br/>
     * Apply secure messaging encryption to APDU.
     *
     * @param commandAPDU Command APDU which should be encrypted
     * @return Command APDU which is encrypted
     */
    public byte[] applySM(byte[] commandAPDU);

    /**
     * Filter function to perform secure messaging after the protocol has been established.<br/>
     * Remove secure messaging encryption from APDU.
     *
     * @param responseAPDU Response APDU which should be decrypted
     * @return Response APDU which is encrypted
     */
    public byte[] removeSM(byte[] responseAPDU);

}
