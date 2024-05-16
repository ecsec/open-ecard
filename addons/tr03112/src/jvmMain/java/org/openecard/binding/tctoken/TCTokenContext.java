/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
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

import org.openecard.binding.tctoken.ex.*;
import org.openecard.bouncycastle.tls.TlsServerCertificate;
import org.openecard.common.DynamicContext;
import org.openecard.common.util.Pair;
import org.openecard.httpcore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.openecard.binding.tctoken.ex.ErrorTranslations.*;


/**
 * Class to fetch a TCToken.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public class TCTokenContext extends ResourceContext {

    private static final Logger LOG = LoggerFactory.getLogger(TCTokenContext.class);

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
	    InvalidAddressException, UserCancellationException {
	// Get TCToken from the given url
	try {
	    ResourceContext ctx = new TrResourceContextLoader().getStream(tcTokenURL);
	    return generateTCToken(ctx.getData(), ctx);
	} catch (InsecureUrlException ex) {
	    throw new InvalidAddressException(INVALID_ADDRESS);
	} catch (InvalidRedirectChain ex) {
	    throw new InvalidAddressException(INVALID_REFRESH_ADDRESS_NOSOP);
	} catch (IOException | HttpResourceException | InvalidProxyException | ValidationError ex) {
	    throw new TCTokenRetrievalException(RETRIEVAL_FAILED, ex);
	}
    }

    private static TCTokenContext generateTCToken(String data, ResourceContext base) throws InvalidTCTokenException,
	    AuthServerException, InvalidRedirectUrlException, InvalidTCTokenElement, InvalidTCTokenUrlException,
	    SecurityViolationException, UserCancellationException {
	// correct common TCToken shortcomings
	LOG.debug("Received TCToken:\n{}", data);
	data = TCTokenHacks.fixPathSecurityParameters(data);
	LOG.debug("Cleaned up TCToken:\n{}", data);

	// Parse the TCToken
	TCTokenParser parser = new TCTokenParser();
	List<TCToken> tokens = parser.parse(data);

	if (tokens.isEmpty()) {
	    throw new InvalidTCTokenException(NO_TCTOKEN_IN_DATA);
	}

	// Verify the TCToken
	TCToken token = tokens.get(0);
	TCTokenVerifier ver = new TCTokenVerifier(token, base);
	if (ver.isErrorToken()) {
	    String minor = ResultMinor.CLIENT_ERROR;
	    throw new AuthServerException(token.getComErrorAddressWithParams(minor), ESERVICE_ERROR);
	}

	DynamicContext dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	List<Pair<URL, TlsServerCertificate>> resultPoints = base.getCerts();
	// probably just for tests
	if (! resultPoints.isEmpty()) {
	    Pair<URL, TlsServerCertificate> last = resultPoints.get(0);
	    dynCtx.put(TR03112Keys.TCTOKEN_URL, last.p1);
	}

	ver.verifyUrlToken();

	return new TCTokenContext(token, base);
    }

}
