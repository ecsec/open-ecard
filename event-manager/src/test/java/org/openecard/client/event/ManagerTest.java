package org.openecard.client.event;

import org.openecard.client.common.interfaces.Environment;
import org.openecard.client.ifd.scio.IFD;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import org.junit.Ignore;
import org.junit.Test;
import org.openecard.client.common.ClientEnv;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ManagerTest {

    @Ignore
    @Test
    public void runManager() throws InterruptedException {
	IFD ifd = new IFD();
	EstablishContext ctx = new EstablishContext();
	EstablishContextResponse ctxR = ifd.establishContext(ctx);
	Environment env = new ClientEnv();
	env.setIFD(ifd);
	EventManager evt = new EventManager(null, env, ctxR.getContextHandle(), "1234567890");
	evt.initialize();
	Thread.sleep(1000);
	//evt.terminate();
	Thread.sleep(1000000);
    }

}
