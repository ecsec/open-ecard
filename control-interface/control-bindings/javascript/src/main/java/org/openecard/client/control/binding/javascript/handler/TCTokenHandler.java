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

package org.openecard.client.control.binding.javascript.handler;

import java.net.URL;
import java.net.URLDecoder;
import org.openecard.client.control.ControlException;
import org.openecard.client.control.client.ClientRequest;
import org.openecard.client.control.client.ClientResponse;
import org.openecard.client.control.client.ControlListeners;
import org.openecard.client.control.module.tctoken.TCToken;
import org.openecard.client.control.module.tctoken.TCTokenFactory;
import org.openecard.client.control.module.tctoken.TCTokenRequest;
import org.openecard.client.control.module.tctoken.TCTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenHandler extends ControlJavaScriptHandler {

    private static final Logger logger = LoggerFactory.getLogger(TCTokenHandler.class);

    /**
     * Create a new TCTokenHandler.
     *
     * @param listeners ControlListeners
     */
    public TCTokenHandler(ControlListeners listeners) {
	super("eID-Client", listeners);
    }

    @Override
    public ClientRequest handleRequest(Object[] data) throws ControlException, Exception {
	try {
	    TCTokenRequest tcTokenRequest = new TCTokenRequest();

	    for (int i = 0; i < data.length; i++) {
		String value = (String) data[i];

		switch (i) {
		    case 0:
			// TCTokenURL
			value = URLDecoder.decode(value, "UTF-8");
			TCToken token = TCTokenFactory.generateTCToken(new URL(value));
			tcTokenRequest.setTCToken(token);
			break;
		    case 1:
			// ContextHandle
			tcTokenRequest.setContextHandle(value);
			break;
		    case 2:
			// IFDName
			value = URLDecoder.decode(value, "UTF-8");
			tcTokenRequest.setIFDName(value);
			break;
		    case 3:
			// SlotIndex
			tcTokenRequest.setSlotIndex(value);
			break;
		    default:
			break;
		}
	    }
	    return tcTokenRequest;
	} catch (Exception e) {
	    logger.error("Exception", e);
	    return null;
	}
    }

    @Override
    public Object[] handleResponse(ClientResponse clientResponse) throws ControlException, Exception {
	if (clientResponse instanceof TCTokenResponse) {
	    TCTokenResponse response = (TCTokenResponse) clientResponse;
	    return new Object[]{response.getResult(), response.getRefreshAddress()};
	}
	return null;
    }

}
