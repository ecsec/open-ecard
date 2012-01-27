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

package org.openecard.client.ifd.scio;

import iso.std.iso_iec._24727.tech.schema.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebService;
import javax.smartcardio.CardException;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.ifd.Protocol;
import org.openecard.client.common.ifd.ProtocolFactory;
import org.openecard.client.common.ifd.anytype.PACEInputType;
import org.openecard.client.common.ifd.anytype.PACEOutputType;
import org.openecard.client.common.logging.LogManager;
import org.openecard.client.common.util.ValueGenerators;
import org.openecard.client.gui.UserConsent;
import org.openecard.client.ifd.scio.reader.*;
import org.openecard.client.ifd.scio.wrapper.SCCard;
import org.openecard.client.ifd.scio.wrapper.SCChannel;
import org.openecard.client.ifd.scio.wrapper.SCTerminal;
import org.openecard.client.ifd.scio.wrapper.SCWrapper;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
@WebService(endpointInterface = "org.openecard.ws.IFD")
public class IFD implements org.openecard.ws.IFD {

    private static final Logger _logger = LogManager.getLogger(IFD.class .getName());

    private byte[] ctxHandle = null;
    private SCWrapper scwrapper;
    private UserConsent gui = null;
    private ProtocolFactories protocolFactories = new ProtocolFactories();

    private AtomicInteger numClients;
    private ExecutorService threadPool;
    private ConcurrentSkipListMap<String,Future> asyncWaitThreads;
    private Future syncWaitThread;


    protected synchronized void removeAsnycTerminal(String session) {
	if (asyncWaitThreads != null) { // be sure the list still exists
	    asyncWaitThreads.remove(session);
	}
    }

    protected Future runThread(Runnable r) {
	return threadPool.submit(r);
    }
    protected Future runCallable(Callable c) {
	return threadPool.submit(c);
    }

    private boolean hasContext() {
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.entering(this.getClass().getName(), "hasContext()");
	} // </editor-fold>
	Boolean hasContext = ctxHandle != null;
	// <editor-fold defaultstate="collapsed" desc="log trace">
	if (_logger.isLoggable(Level.FINER)) {
	    _logger.exiting(this.getClass().getName(), "hasContext()", hasContext);
	} // </editor-fold>
	return hasContext.booleanValue();
    }

    public void setGUI(UserConsent gui) {
	this.gui = gui;
    }

    public boolean addProtocol(String proto, ProtocolFactory factory) {
	return protocolFactories.add(proto, factory);
    }


    @Override
    public synchronized EstablishContextResponse establishContext(EstablishContext parameters) {
	try {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.entering(this.getClass().getName(), "establishContext(EstablishContext parameters)", parameters);
	    } // </editor-fold>
	    // on first call, create a new unique handle
	    if (ctxHandle == null) {
		scwrapper = new SCWrapper();
		ctxHandle = scwrapper.createHandle(ECardConstants.CONTEXT_HANDLE_DEFAULT_SIZE);
		numClients = new AtomicInteger(1);
		threadPool = Executors.newCachedThreadPool();
		asyncWaitThreads = new ConcurrentSkipListMap<String, Future>();
	    } else {
		// on second or further calls, increment usage counter
		numClients.incrementAndGet();
	    }

	    // prepare response
	    EstablishContextResponse response = WSHelper.makeResponse(EstablishContextResponse.class, WSHelper.makeResultOK());
	    response.setContextHandle(ctxHandle);
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.exiting(this.getClass().getName(), "establishContext(EstablishContext parameters)", response);
	    } // </editor-fold>
	    return response;
	} catch (Throwable t) {
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "establishContext(EstablishContext parameters)", t.getMessage(), t);
	    }
	    return WSHelper.makeResponse(EstablishContextResponse.class, WSHelper.makeResult(t));
	}
    }

    @Override
    public synchronized ReleaseContextResponse releaseContext(ReleaseContext parameters) {
	try {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.entering(this.getClass().getName(), "releaseContext(ReleaseContext parameters)", parameters);
	    } // </editor-fold>
	    ReleaseContextResponse response;
	    if (IFDUtils.arrayEquals(ctxHandle, parameters.getContextHandle())) {
		if (numClients.decrementAndGet() == 0) { // last client detaches
		    ctxHandle = null;
		    numClients = null;
		    // terminate thread pool
		    threadPool.shutdown(); // wait for threads to die and block new requests
		    if (! threadPool.isTerminated()) {
			threadPool.awaitTermination(10, TimeUnit.SECONDS); // wait for a clean shutdown
			threadPool.shutdownNow(); // force shutdown
		    }
		    threadPool = null;
		    asyncWaitThreads = null;
		}

		response = WSHelper.makeResponse(ReleaseContextResponse.class, WSHelper.makeResultOK());
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "releaseContext(ReleaseContext parameters)", response);
		} // </editor-fold>
		return response;
	    } else {
		response = WSHelper.makeResponse(ReleaseContextResponse.class, WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, "Invalid context handle specified."));
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "releaseContext(ReleaseContext parameters)", response);
		} // </editor-fold>
		return response;
	    }
	} catch (Throwable t) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "releaseContext(ReleaseContext parameters)", t.getMessage(), t);
	    } // </editor-fold>
	    return WSHelper.makeResponse(ReleaseContextResponse.class, WSHelper.makeResult(t));
	}
    }


    @Override
    public ListIFDsResponse listIFDs(ListIFDs parameters) {
	try {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.entering(this.getClass().getName(), "listIFDs(ListIFDs parameters)", parameters);
	    } // </editor-fold>
	    ListIFDsResponse response;
	    if (!IFDUtils.arrayEquals(ctxHandle, parameters.getContextHandle())) {
		response = WSHelper.makeResponse(ListIFDsResponse.class, WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, "Invalid context handle specified."));
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "listIFDs(ListIFDs parameters)", response);
		} // </editor-fold>
		return response;
	    } else {
		try {
		    List<String> ifds = scwrapper.getTerminalNames(true);
		    response = WSHelper.makeResponse(ListIFDsResponse.class, WSHelper.makeResultOK());
		    response.getIFDName().addAll(ifds);
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.FINER)) {
			_logger.exiting(this.getClass().getName(), "listIFDs(ListIFDs parameters)", response);
		    } // </editor-fold>
		    return response;
		} catch (IFDException ex) {
		    response = WSHelper.makeResponse(ListIFDsResponse.class, ex.getResult());
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.WARNING)) {
			_logger.logp(Level.WARNING, this.getClass().getName(), "listIFDs(ListIFDs parameters)", ex.getMessage(), ex);
		    } // </editor-fold>
		    return response;
		}
	    }
	} catch (Throwable t) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "listIFDs(ListIFDs parameters)", t.getMessage(), t);
	    } // </editor-fold>
	    return WSHelper.makeResponse(ListIFDsResponse.class, WSHelper.makeResult(t));
	}
    }


    private List<String> buildPACEProtocolList(List<Long> paceCapabilities) {
	List<String> supportedProtos = new LinkedList<String>();
	for (Long next : paceCapabilities) {
	    if (next.equals(Long.valueOf(0x10)) || next.equals(Long.valueOf(0x20)) || next.equals(Long.valueOf(0x40))) {
		supportedProtos.add(ECardConstants.Protocol.PACE + "." + next.longValue());
	    }
	}
	return supportedProtos;
    }

    @Override
    public GetIFDCapabilitiesResponse getIFDCapabilities(GetIFDCapabilities parameters) {
	try {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.entering(this.getClass().getName(), "getIFDCapabilities(GetIFDCapabilities parameters)", parameters);
	    } // </editor-fold>
	    GetIFDCapabilitiesResponse response;
	    if (!IFDUtils.arrayEquals(ctxHandle, parameters.getContextHandle())) {
		response = WSHelper.makeResponse(GetIFDCapabilitiesResponse.class, WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, "Invalid context handle specified."));
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "getIFDCapabilities(GetIFDCapabilities parameters)", response);
		} // </editor-fold>
		return response;
	    } else {
		try {
		    // get reader
		    SCTerminal t = scwrapper.getTerminal(parameters.getIFDName(), true);

		    // fetch general reader capabilities
		    IFDCapabilitiesType cap = new IFDCapabilitiesType();
		    cap.setAcousticSignalUnit(t.isAcousticSignal());
		    cap.setOpticalSignalUnit(t.isOpticalSignal());
		    DisplayCapabilityType dispCap = t.getDisplayCapability();
		    if (dispCap != null) {
			cap.getDisplayCapability().add(dispCap);
		    }
		    KeyPadCapabilityType keyCap = t.getKeypadCapability();
		    if (keyCap != null) {
			cap.getKeyPadCapability().add(keyCap);
		    }

		    // fetch capabilities specific to the slot (where is the difference?!?)
		    SlotCapabilityType slotCap = new SlotCapabilityType();
		    slotCap.setIndex(BigInteger.ZERO);
		    cap.getSlotCapability().add(slotCap);
		    // detect PACE capability, start by virtual terminal which is needed for that task
		    // then ask whether the reader can do it or a software solution exists
		    if (gui != null) {
			if (t.supportsPace()) {
			    List<Long> capabilities = t.getPACECapabilities();
			    List<String> protos = buildPACEProtocolList(capabilities);
			    slotCap.getProtocol().addAll(protos);
			}
		    }
		    // detect PinCompare capabilities
		    if (gui != null) {
			slotCap.getProtocol().add(ECardConstants.Protocol.PIN_COMPARE);
		    } else if (t.supportsPinCompare()) {
			slotCap.getProtocol().add(ECardConstants.Protocol.PIN_COMPARE);
		    }

		    // ask protocol factory which types it supports
		    for (String proto : this.protocolFactories.protocols()) {
			if (! slotCap.getProtocol().contains(proto)) {
			    slotCap.getProtocol().add(proto);
			}
		    }

		    // send response
		    response = WSHelper.makeResponse(GetIFDCapabilitiesResponse.class, WSHelper.makeResultOK());
		    response.setIFDCapabilities(cap);
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.FINER)) {
			_logger.exiting(this.getClass().getName(), "getIFDCapabilities(GetIFDCapabilities parameters)", response);
		    } // </editor-fold>
		    return response;
		} catch (IFDException ex) {
		    response = WSHelper.makeResponse(GetIFDCapabilitiesResponse.class, ex.getResult());
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.WARNING)) {
			_logger.logp(Level.WARNING, this.getClass().getName(), "getIFDCapabilities(GetIFDCapabilities parameters)", ex.getMessage(), ex);
		    } // </editor-fold>
		    return response;
		}
	    }
	} catch (Throwable t) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "getIFDCapabilities(GetIFDCapabilities parameters)", t.getMessage(), t);
	    } // </editor-fold>
	    return WSHelper.makeResponse(GetIFDCapabilitiesResponse.class, WSHelper.makeResult(t));
	}
    }


    @Override
    public GetStatusResponse getStatus(GetStatus parameters) {
	try {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.entering(this.getClass().getName(), "getStatus(GetStatus parameters)", parameters);
	    } // </editor-fold>
	    GetStatusResponse response;
            //FIXME
	    if (!IFDUtils.arrayEquals(ctxHandle, parameters.getContextHandle())) {
		response = WSHelper.makeResponse(GetStatusResponse.class, WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, "Invalid context handle specified."));
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "getStatus(GetStatus parameters)", response);
		} // </editor-fold>
		return response;
	    } else {
		// get ifd name from request or directly from the sc-io
		List<SCTerminal> ifds = new LinkedList<SCTerminal>();
		// get ifd names which should be investigated
		try {
		    if (parameters.getIFDName() != null) {
			SCTerminal t = scwrapper.getTerminal(parameters.getIFDName(), true);
			if (t == null) {
			    response = WSHelper.makeResponse(GetStatusResponse.class, WSHelper.makeResult(ECardConstants.Major.ERROR, ECardConstants.Minor.IFD.UNKNOWN_IFD, "Unknown IFD."));
			    // <editor-fold defaultstate="collapsed" desc="log trace">
			    if (_logger.isLoggable(Level.FINER)) {
				_logger.exiting(this.getClass().getName(), "getStatus(GetStatus parameters)", response);
			    } // </editor-fold>
			    return response;
			} else {
			    ifds.add(t);
			}
		    } else {
			ifds.addAll(scwrapper.getTerminals(true));
		    }
		} catch (IFDException ex) {
		    response = WSHelper.makeResponse(GetStatusResponse.class, ex.getResult());
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.WARNING)) {
			_logger.logp(Level.WARNING, this.getClass().getName(), "getStatus(GetStatus parameters)", ex.getMessage(), ex);
		    } // </editor-fold>
		    return response;
		}

		// request status for each ifd
		ArrayList<IFDStatusType> statuss = new ArrayList<IFDStatusType>(ifds.size());
		for (SCTerminal ifd : ifds) {
		    try {
			IFDStatusType s = ifd.getStatus();
			statuss.add(s);
		    } catch (IFDException ex) {
			response = WSHelper.makeResponse(GetStatusResponse.class, ex.getResult());
			// <editor-fold defaultstate="collapsed" desc="log trace">
			if (_logger.isLoggable(Level.WARNING)) {
			    _logger.logp(Level.WARNING, this.getClass().getName(), "getStatus(GetStatus parameters)", ex.getMessage(), ex);
			} // </editor-fold>
			return response;
		    }
		}
		// everything worked out well
		response = WSHelper.makeResponse(GetStatusResponse.class, WSHelper.makeResultOK());
		response.getIFDStatus().addAll(statuss);
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "getStatus(GetStatus parameters)", response);
		} // </editor-fold>
		return response;
	    }
	} catch (Throwable t) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "getStatus(GetStatus parameters)", t.getMessage(), t);
	    } // </editor-fold>
	    return WSHelper.makeResponse(GetStatusResponse.class, WSHelper.makeResult(t));
	}
    }


    @Override
    public WaitResponse wait(Wait parameters) {
	try {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.entering(this.getClass().getName(), "wait(Wait parameters)", parameters);
	    } // </editor-fold>
	    WaitResponse response;
	    // check for context handle
	    if (!IFDUtils.arrayEquals(ctxHandle, parameters.getContextHandle())) {
		response = WSHelper.makeResponse(WaitResponse.class, WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, "Invalid context handle specified."));
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "wait(Wait parameters)", response);
		} // </editor-fold>
		return response;
	    } else {
		BigInteger timeout = parameters.getTimeOut();
		if (timeout == null) {
		    timeout = BigInteger.valueOf(Long.MAX_VALUE);
		}
		ChannelHandleType callback = parameters.getCallback();
		// callback is only useful with a protocol termination point
		if (callback != null && callback.getProtocolTerminationPoint() == null) {
		    callback = null;
		}

		// get expected status or current status for all if none specified
		List<IFDStatusType> expectedStatuses = parameters.getIFDStatus();
		boolean withNew = false;
		if (expectedStatuses.isEmpty()) {
		    withNew = true;
		    GetStatus statusReq = new GetStatus();
		    statusReq.setContextHandle(ctxHandle);
		    GetStatusResponse status = getStatus(statusReq);
		    if (status.getResult().getResultMajor().equals(ECardConstants.Major.ERROR)) {
			response = WSHelper.makeResponse(WaitResponse.class, status.getResult());
			// <editor-fold defaultstate="collapsed" desc="log trace">
			if (_logger.isLoggable(Level.FINER)) {
			    _logger.exiting(this.getClass().getName(), "wait(Wait parameters)", response);
			} // </editor-fold>
			return response;
		    }
		    expectedStatuses = status.getIFDStatus();
		} else {
		    // check that ifdname is present, needed for comparison
		    for (IFDStatusType s : expectedStatuses) {
			if (s.getIFDName() == null) {
			    response = WSHelper.makeResponse(WaitResponse.class, WSHelper.makeResultError(ECardConstants.Minor.IFD.UNKNOWN_IFD, "IFD in a request IFDStatus not known."));
			    // <editor-fold defaultstate="collapsed" desc="log trace">
			    if (_logger.isLoggable(Level.FINER)) {
				_logger.exiting(this.getClass().getName(), "wait(Wait parameters)", response);
			    } // </editor-fold>
			    return response;
			}
		    }
		}

		// if callback, generate session id
		if (callback != null) {
		    ChannelHandleType newCallback = new ChannelHandleType();
		    newCallback.setBinding(callback.getBinding());
		    newCallback.setPathSecurity(callback.getPathSecurity());
		    newCallback.setProtocolTerminationPoint(callback.getProtocolTerminationPoint());
		    newCallback.setSessionIdentifier(ValueGenerators.generateSessionID());
		    callback = newCallback;
		}

		// create the event and fire
		EventListener l = new EventListener(this, ctxHandle, timeout.longValue(), callback, expectedStatuses, withNew);
		FutureTask<List<IFDStatusType>> future = new FutureTask(l);

		if (l.isAsync()) {
		    // add future to async wait list
		    asyncWaitThreads.put(callback.getSessionIdentifier(), future);
		    threadPool.execute(future); // finally run this darn thingy

		    // prepare result with session id in it
		    response = WSHelper.makeResponse(WaitResponse.class, WSHelper.makeResultOK());
		    if (callback.getSessionIdentifier() != null) {
			response.setSessionIdentifier(callback.getSessionIdentifier());
		    }
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.FINER)) {
			_logger.exiting(this.getClass().getName(), "wait(Wait parameters)", response);
		    } // </editor-fold>
		    return response;
		} else {
		    // run wait in a future so it can be easily interrupted
		    syncWaitThread = future;
		    threadPool.execute(future);

		    // get results from the future
		    List<IFDStatusType> events = future.get();

		    // prepare response
		    response = WSHelper.makeResponse(WaitResponse.class, WSHelper.makeResultOK());
		    response.getIFDEvent().addAll(events);
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.FINER)) {
			_logger.exiting(this.getClass().getName(), "wait(Wait parameters)", response);
		    } // </editor-fold>
		    return response;
		}
	    }
	} catch (ExecutionException ex) { // this is the exception from within the future
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "wait(Wait parameters)", ex.getMessage(), ex);
	    } // </editor-fold>
	    return WSHelper.makeResponse(WaitResponse.class, WSHelper.makeResult(ex.getCause()));
	} catch (Throwable t) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "wait(Wait parameters)", t.getMessage(), t);
	    } // </editor-fold>
	    return WSHelper.makeResponse(WaitResponse.class, WSHelper.makeResult(t));
	}
    }


    @Override
    public CancelResponse cancel(Cancel parameters) {
	try {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.entering(this.getClass().getName(), "cancel(Cancel parameters)", parameters);
	    } // </editor-fold>
	    CancelResponse response = null;
	    // check for context handle
	    if (!IFDUtils.arrayEquals(ctxHandle, parameters.getContextHandle())) {
		response = WSHelper.makeResponse(CancelResponse.class, WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, "Invalid context handle specified."));
	    } else {
		if (parameters.getSessionIdentifier() != null) {
		    // async wait
		    String session = parameters.getSessionIdentifier();
		    Future f = this.asyncWaitThreads.get(session);
		    if (f != null) {
			f.cancel(true);
			response = WSHelper.makeResponse(CancelResponse.class, WSHelper.makeResultOK());
		    }
		} else {
		    // sync wait
		    synchronized (this) {
			if (syncWaitThread != null) {
			    syncWaitThread.cancel(true);
			    syncWaitThread = null; // not really needed but seems cleaner
			    response = WSHelper.makeResponse(CancelResponse.class, WSHelper.makeResultOK());
			} else {
			    response = WSHelper.makeResponse(CancelResponse.class, WSHelper.makeResultError(ECardConstants.Minor.IFD.IO.CANCEL_NOT_POSSIBLE, "No synchronous Wait to cancel."));
			}
		    }
		}
		// TODO: other cancel cases
	    }

	    if (response == null) {
		// nothing to cancel
		response = WSHelper.makeResponse(CancelResponse.class, WSHelper.makeResultUnknownError("No cancelable command matches the given parameters."));
	    }
	    // <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "cancel(Cancel parameters)", response);
		} // </editor-fold>
	    return response;
	} catch (Throwable t) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "cancel(Cancel parameters)", t.getMessage(), t);
	    } // </editor-fold>
	    return WSHelper.makeResponse(CancelResponse.class, WSHelper.makeResult(t));
	}
    }

    @Override
    /**
     * Note: the first byte of the command data is the control code.
     */
    public ControlIFDResponse controlIFD(ControlIFD parameters) {
	try {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.entering(this.getClass().getName(), "controlIFD(ControlIFD parameters)", parameters);
	    } // </editor-fold>
	    ControlIFDResponse response;
	    if (!IFDUtils.arrayEquals(ctxHandle, parameters.getContextHandle())) {
		response = WSHelper.makeResponse(ControlIFDResponse.class, WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, "Invalid context handle specified."));
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "controlIFD(ControlIFD parameters)", response);
		} // </editor-fold>
		return response;
	    } else {
		try {
		    SCTerminal t = scwrapper.getTerminal(parameters.getIFDName());
		    byte[] command = parameters.getCommand();
		    byte ctrlCode = command[0];
		    command = Arrays.copyOfRange(command, 1, command.length);
		    // check if the code is present
		    byte[] resultCommand = t.executeCtrlCode(ctrlCode, command);
		    // TODO: evaluate result
		    response = WSHelper.makeResponse(ControlIFDResponse.class, WSHelper.makeResultOK());
		    response.setResponse(resultCommand);
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.FINER)) {
			_logger.exiting(this.getClass().getName(), "controlIFD(ControlIFD parameters)", response);
		    } // </editor-fold>
		    return response;

		} catch (IFDException ex) {
		    response = WSHelper.makeResponse(ControlIFDResponse.class, WSHelper.makeResult(ex));
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.FINER)) {
			_logger.exiting(this.getClass().getName(), "controlIFD(ControlIFD parameters)", response);
		    } // </editor-fold>
		    return response;
		}
	    }
	} catch (Throwable t) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "connect(Connect parameters)", t.getMessage(), t);
	    } // </editor-fold>
	    return WSHelper.makeResponse(ControlIFDResponse.class, WSHelper.makeResult(t));
	}
    }


    @Override
    public ConnectResponse connect(Connect parameters) {
	try {
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.entering(this.getClass().getName(), "connect(Connect parameters)", parameters);
	    }
	    ConnectResponse response;
	    if (!IFDUtils.arrayEquals(ctxHandle, parameters.getContextHandle())) {
		response = WSHelper.makeResponse(ConnectResponse.class, WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, "Invalid context handle specified."));
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "connect(Connect parameters)", response);
		} // </editor-fold>
		return response;
	    } else {
		try {
                    //FIXME
		    if (!IFDUtils.getSlotIndex(parameters.getIFDName()).equals(parameters.getSlot())) {
			response = WSHelper.makeResponse(ConnectResponse.class, WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, "Invalid slot handle."));
			// <editor-fold defaultstate="collapsed" desc="log trace">
			if (_logger.isLoggable(Level.FINER)) {
			    _logger.exiting(this.getClass().getName(), "connect(Connect parameters)", response);
			} // </editor-fold>
			return response;
		    } else {
			SCTerminal t = scwrapper.getTerminal(parameters.getIFDName());
			SCChannel channel = t.connect();
			// make connection exclusive
			Boolean exclusive = parameters.isExclusive();
			if (exclusive != null && exclusive.booleanValue() == true) {
			    BeginTransaction transact = new BeginTransaction();
			    transact.setSlotHandle(channel.getHandle());
			    BeginTransactionResponse resp = beginTransaction(transact);
			    if (resp.getResult().getResultMajor().equals(ECardConstants.Major.ERROR)) {
				response = WSHelper.makeResponse(ConnectResponse.class, resp.getResult());
				// <editor-fold defaultstate="collapsed" desc="log trace">
				if (_logger.isLoggable(Level.FINER)) {
				    _logger.exiting(this.getClass().getName(), "connect(Connect parameters)", response);
				} // </editor-fold>
				return response;
			    }
			}
			// connection established, return result
			response = WSHelper.makeResponse(ConnectResponse.class, WSHelper.makeResultOK());
			response.setSlotHandle(channel.getHandle());
			// <editor-fold defaultstate="collapsed" desc="log trace">
			if (_logger.isLoggable(Level.FINER)) {
			    _logger.exiting(this.getClass().getName(), "connect(Connect parameters)", response);
			} // </editor-fold>
			return response;
		    }
		} catch (IFDException ex) {
		    response = WSHelper.makeResponse(ConnectResponse.class, ex.getResult());
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.WARNING)) {
			_logger.logp(Level.WARNING, this.getClass().getName(), "connect(Connect parameters)", ex.getMessage(), ex);
		    } // </editor-fold>
		    return response;
		}
	    }
	} catch (Throwable t) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "connect(Connect parameters)", t.getMessage(), t);
	    } // </editor-fold>
	    return WSHelper.makeResponse(ConnectResponse.class, WSHelper.makeResult(t));
	}
    }


    @Override
    public synchronized DisconnectResponse disconnect(Disconnect parameters) {
	try {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.entering(this.getClass().getName(), "disconnect(Disconnect parameters)", parameters);
	    } // </editor-fold>
	    DisconnectResponse response;
	    if (!hasContext()) {
		response = WSHelper.makeResponse(DisconnectResponse.class, WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, "Context not initialized."));
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "disconnect(Disconnect parameters)", response);
		} // </editor-fold>
		return response;
	    }

	    byte[] handle = parameters.getSlotHandle();
	    try {
		SCCard c = scwrapper.getCard(handle);
		// TODO: add support for actions
		c.closeChannel(handle, false);
		response = WSHelper.makeResponse(DisconnectResponse.class, WSHelper.makeResultOK());
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "disconnect(Disconnect parameters)", response);
		} // </editor-fold>
		return response;
	    } catch (IFDException ex) {
		response = WSHelper.makeResponse(DisconnectResponse.class, ex.getResult());
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.WARNING)) {
		    _logger.logp(Level.WARNING, this.getClass().getName(), "disconnect(Disconnect parameters)", ex.getMessage(), ex);
		} // </editor-fold>
		return response;
	    }
	} catch (Throwable t) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "disconnect(Disconnect parameters)", t.getMessage(), t);
	    } // </editor-fold>
	    return WSHelper.makeResponse(DisconnectResponse.class, WSHelper.makeResult(t));
	}
    }


    @Override
    public BeginTransactionResponse beginTransaction(BeginTransaction beginTransaction) {
	try {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.entering(this.getClass().getName(), "beginTransaction(BeginTransaction beginTransaction)", beginTransaction);
	    } // </editor-fold>
	    BeginTransactionResponse response;
	    if (!hasContext()) {
		response = WSHelper.makeResponse(BeginTransactionResponse.class, WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, "Context not initialized."));
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "beginTransaction(BeginTransaction beginTransaction)", response);
		} // </editor-fold>
		return response;
	    }
	    byte[] handle = beginTransaction.getSlotHandle();
	    try {
		SCCard c = scwrapper.getCard(handle);
		// TODO: create thread, associate it with the current card instance, and begin exclusive card access
		c.beginExclusive();
		response = WSHelper.makeResponse(BeginTransactionResponse.class, WSHelper.makeResultOK());
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "beginTransaction(BeginTransaction beginTransaction)", response);
		} // </editor-fold>
		return response;
	    } catch (CardException ex) {
		response = WSHelper.makeResponse(BeginTransactionResponse.class, WSHelper.makeResult(ex));
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.WARNING)) {
		    _logger.logp(Level.WARNING, this.getClass().getName(), "BeginTransactionResponse response", ex.getMessage(), ex);
		} // </editor-fold>
		return response;
	    } catch (IFDException ex) {
		response = WSHelper.makeResponse(BeginTransactionResponse.class, ex.getResult());
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.WARNING)) {
		    _logger.logp(Level.WARNING, this.getClass().getName(), "BeginTransactionResponse response", ex.getMessage(), ex);
		} // </editor-fold>
		return response;
	    }
	} catch (Throwable t) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "beginTransaction(BeginTransaction parameters)", t.getMessage(), t);
	    } // </editor-fold>
	    return WSHelper.makeResponse(BeginTransactionResponse.class, WSHelper.makeResult(t));
	}
    }

    @Override
    public EndTransactionResponse endTransaction(EndTransaction parameters) {
	try {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.entering(this.getClass().getName(), "endTransaction(EndTransaction parameters)", parameters);
	    } // </editor-fold>
	    EndTransactionResponse response;
	    if (!hasContext()) {
		response = WSHelper.makeResponse(EndTransactionResponse.class, WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, "Context not initialized."));
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "endTransaction(EndTransaction parameters)", response);
		} // </editor-fold>
		return response;
	    }
	    byte[] handle = parameters.getSlotHandle();
	    try {
		SCCard c = scwrapper.getCard(handle);
		// TODO: retrieve thread associated with the current card instance and end exclusive card access
		c.endExclusive();
		response = WSHelper.makeResponse(EndTransactionResponse.class, WSHelper.makeResultOK());
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "endTransaction(EndTransaction parameters)", response);
		} // </editor-fold>
		return response;
	    } catch (CardException ex) {
		response = WSHelper.makeResponse(EndTransactionResponse.class, WSHelper.makeResult(ex));
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.WARNING)) {
		    _logger.logp(Level.WARNING, this.getClass().getName(), "endTransaction(EndTransaction parameters)", ex.getMessage(), ex);
		} // </editor-fold>
		return response;
	    } catch (IFDException ex) {
		response = WSHelper.makeResponse(EndTransactionResponse.class, ex.getResult());
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.WARNING)) {
		    _logger.logp(Level.WARNING, this.getClass().getName(), "endTransaction(EndTransaction parameters)", ex.getMessage(), ex);
		} // </editor-fold>
		return response;
	    }
	} catch (Throwable t) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "endTransaction(EndTransaction parameters)", t.getMessage(), t);
	    } // </editor-fold>
	    return WSHelper.makeResponse(EndTransactionResponse.class, WSHelper.makeResult(t));
	}
    }


    @Override
    public TransmitResponse transmit(Transmit parameters) {
	try {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.entering(this.getClass().getName(), "transmit(Transmit parameters)", parameters);
	    } // </editor-fold>
	    TransmitResponse response;
	    if (!hasContext()) {
		response = WSHelper.makeResponse(TransmitResponse.class, WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, "Context not initialized."));
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "transmit(Transmit parameters)", response);
		} // </editor-fold>
		return response;
	    }

	    byte[] handle = parameters.getSlotHandle();
	    List<InputAPDUInfoType> apdus = parameters.getInputAPDUInfo();

	    response = WSHelper.makeResponse(TransmitResponse.class, null);
	    Result result;
	    List<byte[]> rapdus = response.getOutputAPDU();
	    try {
		SCChannel ch = scwrapper.getChannel(handle);
		for (InputAPDUInfoType capdu : apdus) {
		    byte[] rapdu = ch.transmit(capdu.getInputAPDU(), capdu.getAcceptableStatusCode());
		    rapdus.add(rapdu);
		}
		result = WSHelper.makeResultOK();
	    } catch (TransmitException ex) {
		rapdus.add(ex.getResponseAPDU());
		result = ex.getResult();
	    } catch (IFDException ex) {
		result = ex.getResult();
	    }

	    response.setResult(result);
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.exiting(this.getClass().getName(), "transmit(Transmit parameters)", response);
	    } // </editor-fold>
	    return response;
	} catch (Throwable t) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "transmit(Transmit parameters)", t.getMessage(), t);
	    } // </editor-fold>
	    return WSHelper.makeResponse(TransmitResponse.class, WSHelper.makeResult(t));
	}
    }


    @Override
    public VerifyUserResponse verifyUser(VerifyUser parameters) {
	try {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.entering(this.getClass().getName(), "verifyUser(VerifyUser parameters)", parameters);
	    } // </editor-fold>
	    VerifyUserResponse response;
	    if (!hasContext()) {
		response = WSHelper.makeResponse(VerifyUserResponse.class, WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, "Context not initialized."));
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "verifyUser(VerifyUser parameters)", response);
		} // </editor-fold>
		return response;
	    }

	    AbstractTerminal aTerm = new AbstractTerminal(this, scwrapper, gui, ctxHandle, parameters.getDisplayIndex());
	    try {
		response = aTerm.verifyUser(parameters);
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "verifyUser(VerifyUser parameters)", response);
		} // </editor-fold>
		return response;
	    } catch (IFDException ex) {
		response = WSHelper.makeResponse(VerifyUserResponse.class, ex.getResult());
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "verifyUser(VerifyUser parameters)", response);
		} // </editor-fold>
		return response;
	    }
	} catch (Throwable t) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "verifyUser(VerifyUser parameters)", t.getMessage(), t);
	    } // </editor-fold>
	    return WSHelper.makeResponse(VerifyUserResponse.class, WSHelper.makeResult(t));
	}
    }


    @Override
    public ModifyVerificationDataResponse modifyVerificationData(ModifyVerificationData parameters) {
	try {
	    if (!hasContext()) {
		return WSHelper.makeResponse(ModifyVerificationDataResponse.class, WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, "Context not initialized."));
	    }
	    throw new UnsupportedOperationException("Not supported yet.");
	} catch (Throwable t) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "modifyVerificationData(ModifyVerificationData parameters)", t.getMessage(), t);
	    } // </editor-fold>
	    return WSHelper.makeResponse(ModifyVerificationDataResponse.class, WSHelper.makeResult(t));
	}
    }


    @Override
    public OutputResponse output(Output parameters) {
	try {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.FINER)) {
		_logger.entering(this.getClass().getName(), "output(Output parameters)", parameters);
	    } // </editor-fold>
	    OutputResponse response;
	    if (!IFDUtils.arrayEquals(ctxHandle, parameters.getContextHandle())) {
		response = WSHelper.makeResponse(OutputResponse.class, WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, "Invalid context handle specified."));
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "output(Output parameters)", response);
		} // </editor-fold>
		return response;
	    } else {
		String ifdName = parameters.getIFDName();
		OutputInfoType outInfo = parameters.getOutputInfo();

		AbstractTerminal aTerm = new AbstractTerminal(this, scwrapper, gui, ctxHandle, outInfo.getDisplayIndex());
		try {
		    aTerm.output(ifdName, outInfo);
		} catch (IFDException ex) {
		    response = WSHelper.makeResponse(OutputResponse.class, ex.getResult());
		    // <editor-fold defaultstate="collapsed" desc="log trace">
		    if (_logger.isLoggable(Level.FINER)) {
			_logger.exiting(this.getClass().getName(), "output(Output parameters)", response);
		    } // </editor-fold>
		    return response;
		}
		response = WSHelper.makeResponse(OutputResponse.class, WSHelper.makeResultOK());
		// <editor-fold defaultstate="collapsed" desc="log trace">
		if (_logger.isLoggable(Level.FINER)) {
		    _logger.exiting(this.getClass().getName(), "output(Output parameters)", response);
		} // </editor-fold>
		return response;
	    }
	} catch (Throwable t) {
	    // <editor-fold defaultstate="collapsed" desc="log trace">
	    if (_logger.isLoggable(Level.WARNING)) {
		_logger.logp(Level.WARNING, this.getClass().getName(), "output(Output parameters)", t.getMessage(), t);
	    } // </editor-fold>
	    return WSHelper.makeResponse(OutputResponse.class, WSHelper.makeResult(t));
	}
    }


    @Override
    public EstablishChannelResponse establishChannel(EstablishChannel parameters) {
	byte[] slotHandle = parameters.getSlotHandle();
	try {
	    SCTerminal term = this.scwrapper.getTerminal(slotHandle);
	    SCCard card = this.scwrapper.getCard(slotHandle);
	    SCChannel channel = card.getChannel(slotHandle);
	    DIDAuthenticationDataType protoParam = parameters.getAuthenticationProtocolData();
	    String protocol = protoParam.getProtocol();

	    // check if it is PACE and try to perform native implementation
	    // get pace capabilities
	    List<Long> paceCapabilities = term.getPACECapabilities();
	    List<String> supportedProtos = buildPACEProtocolList(paceCapabilities);
	    // check out if this actually a PACE request
	    if (!supportedProtos.isEmpty() && supportedProtos.get(0).startsWith(protocol)) { // i don't care which type is supported, i try it anyways
		// yeah, PACE seems to be supported by the reader, big win
		PACEInputType paceParam = new PACEInputType(protoParam);
		// extract variables needed for pace
		byte pinID = paceParam.getPINID();
		// optional elements
		byte[] chat = paceParam.getCHAT();
		String pin = paceParam.getPIN();
		byte[] certDesc = paceParam.getCertificateDescription();

		// prepare pace data structures
		EstablishPACERequest estPaceReq = new EstablishPACERequest(pinID, chat, null, certDesc); // TODO: add supplied PIN
		ExecutePACERequest  execPaceReq = new ExecutePACERequest(ExecutePACERequest.Function.EstablishPACEChannel, estPaceReq.toBytes());
		// see if PACE type demanded for this input value combination is supported
		if (estPaceReq.isSupportedType(paceCapabilities)) {
		    byte[] reqData = execPaceReq.toBytes();
		    // execute pace
		    byte[] resData = term.executeCtrlCode(PCSCFeatures.EXECUTE_PACE, reqData);
		    // evaluate response
		    ExecutePACEResponse execPaceRes = new ExecutePACEResponse(resData);
		    if (execPaceRes.isError()) {
			return WSHelper.makeResponse(EstablishChannelResponse.class, execPaceRes.getResult());
		    }
		    EstablishPACEResponse estPaceRes = new EstablishPACEResponse(execPaceRes.getData());
		    // get values and prepare response
		    PACEOutputType authDataResponse = paceParam.getOutputType();
		    // mandatory fields
		    authDataResponse.setStatusbytes(estPaceRes.getStatus());
		    authDataResponse.setEFCardAccess(estPaceRes.getEFCardAccess());
		    // optional fields
		    if (estPaceRes.hasCurrentCAR()) {
			authDataResponse.setCurrentCAR(estPaceRes.getCurrentCAR());
		    }
		    if (estPaceRes.hasPreviousCAR()) {
			authDataResponse.setPreviousCAR(estPaceRes.getPreviousCAR());
		    }
		    if (estPaceRes.hasIDICC()) {
			authDataResponse.setIDICC(estPaceRes.getIDICC());
		    }
		    // create response type and return
		    EstablishChannelResponse response = WSHelper.makeResponse(EstablishChannelResponse.class, WSHelper.makeResultOK());
		    response.setAuthenticationProtocolData(authDataResponse.getAuthDataType());
		    return response;
		}
	    } // end native pace support

	    // check out available software protocols
	    if (this.protocolFactories.contains(protocol)) {
		ProtocolFactory factory = this.protocolFactories.get(protocol);
		Protocol protoImpl = factory.createInstance();
		EstablishChannelResponse response = protoImpl.establish(parameters, this, null); // TODO: hand over GUI implementation
		// register protocol instance for secure messaging when protocol was processed successful
		if (response.getResult().getResultMajor().equals(ECardConstants.Major.OK)) {
		    channel.addSecureMessaging(protoImpl);
		}
		return response;
	    }

	    // if this point is reached a native implementation is not present, try registered protocols
	    return WSHelper.makeResponse(EstablishChannelResponse.class, WSHelper.makeResultUnknownError("No such protocol available in this IFD."));
	} catch (Throwable t) {
	    return WSHelper.makeResponse(EstablishChannelResponse.class, WSHelper.makeResult(t));
	}
    }

    @Override
    public DestroyChannelResponse destroyChannel(DestroyChannel parameters) {
	try {
	    return WSHelper.makeResponse(DestroyChannelResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
	} catch (Throwable t) {
	    return WSHelper.makeResponse(DestroyChannelResponse.class, WSHelper.makeResult(t));
	}
    }

}
