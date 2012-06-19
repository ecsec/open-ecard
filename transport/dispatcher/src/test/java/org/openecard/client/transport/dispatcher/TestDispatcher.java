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

package org.openecard.client.transport.dispatcher;

import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;
import org.openecard.client.common.interfaces.Environment;
import org.openecard.ws.IFD;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TestDispatcher {

    @Test
    public void testDispatcher1() throws Exception {
	// test with direct annotation with explicit class specification
	IFD ifd = new TestIFD();
	Environment env = new TestEnv1();
	MessageDispatcher disp = new MessageDispatcher(env);

	env.setIFD(ifd);

	Object req = new EstablishContext();
	Object res = disp.deliver(req);

	assertTrue(res instanceof EstablishContextResponse);
    }

    @Test
    public void testDispatcher2() throws Exception {
	// test with direct annotation without explicit class specification
	IFD ifd = new TestIFD();
	Environment env = new TestEnv2();
	MessageDispatcher disp = new MessageDispatcher(env);

	env.setIFD(ifd);

	Object req = new EstablishContext();
	Object res = disp.deliver(req);

	assertTrue(res instanceof EstablishContextResponse);
    }

    @Test
    public void testDispatcher3() throws Exception {
	// test with inherited annotation without explicit class specification
	IFD ifd = new TestIFD();
	Environment env = new TestEnv3();
	MessageDispatcher disp = new MessageDispatcher(env);

	env.setIFD(ifd);

	Object req = new EstablishContext();
	Object res = disp.deliver(req);

	assertTrue(res instanceof EstablishContextResponse);
    }

}
