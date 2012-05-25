/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg
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
package org.openecard.client.sal.protocol.eac;

import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.Protocol;
import org.openecard.client.gui.UserConsent;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class EACProtocol extends Protocol {

    /**
     * Creates a new EACProtocol.
     *
     * @param dispatcher Dispatcher
     * @param gui GUI
     */
    public EACProtocol(Dispatcher dispatcher, UserConsent gui) {
	steps.add(new PACEStep(dispatcher, gui));
	steps.add(new TerminalAuthenticationStep(dispatcher));
	steps.add(new ChipAuthenticationStep(dispatcher));
    }
}
