/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.mobile.activation.model;

import org.openecard.mobile.activation.NfcCapabilityResult;

/**
 *
 * @author Neil Crossley
 */
public class NfcConfig {

    public boolean isAvailable;
    public boolean isEnabled;
    public NfcCapabilityResult checkExtendedLength;

    public NfcConfig(boolean isAvailable, boolean isEnabled, NfcCapabilityResult checkExtendedLength) {
	this.isAvailable = isAvailable;
	this.isEnabled = isEnabled;
	this.checkExtendedLength = checkExtendedLength;
    }

    public static NfcConfig create() {
	return new NfcConfig(true, true, NfcCapabilityResult.SUPPORTED);
    }

    public static NfcConfig createUnavailable() {
	return new NfcConfig(false, true, NfcCapabilityResult.SUPPORTED);
    }
}
