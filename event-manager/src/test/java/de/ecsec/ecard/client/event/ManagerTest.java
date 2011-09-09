package de.ecsec.ecard.client.event;

import de.ecsec.core.common.interfaces.Environment;
import de.ecsec.core.common.interfaces.Transport;
import de.ecsec.core.ifd.IFD;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ManagerTest {

    private class Env implements Environment {
	private IFD ifd;
	@Override
	public void setIFD(iso.std.iso_iec._24727.tech.schema.IFD ifd) {
	    this.ifd = (IFD) ifd;
	}

	@Override
	public iso.std.iso_iec._24727.tech.schema.IFD getIFD() {
	    return this.ifd;
	}

	@Override
	public void setEventManager(de.ecsec.core.common.interfaces.EventManager manager) {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public de.ecsec.core.common.interfaces.EventManager getEventManager() {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void addTransport(String id, Transport transport) {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Transport getTransport(String id) {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Map<String, Transport> getAllTransports() {
	    throw new UnsupportedOperationException("Not supported yet.");
	}

    }

    //@Test
    public void runManager() throws InterruptedException {
	IFD ifd = new IFD();
	EstablishContext ctx = new EstablishContext();
	EstablishContextResponse ctxR = ifd.establishContext(ctx);
	Environment env = new Env();
	env.setIFD(ifd);
	EventManager evt = new EventManager(null, env, ctxR.getContextHandle());
	evt.initialize();
	Thread.sleep(1000);
	evt.terminate();
	Thread.sleep(1000000);
    }

    @Test
    public void dummy() {
    }

}
