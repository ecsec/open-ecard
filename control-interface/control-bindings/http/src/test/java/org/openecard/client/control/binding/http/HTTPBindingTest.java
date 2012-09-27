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
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import org.openecard.client.control.ControlInterface;
import org.openecard.client.ws.WSMarshaller;
import org.openecard.client.ws.WSMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class HTTPBindingTest {

    private static final Logger logger = LoggerFactory.getLogger(HTTPBindingTest.class);
    private static HTTPBinding binding;
    private static WSMarshaller m;

    @BeforeClass
    public static void setUpClass() throws Exception {
	try {
	    m = WSMarshallerFactory.createInstance();

	    // Start control interface and binding
	    binding = new HTTPBinding();
	    ControlInterface controlInterface = new ControlInterface(binding);
	    controlInterface.start();

	    // Start TestClient and at it as al listener
	    TestClient tc = new TestClient();
	    controlInterface.getListeners().addControlListener(tc);

	    // Wait some seconds until the SAL comes up
	    Thread.sleep(2500);
	} catch (Exception e) {
	    logger.debug(e.getMessage(), e);
	    Assert.fail();
	}
    }

    @Test(enabled = !true)
    public void testBinding() {
	try {
	    // Request a "get status"
	    URL u = new URL("http", "127.0.0.1", binding.getPort(), "/status");
	    String response = httpGET(u);
	    
	    if (response == null) {
		Assert.fail("Get status failed");
	    }

	    // Request a "eID-Client"
	    Document d = m.str2doc(response);

	    Node status = d.getFirstChild();
	    NodeList statusElements = status.getChildNodes();

	    Node n1 = statusElements.item(1);
	    String contextHandle = n1.getFirstChild().getNodeValue();

	    Node n2 = statusElements.item(2);
	    String ifdName = n2.getFirstChild().getNodeValue();

	    Node n3 = statusElements.item(3);
	    String slotIndex = n3.getFirstChild().getNodeValue();

	    String tokenURI = "https://willow.mtg.de/eid-server-demo-app/result/request.html";

	    StringBuilder parameters = new StringBuilder();
	    parameters.append("?");
	    parameters.append("tcTokenURL=");
	    parameters.append(tokenURI);
//	    parameters.append("&");
//	    parameters.append("contextHandle=");
//	    parameters.append(contextHandle);
//	    parameters.append("ifdName=");
//	    parameters.append(ifdName);
//	    parameters.append("slotIndex=");
//	    parameters.append(slotIndex);
	    
	    u = new URL("http", "127.0.0.1", binding.getPort(), "/eID-Client" + parameters.toString());
	    response = httpGET(u);
	    
	    if (response == null) {
		Assert.fail("eID-Client failed");
	    }

	} catch (Exception e) {
	    logger.debug(e.getMessage(), e);
	    Assert.fail();
	}
    }

    private static String httpGET(URL url) {
	try {
	    URLConnection c = url.openConnection();
	    BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
	    String inputLine;
	    StringBuilder content = new StringBuilder();
	    
	    while ((inputLine = in.readLine()) != null) {
		content.append(inputLine);
	    }
	    in.close();

	    return content.toString();
	} catch (IOException e) {
	    logger.error(e.getMessage(), e);
	    return null;
	}
    }

    public static void main(String args[]) {
	try {
	    binding = new HTTPBinding(HTTPBinding.DEFAULT_PORT);
	    ControlInterface controlInterface = new ControlInterface(binding);
	    controlInterface.start();
	} catch (Exception e) {
	    logger.debug(e.getMessage(), e);
	}
    }

}
