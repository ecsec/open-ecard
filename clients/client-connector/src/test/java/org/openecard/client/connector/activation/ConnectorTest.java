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

package org.openecard.client.connector.activation;

import org.openecard.client.connector.Connector;
import org.openecard.client.connector.ConnectorServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;


/**
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ConnectorTest {

    private static final Logger _logger = LoggerFactory.getLogger(ConnectorTest.class);

    public static void main(String[] arg) {
	try {
	    Connector result = new Connector(ConnectorServer.DEFAULT_PORT);
	} catch (Exception ex) {
	    _logger.error(ex.getMessage(), ex);
	}
    }

    @Test
    public void testGetInstance() throws Exception {
//	System.out.println("getInstance");
//	Connector expResult = null;
//	assertEquals(expResult, result);
//	fail("The test case is a prototype.");
    }

}
