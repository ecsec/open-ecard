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

import generated.TCTokenType;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;
import org.openecard.client.control.ControlException;
import org.openecard.client.control.client.ClientRequest;
import org.openecard.client.control.client.ClientResponse;
import org.openecard.client.control.client.ControlListeners;
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
    public ClientRequest handleRequest(Map data) throws ControlException, Exception {
	try {
	    TCTokenRequest tcTokenRequest = new TCTokenRequest();

	    // TODO: rewrite code so that it is safer
	    Iterator i = data.entrySet().iterator();
	    while (i.hasNext()) {
		Map.Entry e = (Map.Entry) i.next();
		// check content
		if ("tcTokenURL".equals(e.getKey())) {
		    // TCTokenURL
		    String value = URLDecoder.decode(e.getValue().toString(), "UTF-8");
		    TCTokenType token = TCTokenFactory.generateTCToken(new URL(value));
		    tcTokenRequest.setTCToken(token);
		} else if ("contextHandle".equals(e.getKey())) {
		    // ContextHandle
		    tcTokenRequest.setContextHandle(e.getValue().toString());
		} else if ("ifdName".equals(e.getKey())) {
		    // IFDName
		    String value = URLDecoder.decode(e.getValue().toString(), "UTF-8");
		    tcTokenRequest.setIFDName(value);
		} else if ("slotIndex".equals(e.getKey())) {
		    // SlotIndex
		    tcTokenRequest.setSlotIndex(e.getValue().toString());
		}
	    }

	    return tcTokenRequest;
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
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
