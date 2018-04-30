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

import org.openecard.addons.cg.activate.TlsConnectionHandler;
import org.openecard.addons.cg.ex.VersionTooOld;
import java.util.concurrent.Callable;
import org.openecard.addon.Context;
import org.openecard.addons.cg.ex.AuthServerException;
import org.openecard.addons.cg.ex.ChipGatewayDataError;
import org.openecard.addons.cg.ex.ChipGatewayUnknownError;
import org.openecard.addons.cg.ex.ConnectionError;
import org.openecard.addons.cg.ex.InvalidRedirectUrlException;
import org.openecard.addons.cg.ex.InvalidTCTokenElement;
import org.openecard.addons.cg.ex.ResultMinor;
import org.openecard.addons.cg.tctoken.TCToken;
import static org.openecard.addons.cg.ex.ErrorTranslations.*;
import org.openecard.ws.chipgateway.TerminateType;


/**
 *
 * @author Tobias Wich
 */
public class ChipGatewayTask implements Callable<TerminateType> {

    private final TCToken token;
    private final Context ctx;

    public ChipGatewayTask(TCToken token, Context ctx) {
	this.token = token;
	this.ctx = ctx;
    }

    @Override
    public TerminateType call() throws ConnectionError, VersionTooOld, InvalidTCTokenElement,
	ChipGatewayDataError, InvalidRedirectUrlException, AuthServerException, ChipGatewayUnknownError {
	TlsConnectionHandler tlsHandler = new TlsConnectionHandler(token);
	tlsHandler.setUpClient();

	ChipGateway cg = new ChipGateway(tlsHandler, token, ctx);
	TerminateType result = cg.sendHello();

	if (ChipGatewayStatusCodes.isError(result.getResult())) {
	    throw new ChipGatewayUnknownError(token.finalizeErrorAddress(ResultMinor.SERVER_ERROR),
		    SERVER_SENT_ERROR, result.getResult());
	}

	return result;
    }

}
