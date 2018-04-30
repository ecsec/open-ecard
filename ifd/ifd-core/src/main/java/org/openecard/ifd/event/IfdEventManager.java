/****************************************************************************
 * Copyright (C) 2012-2017 ecsec GmbH.
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

package org.openecard.ifd.event;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse;
import iso.std.iso_iec._24727.tech.schema.IFDCapabilitiesType;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import iso.std.iso_iec._24727.tech.schema.Wait;
import iso.std.iso_iec._24727.tech.schema.WaitResponse;
import java.math.BigInteger;
import java.util.ArrayList;
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
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.event.IfdEventObject;
import org.openecard.common.util.HandlerBuilder;
import org.openecard.common.util.ValueGenerators;
import org.openecard.ifd.scio.wrapper.ChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Main class of the event system.
 * Use this to create and operate an event manager.
 *
 * @author Tobias Wich
 * @author René Lottes
 */
public class IfdEventManager {

    private static final Logger LOG = LoggerFactory.getLogger(IfdEventManager.class);

    private static final AtomicInteger THREAD_NUM = new AtomicInteger(1);

    protected final Environment env;
    protected final ChannelManager cm;
    protected final byte[] ctx;
    protected final String sessionId;
    private final HandlerBuilder builder;

    protected ExecutorService threadPool;

    private IfdEventRunner eventRunner;
    private Future<?> watcher;


    public IfdEventManager(Environment env, ChannelManager cm, byte[] ctx) {
	this.env = env;
	this.cm = cm;
	this.ctx = ctx;
	this.sessionId = ValueGenerators.genBase64Session();
	this.builder = HandlerBuilder.create()
		.setContextHandle(ctx)
		.setSessionId(sessionId);
    }

    public synchronized void initialize() {
	threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
	    private final AtomicInteger num = new AtomicInteger(0);
	    private final ThreadGroup group = new ThreadGroup("IFD Event Manager");
	    @Override
	    public Thread newThread(Runnable r) {
		String name = String.format("IFD Watcher %d", num.getAndIncrement());
		Thread t = new Thread(group, r, name);
		t.setDaemon(false);
		return t;
	    }
	});
	// start watcher thread
	try {
	    eventRunner = new IfdEventRunner(env, this, builder, cm, ctx);
	    watcher = threadPool.submit(eventRunner);
	} catch (WSException ex) {
	    throw new RuntimeException("Failed to request initial status from IFD.");
	}
    }

    public synchronized void terminate() {
	eventRunner.setStoppedFlag();
	watcher.cancel(true);
	threadPool.shutdownNow();
    }

    @Nonnull
    protected List<IFDStatusType> wait(@Nonnull List<IFDStatusType> lastKnown) throws WSException {
	Wait wait = new Wait();
	wait.setContextHandle(ctx);
	wait.getIFDStatus().addAll(lastKnown);
	WaitResponse resp = env.getIFD().wait(wait);

	try {
	    WSHelper.checkResult(resp);
	    List<IFDStatusType> result = resp.getIFDEvent();
	    return result;
	} catch (WSException ex) {
	    if (ECardConstants.Minor.IFD.INVALID_SLOT_HANDLE.equals(ex.getResultMinor())) {
		// this can only happen when the PCSC stack is reloaded, notify all cards have disappeared
		ArrayList<IFDStatusType> result = new ArrayList<>(lastKnown.size());
		if (! lastKnown.isEmpty()) {
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

    /**
     * Resets a card given as connection handle.
     *
     * @param cHandleRm {@link ConnectionHandleType} object representing a card which shall be removed.
     * @param cHandleIn {@link ConnectionHandleType} object representing a card which shall be inserted.
     * @param ifaceProtocol Interface protocol of the connected card.
     */
    public void resetCard(ConnectionHandleType cHandleRm, ConnectionHandleType cHandleIn, String ifaceProtocol) {
	env.getEventDispatcher().notify(EventType.CARD_REMOVED, new IfdEventObject(cHandleRm, null));

	// determine if the reader has a protected auth path
	IFDCapabilitiesType slotCapabilities = getCapabilities(cHandleRm.getContextHandle(), cHandleRm.getIFDName());
	boolean protectedAuthPath = slotCapabilities != null ? ! slotCapabilities.getKeyPadCapability().isEmpty() : false;

	HandlerBuilder chBuilder = HandlerBuilder.create();
	ConnectionHandleType cInNew = chBuilder.setSessionId(sessionId)
		.setCardType(cHandleIn.getRecognitionInfo())
		.setCardIdentifier(cHandleIn.getRecognitionInfo())
		.setContextHandle(cHandleIn.getContextHandle())
		.setIfdName(cHandleIn.getIFDName())
		.setSlotIdx(BigInteger.ZERO)
		.setSlotHandle(cHandleIn.getSlotHandle())
		.setProtectedAuthPath(protectedAuthPath)
		.buildConnectionHandle();
	env.getEventDispatcher().notify(EventType.CARD_INSERTED, new IfdEventObject(cInNew));

	if (isRecognize()) {
	    Recognizer rec = new Recognizer(env, cInNew, ifaceProtocol);
	    Thread recThread = new Thread(rec, "Recoginiton-" + THREAD_NUM.getAndIncrement());
	    recThread.start();
	}
    }

    boolean isRecognize() {
	return env.getRecognition() != null;
    }

    @Nullable
    private IFDCapabilitiesType getCapabilities(byte[] ctxHandle, String ifdName) {
	GetIFDCapabilities req = new GetIFDCapabilities();
	req.setContextHandle(ctxHandle);
	req.setIFDName(ifdName);
	GetIFDCapabilitiesResponse res = (GetIFDCapabilitiesResponse) env.getDispatcher().safeDeliver(req);
	return res.getIFDCapabilities();
    }

}
