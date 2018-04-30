/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.addons.cg.impl;

import org.openecard.addon.bind.AuxDataKeys;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.addons.cg.ex.InvalidRedirectUrlException;
import org.openecard.addons.cg.tctoken.TCToken;


/**
 * Implements a TCTokenResponse.
 *
 * @author Tobias Wich
 */
public class ChipGatewayResponse extends BindingResult {
    
    private TCToken token;

    public void setToken(TCToken token) {
        this.token = token;
    }
    
    /**
     * Completes the response, so that it can be used in the binding.
     * The values extended include result code, result message and the redirect address.
     */
    public void finishResponse() throws InvalidRedirectUrlException {
	setResultCode(BindingResultCode.REDIRECT);
	getAuxResultData().put(AuxDataKeys.REDIRECT_LOCATION, token.finalizeOkAddress());
    }

}
