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
import org.openecard.client.common.ECardConstants;
import org.openecard.client.control.module.tctoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.testng.Assert.fail;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class RichClientTest {

    private static final Logger logger = LoggerFactory.getLogger(RichClient.class.getName());
    private static List<TCToken> tokens;
    private static String tokenURI = "https://willow.mtg.de/eid-server-demo-app/result/request.html";

    @BeforeMethod
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
		throw new TCTokenException("TCToken not available");
	    }

	    // Verify the TCToken
	    TCTokenVerifier ver = new TCTokenVerifier(tokens.get(0));
	    ver.verify();
	} catch (Exception e) {
	    logger.error(e.getMessage());
	    fail(e.getMessage());
	}
    }

    @Test(enabled = !true)
    public void testMain() {
	try {
	    RichClient client = RichClient.getInstance();
	    // Wait some seconds until the client comes up
	    Thread.sleep(2500);

	    TCTokenRequest applicationRequest = new TCTokenRequest();
	    applicationRequest.setTCToken(tokens.get(0));
	    TCTokenResponse applicationReponse = (TCTokenResponse) client.request(applicationRequest);

	    if (!applicationReponse.getResult().getResultMajor().equals(ECardConstants.Major.OK)) {
		fail(applicationReponse.getResult().getResultMajor());
	    }
	} catch (Exception e) {
	    logger.error("Exception", e);
	    fail(e.getMessage());
	}
    }

}
