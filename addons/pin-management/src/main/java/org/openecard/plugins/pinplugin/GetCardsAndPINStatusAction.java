/****************************************************************************
 * Copyright (C) 2014-2018 ecsec GmbH.
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

package org.openecard.plugins.pinplugin;

import iso.std.iso_iec._24727.tech.schema.ActionType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.CreateSession;
import iso.std.iso_iec._24727.tech.schema.CreateSessionResponse;
import iso.std.iso_iec._24727.tech.schema.DestroyChannel;
import iso.std.iso_iec._24727.tech.schema.DestroySession;
import iso.std.iso_iec._24727.tech.schema.Disconnect;
import iso.std.iso_iec._24727.tech.schema.PowerDownDevices;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.openecard.addon.ActionInitializationException;
import org.openecard.addon.Context;
import org.openecard.addon.bind.AppExtensionException;
import org.openecard.addon.sal.SalStateView;
import org.openecard.common.DynamicContext;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherExceptionUnchecked;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.common.interfaces.EventFilter;
import org.openecard.common.interfaces.InvocationTargetExceptionUnchecked;
import org.openecard.common.util.Pair;
import org.openecard.common.util.SysUtils;
import org.openecard.gui.ResultStatus;
import org.openecard.plugins.pinplugin.gui.CardRemovedFilter;
import org.openecard.plugins.pinplugin.gui.PINDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
public class GetCardsAndPINStatusAction extends AbstractPINAction {

    private static final Logger LOG = LoggerFactory.getLogger(GetCardsAndPINStatusAction.class);

    public static final String DYNCTX_INSTANCE_KEY = "GetCardsAndPINStatusAction";

    public static final String PIN_STATUS = "pin-status";
    public static final String PIN_CORRECT = "pin-correct";
    public static final String CAN_CORRECT = "can-correct";
    public static final String PUK_CORRECT = "puk-correct";

    @Override
    public void execute() throws AppExtensionException {
	// init dyn ctx
	DynamicContext ctx = DynamicContext.getInstance(DYNCTX_INSTANCE_KEY);

	ConnectionHandleType sessionHandle = null;

	Pair<CardCapturer, AutoCloseable> managedCardCapturer = null;

	Future<ResultStatus> pinManagement = null;
	try {
	    sessionHandle = createSessionHandle();

	    managedCardCapturer = createCardCapturer(sessionHandle, dispatcher, evDispatcher, this, this.salStateView);

	    CardCapturer cardCapturer = managedCardCapturer.p1;

	    boolean success = cardCapturer.updateCardState();

	    if (!success && !SysUtils.isMobileDevice()) {
		// User cancelled card insertion.
		LOG.debug("User cancelled card insertion");
		return;
	    }

	    try {
		ExecutorService es = Executors.newSingleThreadExecutor(action -> new Thread(action, "ShowPINManagementDialog"));

		pinManagement = es.submit(() -> {
		    PINDialog uc = new PINDialog(gui, dispatcher, cardCapturer);
		    return uc.show();
		});

		EventCallback disconnectEventSink = registerListeners(cardCapturer.aquireView(), pinManagement);

		try {
		    ResultStatus result = pinManagement.get();
		    if (result == ResultStatus.CANCEL || result == ResultStatus.INTERRUPTED) {
			throw new AppExtensionException(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, "PIN Management was cancelled.");
		    }
		} finally {
		    if (disconnectEventSink != null) {
			evDispatcher.del(disconnectEventSink);
		    }
		}
	    } catch (InterruptedException ex) {
		LOG.info("waiting for PIN management to stop interrupted.", ex);
		if (pinManagement != null) {
		    pinManagement.cancel(true);
		}
	    } catch (ExecutionException ex) {
		LOG.warn("Pin Management failed", ex);
	    } catch (CancellationException ex) {
		throw new AppExtensionException(ECardConstants.Minor.IFD.CANCELLATION_BY_USER, "PIN Management was cancelled.");
	    } finally {
		pinManagement = null;

		CardStateView cardView = cardCapturer.aquireView();

		final byte[] slotHandle = cardView.getHandle().getSlotHandle();

		if (slotHandle != null && slotHandle.length > 0) {
		    // destroy the pace channel
		    DestroyChannel destChannel = new DestroyChannel();
		    destChannel.setSlotHandle(slotHandle);
		    dispatcher.safeDeliver(destChannel);

		    // Transaction based communication does not work on java 8 so the PACE channel is not closed after an
		    // EndTransaction call. So do a reset of the card to close the PACE channel.
		    Disconnect disconnect = new Disconnect();
		    disconnect.setSlotHandle(cardView.getHandle().getSlotHandle());
		    disconnect.setAction(ActionType.RESET);
		    dispatcher.safeDeliver(disconnect);
		}
	    }
	} catch (WSException ex){
	    LOG.debug("Error while executing PIN Management.", ex);
	    throw new AppExtensionException(ex.getResultMinor(), ex.getMessage());
	} finally {
	    if (managedCardCapturer != null && managedCardCapturer.p2 != null) {
		try {
		    managedCardCapturer.p2.close();
		} catch(Exception e) {
		    LOG.error("Error while cleaning up card management.", e);
		}
	    }
	    if (sessionHandle != null) {
		try {
		    DestroySession request = new DestroySession();
		    request.setConnectionHandle(sessionHandle);
		    this.dispatcher.safeDeliver(request);
		} catch (Exception e) {
		    LOG.error("Error while cleaning up card management.", e);
		}
	    }
	    try {
		PowerDownDevices pdd = new PowerDownDevices();
		if (sessionHandle != null) {
		    pdd.setContextHandle(sessionHandle.getContextHandle());
		}
		this.dispatcher.safeDeliver(pdd);
	    } catch (Exception e) {
		LOG.error("Error while powering down devices: ", e);
	    }
	    ctx.clear();
	}
    }

    private static Pair<CardCapturer, AutoCloseable> createCardCapturer(
	    ConnectionHandleType sessionHandle,
	    Dispatcher dispatcher,
	    EventDispatcher eventDispatcher,
	    AbstractPINAction pinAction,
	    SalStateView salStateView) {
	boolean isMobileDevice = SysUtils.isMobileDevice();
	CardCapturer cardCapturer = new CardCapturer(
		sessionHandle,
		dispatcher,
		pinAction,
		isMobileDevice,
		salStateView);

	if (isMobileDevice) {
	    EventCallback disconnectEventSink = (eventType, eventData) -> {
		if (null != eventType) {
		    switch (eventType) {
			case CARD_REMOVED:
			    cardCapturer.onCardRemoved(eventData);
			    break;
			case POWER_DOWN_DEVICES:
			    cardCapturer.onPowerDownDevices(eventData);
			    break;
			case PREPARE_DEVICES:
			    cardCapturer.onPrepareDevices(eventData);
			    break;
			default:
			    break;
		    }
		}

	    };
	    eventDispatcher.add(disconnectEventSink);

	    return new Pair(cardCapturer, (AutoCloseable) () -> {
		eventDispatcher.del(disconnectEventSink);
	    });
	} else {
	    return new Pair(cardCapturer, (AutoCloseable) () -> {
	    });
	}
    }

    private EventCallback registerListeners(CardStateView cardView, Future<ResultStatus> pinManagement) {
	EventCallback disconnectEventSink;

	if (!SysUtils.isMobileDevice()) {
	    disconnectEventSink = (eventType, eventData) -> {
		if (eventType == EventType.CARD_REMOVED) {
		    LOG.info("Card has been removed. Shutting down PIN Management process.");
		    pinManagement.cancel(true);
		}
	    };

	    EventFilter evFilter = new CardRemovedFilter(cardView.getHandle().getIFDName(),
		    cardView.getHandle().getSlotIndex());
	    evDispatcher.add(disconnectEventSink, evFilter);
	} else {
	    disconnectEventSink = null;
	}
	return disconnectEventSink;
    }

    private ConnectionHandleType createSessionHandle() throws DispatcherExceptionUnchecked, InvocationTargetExceptionUnchecked, WSException {
	CreateSessionResponse response = (CreateSessionResponse)this.dispatcher.safeDeliver(new CreateSession());
	WSHelper.checkResult(response);

	ConnectionHandleType sessionHandle = response.getConnectionHandle();
	return sessionHandle;
    }

    @Override
    public void init(Context aCtx) throws ActionInitializationException {
	dispatcher = aCtx.getDispatcher();
	this.gui = aCtx.getUserConsent();
	this.recognition = aCtx.getRecognition();
	this.evDispatcher = aCtx.getEventDispatcher();
	this.salStateView = aCtx.getSalStateView();
    }

    @Override
    public void destroy(boolean force) {
	//ignore
    }

}
