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

import static org.mockito.Mockito.*;
import org.openecard.mobile.activation.NFCCapabilities;
import org.openecard.mobile.activation.NfcCapabilityResult;

/**
 *
 * @author Neil Crossley
 */
public class MockNFCCapabilitiesConfigurator implements Builder<NFCCapabilities> {

    private final NFCCapabilities nfc;

    private MockNFCCapabilitiesConfigurator() {
	this.nfc = mock(NFCCapabilities.class);
    }

    private MockNFCCapabilitiesConfigurator(NFCCapabilities nfc) {
	this.nfc = nfc;
    }

    public MockNFCCapabilitiesConfigurator withFullNfc() {
	when(nfc.isAvailable()).thenReturn(Boolean.TRUE);
	when(nfc.isEnabled()).thenReturn(Boolean.TRUE);
	when(nfc.checkExtendedLength()).thenReturn(NfcCapabilityResult.SUPPORTED);
	return this;
    }

    public static MockNFCCapabilitiesConfigurator createWithFullNfc() {
	NFCCapabilities mock = mock(NFCCapabilities.class);
	return new MockNFCCapabilitiesConfigurator(mock).withFullNfc();
    }

    @Override
    public NFCCapabilities build() {
	return nfc;
    }
}
