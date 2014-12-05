/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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

import iso.std.iso_iec._24727.tech.schema.BeginTransaction;
import iso.std.iso_iec._24727.tech.schema.BeginTransactionResponse;
import iso.std.iso_iec._24727.tech.schema.Cancel;
import iso.std.iso_iec._24727.tech.schema.CancelResponse;
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectResponse;
import iso.std.iso_iec._24727.tech.schema.ControlIFD;
import iso.std.iso_iec._24727.tech.schema.ControlIFDResponse;
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType;
import iso.std.iso_iec._24727.tech.schema.DestroyChannel;
import iso.std.iso_iec._24727.tech.schema.DestroyChannelResponse;
import iso.std.iso_iec._24727.tech.schema.Disconnect;
import iso.std.iso_iec._24727.tech.schema.DisconnectResponse;
import iso.std.iso_iec._24727.tech.schema.DisplayCapabilityType;
import iso.std.iso_iec._24727.tech.schema.EndTransaction;
import iso.std.iso_iec._24727.tech.schema.EndTransactionResponse;
import iso.std.iso_iec._24727.tech.schema.EstablishChannel;
import iso.std.iso_iec._24727.tech.schema.EstablishChannelResponse;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse;
import iso.std.iso_iec._24727.tech.schema.GetStatus;
import iso.std.iso_iec._24727.tech.schema.GetStatusResponse;
import iso.std.iso_iec._24727.tech.schema.IFDCapabilitiesType;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType;
import iso.std.iso_iec._24727.tech.schema.KeyPadCapabilityType;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import iso.std.iso_iec._24727.tech.schema.ModifyVerificationData;
import iso.std.iso_iec._24727.tech.schema.ModifyVerificationDataResponse;
import iso.std.iso_iec._24727.tech.schema.Output;
import iso.std.iso_iec._24727.tech.schema.OutputInfoType;
import iso.std.iso_iec._24727.tech.schema.OutputResponse;
import iso.std.iso_iec._24727.tech.schema.ReleaseContext;
import iso.std.iso_iec._24727.tech.schema.ReleaseContextResponse;
import iso.std.iso_iec._24727.tech.schema.SlotCapabilityType;
import iso.std.iso_iec._24727.tech.schema.Transmit;
import iso.std.iso_iec._24727.tech.schema.TransmitResponse;
import iso.std.iso_iec._24727.tech.schema.VerifyUser;
import iso.std.iso_iec._24727.tech.schema.VerifyUserResponse;
import iso.std.iso_iec._24727.tech.schema.Wait;
import iso.std.iso_iec._24727.tech.schema.WaitResponse;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import javax.jws.WebService;
import javax.smartcardio.CardException;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.common.ifd.PACECapabilities;
import org.openecard.common.ifd.Protocol;
import org.openecard.common.ifd.ProtocolFactory;
import org.openecard.common.ifd.anytype.PACEInputType;
import org.openecard.common.ifd.anytype.PACEOutputType;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.Publish;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.ValueGenerators;
import org.openecard.gui.UserConsent;
import org.openecard.ifd.scio.reader.EstablishPACERequest;
import org.openecard.ifd.scio.reader.EstablishPACEResponse;
import org.openecard.ifd.scio.reader.ExecutePACERequest;
import org.openecard.ifd.scio.reader.ExecutePACEResponse;
import org.openecard.ifd.scio.reader.PCSCFeatures;
import org.openecard.ifd.scio.wrapper.SCCard;
import org.openecard.ifd.scio.wrapper.SCChannel;
import org.openecard.ifd.scio.wrapper.SCTerminal;
import org.openecard.ifd.scio.wrapper.SCWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
@WebService(endpointInterface = "org.openecard.ws.IFD")
public class IFD implements org.openecard.ws.IFD {

    private static final Logger _logger = LoggerFactory.getLogger(IFD.class);

    private byte[] ctxHandle = null;
    private SCWrapper scwrapper;
    private Dispatcher dispatcher;
    private UserConsent gui = null;
    private final ProtocolFactories protocolFactories = new ProtocolFactories();

    private AtomicInteger numClients;
    private ExecutorService threadPool;
    private ConcurrentSkipListMap<String,Future<List<IFDStatusType>>> asyncWaitThreads;
    private Future<List<IFDStatusType>> syncWaitThread;


    protected synchronized void removeAsnycTerminal(String session) {
	if (asyncWaitThreads != null) { // be sure the list still exists
	    asyncWaitThreads.remove(session);
	}
    }

    private boolean hasContext() {
	boolean hasContext = ctxHandle != null;
	return hasContext;
    }

    public void setGUI(UserConsent gui) {
	this.gui = gui;
    }

    public void setDispatcher(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    public boolean addProtocol(String proto, ProtocolFactory factory) {
	return protocolFactories.add(proto, factory);
    }


    @Override
    public synchronized EstablishContextResponse establishContext(EstablishContext parameters) {
	try {
	    // on first call, create a new unique handle
	    if (ctxHandle == null) {
		scwrapper = new SCWrapper();
		ctxHandle = scwrapper.createHandle(ECardConstants.CONTEXT_HANDLE_DEFAULT_SIZE);
		numClients = new AtomicInteger(1);
		threadPool = Executors.newCachedThreadPool();
		asyncWaitThreads = new ConcurrentSkipListMap<>();
	    } else {
		// on second or further calls, increment usage counter
		numClients.incrementAndGet();
	    }

	    // prepare response
	    EstablishContextResponse response = WSHelper.makeResponse(EstablishContextResponse.class, WSHelper.makeResultOK());
	    response.setContextHandle(ctxHandle);
	    return response;
	} catch (Exception ex) {
	    if (ex instanceof RuntimeException) {
		throw (RuntimeException)ex;
	    }
	    _logger.warn(ex.getMessage(), ex);
	    return WSHelper.makeResponse(EstablishContextResponse.class, WSHelper.makeResult(ex));
	}
    }

    @Override
    public synchronized ReleaseContextResponse releaseContext(ReleaseContext parameters) {
	try {
	    ReleaseContextResponse response;
	    if (ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
		if (numClients.decrementAndGet() == 0) { // last client detaches
		    ctxHandle = null;
		    numClients = null;
		    // terminate thread pool
		    threadPool.shutdownNow(); // wait for threads to die and block new requests
		    // just assume it worked ... and don't wait
		    threadPool = null;
		    asyncWaitThreads = null;
		}

		response = WSHelper.makeResponse(ReleaseContextResponse.class, WSHelper.makeResultOK());
		return response;
	    } else {
		String msg = "Invalid context handle specified.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg);
		response = WSHelper.makeResponse(ReleaseContextResponse.class, r);
		return response;
	    }
	} catch (Exception ex) {
	    if (ex instanceof RuntimeException) {
		throw (RuntimeException)ex;
	    }
	    _logger.warn(ex.getMessage(), ex);
	    return WSHelper.makeResponse(ReleaseContextResponse.class, WSHelper.makeResult(ex));
	}
    }


    @Override
    public ListIFDsResponse listIFDs(ListIFDs parameters) {
	try {
	    ListIFDsResponse response;
	    if (! ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
		String msg = "Invalid context handle specified.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg);
		response = WSHelper.makeResponse(ListIFDsResponse.class, r);
		return response;
	    } else {
		try {
		    List<String> ifds = scwrapper.getTerminalNames(true);
		    response = WSHelper.makeResponse(ListIFDsResponse.class, WSHelper.makeResultOK());
		    response.getIFDName().addAll(ifds);
		    return response;
		} catch (IFDException ex) {
		    response = WSHelper.makeResponse(ListIFDsResponse.class, ex.getResult());
		    _logger.warn(ex.getMessage(), ex);
		    return response;
		}
	    }
	} catch (Exception ex) {
	    if (ex instanceof RuntimeException) {
		throw (RuntimeException)ex;
	    }
	    _logger.warn(ex.getMessage(), ex);
	    return WSHelper.makeResponse(ListIFDsResponse.class, WSHelper.makeResult(ex));
	}
    }


    private List<String> buildPACEProtocolList(List<PACECapabilities.PACECapability> paceCapabilities) {
	List<String> supportedProtos = new LinkedList<>();
	for (PACECapabilities.PACECapability next : paceCapabilities) {
	    supportedProtos.add(next.getProtocol());
	}
	return supportedProtos;
    }

    @Override
    public GetIFDCapabilitiesResponse getIFDCapabilities(GetIFDCapabilities parameters) {
	try {
	    GetIFDCapabilitiesResponse response;
	    if (! ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
		String msg = "Invalid context handle specified.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg);
		response = WSHelper.makeResponse(GetIFDCapabilitiesResponse.class, r);
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
			    List<PACECapabilities.PACECapability> capabilities = t.getPACECapabilities();
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
		    return response;
		} catch (IFDException ex) {
		    response = WSHelper.makeResponse(GetIFDCapabilitiesResponse.class, ex.getResult());
		    _logger.warn(ex.getMessage(), ex);
		    return response;
		}
	    }
	} catch (Exception ex) {
	    if (ex instanceof RuntimeException) {
		throw (RuntimeException) ex;
	    }
	    _logger.warn(ex.getMessage(), ex);
	    return WSHelper.makeResponse(GetIFDCapabilitiesResponse.class, WSHelper.makeResult(ex));
	}
    }


    @Override
    public GetStatusResponse getStatus(GetStatus parameters) {
	try {
	    GetStatusResponse response;
	    //FIXME
	    if (! ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
		String msg = "Invalid context handle specified.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg);
		response = WSHelper.makeResponse(GetStatusResponse.class, r);
		return response;
	    } else {
		// get ifd name from request or directly from the sc-io
		List<SCTerminal> ifds = new LinkedList<>();
		// get ifd names which should be investigated
		try {
		    if (parameters.getIFDName() != null) {
			SCTerminal t = scwrapper.getTerminal(parameters.getIFDName(), true);
			if (t == null) {
			    String minor = ECardConstants.Minor.IFD.Terminal.UNKNOWN_IFD;
			    Result r = WSHelper.makeResult(ECardConstants.Major.ERROR, minor, "Unknown IFD.");
			    response = WSHelper.makeResponse(GetStatusResponse.class, r);
			    return response;
			} else {
			    ifds.add(t);
			}
		    } else {
			ifds.addAll(scwrapper.getTerminals(true));
		    }
		} catch (IFDException ex) {
		    response = WSHelper.makeResponse(GetStatusResponse.class, ex.getResult());
		    _logger.warn(ex.getMessage(), ex);
		    return response;
		}

		// request status for each ifd
		ArrayList<IFDStatusType> statuss = new ArrayList<>(ifds.size());
		for (SCTerminal ifd : ifds) {
		    try {
			IFDStatusType s = ifd.getStatus();
			statuss.add(s);
		    } catch (IFDException ex) {
			response = WSHelper.makeResponse(GetStatusResponse.class, ex.getResult());
			_logger.warn(ex.getMessage(), ex);
			return response;
		    }
		}
		// everything worked out well
		response = WSHelper.makeResponse(GetStatusResponse.class, WSHelper.makeResultOK());
		response.getIFDStatus().addAll(statuss);
		return response;
	    }
	} catch (Exception ex) {
	    if (ex instanceof RuntimeException) {
		throw (RuntimeException)ex;
	    }
	    _logger.warn(ex.getMessage(), ex);
	    return WSHelper.makeResponse(GetStatusResponse.class, WSHelper.makeResult(ex));
	}
    }


    @Override
    public WaitResponse wait(Wait parameters) {
	try {
	    WaitResponse response;
	    // check for context handle
	    if (! ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
		String msg = "Invalid context handle specified.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg);
		response = WSHelper.makeResponse(WaitResponse.class, r);
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
			return response;
		    }
		    expectedStatuses = status.getIFDStatus();
		} else {
		    // check that ifdname is present, needed for comparison
		    for (IFDStatusType s : expectedStatuses) {
			if (s.getIFDName() == null) {
			    String msg = "IFD in a request IFDStatus not known.";
			    Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.Terminal.UNKNOWN_IFD, msg);
			    response = WSHelper.makeResponse(WaitResponse.class, r);
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
		    newCallback.setSessionIdentifier(ValueGenerators.genBase64Session());
		    callback = newCallback;
		}

		// create the event and fire
		EventListener l = new EventListener(this, scwrapper, threadPool, ctxHandle, timeout.longValue(), callback, expectedStatuses, withNew);
		FutureTask<List<IFDStatusType>> future = new FutureTask<>(l);

		if (l.isAsync()) {
		    // add future to async wait list
		    asyncWaitThreads.put(callback.getSessionIdentifier(), future);
		    threadPool.execute(future); // finally run this darn thingy

		    // prepare result with session id in it
		    response = WSHelper.makeResponse(WaitResponse.class, WSHelper.makeResultOK());
		    if (callback.getSessionIdentifier() != null) {
			response.setSessionIdentifier(callback.getSessionIdentifier());
		    }
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
		    return response;
		}
	    }
	} catch (ExecutionException ex) { // this is the exception from within the future
	    _logger.warn(ex.getMessage(), ex);
	    return WSHelper.makeResponse(WaitResponse.class, WSHelper.makeResult(ex.getCause()));
	} catch (Exception ex) {
	    if (ex instanceof RuntimeException) {
		throw (RuntimeException)ex;
	    }
	    _logger.warn(ex.getMessage(), ex);
	    return WSHelper.makeResponse(WaitResponse.class, WSHelper.makeResult(ex));
	}
    }


    @Override
    public CancelResponse cancel(Cancel parameters) {
	try {
	    CancelResponse response = null;
	    // check for context handle
	    if (! ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
		String msg = "Invalid context handle specified.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg);
		response = WSHelper.makeResponse(CancelResponse.class, r);
	    } else {
		if (parameters.getSessionIdentifier() != null) {
		    // async wait
		    String session = parameters.getSessionIdentifier();
		    Future<List<IFDStatusType>> f = this.asyncWaitThreads.get(session);
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
			    String msg = "No synchronous Wait to cancel.";
			    Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.IO.CANCEL_NOT_POSSIBLE, msg);
			    response = WSHelper.makeResponse(CancelResponse.class, r);
			}
		    }
		}
		// TODO: other cancel cases
	    }

	    if (response == null) {
		// nothing to cancel
		String msg = "No cancelable command matches the given parameters.";
		response = WSHelper.makeResponse(CancelResponse.class, WSHelper.makeResultUnknownError(msg));
	    }
	    return response;
	} catch (Exception ex) {
	    if (ex instanceof RuntimeException) {
		throw (RuntimeException)ex;
	    }
	    _logger.warn(ex.getMessage(), ex);
	    return WSHelper.makeResponse(CancelResponse.class, WSHelper.makeResult(ex));
	}
    }


    /**
     * Note: the first byte of the command data is the control code.
     */
    @Override
    public ControlIFDResponse controlIFD(ControlIFD parameters) {
	try {
	    ControlIFDResponse response;
	    if (! ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
		String msg = "Invalid context handle specified.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg);
		response = WSHelper.makeResponse(ControlIFDResponse.class, r);
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
		    Result result = evaluateControlIFDRAPDU(resultCommand);
		    response = WSHelper.makeResponse(ControlIFDResponse.class, result);
		    response.setResponse(resultCommand);
		    return response;

		} catch (IFDException ex) {
		    response = WSHelper.makeResponse(ControlIFDResponse.class, WSHelper.makeResult(ex));
		    return response;
		}
	    }
	} catch (Throwable ex) {
	    if (ex instanceof RuntimeException) {
		throw (RuntimeException)ex;
	    }
	    _logger.warn(ex.getMessage(), ex);
	    return WSHelper.makeResponse(ControlIFDResponse.class, WSHelper.makeResult(ex));
	}
    }


    @Override
    public ConnectResponse connect(Connect parameters) {
	try {
	    ConnectResponse response;
	    if (! ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
		String msg = "Invalid context handle specified.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg);
		response = WSHelper.makeResponse(ConnectResponse.class, r);
		return response;
	    } else {
		try {
		    //FIXME
		    if (! IFDUtils.getSlotIndex(parameters.getIFDName()).equals(parameters.getSlot())) {
			String msg = "Invalid slot handle.";
			Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg);
			response = WSHelper.makeResponse(ConnectResponse.class, r);
			return response;
		    } else {
			SCTerminal t = scwrapper.getTerminal(parameters.getIFDName());
			SCChannel channel = t.connect();
			// make connection exclusive
			Boolean exclusive = parameters.isExclusive();
			if (exclusive != null && exclusive == true) {
			    BeginTransaction transact = new BeginTransaction();
			    transact.setSlotHandle(channel.getHandle());
			    BeginTransactionResponse resp = beginTransaction(transact);
			    if (resp.getResult().getResultMajor().equals(ECardConstants.Major.ERROR)) {
				response = WSHelper.makeResponse(ConnectResponse.class, resp.getResult());
				return response;
			    }
			}
			// connection established, return result
			response = WSHelper.makeResponse(ConnectResponse.class, WSHelper.makeResultOK());
			response.setSlotHandle(channel.getHandle());
			return response;
		    }
		} catch (IFDException ex) {
		    response = WSHelper.makeResponse(ConnectResponse.class, ex.getResult());
		    _logger.warn(ex.getMessage(), ex);
		    return response;
		}
	    }
	} catch (Exception ex) {
	    if (ex instanceof RuntimeException) {
		throw (RuntimeException)ex;
	    }
	    _logger.warn(ex.getMessage(), ex);
	    return WSHelper.makeResponse(ConnectResponse.class, WSHelper.makeResult(ex));
	}
    }


    @Override
    public synchronized DisconnectResponse disconnect(Disconnect parameters) {
	try {
	    DisconnectResponse response;
	    if (! hasContext()) {
		String msg = "Context not initialized.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg);
		response = WSHelper.makeResponse(DisconnectResponse.class, r);
		return response;
	    }

	    byte[] handle = parameters.getSlotHandle();
	    try {
		SCCard c = scwrapper.getCard(handle);
		// TODO: add support for actions
		c.closeChannel(handle, false);
		response = WSHelper.makeResponse(DisconnectResponse.class, WSHelper.makeResultOK());
		return response;
	    } catch (IFDException ex) {
		response = WSHelper.makeResponse(DisconnectResponse.class, ex.getResult());
		_logger.warn(ex.getMessage(), ex);
		return response;
	    }
	} catch (Exception ex) {
	    if (ex instanceof RuntimeException) {
		throw (RuntimeException)ex;
	    }
	    _logger.warn(ex.getMessage(), ex);
	    return WSHelper.makeResponse(DisconnectResponse.class, WSHelper.makeResult(ex));
	}
    }


    @Override
    public BeginTransactionResponse beginTransaction(BeginTransaction beginTransaction) {
	try {
	    BeginTransactionResponse response;
	    if (! hasContext()) {
		String msg = "Context not initialized.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg);
		response = WSHelper.makeResponse(BeginTransactionResponse.class, r);
		return response;
	    }
	    byte[] handle = beginTransaction.getSlotHandle();
	    try {
		SCCard c = scwrapper.getCard(handle);
		// TODO: create thread, associate it with the current card instance, and begin exclusive card access
		c.beginExclusive();
		response = WSHelper.makeResponse(BeginTransactionResponse.class, WSHelper.makeResultOK());
		return response;
	    } catch (CardException ex) {
		response = WSHelper.makeResponse(BeginTransactionResponse.class, WSHelper.makeResult(ex));
		_logger.warn(ex.getMessage(), ex);
		return response;
	    } catch (IFDException ex) {
		response = WSHelper.makeResponse(BeginTransactionResponse.class, ex.getResult());
		_logger.warn(ex.getMessage(), ex);
		return response;
	    }
	} catch (Exception ex) {
	    if (ex instanceof RuntimeException) {
		throw (RuntimeException)ex;
	    }
	    _logger.warn(ex.getMessage(), ex);
	    return WSHelper.makeResponse(BeginTransactionResponse.class, WSHelper.makeResult(ex));
	}
    }

    @Override
    public EndTransactionResponse endTransaction(EndTransaction parameters) {
	try {
	    EndTransactionResponse response;
	    if (! hasContext()) {
		String msg = "Context not initialized.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg);
		response = WSHelper.makeResponse(EndTransactionResponse.class, r);
		return response;
	    }
	    byte[] handle = parameters.getSlotHandle();
	    try {
		SCCard c = scwrapper.getCard(handle);
		// TODO: retrieve thread associated with the current card instance and end exclusive card access
		c.endExclusive();
		response = WSHelper.makeResponse(EndTransactionResponse.class, WSHelper.makeResultOK());
		return response;
	    } catch (CardException ex) {
		response = WSHelper.makeResponse(EndTransactionResponse.class, WSHelper.makeResult(ex));
		_logger.warn(ex.getMessage(), ex);
		return response;
	    } catch (IFDException ex) {
		response = WSHelper.makeResponse(EndTransactionResponse.class, ex.getResult());
		_logger.warn(ex.getMessage(), ex);
		return response;
	    }
	} catch (Exception ex) {
	    if (ex instanceof RuntimeException) {
		throw (RuntimeException)ex;
	    }
	    _logger.warn(ex.getMessage(), ex);
	    return WSHelper.makeResponse(EndTransactionResponse.class, WSHelper.makeResult(ex));
	}
    }


    @Publish
    @Override
    public TransmitResponse transmit(Transmit parameters) {
	try {
	    TransmitResponse response;
	    if (! hasContext()) {
		String msg = "Context not initialized.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg);
		response = WSHelper.makeResponse(TransmitResponse.class, r);
		return response;
	    }

	    byte[] handle = parameters.getSlotHandle();
	    List<InputAPDUInfoType> apdus = parameters.getInputAPDUInfo();

	    // check that the apdus contain sane values
	    for (InputAPDUInfoType apdu : apdus) {
		for (byte[] code : apdu.getAcceptableStatusCode()) {
		    if (code.length == 0 || code.length > 2) {
			String msg = "Invalid accepted status code given.";
			Result r = WSHelper.makeResultError(ECardConstants.Minor.App.PARM_ERROR, msg);
			response = WSHelper.makeResponse(TransmitResponse.class, r);
			return response;
		    }
		}
	    }

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
	    return response;
	} catch (Exception ex) {
	    if (ex instanceof RuntimeException) {
		throw (RuntimeException)ex;
	    }
	    _logger.warn(ex.getMessage(), ex);
	    return WSHelper.makeResponse(TransmitResponse.class, WSHelper.makeResult(ex));
	}
    }


    @Override
    public VerifyUserResponse verifyUser(VerifyUser parameters) {
	try {
	    VerifyUserResponse response;
	    if (! hasContext()) {
		String msg = "Context not initialized.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg);
		response = WSHelper.makeResponse(VerifyUserResponse.class, r);
		return response;
	    }

	    AbstractTerminal aTerm = new AbstractTerminal(this, scwrapper, gui, ctxHandle, parameters.getDisplayIndex());
	    try {
		response = aTerm.verifyUser(parameters);
		return response;
	    } catch (IFDException ex) {
		response = WSHelper.makeResponse(VerifyUserResponse.class, ex.getResult());
		return response;
	    }
	} catch (Exception ex) {
	    if (ex instanceof RuntimeException) {
		throw (RuntimeException)ex;
	    }
	    _logger.warn(ex.getMessage(), ex);
	    return WSHelper.makeResponse(VerifyUserResponse.class, WSHelper.makeResult(ex));
	}
    }


    @Override
    public ModifyVerificationDataResponse modifyVerificationData(ModifyVerificationData parameters) {
	try {
	    if (! hasContext()) {
		String msg = "Context not initialized.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE, msg);
		return WSHelper.makeResponse(ModifyVerificationDataResponse.class, r);
	    }
	    throw new UnsupportedOperationException("Not supported yet.");
	} catch (Exception ex) {
	    if (ex instanceof RuntimeException) {
		throw (RuntimeException)ex;
	    }
	    _logger.warn(ex.getMessage(), ex);
	    return WSHelper.makeResponse(ModifyVerificationDataResponse.class, WSHelper.makeResult(ex));
	}
    }


    @Override
    public OutputResponse output(Output parameters) {
	try {
	    OutputResponse response;
	    if (! ByteUtils.compare(ctxHandle, parameters.getContextHandle())) {
		String msg = "Invalid context handle specified.";
		Result r = WSHelper.makeResultError(ECardConstants.Minor.IFD.INVALID_CONTEXT_HANDLE, msg);
		response = WSHelper.makeResponse(OutputResponse.class, r);
		return response;
	    } else {
		String ifdName = parameters.getIFDName();
		OutputInfoType outInfo = parameters.getOutputInfo();

		AbstractTerminal aTerm = new AbstractTerminal(this, scwrapper, gui, ctxHandle, outInfo.getDisplayIndex());
		try {
		    aTerm.output(ifdName, outInfo);
		} catch (IFDException ex) {
		    response = WSHelper.makeResponse(OutputResponse.class, ex.getResult());
		    return response;
		}
		response = WSHelper.makeResponse(OutputResponse.class, WSHelper.makeResultOK());
		return response;
	    }
	} catch (Exception ex) {
	    if (ex instanceof RuntimeException) {
		throw (RuntimeException)ex;
	    }
	    _logger.warn(ex.getMessage(), ex);
	    return WSHelper.makeResponse(OutputResponse.class, WSHelper.makeResult(ex));
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
	    List<PACECapabilities.PACECapability> paceCapabilities = term.getPACECapabilities();
	    List<String> supportedProtos = buildPACEProtocolList(paceCapabilities);
	    // check out if this actually a PACE request
	    // FIXME: check type of protocol

	    // i don't care which type is supported, i try it anyways
	    if (! supportedProtos.isEmpty() && supportedProtos.get(0).startsWith(protocol)) {
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
		// TODO: check if this additional check is really necessary
		if (estPaceReq.isSupportedType(paceCapabilities)) {
		    byte[] reqData = execPaceReq.toBytes();
		    _logger.debug("executeCtrlCode request: {}", ByteUtils.toHexString(reqData));
		    // execute pace
		    byte[] resData = term.executeCtrlCode(PCSCFeatures.EXECUTE_PACE, reqData);
		    _logger.debug("Response of executeCtrlCode: {}", ByteUtils.toHexString(resData));
		    // evaluate response
		    ExecutePACEResponse execPaceRes = new ExecutePACEResponse(resData);
		    if (execPaceRes.isError()) {
			return WSHelper.makeResponse(EstablishChannelResponse.class, execPaceRes.getResult());
		    }
		    EstablishPACEResponse estPaceRes = new EstablishPACEResponse(execPaceRes.getData());
		    // get values and prepare response
		    PACEOutputType authDataResponse = paceParam.getOutputType();
		    // mandatory fields
		    authDataResponse.setRetryCounter(estPaceRes.getRetryCounter());
		    authDataResponse.setEFCardAccess(estPaceRes.getEFCardAccess());
		    // optional fields
		    if (estPaceRes.hasCurrentCAR()) {
			authDataResponse.setCurrentCAR(estPaceRes.getCurrentCAR());
		    }
		    if (estPaceRes.hasPreviousCAR()) {
			authDataResponse.setPreviousCAR(estPaceRes.getPreviousCAR());
		    }
		    if (estPaceRes.hasIDICC()) {
			authDataResponse.setIDPICC(estPaceRes.getIDICC());
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
		EstablishChannelResponse response = protoImpl.establish(parameters, dispatcher, this.gui);
		// register protocol instance for secure messaging when protocol was processed successful
		if (response.getResult().getResultMajor().equals(ECardConstants.Major.OK)) {
		    channel.addSecureMessaging(protoImpl);
		}
		return response;
	    }

	    // if this point is reached a native implementation is not present, try registered protocols
	    Result r = WSHelper.makeResultUnknownError("No such protocol available in this IFD.");
	    return WSHelper.makeResponse(EstablishChannelResponse.class, r);
	} catch (Throwable t) {
	    return WSHelper.makeResponse(EstablishChannelResponse.class, WSHelper.makeResult(t));
	}
    }

    @Override
    public DestroyChannelResponse destroyChannel(DestroyChannel parameters) {
	try {
	    byte[] slotHandle = parameters.getSlotHandle();
	    SCCard card = this.scwrapper.getCard(slotHandle);
	    SCChannel channel = card.getChannel(slotHandle);
	    channel.removeSecureMessaging();
	    DestroyChannelResponse destroyChannelResponse = new DestroyChannelResponse();
	    Result r = new Result();
	    r.setResultMajor(ECardConstants.Major.OK);
	    destroyChannelResponse.setResult(r);
	    return destroyChannelResponse;
	} catch (Throwable t) {
	    return WSHelper.makeResponse(DestroyChannelResponse.class, WSHelper.makeResult(t));
	}
    }

    private Result evaluateControlIFDRAPDU(byte[] resultCommand) {
	int result = ByteUtils.toInteger(resultCommand);
	switch (result) {
	    case 0x9000: return WSHelper.makeResultOK();
	    case 0x6400: return WSHelper.makeResultError(ECardConstants.Minor.IFD.TIMEOUT_ERROR, "Timeout.");
            default:
                return WSHelper.makeResultUnknownError("Unknown return code from terminal.");
	}
    }

}
