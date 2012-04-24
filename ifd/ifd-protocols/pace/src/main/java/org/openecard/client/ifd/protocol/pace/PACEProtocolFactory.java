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

import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.ifd.Protocol;
import org.openecard.client.common.ifd.ProtocolFactory;

/**
 * Implements a ProtocolFactory for the PACE protocol.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class PACEProtocolFactory implements ProtocolFactory {

    @Override
    public String getProtocol() {
	return ECardConstants.Protocol.PACE;
    }

    @Override
    public Protocol createInstance() {
	return new PACEProtocol();
    }
}
