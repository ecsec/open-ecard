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

package org.openecard.control.binding.javascript.handler;

import generated.TCTokenType;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;
import org.openecard.bouncycastle.crypto.tls.Certificate;
import org.openecard.common.util.Pair;
import org.openecard.control.ControlException;
import org.openecard.control.module.tctoken.GenericTCTokenHandler;
import org.openecard.control.module.tctoken.TCTokenFactory;
import org.openecard.control.module.tctoken.TCTokenRequest;
import org.openecard.control.module.tctoken.TCTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class JavaScriptTCTokenHandler extends JavaScriptControlHandler {

    private static final Logger logger = LoggerFactory.getLogger(JavaScriptTCTokenHandler.class);

    private final GenericTCTokenHandler genericTCTokenHandler;

    /**
     * Create a new JavaScriptTCTokenHandler.
     *
     *  @param genericTCTokenHandler to handle the generic part of the TCToken request
     *
     */
    public JavaScriptTCTokenHandler(GenericTCTokenHandler genericTCTokenHandler) {
	super("eID-Client");
	this.genericTCTokenHandler = genericTCTokenHandler;
    }

    @Override
    public Object[] handle(Map request) {
	if (logger.isDebugEnabled()) {
	    StringBuilder b = new StringBuilder(2048);
	    Iterator<Map.Entry> i = request.entrySet().iterator();
	    while (i.hasNext()) {
		Map.Entry e = i.next();
		b.append("\n '").append(e.getKey()).append("' : '").append(e.getValue()).append("'");
	    }
	    logger.debug("JavaScript request: {}", b.toString());
	    logger.debug("JavaScript request handled by: {}", getClass().getName());
	}
	try {
	    TCTokenRequest tcTokenRequest = handleRequest(request);
	    TCTokenResponse response = genericTCTokenHandler.handleActivate(tcTokenRequest);
	    return handleResponse(response);
	} catch (ControlException e) {
	    // TODO
	    throw e;
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    return null;
	}
    }

    /**
     * Extracts the TCTokenRequest from the request-data.
     * @param data the request data
     * @return the extracted TCTokenRequest or null if an error occurred
     */
    private TCTokenRequest handleRequest(Map data) {
	try {
	    TCTokenRequest tcTokenRequest = new TCTokenRequest();

	    // TODO: rewrite code so that it is safer
	    Iterator i = data.entrySet().iterator();
	    while (i.hasNext()) {
		Map.Entry e = (Map.Entry) i.next();
		// check content
		if ("tcTokenURL".equals(e.getKey())) {
		    // TCTokenURL
		    String value = e.getValue().toString();
		    Pair<TCTokenType, Certificate> token = TCTokenFactory.generateTCToken(new URL(value));
		    tcTokenRequest.setTCToken(token.p1);
		    tcTokenRequest.setCertificate(token.p2);
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

    /**
     * Builds the Javascript response data from the TCTokenResponse.
     * @param tcTokenResponse the response to build the response data
     * @return response data
     */
    private Object[] handleResponse(TCTokenResponse tcTokenResponse)  {
	TCTokenResponse response = tcTokenResponse;
	return new Object[] { response.getResult(), response.getRefreshAddress() };
    }

}
