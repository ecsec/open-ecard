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

package org.openecard.client.sal.protocol.pincompare;

import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.sal.FunctionType;
import org.openecard.client.common.sal.Protocol;


/**
 * Implements the PIN Compare protocol.
 * See TR-03112, version 1.1.2, part 7, section 4.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PINCompareProtocol extends Protocol {

    /**
     * Creates a new PINCompare protocol.
     *
     * @param dispatcher Dispatcher
     */
    public PINCompareProtocol(Dispatcher dispatcher) {
	steps.add(new DIDCreateStep(dispatcher));
	steps.add(new DIDUpdateStep(dispatcher));
	steps.add(new DIDGetStep());
	steps.add(new DIDAuthenticateStep(dispatcher));
    }


    @Override
    public boolean hasNextStep(FunctionType functionName) {
	for (int i = 0; i < steps.size(); i++) {
	    if (steps.get(i).getFunctionType().equals(functionName)) {
		super.curStep = i;
		return true;
	    }
	}
	return false;
    }

    @Override
    public String toString() {
	return "PINCompare";
    }
}
