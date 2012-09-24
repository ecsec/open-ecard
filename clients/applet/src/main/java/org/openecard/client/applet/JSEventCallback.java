/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.applet;

import generated.StatusChangeType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.control.ControlInterface;
import org.openecard.client.control.binding.javascript.JavaScriptBinding;
import org.openecard.client.control.client.ClientResponse;
import org.openecard.client.control.module.status.StatusChangeRequest;
import org.openecard.client.control.module.status.StatusChangeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 * @author Benedikt Biallowons <benedikt.biallowons@ecsec.de>
 */
public class JSEventCallback {

    private static final Logger logger = LoggerFactory.getLogger(JSEventCallback.class);

    private final ExecutorService workerPool;
    private final ApplicationHandler handler;
    private final JSObject jsObject;
    private final String jsEventCallback;
    private final String jsMessageCallback;

    private JavaScriptBinding binding;

    public JSEventCallback(ECardApplet applet, ApplicationHandler handler) {
	this.workerPool = Executors.newCachedThreadPool();
	this.handler = handler;
	this.jsObject = JSObject.getWindow(applet);
	this.jsEventCallback = applet.getParameter("jsEventCallback");
	this.jsMessageCallback = applet.getParameter("jsMessageCallback");

	setupJSBinding(this.handler);
    }

    /**
     * Prepare the JavaScript internal binding.
     *
     * @param handler to handle requests/responses
     */
    private void setupJSBinding(ApplicationHandler handler) {
	try {
	    this.binding = new JavaScriptBinding();
	    ControlInterface control = new ControlInterface(this.binding);
	    control.getListeners().addControlListener(handler);
	    control.start();
	} catch (Exception ex) {
	    logger.error("Exception", ex);
	}
    }

    /**
     * Start event polling and push available events to the JavaScript frontend.
     */
    public void notifyScript() {
	if (this.jsEventCallback == null) {
	    return;
	}

	this.workerPool.execute(new Runnable() {
	    @Override
	    public void run() {
		// FIXME: this is a workaround until the javascript binding supports StatusChangeRequests/Responses
		StatusChangeRequest statusChangeRequest = new StatusChangeRequest();
		ClientResponse clientResponse = null;

		while (true) {
		    clientResponse = handler.request(statusChangeRequest);

		    if (clientResponse != null && clientResponse instanceof StatusChangeResponse
			    && ((StatusChangeResponse) clientResponse).getStatusChangeType() != null) {
			StatusChangeType statusChange = ((StatusChangeResponse) clientResponse).getStatusChangeType();
			Object[] response = buildEvent(statusChange);

			try {
			    jsObject.call(jsEventCallback, response);
			} catch (JSException ignore) {
			}
		    }
		}
	    }
	});
    }

    /**
     * Send a message to the JavaScript frontend.
     *
     * @param message containing desired information
     */
    public void sendMessage(String message) {
	if (this.jsMessageCallback == null) {
	    return;
	}

	try {
	    this.jsObject.call(this.jsMessageCallback, new Object[] { message });
	} catch (JSException ignore) {
	}
    }

    /**
     * This is the entry point for JavaScript to Java communication.
     *
     * @param callback to call after finished processing
     * @param id to identify the request
     * @param data as input parameters
     */
    public void handle(final String callback, final String id, final Object[] data) {
	this.workerPool.execute(new Runnable() {
	    @Override
	    public void run() {
		Object[] response = AccessController.doPrivileged(new ActivateAction(binding, id, data));

		try {
		    jsObject.call(callback, response);
		} catch (JSException ignore) {
		}
	    }
	});
    }

    /**
     * Build the JavaScript event callback parameter array.
     *
     * @param statusChange that occurred
     * @return JavaScript parameters array
     */
    private static Object[] buildEvent(StatusChangeType statusChange) {
	ConnectionHandleType cHandle = statusChange.getConnectionHandle();

	String action = statusChange.getAction();
	String ifdId = makeId(cHandle.getIFDName());
	String ifdName = cHandle.getIFDName();
	String cardType = cHandle.getRecognitionInfo() != null ? cHandle.getRecognitionInfo().getCardType() : null;
	String contextHandle = ByteUtils.toHexString(cHandle.getContextHandle());
	String slotIndex = cHandle.getSlotIndex() != null ? cHandle.getSlotIndex().toString() : null;

	return new Object[] { action, ifdId, ifdName, cardType, contextHandle, slotIndex };
    }

    /**
     * Helper method to generate a unique id from a string input.
     * Used by the JavaScript frontend.
     *
     * @param input to generate unique id
     * @return unique id
     */
    private static String makeId(String input) {
	try {
	    MessageDigest md = MessageDigest.getInstance("SHA");
	    byte[] bytes = md.digest(input.getBytes());
	    return ByteUtils.toHexString(bytes);
	} catch (NoSuchAlgorithmException ex) {
	    return input.replaceAll(" ", "_");
	}
    }

    /**
     * Some methods triggered by JavaScript calls need privileged access to various
     * resources like network connections to external hosts (eg. to fetch the TcToken).
     */
    private static class ActivateAction implements PrivilegedAction<Object[]> {

	private final JavaScriptBinding binding;
	private final String id;
	private final Object[] data;

	public ActivateAction(JavaScriptBinding binding, String id, Object[] data) {
	    this.binding = binding;
	    this.id = id;
	    this.data = data;
	}

	@Override
	public Object[] run() {
	    return this.binding.handle(this.id, this.data);
	}
    }

}
