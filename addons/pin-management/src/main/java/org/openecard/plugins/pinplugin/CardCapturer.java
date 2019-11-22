/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.plugins.pinplugin;

import iso.std.iso_iec._24727.tech.schema.ActionType;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationDisconnect;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPath;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathResponse;
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.PrepareDevices;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.openecard.common.DynamicContext;
import org.openecard.common.WSHelper;
import org.openecard.common.event.EventObject;
import org.openecard.common.interfaces.Dispatcher;
import static org.openecard.plugins.pinplugin.AbstractPINAction.GERMAN_IDENTITY_CARD;
import static org.openecard.plugins.pinplugin.GetCardsAndPINStatusAction.DYNCTX_INSTANCE_KEY;
import static org.openecard.plugins.pinplugin.GetCardsAndPINStatusAction.PIN_STATUS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Neil Crossley
 */
public class CardCapturer {

    private static final Logger LOG = LoggerFactory.getLogger(CardCapturer.class);
    private final ReadOnlyCardStateView emptyState;

    private final ConnectionHandleType sessionHandle;
    private final Dispatcher dispatcher;
    private final AbstractPINAction pinAction;
    private final Object cardViewLock = new Object();
    private final Object devicesLock = new Object();
    private final DelegatingCardStateView cardStateView;
    private boolean areDevicesPoweredDown;
    private int deviceSessionCount = 0;
    private boolean hasInitialized = false;

    CardCapturer(ConnectionHandleType sessionHandle, Dispatcher dispatcher, AbstractPINAction pinAction, boolean areDevicesPoweredDown) {
	this.sessionHandle = sessionHandle;
	this.dispatcher = dispatcher;
	this.pinAction = pinAction;

	this.emptyState = new ReadOnlyCardStateView(sessionHandle, RecognizedState.UNKNOWN, true, true, true, deviceSessionCount);
	this.cardStateView = new DelegatingCardStateView(emptyState);
	this.areDevicesPoweredDown = areDevicesPoweredDown;
    }

    public CardStateView aquireView() {
	return cardStateView;
    }

    public boolean updateCardState() throws WSHelper.WSException {

	synchronized (cardViewLock) {
	    if (!hasInitialized || areDevicesPoweredDown || this.cardStateView.preparedDeviceSession() != deviceSessionCount) {
		DynamicContext ctx = DynamicContext.getInstance(DYNCTX_INSTANCE_KEY);
		ReadOnlyCardStateView createdState = initialState(ctx);
		boolean success;
		if (createdState == null) {
		    createdState = emptyState;
		    success = false;
		} else {
		    success = true;
		}

		this.cardStateView.setDelegate(createdState);
		this.hasInitialized = true;
		return success;
	    }
	    else {
		if (this.cardStateView.getPinState() != RecognizedState.PIN_resumed && this.cardStateView.isDisconnected()) {
		    // do not update in case of status resumed, it destroys the the pace channel and there is no disconnect after
		    // the verification of the CAN so the handle stays the same
		    updateConnectionHandle();
		}
		return true;
	    }
	}
    }

    private ReadOnlyCardStateView initialState(DynamicContext ctx) throws WSHelper.WSException {
	synchronized (devicesLock) {
	    if (areDevicesPoweredDown) {
		LOG.debug("Call prepare devices");
		PrepareDevices pd = new PrepareDevices();
		pd.setContextHandle(sessionHandle.getContextHandle());
		dispatcher.safeDeliver(pd);
	    }
	}

	// check if a german identity card is inserted, if not wait for it
	ConnectionHandleType cHandle = pinAction.waitForCardType(GERMAN_IDENTITY_CARD);
	if (cHandle == null) {
	    LOG.debug("User cancelled card insertion.");
	    return null;
	}
	copySession(sessionHandle, cHandle);
	cHandle = pinAction.connectToRootApplication(cHandle);
	final RecognizedState pinState = pinAction.recognizeState(cHandle);

	pinAction.getPUKStatus(cHandle);
	ctx.put(PIN_STATUS, pinState);
	boolean nativePace = pinAction.genericPACESupport(cHandle);
	final boolean capturePin = !nativePace;
	ReadOnlyCardStateView cardState = new ReadOnlyCardStateView(cHandle,
		pinState,
		capturePin,
		false,
		false,
		deviceSessionCount);
	return cardState;
    }

    /**
     * Update the connection handle.
     * This is necessary after every step because we Disconnect the card with a reset if we have success or not.
     */
    private void updateConnectionHandle() {
	CardApplicationPath cPath = new CardApplicationPath();
	CardApplicationPathType cPathType = new CardApplicationPathType();
	final ConnectionHandleType handle = this.cardStateView.getHandle();
	cPathType.setChannelHandle(handle.getChannelHandle());
	cPath.setCardAppPathRequest(cPathType);

	CardApplicationPathResponse cPathResp = (CardApplicationPathResponse) dispatcher.safeDeliver(cPath);
	List<CardApplicationPathType> cRes = cPathResp.getCardAppPathResultSet().getCardApplicationPathResult();
	for (CardApplicationPathType capt : cRes) {
	    CardApplicationConnect cConn = new CardApplicationConnect();
	    cConn.setCardApplicationPath(capt);
	    CardApplicationConnectResponse conRes = (CardApplicationConnectResponse) dispatcher.safeDeliver(cConn);

	    String cardType = conRes.getConnectionHandle().getRecognitionInfo().getCardType();
	    ConnectionHandleType cHandleNew = conRes.getConnectionHandle();
	    if (cardType.equals("http://bsi.bund.de/cif/npa.xml")) {
		// ensure same terminal and get the new slothandle
		if (cHandleNew.getIFDName().equals(handle.getIFDName())
			&& ! Arrays.equals(cHandleNew.getSlotHandle(), handle.getSlotHandle())) {

		    this.cardStateView.setDelegate(new ReadOnlyCardStateView(cHandleNew,
			    this.cardStateView.getPinState(),
			    this.cardStateView.capturePin(),
			    this.cardStateView.isRemoved(),
			    this.cardStateView.isRemoved(),
			    this.cardStateView.preparedDeviceSession()));
		    break;
		  // also end if the connection handle found as before than it is still valid
		} else if (cHandleNew.getIFDName().equals(handle.getIFDName()) &&
			Arrays.equals(cHandleNew.getSlotHandle(), handle.getSlotHandle())) {
		    break;
		}
	    } else {
		CardApplicationDisconnect disconnect = new CardApplicationDisconnect();
		disconnect.setConnectionHandle(conRes.getConnectionHandle());
		disconnect.setAction(ActionType.RESET);
		dispatcher.safeDeliver(disconnect);
	    }
	}
    }

    private void copySession(ConnectionHandleType source, ConnectionHandleType target) {
	ChannelHandleType sourceChannel = source.getChannelHandle();
	ChannelHandleType targetChannel = target.getChannelHandle();
	if (targetChannel == null) {
	    targetChannel = new ChannelHandleType();
	    target.setChannelHandle(targetChannel);
	}
	targetChannel.setSessionIdentifier(sourceChannel.getSessionIdentifier());
    }

    public void notifyCardStateChange(final RecognizedState pinState) {
	synchronized (cardViewLock) {
	    DynamicContext ctx = DynamicContext.getInstance(DYNCTX_INSTANCE_KEY);
	    ctx.put(PIN_STATUS, pinState);

	    CardStateView newView = new ReadOnlyCardStateView(this.cardStateView.getHandle(),
		    pinState,
		    this.cardStateView.capturePin(),
		    this.cardStateView.isRemoved(),
		    this.cardStateView.isDisconnected(),
		    this.cardStateView.preparedDeviceSession()
		);
	    this.cardStateView.setDelegate(newView);
	}
    }

    public void onCardRemoved(EventObject eventData) {
	synchronized (cardViewLock) {
	    CardStateView currentView = this.cardStateView.getDelegate();

	    ConnectionHandleType eventConnHandle = eventData.getHandle();
	    ConnectionHandleType viewConnHandle = currentView.getHandle();

	    if (eventConnHandle != null && viewConnHandle != null) {
		String viewIfdName = viewConnHandle.getIFDName();
		BigInteger viewSlotIndex = viewConnHandle.getSlotIndex();

		if (viewIfdName != null && viewIfdName.equals(eventConnHandle.getIFDName()) &&
			viewSlotIndex != null && viewSlotIndex.equals(eventConnHandle.getSlotIndex())) {

		    CardStateView newView = new ReadOnlyCardStateView(viewConnHandle,
			    currentView.getPinState(),
			    currentView.capturePin(),
			    true,
			    currentView.isDisconnected(),
			    this.cardStateView.preparedDeviceSession());
		    this.cardStateView.setDelegate(newView);
		}
	    }
	}
    }

    public void onCardDisconnected(EventObject eventData) {
	synchronized (cardViewLock) {
	    CardStateView currentView = this.cardStateView.getDelegate();

	    ConnectionHandleType eventConnHandle = eventData.getHandle();
	    ConnectionHandleType viewConnHandle = currentView.getHandle();

	    if (eventConnHandle != null && viewConnHandle != null) {
		String viewIfdName = viewConnHandle.getIFDName();
		BigInteger viewSlotIndex = viewConnHandle.getSlotIndex();

		if (viewIfdName != null && viewIfdName.equals(eventConnHandle.getIFDName()) &&
			viewSlotIndex != null && viewSlotIndex.equals(eventConnHandle.getSlotIndex())) {

		    CardStateView newView = new ReadOnlyCardStateView(viewConnHandle,
			    currentView.getPinState(),
			    currentView.capturePin(),
			    currentView.isRemoved(),
			    true,
			    this.cardStateView.preparedDeviceSession());
		    this.cardStateView.setDelegate(newView);
		}
	    }
	}
    }

    void onPowerDownDevices(EventObject eventData) {
	synchronized (devicesLock) {
	    this.areDevicesPoweredDown = true;
	}
    }

    void onPrepareDevices(EventObject eventData) {
	synchronized (devicesLock) {
	    this.areDevicesPoweredDown = false;
	    this.deviceSessionCount += 1;
	}
    }
}
