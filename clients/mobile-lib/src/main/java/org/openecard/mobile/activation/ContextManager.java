/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.mobile.activation;

import org.openecard.mobile.ex.ApduExtLengthNotSupported;
import org.openecard.mobile.ex.NfcDisabled;
import org.openecard.mobile.ex.NfcUnavailable;
import org.openecard.mobile.ex.UnableToInitialize;

/**
 *
 * @author Neil Crossley
 */
public interface ContextManager {
    void start(OpeneCardServiceHandler handler) throws UnableToInitialize, NfcUnavailable, NfcDisabled, ApduExtLengthNotSupported;

    void stop(OpeneCardServiceHandler handler);
}
