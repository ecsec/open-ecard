/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.control.client;

import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.control.ControlException;


/**
 * Implements a common response for client interaction.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public abstract class ClientResponse {

    private Result result = WSHelper.makeResultOK();

    /**
     * Returns the result of the client request.
     *
     * @return Result
     */
    public Result getResult() {
	return result;
    }

    /**
     * Sets the result of the client request.
     *
     * @param result
     */
    public void setResult(Result result) {
	this.result = result;
    }

    /**
     * Checks the result if an error or a warning occurred.
     *
     * @throws ControlException If an error or a warning occurred
     */
    public void checkResult() throws ControlException {
	if (this.result.getResultMajor().equals(ECardConstants.Major.ERROR)
		|| this.result.getResultMajor().equals(ECardConstants.Major.WARN)) {
	    throw new ControlException();
	}
    }

}
