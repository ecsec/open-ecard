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

import org.openecard.binding.tctoken.ex.InvalidTCTokenElement;
import org.openecard.binding.tctoken.ex.InvalidTCTokenUrlException;
import org.openecard.binding.tctoken.ex.InvalidTCTokenException;
import org.openecard.binding.tctoken.ex.TCTokenRetrievalException;
import org.openecard.binding.tctoken.ex.SecurityViolationException;
import org.openecard.binding.tctoken.ex.AuthServerException;
import org.openecard.binding.tctoken.ex.InvalidRedirectUrlException;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import org.openecard.binding.tctoken.ex.InvalidAddressException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to fetch a TCToken.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public class TCTokenContext extends ResourceContext {

    private static final Logger logger = LoggerFactory.getLogger(TCTokenContext.class);
    private final TCToken token;

    private TCTokenContext(TCToken token, ResourceContext base) {
	super(base.getTlsClient(), base.getTlsClientProto(), base.getCerts());
	this.token = token;
    }

    public TCToken getToken() {
	return token;
    }

    public static TCTokenContext generateTCToken(URL tcTokenURL) throws InvalidTCTokenException, AuthServerException,
	    InvalidRedirectUrlException, InvalidTCTokenElement, InvalidTCTokenUrlException, SecurityViolationException,
	    InvalidAddressException {
	// Get TCToken from the given url
	try {
	    ResourceContext ctx = ResourceContext.getStream(tcTokenURL);
	    return generateTCToken(ctx.getData(), ctx);
	} catch (IOException | ResourceException | ValidationError ex) {
	    throw new TCTokenRetrievalException("Failed to retrieve the TCToken.", ex);
	}
    }

    public static TCTokenContext generateTCToken(String data) throws InvalidTCTokenException, AuthServerException,
	    InvalidRedirectUrlException, InvalidTCTokenElement, InvalidTCTokenUrlException, SecurityViolationException {
	return generateTCToken(data, new ResourceContext(null, null, Collections.EMPTY_LIST));
    }

    private static TCTokenContext generateTCToken(String data, ResourceContext base) throws InvalidTCTokenException,
	    AuthServerException, InvalidRedirectUrlException, InvalidTCTokenElement, InvalidTCTokenUrlException,
	    SecurityViolationException {
	// FIXME: Hack
	data = TCTokenHacks.fixObjectTag(data);
	// FIXME: Hack
	data = TCTokenHacks.fixPathSecurityParameters(data);
	logger.debug("Cleaned up TCToken:\n{}", data);

	// Parse the TCToken
	TCTokenParser parser = new TCTokenParser();
	List<TCToken> tokens = parser.parse(data);

	if (tokens.isEmpty()) {
	    throw new InvalidTCTokenException("No TCToken found in the given data.");
	}

	// Verify the TCToken
	TCToken token = tokens.get(0);
	TCTokenVerifier ver = new TCTokenVerifier(token, base);
	if (ver.isErrorToken()) {
	    // TODO: find out what is the correct minor type
	    String minor = "";
	    String msg = "eService indicated an error.";
	    throw new AuthServerException(token.getErrorRedirectAddress(minor), msg);
	}

	ver.verify();

	return new TCTokenContext(token, base);
    }

}
