package org.openecard.client.event;

import org.openecard.client.common.WSHelper.WSException;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.util.IFDStatusDiff;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class EventRunner implements Callable<Void> {

    private static final Logger _logger = LogManager.getLogger(EventRunner.class.getPackage().getName());
    private final EventManager evtManager;

    private List<IFDStatusType> oldStati;
    private Future wait;

    public EventRunner(EventManager evtManager) {
	this.evtManager = evtManager;
	this.oldStati = new ArrayList<IFDStatusType>();
    }


    @Override
    public Void call() throws Exception {
	try {
	    while (true) {
		wait = evtManager.threadPool.submit(new WaitFuture(evtManager));
		try {
		    List<IFDStatusType> newStati = evtManager.ifdStatus();
		    IFDStatusDiff diff = new IFDStatusDiff(oldStati);
		    diff.diff(newStati, true);
		    if (diff.hasChanges()) {
			evtManager.sendEvents(oldStati, diff.result());
		    }
		    oldStati = newStati;
		} catch (WSException ex) {
		    _logger.log(Level.WARNING, "GetStatus returned with error.", ex);
		}
		// wait for change if it hasn't already happened
		wait.get();
	    }
	} finally {
	    if (wait != null) {
		wait.cancel(true);
	    }
	}
    }

}
