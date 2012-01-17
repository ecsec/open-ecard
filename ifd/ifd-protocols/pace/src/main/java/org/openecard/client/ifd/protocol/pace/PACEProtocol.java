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
package org.openecard.client.ifd.protocol.pace;

import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import org.openecard.client.common.ifd.Protocol;
import org.openecard.client.gui.UserConsent;
import org.openecard.ws.IFD;


/**
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public class PACEProtocol implements Protocol {

    @Override
    public EstablishChannelResponse establish(EstablishChannel req, IFD ifd, UserConsent gui) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] applySM(byte[] commandAPDU) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] removeSM(byte[] responseAPDU) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
