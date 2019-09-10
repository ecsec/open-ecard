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

import org.openecard.mobile.activation.ContextManager;
import org.openecard.mobile.activation.NFCCapabilities;
import org.openecard.mobile.activation.OpeneCardServiceHandler;
import org.openecard.mobile.activation.ServerErrorResponse;
import org.openecard.mobile.ex.ApduExtLengthNotSupported;
import org.openecard.mobile.ex.NfcDisabled;
import org.openecard.mobile.ex.NfcUnavailable;
import org.openecard.mobile.ex.UnableToInitialize;
import org.openecard.mobile.system.OpeneCardContext;
import org.openecard.mobile.system.OpeneCardContextConfig;
import org.openecard.mobile.system.ServiceConstants;

/**
 *
 * @author Neil Crossley
 */
public class CommonContextManager implements ContextManager, OpeneCardContextProvider {

    private final Object lock = new Object();
    private final NFCCapabilities nfc;
    private final OpeneCardContextConfig config;
    private OpeneCardContext context;

    public CommonContextManager(NFCCapabilities nfc, OpeneCardContextConfig config) {
	this.nfc = nfc;
	this.config = config;
    }

    @Override
    public OpeneCardContext getContext() {
	return context;
    }

    @Override
    public void start(OpeneCardServiceHandler handler) throws UnableToInitialize, NfcUnavailable, NfcDisabled, ApduExtLengthNotSupported {
	if (handler == null) {
	    throw new IllegalArgumentException("Given handler cannot be null");
	}
	try {
	    synchronized (this.lock) {
		if (context != null) {
		    throw new UnableToInitialize("The context has already been initialized");
		}
		OpeneCardContext newContext = new OpeneCardContext(nfc, this.config);

		newContext.initialize();

		this.context = newContext;

		handler.onSuccess();
	    }
	} finally {
	    handler.onFailure(new ServerErrorResponse());
	}

    }

    @Override
    public void stop(OpeneCardServiceHandler handler) {
	if (handler == null) {
	    throw new IllegalArgumentException("Given handler cannot be null.");
	}
	synchronized (this.lock) {
	    if (context == null) {
		handler.onFailure(new ServerErrorResponse());
	    }
	    try {
		String result = this.context.shutdown();
		if (ServiceConstants.SUCCESS.equalsIgnoreCase(result)) {
		    handler.onSuccess();
		} else {
		    handler.onFailure(new ServerErrorResponse());
		}
	    } finally {
		this.context = null;
		handler.onFailure(new ServerErrorResponse());
	    }
	}
    }

}
