/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.sal;

import iso.std.iso_iec._24727.tech.schema.CardInfoType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse;
import iso.std.iso_iec._24727.tech.schema.GetStatus;
import iso.std.iso_iec._24727.tech.schema.GetStatusResponse;
import iso.std.iso_iec._24727.tech.schema.IFDCapabilitiesType;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import iso.std.iso_iec._24727.tech.schema.KeyPadCapabilityType;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import iso.std.iso_iec._24727.tech.schema.SlotStatusType;
import iso.std.iso_iec._24727.tech.schema.Wait;
import iso.std.iso_iec._24727.tech.schema.WaitResponse;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.common.event.EventType;
import org.openecard.common.event.IfdEventObject;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.interfaces.RecognitionException;
import org.openecard.common.sal.state.DuplicateCardEntry;
import org.openecard.common.sal.state.SalStateManager;
import org.openecard.common.sal.state.cif.CardInfoWrapper;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.HandlerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class SalEventManager {

    private static final Logger LOG = LoggerFactory.getLogger(SalEventManager.class);

    private static final long[] RECOVER_TIME = {1, 500, 2000, 5000};

    protected final SalStateManager salStates;
    protected final Environment env;
    protected final byte[] ctx;
    private final HandlerBuilder builder;

    protected ExecutorService threadPool;

    private SalEventRunner eventRunner;
    private Future<?> eventRunnerTask;

    public SalEventManager(SalStateManager states, Environment env, byte[] ctx) {
	this.salStates = states;
	this.env = env;
	this.ctx = ctx;
	this.builder = HandlerBuilder.create()
		.setContextHandle(ctx);
    }

    public synchronized void initialize() {
	threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
	    private final AtomicInteger num = new AtomicInteger(0);
	    private final ThreadGroup group = new ThreadGroup("SAL Event Manager");
	    @Override
	    public Thread newThread(Runnable r) {
		String name = String.format("SAL Watcher %d", num.getAndIncrement());
		Thread t = new Thread(group, r, name);
		t.setDaemon(false);
		return t;
	    }
	});
	// start watcher thread
	try {
	    eventRunner = new SalEventRunner(env, builder, ctx);
	    eventRunnerTask = threadPool.submit(eventRunner);
	} catch (WSHelper.WSException ex) {
	    throw new RuntimeException("Failed to request initial status from IFD.");
	}
    }

    public synchronized void terminate() {
	if (eventRunner != null) {
	    eventRunner.setStoppedFlag();
	    eventRunner = null;
	}
	if (eventRunnerTask != null) {
	    eventRunnerTask.cancel(true);
	    eventRunnerTask = null;
	}
	if (threadPool != null) {
	    threadPool.shutdownNow();
	}
    }


    private class SalEventRunner implements Runnable {

	private final Environment env;
	private final HandlerBuilder builder;
	private final byte[] ctxHandle;

	private final List<IFDStatusType> initialState;
	private final List<IFDStatusType> currentState;

	private boolean stopped;

	public SalEventRunner(Environment env, HandlerBuilder builder, byte[] ctxHandle)
		throws WSHelper.WSException {
	    this.env = env;
	    this.builder = builder;
	    this.ctxHandle = ctxHandle;
	    this.initialState = new ArrayList<>(ifdStatus());
	    this.currentState = new ArrayList<>();
	    this.stopped = false;
	}

	/**
	 * Set stopped flag, so that the loop stops when another iteration is repeated. This flag is used as a failsafe
	 * when the InterruptedException gets lost du to wrong code in the IFD stack.
	 */
	public void setStoppedFlag() {
	    stopped = true;
	}

	@Nonnull
	private List<IFDStatusType> ifdStatus() throws WSHelper.WSException {
	    LOG.debug("Requesting terminal names.");
	    ListIFDs listReq = new ListIFDs();
	    listReq.setContextHandle(ctxHandle);
	    ListIFDsResponse ifds = env.getIFD().listIFDs(listReq);
	    WSHelper.checkResult(ifds);

	    LOG.debug("Requesting status for all terminals found.");
	    ArrayList<IFDStatusType> result = new ArrayList<>();
	    for (String ifd : ifds.getIFDName()) {
		GetStatus status = new GetStatus();
		status.setContextHandle(ctxHandle);
		status.setIFDName(ifd);
		GetStatusResponse statusResponse = env.getIFD().getStatus(status);

		try {
		    WSHelper.checkResult(statusResponse);
		    result.addAll(statusResponse.getIFDStatus());
		} catch (WSHelper.WSException ex) {
		    String msg = "Failed to request status from terminal, assuming no card present.";
		    LOG.error(msg, ex);
		    IFDStatusType is = new IFDStatusType();
		    is.setIFDName(ifd);
		    result.add(is);
		}
	    }
	    return result;
	}

	@Nonnull
	private List<IFDStatusType> wait(@Nonnull List<IFDStatusType> lastKnown) throws WSHelper.WSException {
	    LOG.info("Waiting for IFD changes");
	    Wait wait = new Wait();
	    wait.setContextHandle(ctxHandle);
	    wait.getIFDStatus().addAll(lastKnown);
	    WaitResponse resp = env.getIFD().wait(wait);

	    try {
		WSHelper.checkResult(resp);
		List<IFDStatusType> result = resp.getIFDEvent();
		return result;
	    } catch (WSHelper.WSException ex) {
		if (ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE.equals(ex.getResultMinor())) {
		    // this can only happen when the PCSC stack is reloaded, notify all cards have disappeared
		    ArrayList<IFDStatusType> result = new ArrayList<>(lastKnown.size());
		    if (!lastKnown.isEmpty()) {
			LOG.info("PCSC stack seemed to disappear. Signalling that no cards are available anymore.");
			for (IFDStatusType next : lastKnown) {
			    LOG.debug("Removing terminal {}.", next.getIFDName());
			    IFDStatusType newStatus = new IFDStatusType();
			    newStatus.setIFDName(next.getIFDName());
			    newStatus.setConnected(Boolean.FALSE);
			    result.add(newStatus);
			}
		    }
		    return result;
		} else {
		    throw ex;
		}
	    }
	}

	@Override
	public void run() {
	    // fire events for current state
	    handleEvents(initialState);
	    try {
		int failCount = 0;
		while (!stopped) {
		    try {
			List<IFDStatusType> diff = wait(currentState);
			handleEvents(diff); // also updates current status
			failCount = 0;
		    } catch (WSHelper.WSException ex) {
			LOG.warn("IFD Wait returned with error.", ex);
			// wait a bit and try again
			int sleepIdx = failCount < RECOVER_TIME.length ? failCount : RECOVER_TIME.length - 1;
			Thread.sleep(RECOVER_TIME[sleepIdx]);
			failCount++;
		    }
		}
	    } catch (InterruptedException ex) {
		LOG.info("Event thread interrupted.", ex);
	    }
	    LOG.info("Stopping IFD event thread.");
	}

	private IFDStatusType getCorresponding(String ifdName, List<IFDStatusType> statuses) {
	    for (IFDStatusType next : statuses) {
		if (next.getIFDName().equals(ifdName)) {
		    return next;
		}
	    }
	    return null;
	}

	private SlotStatusType getCorresponding(BigInteger idx, List<SlotStatusType> statuses) {
	    for (SlotStatusType next : statuses) {
		if (next.getIndex().equals(idx)) {
		    return next;
		}
	    }
	    return null;
	}

	private ConnectionHandleType makeConnectionHandle(String ifdName, BigInteger slotIdx,
		IFDCapabilitiesType slotCapabilities) {
	    ConnectionHandleType h = builder.setIfdName(ifdName)
		    .setSlotIdx(slotIdx)
		    .setProtectedAuthPath(hasKeypad(slotCapabilities))
		    .buildConnectionHandle();
	    return h;
	}

	private ConnectionHandleType makeUnknownCardHandle(String ifdName, SlotStatusType status,
		IFDCapabilitiesType slotCapabilities) {
	    ConnectionHandleType h = builder
		    .setIfdName(ifdName)
		    .setSlotIdx(status.getIndex())
		    .setCardType(ECardConstants.UNKNOWN_CARD)
		    .setCardIdentifier(status.getATRorATS())
		    .setProtectedAuthPath(hasKeypad(slotCapabilities))
		    .buildConnectionHandle();
	    return h;
	}

	private void handleEvents(@Nonnull List<IFDStatusType> diff) {
	    for (IFDStatusType term : diff) {
		String ifdName = term.getIFDName();

		// find out if the terminal is new, or only a slot got updated
		IFDStatusType oldTerm = getCorresponding(ifdName, currentState);
		boolean terminalAdded = oldTerm == null;
		IFDCapabilitiesType slotCapabilities = getCapabilities(ifdName);

		if (terminalAdded) {
		    // TERMINAL ADDED
		    // make copy of term
		    oldTerm = new IFDStatusType();
		    oldTerm.setIFDName(ifdName);
		    oldTerm.setConnected(true);
		    // add to current list
		    currentState.add(oldTerm);
		    LOG.debug("Received terminal added event ({}).", ifdName);
		}

		// check each slot
		for (SlotStatusType slot : term.getSlotStatus()) {
		    SlotStatusType oldSlot = getCorresponding(slot.getIndex(), oldTerm.getSlotStatus());
		    boolean cardPresent = slot.isCardAvailable();
		    boolean cardWasPresent = oldSlot != null && oldSlot.isCardAvailable();

		    if (cardPresent && !cardWasPresent) {
			// CARD INSERTED
			// copy slot and add to list
			SlotStatusType newSlot = oldSlot;
			if (newSlot == null) {
			    newSlot = new SlotStatusType();
			    oldTerm.getSlotStatus().add(newSlot);
			}
			newSlot.setIndex(slot.getIndex());
			newSlot.setCardAvailable(true);
			newSlot.setATRorATS(slot.getATRorATS());
			// create event
			LOG.debug("Found a card insert event ({}).", ifdName);
			LOG.info("Card with ATR={} inserted.", ByteUtils.toHexString(slot.getATRorATS()));
			ConnectionHandleType handle = makeUnknownCardHandle(ifdName, newSlot, slotCapabilities);

			// perform recognition
			RecognitionInfo recognitionInfo = recogniseCard(handle);
			if (recognitionInfo != null) {
			    LOG.debug("Starting recognition for terminal {}.", ifdName);
			    handle.setRecognitionInfo(recognitionInfo);

			    String type = recognitionInfo.getCardType();
			    LOG.info("Recognised card type={}", type);
			    CardInfoType cif = env.getRecognition().getCardInfo(type);
			    if (cif != null) {
				// add card to SAL state
				// TODO: add interface protocol, but it looks like it was never used so probably ok to leave it null
				try {
				    // Register card before triggering events.
				    salStates.addCard(ctx, ifdName, handle.getSlotIndex(), new CardInfoWrapper(cif, null));
				} catch (DuplicateCardEntry ex) {
				    LOG.error("Duplicate card entry detected, ignoring new card.");
				}

				env.getEventDispatcher().notify(EventType.CARD_RECOGNIZED, new IfdEventObject(handle));
			    } else {
				LOG.info("No card info evailable for type={}", type);
			    }

			} else {
			    LOG.debug("No card regonition for terminal {}.", ifdName);
			    handleCardRemoved(oldSlot, oldTerm, ifdName, slotCapabilities);
			}

		    } else if (!terminalAdded && !cardPresent && cardWasPresent) {
			handleCardRemoved(oldSlot, oldTerm, ifdName, slotCapabilities);
		    }
		}

		// terminal removed event comes after card removed events
		boolean terminalPresent = term.isConnected();
		if (!terminalPresent) {
		    // TERMINAL REMOVED
		    Iterator<IFDStatusType> it = currentState.iterator();
		    while (it.hasNext()) {
			IFDStatusType toDel = it.next();
			if (toDel.getIFDName().equals(term.getIFDName())) {
			    it.remove();
			}
		    }
		    LOG.debug("Found a terminal removed event ({}).", ifdName);
		}
	    }
	}

	private void handleCardRemoved(SlotStatusType oldSlot, IFDStatusType oldTerm, String ifdName, IFDCapabilitiesType slotCapabilities) {
	    // this makes only sense when the terminal was already there
	    // CARD REMOVED
	    // remove slot entry
	    BigInteger idx = oldSlot.getIndex();
	    Iterator<SlotStatusType> it = oldTerm.getSlotStatus().iterator();
	    while (it.hasNext()) {
		SlotStatusType next = it.next();
		if (idx.equals(next.getIndex())) {
		    it.remove();
		    break;
		}
	    }
	    LOG.debug("Found a card removed event ({}).", ifdName);
	    ConnectionHandleType h = makeConnectionHandle(ifdName, idx, slotCapabilities);
	    // remove information from SAL state
	    salStates.removeCard(ctx, ifdName, idx);
	}

	@Nullable
	private IFDCapabilitiesType getCapabilities(String ifdName) {
	    try {
		GetIFDCapabilities req = new GetIFDCapabilities();
		req.setContextHandle(ctxHandle);
		req.setIFDName(ifdName);
		GetIFDCapabilitiesResponse res = (GetIFDCapabilitiesResponse) env.getDispatcher().safeDeliver(req);
		WSHelper.checkResult(res);
		return res.getIFDCapabilities();
	    } catch (WSHelper.WSException ex) {
		LOG.warn("Error while requesting infos from terminal {}.", ifdName);
	    }

	    return null;
	}

	private boolean hasKeypad(@Nullable IFDCapabilitiesType capabilities) {
	    if (capabilities != null) {
		List<KeyPadCapabilityType> keyCaps = capabilities.getKeyPadCapability();
		// the presence of the element is sufficient to know whether it has a pinpad
		return !keyCaps.isEmpty();
	    }

	    // nothing found
	    return false;
	}

	private RecognitionInfo recogniseCard(ConnectionHandleType handle) {
	    try {
		return env.getRecognition().recognizeCard(ctx, handle.getIFDName(), handle.getSlotIndex());
	    } catch (RecognitionException ex) {
		LOG.error("Error during card recognition.", ex);
		return null;
	    }
	}
    }

}
