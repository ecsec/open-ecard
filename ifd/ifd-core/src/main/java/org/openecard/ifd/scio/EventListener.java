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

package org.openecard.ifd.scio;

import iso.std.iso_iec._24727.tech.schema.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import org.openecard.common.ECardConstants;
import org.openecard.common.util.IFDStatusDiff;
import org.openecard.ifd.scio.wrapper.SCTerminal;
import org.openecard.ifd.scio.wrapper.SCWrapper;
import org.openecard.ws.IFDCallback;
import org.openecard.ws.WSClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class EventListener implements Callable<List<IFDStatusType>> {

    private static final Logger logger = LoggerFactory.getLogger(EventListener.class);

    private static final long pollDelay;
    private static final long pauseDelay;

    private static long pauseTime = 0;

    static {
	String delayStr = IFDProperties.getProperty("org.openecard.ifd.wait.delay");
	long delay = 500;
	if (delayStr != null) {
	    try {
		delay = Long.parseLong(delayStr);
	    } catch (NumberFormatException ex) {
		logger.warn("Property 'org.openecard.ifd.wait.delay' contains a malformed number.", ex);
	    }
	}
	pollDelay = delay;

	String pauseStr = IFDProperties.getProperty("org.openecard.ifd.wait.pause");
	long pause = 2000;
	if (delayStr != null) {
	    try {
		pause = Long.parseLong(pauseStr);
	    } catch (NumberFormatException ex) {
		logger.warn("Property 'org.openecard.ifd.wait.pause' contains a malformed number.", ex);
	    }
	}
	pauseDelay = pause;
    }


    private final IFD ifd;
    private final SCWrapper scWrapper;
    private final byte[] ctxHandle;
    private final boolean withNew;

    private final ChannelHandleType callback;
    private final List<IFDStatusType> expectedStatuses;
    private final long timeout;
    private final long startTime;

    private Future termWatcher = null;


    public EventListener(IFD ifd, SCWrapper scWrapper, byte[] ctxHandle, long timeout, ChannelHandleType callback, List<IFDStatusType> expectedStatuses, boolean withNew) {
	this.ifd = ifd;
	this.scWrapper = scWrapper;
	this.ctxHandle = ctxHandle;
	this.timeout = timeout;
	this.callback = callback;
	this.expectedStatuses = expectedStatuses;
	this.withNew = withNew;
	startTime = System.currentTimeMillis();
    }

    /**
     * Pause wait for events.
     * The time to pause is set via the property org.openecard.ifd.wait.pause.
     * If the property is invalid or unset, 2000ms is the default.
     */
    public static synchronized void pause() {
	pauseTime = System.currentTimeMillis() + pauseDelay;
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
	    logger.warn(ex.getMessage(), ex);
	    throw new IFDException(ECardConstants.Minor.IFD.TIMEOUT_ERROR, "Wait timed out.");
	} catch (Exception ex) {
	    logger.warn(ex.getMessage(), ex);
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
	GetStatus statusReq = new GetStatus();
	statusReq.setContextHandle(ctxHandle);
	GetStatusResponse status = ifd.getStatus(statusReq);
	if (status.getResult().getResultMajor().equals(ECardConstants.Major.ERROR)) {
	    IFDException ex = new IFDException(status.getResult());
	    logger.warn(ex.getMessage(), ex);
	    throw ex;
	}
	return status.getIFDStatus();
    }


    /**
     * Send a SOAP call with the given result to the IFDCallback address set when creating the class instance.
     *
     * @param result List of result stati.
     */
    private void sendResult(List<IFDStatusType> result) {
	try {
	    IFDCallback endpoint = (IFDCallback) WSClassLoader.getClientService("IFDCallback", callback.getProtocolTerminationPoint());

	    SignalEvent sevt = new SignalEvent();
	    sevt.setContextHandle(ctxHandle);
	    sevt.setSessionIdentifier(callback.getSessionIdentifier());
	    sevt.getIFDEvent().addAll(result);
	    ResponseType sevtResp = endpoint.signalEvent(sevt);
	    if (sevtResp.getResult().getResultMajor().equals(ECardConstants.Major.ERROR)) {
		logger.error("SignalEvent returned with an error.\n{}", sevtResp);
	    }
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	}
    }


    public boolean isAsync() {
	return this.callback != null;
    }


    private boolean expectedContains(String ifdName) {
	Boolean b = expectedGet(ifdName) != null;
	return b.booleanValue();
    }

    private boolean expectedHasCard(String ifdName) {
	IFDStatusType s = expectedGet(ifdName);
	List<SlotStatusType> slots = s.getSlotStatus();
	boolean result = false;
	if (! slots.isEmpty()) {
	    SlotStatusType slot = slots.get(0);
	    result = slot.isCardAvailable();
	}
	return result;
    }

    private IFDStatusType expectedGet(String ifdName) {
	IFDStatusType result = null;
	for (IFDStatusType s : expectedStatuses) {
	    if (s.getIFDName().equals(ifdName)) {
		result = s;
		break;
	    }
	}
	return result;
    }


    private class TerminalWatcher implements Callable<Void> {

	@Override
	public Void call() throws Exception {
	    int pcscErrorCount = 0; // used to break out of the call if pcsc doesn't come back online
	    boolean change = false;

	    while (!change) {
		// get list of terminals
		List<SCTerminal> termList = scWrapper.getTerminals(true);

		// observe status
		try {
		    // check if there are new or changed terminals
		    List<IFDStatusType> deleted = new LinkedList<IFDStatusType>(expectedStatuses);
		    for (SCTerminal t : termList) {
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

		    // block execution here
		    while(true) {
			long currentPauseTime;
			synchronized (EventListener.class) {
			    currentPauseTime = pauseTime;
			}
			long now = System.currentTimeMillis();
			if (now > currentPauseTime) {
			    break;
			}
			Thread.sleep(currentPauseTime - now);
		    }

		    // waitForChange has a severe bug in OSX, the code works without waitForChange.
		    // only the CPU load is slightly higher
		    // TODO: only do this on 64bit VMs. 32bit seems to run fine with Apple PCSC
		    if (! System.getProperty("os.name").contains("OS X")) {
			change = scWrapper.waitForChange(pollDelay); // in millis
		    } else {
			Thread.sleep(50);
		    }

		} catch (IFDException ex) {
		    try {
			// PCSC pooped, try again after a short break
			pcscErrorCount++;
			if (pcscErrorCount == 500) {
			    throw ex;
			}
			Thread.sleep(1000);
		    } catch (InterruptedException exc) {
			throw exc; // somebody wants me to quit, so i do it.
		    }
		} catch (IllegalStateException ex) {
		    try {
			// no terminals in list triggered this error
			Thread.sleep(pollDelay); // repeat wait from above
		    } catch (InterruptedException exc) {
			throw exc; // somebody wants me to quit, so i do it.
		    }
		} catch (Exception ex) {
		    logger.error(ex.getMessage(), ex);
		    throw ex;
		}
	    }

	    return null;
	}

    }

}
