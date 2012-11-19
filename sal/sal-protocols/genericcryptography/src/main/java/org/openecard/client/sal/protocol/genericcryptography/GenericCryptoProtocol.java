/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.client.sal.protocol.genericcryptography;

import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.Protocol;


/**
 * Implements the Generic cryptography protocol.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.9.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class GenericCryptoProtocol extends Protocol {

    public GenericCryptoProtocol(Dispatcher dispatcher) {
	steps.add(new SignStep(dispatcher));
	steps.add(new DIDGetStep());
	steps.add(new DecipherStep(dispatcher));
	steps.add(new VerifySignatureStep(dispatcher));
    }

    @Override
    public boolean hasNextStep(FunctionType functionName) {
	for (int i = 0; i < steps.size(); i++) {
	    if (steps.get(i).getFunctionType().equals(functionName)) {
		super.curStep = i;
	    }
	}
	return true;
    }

    @Override
    public String toString() {
	return "Generic cryptography";
    }

}
