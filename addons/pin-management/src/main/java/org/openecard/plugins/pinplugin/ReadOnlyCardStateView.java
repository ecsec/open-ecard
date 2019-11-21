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

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;

/**
 *
 * @author Neil Crossley
 */
public class ReadOnlyCardStateView implements CardStateView {

    private final ConnectionHandleType connectionHandle;
    private final RecognizedState pinState;
    private final boolean capturePin;
    private final boolean removed;
    private final boolean disconnected;

    public ReadOnlyCardStateView(ConnectionHandleType connectionHandle,
	    RecognizedState pinState,
	    boolean capturePin,
	    boolean removed,
	    boolean disconnected) {
	this.connectionHandle = connectionHandle;
	this.pinState = pinState;
	this.capturePin = capturePin;
	this.removed = removed;
	this.disconnected = disconnected;
    }

    @Override
    public ConnectionHandleType getHandle() {
	return connectionHandle;
    }

    @Override
    public RecognizedState getPinState() {
	return this.pinState;
    }

    @Override
    public boolean capturePin() {
	return this.capturePin;
    }

    @Override
    public boolean isRemoved() {
	return removed;
    }

    @Override
    public boolean isDisconnected() {
	return disconnected;
    }


}
