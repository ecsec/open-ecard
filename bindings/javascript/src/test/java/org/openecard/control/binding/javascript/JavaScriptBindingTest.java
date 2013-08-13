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

package org.openecard.control.binding.javascript;

import java.util.HashMap;
import javax.xml.transform.TransformerException;
import org.openecard.addon.AddonManager;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
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
	    m.removeAllTypeClasses();
	    m.addXmlTypeClass(Status.class);

	    // Start TestClient
	    tc = new TestClient();

	    // Wait some seconds until the SAL comes up
	    Thread.sleep(2500);
	    // Start control interface and binding
	    // TODO: add protocol info
	    AddonManager manager = new AddonManager(tc.getDispatcher(), tc.getGUI(), tc.getCardStates(), tc.getCardRecognition(), tc.getEventManager());
	    binding = new JavaScriptBinding(manager);

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
	    Assert.assertNotNull(response);

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
	    Assert.assertNotNull(response);
	    logger.debug(response[0].toString());

	    Status status = (Status) m.unmarshal(m.str2doc(response[0].toString()));
	    String session = status.getConnectionHandle().get(0).getChannelHandle().getSessionIdentifier();

	    // Request a "get status" with GET and with optional session parameter
	    parameters.put("session", session);
	    response = binding.handle("getStatus", parameters);

	    Assert.assertNotNull(response);

	    logger.debug(response[0].toString());

	    Thread.sleep(30 * 1000);
	    // Request a waitForChange
	    response = binding.handle("waitForChange", parameters);
	    Assert.assertNotNull(response);
	    logger.debug(response[0].toString());

	    Thread.sleep(45 * 1000);
	    // Request a waitForChange
	    response = binding.handle("waitForChange", parameters);
	    Assert.assertNotNull(response);
	    logger.debug(response[0].toString());

	    Thread.sleep(70 * 1000);
	    // Request a waitForChange
	    response = binding.handle("waitForChange", parameters);
	    Assert.assertNull(response);

	} catch (Exception e) {
	    logger.debug(e.getMessage(), e);
	    Assert.fail();
	}
    }

}
