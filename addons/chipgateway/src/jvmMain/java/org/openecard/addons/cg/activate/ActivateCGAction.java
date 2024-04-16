/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.addons.cg.activate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import org.openecard.addon.ActionInitializationException;
import org.openecard.addon.Context;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.Attachment;
import org.openecard.addon.bind.AuxDataKeys;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.addon.bind.HeaderEntry;
import org.openecard.addon.bind.Headers;
import org.openecard.addon.bind.RequestBody;
import org.openecard.addons.cg.tctoken.TCToken;
import org.openecard.addons.cg.ex.InvalidRedirectUrlException;
import org.openecard.addons.cg.ex.InvalidTCTokenElement;
import org.openecard.common.DynamicContext;
import org.openecard.common.ThreadTerminateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class ActivateCGAction implements AppPluginAction {

    private static final Logger LOG = LoggerFactory.getLogger(ActivateCGAction.class);

    private static final String METHOD_HDR = "X-OeC-Method";
    private static final AtomicInteger THREAD_NUM = new AtomicInteger(1);
    private static final Semaphore MUTEX = new Semaphore(1, true);

    private static volatile Thread currentTaskThread;

    private Context ctx;
    private TCTokenHandler tokenHandler;

    @Override
    public void init(Context aCtx) throws ActionInitializationException {
	this.ctx = aCtx;
	tokenHandler = new TCTokenHandler(ctx);
    }

    @Override
    public void destroy(boolean force) {
	this.ctx = null;
    }

    @Override
    public BindingResult execute(RequestBody body, Map<String, String> params, Headers headers, List<Attachment> att, Map<String, Object> extraParams) {
	BindingResult response;
	boolean aquired = false;

	try {
	    checkMethod(headers);
	    final TCToken token = TCToken.generateToken(params);

	    Runnable cgAction = new Runnable() {
		@Override
		public void run() {
		    try {
			tokenHandler.handleNoCardActivate(token);

			// run a full GC to free some heap memory
			System.gc();
			System.runFinalization();
			System.gc();
		    } catch (ThreadTerminateException ex) {
			LOG.debug("Activation task terminated by an interrupt.", ex);
		    } catch (RuntimeException ex) {
			LOG.error("Unhandled exception in activation process.", ex);
		    } finally {
			currentTaskThread = null;
			// in some cases an error does not lead to a removal of the dynamic context so remove it here
			DynamicContext.remove();
		    }
		}
	    };

	    // guard thread creation
	    MUTEX.acquire();
	    aquired = true;

	    Thread t = currentTaskThread;
	    if (t != null) {
		if (token.isForceProcessing()) {
		    LOG.info("Stopping already running ChipGateway Protocol instance.");
		    t.interrupt();
		    // wait for other task to complete
		    t.join();
		} else {
		    LOG.info("Another ChipGateway Protocol instance is already running, return status=busy.");
		    response = new BindingResult(BindingResultCode.REDIRECT);
		    response.getAuxResultData().put(AuxDataKeys.REDIRECT_LOCATION, token.finalizeBusyAddress());
		    return response;
		}
	    }

	    // perform ChipGateway Protocol in background thread, so that we can return directly
	    currentTaskThread = new Thread(cgAction);
	    currentTaskThread.setDaemon(true);
	    currentTaskThread.setName("ChipGateway-Activation-" + THREAD_NUM.getAndIncrement());
	    currentTaskThread.start();

	    // create redirect
	    response = new BindingResult(BindingResultCode.REDIRECT);
	    response.getAuxResultData().put(AuxDataKeys.REDIRECT_LOCATION, token.finalizeOkAddress());
	} catch (WrongMethodException ex) {
	    LOG.warn(ex.getMessage());
	    response = new BindingResult(BindingResultCode.WRONG_PARAMETER);
	    response.setResultMessage(ex.getMessage());
	} catch (NoMethodException ex) {
	    LOG.error("No method given in headers, maybe wrong binging.", ex);
	    response = new BindingResult(BindingResultCode.INTERNAL_ERROR);
	    response.setResultMessage(ex.getMessage());
	} catch (InvalidRedirectUrlException | InvalidTCTokenElement ex) {
            LOG.error("Failed to create TCToken.", ex);
            response = ex.getBindingResult();
        } catch (InterruptedException ex) {
	    LOG.info("ChipGateway activation interrupted.");
	    response = new BindingResult(BindingResultCode.INTERNAL_ERROR);
	    response.setResultMessage(ex.getMessage());
	} finally {
	    if (aquired) {
		MUTEX.release();
	    }
	}

	return response;
    }

    private void checkMethod(Headers headers) throws WrongMethodException, NoMethodException {
	HeaderEntry methodHdr = headers.getFirstHeader(METHOD_HDR);
	if (methodHdr != null) {
	    String method = methodHdr.getValue();
	    if (! method.equals("GET")) {
		String msg = String.format("Wrong method (%s) used to call the plugin action.", method);
		throw new WrongMethodException(msg);
	    }
	} else {
	    throw new NoMethodException("No method in headers available, make sure " + METHOD_HDR + " is set.");
	}
    }

}
