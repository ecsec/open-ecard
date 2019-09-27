/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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
