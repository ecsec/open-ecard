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

import org.openecard.mobile.activation.ContextManager;
import org.openecard.mobile.activation.NFCCapabilities;
import org.openecard.mobile.activation.OpeneCardServiceHandler;
import org.openecard.mobile.activation.ServiceErrorResponse;
import org.openecard.mobile.ex.ApduExtLengthNotSupported;
import org.openecard.mobile.ex.NfcDisabled;
import org.openecard.mobile.ex.NfcUnavailable;
import org.openecard.mobile.ex.UnableToInitialize;
import org.openecard.mobile.system.OpeneCardContext;
import org.openecard.mobile.system.OpeneCardContextConfig;
import org.openecard.mobile.system.ServiceErrorCode;
import org.openecard.mobile.system.ServiceMessages;

/**
 *
 * @author Neil Crossley
 */
public class CommonContextManager implements ContextManager, OpeneCardContextProvider {

    private final Object contextLock = new Object();
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
    public void start(OpeneCardServiceHandler handler) {
	if (handler == null) {
	    throw new IllegalArgumentException("Given handler cannot be null");
	}
	new Thread(() -> {
	    try {
		synchronized (contextLock) {
		    if (context != null) {
			handler.onFailure(new ServiceErrorResponse(ServiceErrorCode.ALREADY_STARTED, ServiceMessages.SERVICE_ALREADY_INITIALIZED));
			return;
		    }
		    OpeneCardContext newContext = new OpeneCardContext(nfc, config);

		    newContext.initialize();

		    context = newContext;

		    handler.onSuccess();
		}
	    } catch (UnableToInitialize e) {
		handler.onFailure(new ServiceErrorResponse(ServiceErrorCode.ALREADY_STARTED, ServiceMessages.SERVICE_ALREADY_INITIALIZED));
	    } catch (NfcUnavailable ex) {
		handler.onFailure(new ServiceErrorResponse(ServiceErrorCode.NFC_NOT_AVAILABLE, ServiceMessages.NFC_NOT_AVAILABLE_FAIL));
	    } catch (NfcDisabled ex) {
		handler.onFailure(new ServiceErrorResponse(ServiceErrorCode.NFC_NOT_ENABLED, ServiceMessages.NFC_NOT_ENABLED_FAIL));
	    } catch (ApduExtLengthNotSupported ex) {
		handler.onFailure(new ServiceErrorResponse(ServiceErrorCode.NFC_NO_EXTENDED_LENGTH, ServiceMessages.NFC_NO_EXTENDED_LENGTH_SUPPORT));
	    }
	}).start();

    }

    @Override
    public void stop(OpeneCardServiceHandler handler) {
	if (handler == null) {
	    throw new IllegalArgumentException("Given handler cannot be null.");
	}
	new Thread(() -> {
	    synchronized (this.contextLock) {
		if (context == null) {
		    handler.onFailure(new ServiceErrorResponse(ServiceErrorCode.ALREADY_STOPPED, ServiceMessages.SERVICE_ALREADY_STOPPED));
		}
		try {
		    boolean result = this.context.shutdown();
		    if (result) {
			handler.onSuccess();
		    } else {
			handler.onFailure(new ServiceErrorResponse(ServiceErrorCode.SHUTDOWN_FAILED, ServiceMessages.SERVICE_TERMINATE_FAILURE));
		    }
		} finally {
		    this.context = null;
		}
	    }
	}).start();
    }
}
