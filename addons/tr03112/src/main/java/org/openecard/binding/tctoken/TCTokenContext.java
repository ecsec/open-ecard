/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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

package org.openecard.binding.tctoken;

import generated.TCTokenType;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to fetch a TCToken and return it as @{link TCTokenType}.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenContext extends ResourceContext {

    private static final Logger logger = LoggerFactory.getLogger(TCTokenContext.class);
    private final TCTokenType token;

    private TCTokenContext(TCTokenType token, ResourceContext base) {
	super(base.getTlsClient(), base.getTlsClientProto(), base.getCerts());
	this.token = token;
    }

    public TCTokenType getToken() {
	return token;
    }

    public static TCTokenContext generateTCToken(URL tcTokenURL) throws TCTokenException, IOException,
	    ResourceException, ValidationError, CommunicationError {
	// Get TCToken from the given url
	ResourceContext ctx = ResourceContext.getStream(tcTokenURL);
	return generateTCToken(ctx.getData(), ctx);
    }

    public static TCTokenContext generateTCToken(String data) throws TCTokenException, CommunicationError {
	return generateTCToken(data, new ResourceContext(null, null, Collections.EMPTY_LIST));
    }

    private static TCTokenContext generateTCToken(String data, ResourceContext base) throws TCTokenException,
	    CommunicationError {
	// FIXME: Hack
	data = TCTokenHacks.fixObjectTag(data);
	// FIXME: Hack
	data = TCTokenHacks.fixPathSecurityParameters(data);
	logger.debug("Cleaned up TCToken:\n{}", data);

	// Parse the TCToken
	TCTokenParser parser = new TCTokenParser();
	List<TCTokenType> tokens = parser.parse(data);

	if (tokens.isEmpty()) {
	    throw new TCTokenException("TCToken not available");
	}

	// Verify the TCToken
	TCTokenType token = tokens.get(0);
	TCTokenVerifier ver = new TCTokenVerifier(token, base);
	boolean isError = ver.isErrorToken();
	if (isError) {
	    // TODO: find out what is the correct minor type
	    String msg = "eService indicated an error.";
	    throw new CommunicationError(token.getCommunicationErrorAddress(), null, msg);
	}

	ver.verify();

	return new TCTokenContext(token, base);
    }

}
