package org.openecard.client.ifd.scio;

import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.ifd.TerminalFactory;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.util.IFDStatusDiff;
import org.openecard.client.ifd.scio.wrapper.IFDTerminalFactory;
import org.openecard.client.ws.WSClassLoader;
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.GetStatus;
import iso.std.iso_iec._24727.tech.schema.GetStatusResponse;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import iso.std.iso_iec._24727.tech.schema.ResponseType;
import iso.std.iso_iec._24727.tech.schema.SignalEvent;
import iso.std.iso_iec._24727.tech.schema.SlotStatusType;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import org.openecard.ws.IFDCallback;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class EventListener implements Callable<List<IFDStatusType>> {

    private static final Logger _logger = LogManager.getLogger(EventListener.class.getName());

    private final IFD ifd;
    private final byte[] ctxHandle;
    private final boolean withNew;

    private final ChannelHandleType callback;
    private final List<IFDStatusType> expectedStatuses;
    private final long timeout;
    private final long startTime;

    private Future termWatcher = null;


    public EventListener(IFD ifd, byte[] ctxHandle, long timeout, ChannelHandleType callback, List<IFDStatusType> expectedStatuses, boolean withNew) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "EventListener(IFD ifd, byte[] ctxHandle, long timeout, ChannelHandleType callback, List<IFDStatusType> expectedStatuses, boolean withNew)", new Object[]{ifd, ctxHandle, timeout, callback, expectedStatuses, withNew});
	} // </editor-fold>
	this.ifd = ifd;
	this.ctxHandle = ctxHandle;
	this.timeout = timeout;
	this.callback = callback;
	this.expectedStatuses = expectedStatuses;
	this.withNew = withNew;
	startTime = System.currentTimeMillis();
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "EventListener(IFD ifd, byte[] ctxHandle, long timeout, ChannelHandleType callback, List<IFDStatusType> expectedStatuses, boolean withNew)");
	} // </editor-fold>
    }


    @Override
    public List<IFDStatusType> call() throws Exception {
	try {
	    List<IFDStatusType> result = waitForEvent();

	    if (isAsync()) {
		sendResult(result);
	    }

	    return result;

	} catch (TimeoutException ex) {
            // <editor-fold defaultstate="collapsed" desc="log exception">
            if (_logger.isLoggable(Level.WARNING)) {
                _logger.logp(Level.WARNING, this.getClass().getName(), "call", ex.getMessage(), ex);
            } // </editor-fold>
	    throw new IFDException(ECardConstants.Minor.IFD.TIMEOUT_ERROR, "Wait timed out.");
	} catch (Exception ex) {
            // <editor-fold defaultstate="collapsed" desc="log exception">
            if (_logger.isLoggable(Level.WARNING)) {
                _logger.logp(Level.WARNING, this.getClass().getName(), "call", ex.getMessage(), ex);
            } // </editor-fold>
	    throw ex; // needed to process finally block
	} finally {
	    // remove async thread from IFD
	    if (isAsync()) {
		ifd.removeAsnycTerminal(callback.getSessionIdentifier());
	    }
	    if (termWatcher != null) {
		termWatcher.cancel(true);
	    }
	}
    }



    private List<IFDStatusType> waitForEvent() throws IFDException, InterruptedException, ExecutionException, TimeoutException {
	// start watch thread
	termWatcher = ifd.runCallable(new TerminalWatcher());

	// get current status and compare it
	List<IFDStatusType> currentStatus = getCurrentStatus();
	IFDStatusDiff diff = new IFDStatusDiff(expectedStatuses);
	diff.diff(currentStatus, withNew);
	if (diff.hasChanges()) {
	    termWatcher.cancel(true);
	    return diff.result();
	}

	// no change, wait for watcher to complete
	long elapsedTime = System.currentTimeMillis() - startTime;
	long actualTimeout = timeout - elapsedTime;
	actualTimeout = actualTimeout < 0 ? 1 : actualTimeout;
	termWatcher.get(actualTimeout, TimeUnit.MILLISECONDS);

	// get current status and return it
	currentStatus = getCurrentStatus();
	diff = new IFDStatusDiff(expectedStatuses);
	diff.diff(currentStatus, withNew);
	return diff.result();
    }


    private List<IFDStatusType> getCurrentStatus() throws IFDException {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "getCurrentStatus()");
	} // </editor-fold>
	GetStatus statusReq = new GetStatus();
	statusReq.setContextHandle(ctxHandle);
	GetStatusResponse status = ifd.getStatus(statusReq);
	if (status.getResult().getResultMajor().equals(ECardConstants.Major.ERROR)) {
	    IFDException ex = new IFDException(status.getResult());
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "getCurrentStatus()", ex.getMessage(), ex);
	    } // </editor-fold>
	    throw ex;
	}
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "getCurrentStatus()", status);
	} // </editor-fold>
	return status.getIFDStatus();
    }


    private void sendResult(List<IFDStatusType> result) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "sendResult(List<IFDStatusType> result)");
	} // </editor-fold>
	IFDCallback endpoint = null;
	try {
	    endpoint = (IFDCallback) WSClassLoader.getClientService("IFDCallback", callback.getProtocolTerminationPoint());
	} catch (Exception ex) {
	    _logger.logp(Level.SEVERE, this.getClass().getName(), "sendResult(List<IFDStatusType> result)", ex.getMessage(), ex);
	    return;
	}

	SignalEvent sevt = new SignalEvent();
	sevt.setContextHandle(ctxHandle);
	sevt.setSessionIdentifier(callback.getSessionIdentifier());
	sevt.getIFDEvent().addAll(result);
	ResponseType sevtResp = endpoint.signalEvent(sevt);
	if (sevtResp.getResult().getResultMajor().equals(ECardConstants.Major.ERROR)) {
	    _logger.logp(Level.SEVERE, this.getClass().getName(), "sendResult(List<IFDStatusType> result)", "SignalEvent returned with an error.", sevtResp);
	}

	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "sendResult(List<IFDStatusType> result)");
	} // </editor-fold>
    }


    public boolean isAsync() {
	return this.callback != null;
    }


    private boolean expectedContains(String ifdName) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "expectedContains(String ifdName)", ifdName);
	} // </editor-fold>
	Boolean b = expectedGet(ifdName) != null;
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "expectedContains(String ifdName)", b);
	} // </editor-fold>
	return b.booleanValue();
    }

    private boolean expectedHasCard(String ifdName) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "expectedHasCard(String ifdName)", ifdName);
	} // </editor-fold>
	IFDStatusType s = expectedGet(ifdName);
	List<SlotStatusType> slots = s.getSlotStatus();
	boolean result = false;
	if (! slots.isEmpty()) {
	    SlotStatusType slot = slots.get(0);
	    result = slot.isCardAvailable();
	}
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "expectedHasCard(String ifdName)", result);
	} // </editor-fold>
	return result;
    }

    private IFDStatusType expectedGet(String ifdName) {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "expectedGet(String ifdName)", ifdName);
	} // </editor-fold>
	IFDStatusType result = null;
	for (IFDStatusType s : expectedStatuses) {
	    if (s.getIFDName().equals(ifdName)) {
		result = s;
		break;
	    }
	}
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "expectedGet(String ifdName)", result);
	} // </editor-fold>
	return result;
    }


    private class TerminalWatcher implements Callable<Void> {

	private final TerminalFactory factory;

	public TerminalWatcher() throws IFDException {
	    factory = IFDTerminalFactory.getInstance();
	}

	@Override
	public Void call() throws Exception {
	    boolean change = false;
	    while (!change) {
		// get list of terminals
		CardTerminals terminals = factory.terminals();
		List<CardTerminal> termList;
		try {
		    termList = terminals.list();
		} catch (CardException e) {
		    termList = new LinkedList<CardTerminal>(); // empty list because list call can fail with exception on some systems
		}

		// observe status
		try {
		    // check if there are new or changed terminals
		    List<IFDStatusType> deleted = new LinkedList<IFDStatusType>(expectedStatuses);
		    for (CardTerminal t : termList) {
			if (expectedContains(t.getName())) {
			    if (t.isCardPresent() != expectedHasCard(t.getName())) {
				return null;
			    }
			    deleted.remove(expectedGet(t.getName()));
			} else if (withNew) {
			    return null;
			}
		    }
		    // check for deleted terminals
		    if (!deleted.isEmpty()) {
			return null;
		    }

		    change = terminals.waitForChange(100); // in millis

		} catch (CardException ex) {
		    try {
			// PCSC pooped, try again in a second
			Thread.sleep(250);
		    } catch (InterruptedException exc) {
			throw exc; // somebody wants me to quit, so i do it.
		    }
		} catch (IllegalStateException ex) {
		    try {
			// no terminals in list triggered this error
			Thread.sleep(100); // repeat wait from above
		    } catch (InterruptedException exc) {
			throw exc; // somebody wants me to quit, so i do it.
		    }
                } catch (Exception ex) {
		    throw ex;
		}
	    }

	    return null;
	}

    }

}
