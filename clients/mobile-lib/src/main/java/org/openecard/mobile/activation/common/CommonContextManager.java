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

import org.openecard.mobile.activation.ActivationSource;
import org.openecard.mobile.activation.ContextManager;
import org.openecard.mobile.activation.NFCCapabilities;
import org.openecard.mobile.activation.ServiceErrorCode;
import org.openecard.mobile.activation.ServiceErrorResponse;
import org.openecard.mobile.activation.StartServiceHandler;
import org.openecard.mobile.ex.ApduExtLengthNotSupported;
import org.openecard.mobile.ex.NfcDisabled;
import org.openecard.mobile.ex.NfcUnavailable;
import org.openecard.mobile.ex.UnableToInitialize;
import org.openecard.mobile.system.OpeneCardContext;
import org.openecard.mobile.system.OpeneCardContextConfig;
import org.openecard.mobile.system.ServiceMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openecard.mobile.activation.StopServiceHandler;

/**
 *
 * @author Neil Crossley
 */
public class CommonContextManager implements ContextManager, OpeneCardContextProvider {

    private static Logger LOG = LoggerFactory.getLogger(CommonContextManager.class);

    private final Object contextLock = new Object();
    private final NFCCapabilities nfc;
    private final OpeneCardContextConfig config;
    private final ActivationSource source;
    private OpeneCardContext context;
    private boolean isRunning = false;

    public CommonContextManager(NFCCapabilities nfc, OpeneCardContextConfig config, ActivationSource source) {
	this.nfc = nfc;
	this.config = config;
	this.source = source;
    }

    @Override
    public OpeneCardContext getContext() {
	synchronized (contextLock) {
	    if (context == null) {
		throw new IllegalStateException("The Open eCard context is missing because the context manager has not been successfully started.");
	    }
	    return context;
	}
    }

    @Override
    public void start(StartServiceHandler handler) {
	if (handler == null) {
	    throw new IllegalArgumentException("Given handler cannot be null");
	}
	new Thread(() -> {
	    LOG.debug("Starting");
	    ServiceErrorResponse error = null;
	    synchronized (contextLock) {
		if (context != null || isRunning) {
		    error = new CommonServiceErrorResponse(ServiceErrorCode.ALREADY_STARTED, ServiceMessages.SERVICE_ALREADY_INITIALIZED);
		} else {
		    isRunning = true;
		}
	    }
	    if (error != null) {
		handler.onFailure(error);
		return;
	    }
	    OpeneCardContext newContext = new OpeneCardContext(nfc, config);
	    try {
		newContext.initialize();
	    } catch (UnableToInitialize ex) {
		error = new CommonServiceErrorResponse(ServiceErrorCode.ALREADY_STARTED, ServiceMessages.SERVICE_ALREADY_INITIALIZED);
	    } catch (NfcUnavailable ex) {
		error = new CommonServiceErrorResponse(ServiceErrorCode.NFC_NOT_AVAILABLE, ServiceMessages.NFC_NOT_AVAILABLE_FAIL);
	    } catch (NfcDisabled ex) {
		error = new CommonServiceErrorResponse(ServiceErrorCode.NFC_NOT_ENABLED, ServiceMessages.NFC_NOT_ENABLED_FAIL);
	    } catch (ApduExtLengthNotSupported ex) {
		error = new CommonServiceErrorResponse(ServiceErrorCode.NFC_NO_EXTENDED_LENGTH, ServiceMessages.NFC_NO_EXTENDED_LENGTH_SUPPORT);
	    } catch (Exception ex) {
		LOG.error("An unexpected error occurred while initializing the Open eCard service context.", ex);
		error = new CommonServiceErrorResponse(ServiceErrorCode.INTERNAL_ERROR, ServiceMessages.UNEXCPECTED_ERROR);
	    } finally {
		synchronized (contextLock) {
		    if (error == null) {
			context = newContext;
		    } else {
			context = null;
			isRunning = false;
		    }
		}
		if (error == null) {
		    LOG.debug("Started");
		    handler.onSuccess(this.source);
		} else {
		    LOG.debug("Started failed");
		    handler.onFailure(error);
		}
	    }
	}
	).start();

    }

    @Override
    public void stop(StopServiceHandler handler) {
	if (handler == null) {
	    throw new IllegalArgumentException("Given handler cannot be null.");
	}
	new Thread(() -> {
	    LOG.debug("Stopping");
	    OpeneCardContext targetContext;
	    ServiceErrorResponse error = null;
	    synchronized (this.contextLock) {
		targetContext = context;
		if (targetContext == null || !isRunning) {
		    error = new CommonServiceErrorResponse(ServiceErrorCode.ALREADY_STOPPED, ServiceMessages.SERVICE_ALREADY_STOPPED);
		}
	    }
	    if (error != null) {
		handler.onFailure(error);
		return;
	    }
	    Boolean result = null;
	    try {

		result = this.context.shutdown();
	    } finally {
		synchronized (this.contextLock) {
		    this.context = null;
		    isRunning = false;
		}
		if (result != null && result) {
		    handler.onSuccess();
		} else {
		    handler.onFailure(new CommonServiceErrorResponse(ServiceErrorCode.SHUTDOWN_FAILED, ServiceMessages.SERVICE_TERMINATE_FAILURE));
		}
	    }
	}).start();
    }

}
