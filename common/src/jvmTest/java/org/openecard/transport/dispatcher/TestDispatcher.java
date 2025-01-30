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

package org.openecard.transport.dispatcher;

import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import org.openecard.common.interfaces.Environment;
import org.openecard.ws.IFD;
import org.testng.annotations.Test;
import static org.testng.Assert.*;


/**
 * Test of the dispatcher.
 * The test checks if a environment can be loaded and if a method can be dispatched successfully.
 *
 * @author Tobias Wich
 */
public class TestDispatcher {

    /**
     * Test instance of TestEnv1.
     *
     * @throws Exception If the test is a failure.
     */
    @Test
    public void testDispatcher1() throws Exception {
	// test with direct annotation with explicit class specification
	IFD ifd = new TestIFD();
	Environment env = new TestEnv1();
	MessageDispatcher disp = new MessageDispatcher(env);

	env.setIfd(ifd);

	Object req = new EstablishContext();
	Object res = disp.deliver(req);

	assertTrue(res instanceof EstablishContextResponse);
    }

    /**
     * Test instance of TestEnv2.
     *
     * @throws Exception If the test is a failure.
     */
    @Test
    public void testDispatcher2() throws Exception {
	// test with inherited annotation without explicit class specification
	IFD ifd = new TestIFD();
	Environment env = new TestEnv2();
	MessageDispatcher disp = new MessageDispatcher(env);

	env.setIfd(ifd);

	Object req = new EstablishContext();
	Object res = disp.deliver(req);

	assertTrue(res instanceof EstablishContextResponse);
    }

}
