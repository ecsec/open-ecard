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

/**
 *
 * @author Neil Crossley
 */
public interface CompletionCallbacks {

    void onAuthenticationSuccess(ActivationResult result);

    void onAuthenticationInterrupted(ActivationResult result);

    void onAuthenticationFailure(ActivationResult result);
}
