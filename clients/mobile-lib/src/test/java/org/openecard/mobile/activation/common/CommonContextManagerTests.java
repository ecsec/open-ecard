/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.mobile.activation.common;

import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import org.openecard.mobile.activation.ContextManager;
import org.openecard.mobile.activation.NFCCapabilities;
import org.openecard.mobile.activation.OpeneCardServiceHandler;
import org.openecard.mobile.ex.ApduExtLengthNotSupported;
import org.openecard.mobile.ex.NfcDisabled;
import org.openecard.mobile.ex.NfcUnavailable;
import org.openecard.mobile.ex.UnableToInitialize;
import org.openecard.mobile.system.OpeneCardContextConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Neil Crossley
 */
public class CommonContextManagerTests {

    @Injectable
    NFCCapabilities mockNfc;

    @Injectable()
    OpeneCardContextConfig config;

    @Tested
    CommonContextManager sut;
    @Mocked
    OpeneCardServiceHandler handler;

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
	Assert.assertNull(sut.getContext());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    void startRequiresHandler() throws UnableToInitialize, NfcUnavailable, NfcUnavailable, NfcDisabled, NfcDisabled, ApduExtLengthNotSupported {
	sut.start(null);
    }

}
