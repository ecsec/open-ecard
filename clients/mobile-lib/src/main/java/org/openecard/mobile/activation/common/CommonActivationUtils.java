/** **************************************************************************
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
 ************************************************************************** */
package org.openecard.mobile.activation.common;

import java.net.MalformedURLException;
import org.openecard.mobile.activation.ActivationSource;
import org.openecard.mobile.activation.ActivationUtils;
import org.openecard.mobile.activation.ContextManager;
import org.openecard.mobile.activation.EacControllerFactory;
import org.openecard.mobile.activation.NFCCapabilities;
import org.openecard.mobile.activation.PinManagementControllerFactory;
import org.openecard.mobile.system.OpeneCardContext;
import org.openecard.mobile.system.OpeneCardContextConfig;

/**
 *
 * @author Neil Crossley
 */
public class CommonActivationUtils implements ActivationUtils, OpeneCardContextProvider, ActivationSource {

    private CommonContextManager contextManager = null;
    private final Object managerLock = new Object();
    private final OpeneCardContextConfig config;
    private final ActivationControllerService activationControllerService;
    private EacControllerFactory eacControllerFactory;
    private PinManagementControllerFactory pinManagementControllerFactory;
    private final NFCDialogMsgSetter msgSetter;


    public CommonActivationUtils(OpeneCardContextConfig config, NFCDialogMsgSetter msgSetter) {
	this.config = config;
	this.activationControllerService = new ActivationControllerService(this);
	this.msgSetter = msgSetter;
    }

    @Override
    public EacControllerFactory eacFactory() {
	if (eacControllerFactory == null) {
	    eacControllerFactory = CommonEacControllerFactory.create(activationControllerService, msgSetter);
	}
	return eacControllerFactory;
    }

    @Override
    public PinManagementControllerFactory pinManagementFactory() {
	if (pinManagementControllerFactory == null) {
	    try {
		pinManagementControllerFactory = CommonPinManagementControllerFactory.create(activationControllerService, msgSetter);
	    } catch (MalformedURLException ex) {
		throw new IllegalStateException("The internal activation URL is not parsing.", ex);
	    }
	}
	return pinManagementControllerFactory;

    }

    @Override
    public ContextManager context(NFCCapabilities nfc) {
	if (this.contextManager != null) {
	    return this.contextManager;
	}
	if (nfc == null) {
	    throw new IllegalArgumentException("Given nfc capabilities cannot be null.");
	}
	synchronized (managerLock) {
	    if (this.contextManager != null) {
		return this.contextManager;
	    }
	    this.contextManager = new CommonContextManager(nfc, this.config, this);
	    return this.contextManager;
	}
    }

    @Override
    public OpeneCardContext getContext() {
	if (this.contextManager != null) {
	    return this.contextManager.getContext();
	}
	CommonContextManager targetManager;
	synchronized (managerLock) {
	    if (contextManager == null) {
		throw new IllegalStateException("The Open eCard context is missing because the context manager has not been successfully started.");
	    }
	    targetManager = contextManager;
	}
	return targetManager.getContext();
    }
}
