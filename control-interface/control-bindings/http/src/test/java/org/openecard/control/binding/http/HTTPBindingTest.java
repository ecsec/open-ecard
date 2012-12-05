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

package org.openecard.control.binding.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.openecard.ws.schema.Status;
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

    private static WSMarshaller m;

    /**
     * Start up the TestClient.
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
	try {
	    TestClient tc = new TestClient();
	    m = WSMarshallerFactory.createInstance();
	    m.removeAllTypeClasses();
	    m.addXmlTypeClass(Status.class);

	    // Wait some seconds until the SAL comes up
	    Thread.sleep(2500);
	} catch (Exception e) {
	    logger.debug(e.getMessage(), e);
	    Assert.fail();
	}
    }

    /**
     * Tests the HttpStatusChangeHandler by sending:
     * 1. a get status to get the session identifier
     * 2. a get status with session identifier to set up event queue
     * 3. after 30 sec. a waitForChange
     * 4. after 45 sec. a waitForChange to see if the event queue still exists
     * 5. after 70 sec. a waitForChange to see if the event queue has correctly been removed due to timeout
     * 6. a waitForChange as POST request
     */
    @Test(enabled = !true)
    public void testWaitForChange() {
	try {
	    // Request a "get status" with GET and without optional session parameter
	    URL u = new URL("http", "127.0.0.1", 24727, "/getStatus");
	    String response = httpRequest(u, false);

	    Assert.assertNotNull(response);

	    logger.debug(response);
	    Status status = (Status) m.unmarshal(m.str2doc(response));
	    String session = status.getConnectionHandle().get(0).getChannelHandle().getSessionIdentifier();

	    // Request a "get status" with GET and with optional session parameter
	    u = new URL("http", "127.0.0.1", 24727, "/getStatus?session=" + session);
	    response = httpRequest(u, false);

	    Assert.assertNotNull(response);

	    logger.debug(response);

	    Thread.sleep(30 * 1000);
	    // Request a "waitForChange" with GET
	    u = new URL("http", "127.0.0.1", 24727, "/waitForChange?session=" + session);
	    response = httpRequest(u, false);

	    Assert.assertNotNull(response);

	    logger.debug(response);

	    Thread.sleep(45 * 1000);
	    // Request a "waitForChange" with GET
	    u = new URL("http", "127.0.0.1", 24727, "/waitForChange?session=" + session);
	    response = httpRequest(u, false);

	    Assert.assertNotNull(response);

	    logger.debug(response);

	    Thread.sleep(70 * 1000);
	    // Request a "waitForChange" with GET
	    u = new URL("http", "127.0.0.1", 24727, "/waitForChange?session=" + session);
	    response = httpRequest(u, false);
	    // we expect response code 400, therefore response must be null
	    Assert.assertNull(response);

	    // Request a "waitForChange" with POST
	    response = httpRequest(u, true);

	    // we expect response code 405, therefore response must be null
	    Assert.assertNull(response);
	} catch (Exception e) {
	    logger.debug(e.getMessage(), e);
	    Assert.fail();
	}
    }

    /**
     * Tests the HttpStatusHandler by sending:
     * 1. a GET request without optional session parameter
     * 2. a GET request with optional session parameter
     * 3. a POST request
     * 4. a GET request with optional and malformed session parameter
     */
    @Test(enabled = !true)
    public void testGetStatus() {
	try {
	    // Request a "get status" with GET and without optional session parameter
	    URL u = new URL("http", "127.0.0.1", 24727, "/getStatus");
	    String response = httpRequest(u, false);

	    Assert.assertNotNull(response);

	    logger.debug(response);
	    Status status = (Status) m.unmarshal(m.str2doc(response));
	    String session = status.getConnectionHandle().get(0).getChannelHandle().getSessionIdentifier();

	    // Request a "get status" with GET and with optional session parameter
	    u = new URL("http", "127.0.0.1", 24727, "/getStatus?session=" + session);
	    response = httpRequest(u, false);

	    Assert.assertNotNull(response);

	    logger.debug(response);

	    // Request a "get status" with POST
	    response = httpRequest(u, true);
	    // we expect response code 405, therefore response must be null
	    Assert.assertNull(response);

	    // Request a "get status" with GET and with optional malformed session parameter
	    u = new URL("http", "127.0.0.1", 24727, "/getStatus?session=");
	    response = httpRequest(u, false);

	    // we expect response code 400, therefore response must be null
	    Assert.assertNull(response);

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

	    Assert.assertNotNull(response);
	} catch (Exception e) {
	    logger.debug(e.getMessage(), e);
	    Assert.fail();
	}
    }

    /**
     * Performs a HTTP Request (GET or POST) to the specified URL and returns the response as String.
     *
     * @param url URL to connect to
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
	    StringBuilder content = new StringBuilder(4096);

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
	StringBuilder stringBuilder = new StringBuilder(4096);

	String line;
	while ((line = bufferedReader.readLine()) != null) {
	    stringBuilder.append(line);
	}

	logger.error("HTML Error response from server:\n{}", stringBuilder.toString());
    }

}
