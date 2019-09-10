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

import java.net.URL;
import java.util.Set;
import java.util.function.Predicate;
import org.openecard.mobile.activation.ActivationController;
import org.openecard.mobile.activation.CloseableController;
import org.openecard.mobile.activation.CompletionCallbacks;
import org.openecard.mobile.activation.EacInteraction;
import org.openecard.mobile.activation.PinManagementControllerFactory;

/**
 *
 * @author Neil Crossley
 */
public class CommonPinManagementControllerFactory implements PinManagementControllerFactory {

    private OpeneCardContextProvider contextProvider;

    @Override
    public CloseableController create(URL url, Predicate<String> supportedCard, CompletionCallbacks activation, EacInteraction interaction) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CloseableController create(URL url, Set<String> supportedCard, CompletionCallbacks activation, EacInteraction interaction) {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void destroy(ActivationController controller) {

    }

}
