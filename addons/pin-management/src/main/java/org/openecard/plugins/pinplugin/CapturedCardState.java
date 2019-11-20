/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/
package org.openecard.plugins.pinplugin;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;

/**
 *
 * @author Neil Crossley
 */

public class CapturedCardState {

    private final boolean capturePin;
    private final ConnectionHandleType handle;
    private final RecognizedState pinState;

    public CapturedCardState(boolean capturePin, ConnectionHandleType handle, RecognizedState pinState) {
	this.capturePin = capturePin;
	this.handle = handle;
	this.pinState = pinState;
    }

    public ConnectionHandleType getHandle() {
	return handle;
    }

    public RecognizedState getPinState() {
	return pinState;
    }

    public boolean isCapturePin() {
	return capturePin;
    }
}

