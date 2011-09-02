package de.ecsec.ecard.client.event;

import de.ecsec.core.common.WSHelper;
import de.ecsec.core.common.WSHelper.WSException;
import de.ecsec.core.common.logging.LogManager;
import iso.std.iso_iec._24727.tech.schema.Wait;
import iso.std.iso_iec._24727.tech.schema.WaitResponse;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class WaitFuture implements Callable<Boolean> {

    private static final Logger _logger = LogManager.getLogger(WaitFuture.class.getPackage().getName());
    private final EventManager evtManager;

    public WaitFuture(EventManager evtManager) {
	this.evtManager = evtManager;
    }


    @Override
    public Boolean call() throws Exception {
	Wait wait = new Wait();
	wait.setContextHandle(evtManager.ctx);
	WaitResponse waitResponse = evtManager.env.getIFD().wait(wait);
	try {
	    WSHelper.checkResult(waitResponse);
	    return !waitResponse.getIFDEvent().isEmpty();
	} catch (WSException ex) {
	    _logger.logp(Level.WARNING, this.getClass().getName(), "call()", ex.getMessage(), ex);
	    return false;
	}
    }

}
