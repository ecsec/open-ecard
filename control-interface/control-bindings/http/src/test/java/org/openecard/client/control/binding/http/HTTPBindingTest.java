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

package org.openecard.client.control.binding.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public final class HTTPBindingTest {

    private static final Logger logger = LoggerFactory.getLogger(HTTPBindingTest.class);

    /**
     * Start up the TestClient.
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
	try {
	    TestClient tc = new TestClient();

	    // Wait some seconds until the SAL comes up
	    Thread.sleep(2500);
	} catch (Exception e) {
	    logger.debug(e.getMessage(), e);
	    Assert.fail();
	}
    }

    @Test(enabled = !true)
    public void testWaitForChange() throws MalformedURLException {

	// Request a "waitForChange" with GET
	URL u = new URL("http", "127.0.0.1", 24727, "/waitForChange");
	String response = httpRequest(u, false);

	if (response == null) {
	    Assert.fail("Get status failed");
	}

	logger.debug(response);
	// Request a "waitForChange" with POST
	response = httpRequest(u, true);

	// we expect response code 405, therefore response must be null
	if (response != null)
	    Assert.fail();

    }

    @Test(enabled = !true)
    public void testGetStatus() {
	try {
	    // Request a "get status" with GET
	    URL u = new URL("http", "127.0.0.1", 24727, "/getStatus");
	    String response = httpRequest(u, false);

	    if (response == null) {
		Assert.fail("Get status failed");
	    }

	    logger.debug(response);

	    // Request a "get status" with POST
	    response = httpRequest(u, true);
	    // we expect response code 405, therefore response must be null
	    if (response != null)
		Assert.fail();
	} catch (Exception e) {
	    logger.debug(e.getMessage(), e);
	    Assert.fail();
	}
    }

    @Test(enabled = !true)
    public void testeIDClient() {
	try {
	    // Request a "eID-Client"
	    URL u = new URL(
		    "http://localhost:24727/eID-Client?tcTokenURL=http%3A%2F%2Fopenecard-demo.vserver-001.urospace.de%2FtcToken%3Fcard-type%3Dhttp%3A%2F%2Fbsi.bund.de%2Fcif%2Fnpa.xml");
	    String response = httpRequest(u, false);

	    if (response == null) {
		Assert.fail("eID-Client failed");
	    }
	} catch (Exception e) {
	    logger.debug(e.getMessage(), e);
	    Assert.fail();
	}
    }

    /**
     * Performs a HTTP Request (GET or POST) to the specified URL and returns the response as String.
     * 
     * @param url
     *            URL to connect to
     * @param doPOST true for POST, false for GET
     * @return response as string
     */
    private static String httpRequest(URL url, boolean doPOST) {
	HttpURLConnection c = null;
	try {
	    c = (HttpURLConnection) url.openConnection();
	    if (doPOST) {
		c.setDoOutput(true);
		c.getOutputStream();
	    }
	    BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
	    String inputLine;
	    StringBuilder content = new StringBuilder();

	    while ((inputLine = in.readLine()) != null) {
		content.append(inputLine);
	    }
	    in.close();

	    return content.toString();
	} catch (IOException e) {
	    if (c.getErrorStream() != null) {
		try {
		    readErrorStream(c.getErrorStream());
		} catch (IOException ioe) {
		    logger.error(e.getMessage(), e);
		}
	    }
	    logger.error(e.getMessage(), e);
	    return null;
	}
    }

    /**
     * Reads the HTML Error Response from the Server.
     */
    private static void readErrorStream(InputStream errorStream) throws IOException {
	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(errorStream));
	StringBuilder stringBuilder = new StringBuilder();
	String line = null;

	while ((line = bufferedReader.readLine()) != null) {
	    stringBuilder.append(line);
	}

	logger.error("HTML Error response from server:\n{}", stringBuilder.toString());
    }

}
