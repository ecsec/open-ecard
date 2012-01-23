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

package org.openecard.client.scio;

import javax.smartcardio.CardTerminals;
import org.openecard.client.common.ifd.TerminalFactory;

/**
 * Seek specific implementation of the TerminalFactory
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
public class SeekFactory implements TerminalFactory {

    @Override
    public String getType() {
	return "seek for android";
    }

    @Override
    public CardTerminals terminals() {
	try {
	    return SeekTerminals.getInstance();
	} catch (Exception e) {
	    return null;
	}
    }

}
