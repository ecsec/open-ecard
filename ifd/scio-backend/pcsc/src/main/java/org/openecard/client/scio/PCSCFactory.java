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

package org.openecard.client.scio;

import java.security.NoSuchAlgorithmException;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;


/**
 * Proxy and abstracted Factory for SCIO PC/SC driver.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PCSCFactory implements org.openecard.client.common.ifd.TerminalFactory {

    private final TerminalFactory factory;

    public PCSCFactory() throws NoSuchAlgorithmException {
        factory = TerminalFactory.getInstance("PC/SC", null);
    }


    @Override
    public String getType() {
	return factory.getType();
    }

    @Override
    public CardTerminals terminals() {
	return factory.terminals();
    }

}
