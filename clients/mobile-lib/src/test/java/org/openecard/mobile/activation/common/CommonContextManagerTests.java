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

package org.openecard.mobile.activation.common;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.openecard.mobile.activation.ActivationSource;
import org.openecard.mobile.activation.ContextManager;
import org.openecard.mobile.activation.NFCCapabilities;
import org.openecard.mobile.ex.ApduExtLengthNotSupported;
import org.openecard.mobile.ex.NfcDisabled;
import org.openecard.mobile.ex.NfcUnavailable;
import org.openecard.mobile.ex.UnableToInitialize;
import org.openecard.mobile.system.OpeneCardContextConfig;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.openecard.mobile.activation.StopServiceHandler;

/**
 *
 * @author Neil Crossley
 */
public class CommonContextManagerTests {

    @Mock
    NFCCapabilities mockNfc;

    @Mock
    OpeneCardContextConfig config;

    @Mock
    StopServiceHandler handler;

    @Mock
    ActivationSource source;

    MockitoSession mockito;

    private CommonContextManager sut;

    @BeforeMethod()
    void setup() {

	mockito = Mockito.mockitoSession()
		.initMocks(this)
		.strictness(Strictness.STRICT_STUBS)
		.startMocking();

	this.sut = new CommonContextManager(mockNfc, config, source);
    }

    @AfterMethod()
    void teardown() {
	mockito.finishMocking();
    }

    @Test
    void sutIsContextManager() {
	Assert.assertTrue(sut instanceof ContextManager);
    }

    @Test
    void sutIsContextProvider() {
	Assert.assertTrue(sut instanceof OpeneCardContextProvider);
    }

    @Test
    void sutStartsWithoutContext() {
	Assert.assertThrows(IllegalStateException.class, () -> sut.getContext());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    void startRequiresHandler() throws UnableToInitialize, NfcUnavailable, NfcUnavailable, NfcDisabled, NfcDisabled, ApduExtLengthNotSupported {
	sut.start(null);
    }

}
