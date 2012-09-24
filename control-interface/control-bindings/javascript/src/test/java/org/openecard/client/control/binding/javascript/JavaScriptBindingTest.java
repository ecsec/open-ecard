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

package org.openecard.client.control.binding.javascript;

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
public final class JavaScriptBindingTest {

    private static final Logger logger = LoggerFactory.getLogger(JavaScriptBindingTest.class);
    private static JavaScriptBinding binding;
    private static WSMarshaller m;

    @BeforeClass
    public static void setUpClass() throws Exception {
	try {
	    m = WSMarshallerFactory.createInstance();

	    // Start control interface and binding
	    binding = new JavaScriptBinding();
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
	    Object[] response = binding.handle("status", null);
	    if (response == null) {
		Assert.fail("Get status failed");
	    }

	    // Request a "eID-Client"
	    Document d = m.str2doc(response[0].toString());

	    Node status = d.getFirstChild();
	    NodeList statusElements = status.getChildNodes();

	    Node n1 = statusElements.item(1);
	    String contextHandle = n1.getFirstChild().getNodeValue();

	    Node n2 = statusElements.item(2);
	    String ifdName = n2.getFirstChild().getNodeValue();

	    Node n3 = statusElements.item(3);
	    String slotIndex = n3.getFirstChild().getNodeValue();

	    String tokenURI = "https://willow.mtg.de/eid-server-demo-app/result/request.html";

	    Object[] parameters = new Object[]{tokenURI, contextHandle, ifdName, slotIndex};
	    
	    response = binding.handle("eID-Client", parameters);
	    if (response == null) {
		Assert.fail("Get status failed");
	    }

	} catch (Exception e) {
	    logger.debug(e.getMessage(), e);
	    Assert.fail();
	}
    }

}
