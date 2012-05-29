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

package org.openecard.client.richclient;

import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.connector.common.ConnectorConstants;
import org.openecard.client.connector.messages.TCTokenRequest;
import org.openecard.client.connector.messages.TCTokenResponse;
import org.openecard.client.connector.tctoken.TCToken;
import org.openecard.client.connector.tctoken.TCTokenConverter;
import org.openecard.client.connector.tctoken.TCTokenException;
import org.openecard.client.connector.tctoken.TCTokenGrabber;
import org.openecard.client.connector.tctoken.TCTokenParser;
import org.openecard.client.connector.tctoken.TCTokenVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
@Ignore
public class RichClientTest {

    private static final Logger logger = LoggerFactory.getLogger(RichClient.class.getName());
    private static List<TCToken> tokens;
    private static String tokenURI = "https://willow.mtg.de/eid-server-demo-app/result/request.html";

    @Before
    public void setUp() {
	try {
	    // Get TCToken from the given URL
	    TCTokenGrabber grabber = new TCTokenGrabber();
	    String data = grabber.getResource(tokenURI);

	    //FIXME Remove me
	    TCTokenConverter converter = new TCTokenConverter();
	    data = converter.convert(data);

	    // Parse the TCToken
	    TCTokenParser parser = new TCTokenParser();
	    tokens = parser.parse(data);

	    if (tokens.isEmpty()) {
		throw new TCTokenException(ConnectorConstants.ConnectorError.TC_TOKEN_NOT_AVAILABLE.toString());
	    }

	    // Verify the TCToken
	    TCTokenVerifier ver = new TCTokenVerifier(tokens);
	    ver.verify();
	} catch (Exception e) {
	    logger.error(e.getMessage());
	    fail(e.getMessage());
	}
    }

    @Test
    public void testMain() {
	try {

	    java.util.logging.LogManager.getLogManager().reset();

	    ConsoleHandler ch = new ConsoleHandler();
	    ch.setLevel(Level.ALL);

	    LogManager.getLogger("org.openecard.client.ifd.scio.wrapper").addHandler(ch);
	    LogManager.getLogger("org.openecard.client.ifd.scio.wrapper").setLevel(Level.FINE);


	    RichClient client = RichClient.getInstance();
	    // Wait some seconds until the client comes up
	    Thread.sleep(2500);

	    LogManager.getLogger("org.openecard.client.transport.paos").addHandler(ch);
	    LogManager.getLogger("org.openecard.client.transport.paos").setLevel(Level.FINE);

	    TCTokenRequest applicationRequest = new TCTokenRequest();
	    applicationRequest.setTCToken(tokens.get(0));
	    TCTokenResponse applicationReponse = (TCTokenResponse) client.request(applicationRequest);

	    System.out.println("RICH CLIENT RESULT");
	    System.out.println(applicationReponse.getErrorMessage());
	    System.out.println(applicationReponse.getRefreshAddress());
	    System.out.println(applicationReponse.getErrorPage());


	} catch (Exception e) {
	    e.printStackTrace();
	    logger.error(e.getMessage());
	    fail(e.getMessage());
	}
    }

}
