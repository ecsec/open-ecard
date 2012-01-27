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

package org.openecard.client.transport.paos;

import org.openecard.client.common.util.ValueGenerators;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class MessageGenerator {

    private static String otherMsg = null;
    private static String myMsg = null;

    public static String getRemoteId() {
	return otherMsg;
    }

    public static boolean setRemoteId(String newId) {
	if (myMsg != null && newId.equals(myMsg)) {
	    // messages don't fit together
	    return false;
	}
	otherMsg = newId;
	return true;
    }

    public static String createNewId() {
	myMsg = ValueGenerators.generateUUID();
	return myMsg;
    }

}
