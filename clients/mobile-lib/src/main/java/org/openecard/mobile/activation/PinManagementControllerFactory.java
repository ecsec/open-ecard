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

import java.net.URL;
import java.util.Set;
import java.util.function.Predicate;

/**
 *
 * @author Neil Crossley
 */
public interface PinManagementControllerFactory {

    CloseableController create(URL url, Predicate<String> supportedCard, CompletionCallbacks activation, EacInteraction interaction);

    CloseableController create(URL url, Set<String> supportedCard, CompletionCallbacks activation, EacInteraction interaction);

    void destroy(ActivationController controller);
}
