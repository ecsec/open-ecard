/*
 * Copyright 2012 Tobias Wich ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.transport.dispatcher;

import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class TestDispatcher {

    @Test
    public void testDispatcher() throws Exception {
	TestIFD ifd = new TestIFD();
	TestEnv env = new TestEnv();
	MessageDispatcher disp = new MessageDispatcher(env);

	env.setIFD(ifd);

	Object req = new EstablishContext();
	Object res = disp.deliver(req);

	assertTrue(res instanceof EstablishContextResponse);
    }

}
