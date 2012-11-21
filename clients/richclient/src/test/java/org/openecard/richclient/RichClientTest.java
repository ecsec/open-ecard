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

package org.openecard.richclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class RichClientTest {

    private static final Logger logger = LoggerFactory.getLogger(RichClient.class.getName());

    private static URL tcTokenURL;
    private static URL statusURL;
    private static URL waitForChangeURL;

    /**
     * Starts up the RichClient.
     */
    @BeforeMethod
    public void setUp() {
	try {
	    tcTokenURL = new URL("http", "127.0.0.1", 24727,
		   "/eID-Client?tcTokenURL=http%3A%2F%2Fopenecard-demo.vserver-001.urospace.de%2FtcToken%3Fcard-type%3Dhttp%3A%2F%2Fbsi.bund.de%2Fcif%2Fnpa.xml");
	    statusURL = new URL("http", "127.0.0.1", 24727, "/getStatus");
	    waitForChangeURL = new URL("http", "127.0.0.1", 24727, "/waitForChange");
	    RichClient client = RichClient.getInstance();
	    // Wait some seconds until the client comes up
	    Thread.sleep(2500);
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    fail(e.getMessage());
	}
    }

    /**
     * Test the Response of the RichClient to a TCTokenRequest.
     */
    @Test(enabled = false)
    public void testTCToken() {
	try {
	    HttpURLConnection urlConnection = (HttpURLConnection) tcTokenURL.openConnection();
	    getResponse(urlConnection);
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    fail(e.getMessage());
	}
    }

    /**
     * Test the Response of the RichClient to a StatusRequest.
     */
    @Test(enabled = false)
    public void testStatus() {
	try {
	    HttpURLConnection urlConnection = (HttpURLConnection) statusURL.openConnection();
	    getResponse(urlConnection);
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    fail(e.getMessage());
	}
    }

    /**
     * Test the Response of the RichClient to a WaitForChangeReuquest.
     */
    @Test(enabled = false)
    public void testWaitForChange() {
	try {
	    HttpURLConnection urlConnection = (HttpURLConnection) waitForChangeURL.openConnection();
	    getResponse(urlConnection);
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    fail(e.getMessage());
	}
    }

    /**
     * Opens the URLConnection, gets the Response and checks the ResponseCode.
     *
     * @param urlConnection the connection to open
     * @throws IOException if an I/O error occurs
     */
    private static void getResponse(HttpURLConnection urlConnection) throws IOException {
	try {
	    StringBuilder sb = new StringBuilder(8192);
	    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
	    String read = br.readLine();

	    while (read != null) {
		sb.append(read);
		read = br.readLine();
	    }

	    logger.debug(sb.toString());
	    assertTrue(checkResponseCode(urlConnection.getResponseCode()));
	} finally {
	    urlConnection.disconnect();
	}
    }

    /**
     * Check for a successful status code (2xx).
     *
     * @param code status code to be checked
     * @return true if successful, else false
     */
    private static boolean checkResponseCode(int code) {
	return ((code >= 200) && (code < 300));
    }

}
