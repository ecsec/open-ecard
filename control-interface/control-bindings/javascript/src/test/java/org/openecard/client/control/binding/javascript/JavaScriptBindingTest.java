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

import java.util.HashMap;
import javax.xml.transform.TransformerException;
import org.openecard.client.control.ControlInterface;
import org.openecard.client.control.binding.javascript.handler.JavaScriptTCTokenHandler;
import org.openecard.client.control.handler.ControlHandlers;
import org.openecard.client.control.module.status.EventHandler;
import org.openecard.client.control.module.tctoken.GenericTCTokenHandler;
import org.openecard.client.ws.WSMarshaller;
import org.openecard.client.ws.WSMarshallerException;
import org.openecard.client.ws.WSMarshallerFactory;
import org.openecard.ws.schema.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public final class JavaScriptBindingTest {

    private static final Logger logger = LoggerFactory.getLogger(JavaScriptBindingTest.class);
    private static WSMarshaller m;
    static TestClient tc;
    static JavaScriptBinding binding;

    @BeforeClass
    public static void setUpClass() throws Exception {
	try {
	    m = WSMarshallerFactory.createInstance();

	    // Start TestClient
	    tc = new TestClient();

	    // Wait some seconds until the SAL comes up
	    Thread.sleep(2500);
	    // Start control interface and binding
	    ControlHandlers handler = new ControlHandlers();
	    GenericTCTokenHandler genericTCTokenHandler = new GenericTCTokenHandler(tc.getCardStates(), tc.getDispatcher(), tc.getGUI(), tc.getCardRecognition());
	    JavaScriptTCTokenHandler jsTCTokenHandler = new JavaScriptTCTokenHandler(genericTCTokenHandler);
	    handler.addControlHandler(jsTCTokenHandler);
	    binding = new JavaScriptBinding();
	    ControlInterface controlInterface = new ControlInterface(binding);
	    controlInterface.start();

	} catch (Exception e) {
	    logger.debug(e.getMessage(), e);
	    Assert.fail();
	}
    }

    @Test(enabled = !true)
    public void testGetStatus() throws TransformerException, WSMarshallerException, SAXException {

	// Request a "get status"
	HashMap<String, Object> parameters = new HashMap<String, Object>();
	Object[] response = binding.handle("getStatus", parameters);
	if (response == null) {
	    Assert.fail("Get status failed");
	}
	logger.debug(response[0].toString());

	Status status = (Status) m.unmarshal(m.str2doc(response[0].toString()));
	String session = status.getConnectionHandle().get(0).getChannelHandle().getSessionIdentifier();

	// Request a "get status" with GET and with optional session parameter
	parameters.put("session", session);
	response = binding.handle("getStatus", parameters);

	if (response == null) {
	    Assert.fail("Get status failed");
	}

	logger.debug(response[0].toString());

    }

    @Test(enabled = !true)
    public void testeIDClient() {
	try {

	    String tokenURI = "http://openecard-demo.vserver-001.urospace.de/tcToken?card-type=http://bsi.bund.de/cif/npa.xml";

	    HashMap<String, Object> parameters = new HashMap<String, Object>();
	    parameters.put("tcTokenURL", tokenURI);

	    Object[] response = binding.handle("eID-Client", parameters);
	    if (response == null) {
		Assert.fail("Get eID-Client failed");
	    }

	} catch (Exception e) {
	    logger.debug(e.getMessage(), e);
	    Assert.fail();
	}
    }

    @Test(enabled = !true)
    public void testWaitForChange() {
	try {

	    // Request a "get status"
	    HashMap<String, Object> parameters = new HashMap<String, Object>();
	    Object[] response = binding.handle("getStatus", parameters);
	    if (response == null) {
		Assert.fail("Get status failed");
	    }
	    logger.debug(response[0].toString());

	    Status status = (Status) m.unmarshal(m.str2doc(response[0].toString()));
	    String session = status.getConnectionHandle().get(0).getChannelHandle().getSessionIdentifier();

	    // Request a "get status" with GET and with optional session parameter
	    parameters.put("session", session);
	    response = binding.handle("getStatus", parameters);

	    if (response == null) {
		Assert.fail("Get status failed");
	    }

	    logger.debug(response[0].toString());

	    Thread.sleep(30 * 1000);
	    // Request a waitForChange
	    response = binding.handle("waitForChange", parameters);
	    if (response == null) {
		Assert.fail("WaitForChange failed");
	    }
	    logger.debug(response[0].toString());

	    Thread.sleep(45 * 1000);
	    // Request a waitForChange
	    response = binding.handle("waitForChange", parameters);
	    if (response == null) {
		Assert.fail("WaitForChange failed");
	    }
	    logger.debug(response[0].toString());

	    Thread.sleep(70 * 1000);
	    // Request a waitForChange
	    response = binding.handle("waitForChange", parameters);
	    if (response != null) {
		Assert.fail("WaitForChange failed");
	    }

	} catch (Exception e) {
	    logger.debug(e.getMessage(), e);
	    Assert.fail();
	}
    }

}
