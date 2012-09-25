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
import java.applet.Applet;
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
    private final JSObjectWrapper jsObjectWrapper;
    private final String jsEventCallback;
    private final String jsMessageCallback;

    private JavaScriptBinding binding;

    public JSEventCallback(ECardApplet applet, ApplicationHandler handler) {
	this.workerPool = Executors.newCachedThreadPool();
	this.handler = handler;
	this.jsObjectWrapper = new JSObjectWrapper(applet);
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
    public void startEventPush() {
	if (this.jsEventCallback == null) {
	    return;
	}

	this.workerPool.execute(new Runnable() {
	    @Override
	    public void run() {
		JSObject jsObject = jsObjectWrapper.getNamespacedJSObject(jsEventCallback);
		String function = JSObjectWrapper.getFunction(jsEventCallback);

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
			    jsObject.call(function, response);
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
	    this.jsObjectWrapper.call(this.jsMessageCallback, message);
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
		// Some methods triggered by JavaScript calls need privileged access to various
		// resources like network connections to external hosts (eg. to fetch the TcToken).
		Object[] response = AccessController.doPrivileged(new PrivilegedAction<Object[]>() {
		    @Override
		    public Object[] run() {
			return binding.handle(id, data);
		    }
		});

		try {
		    jsObjectWrapper.call(callback, response);
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
     * JSObject wrapper class to handle namespaced function calls.
     *
     * @author Benedikt Biallowons <benedikt.biallowons@ecsec.de>
     */
    private static final class JSObjectWrapper {

	private final JSObject jsObject;

	/**
	 * Create wrapper instance from applet.
	 *
	 * @param applet to get JavaScript window object
	 * @throws JSException throw by underlying JSObject.getWindow()
	 */
	public JSObjectWrapper(Applet applet) throws JSException {
	    this.jsObject = JSObject.getWindow(applet);
	}

	/**
	 * Call namespaced JavaScript function with single parameter.
	 *
	 * @param function to call
	 * @param parameter to use with the JavaScript function call
	 * @throws JSException thrown by underlying JSObject.call() or namespace resolution
	 */
	public void call(String function, Object parameter) throws JSException {
	    call(function, new Object[] { parameter });
	}

	/**
	 * Call namespaced JavaScript function with parameters array.
	 *
	 * @param function to call
	 * @param parameters to use with the JavaScript function call
	 * @throws JSException thrown by underlying JSObject.call() or namespace resolution
	 */
	public void call(String function, Object[] parameters)  throws JSException {
	    getNamespacedJSObject(function).call(getFunction(function), parameters);
	}

	/**
	 * Used to get the correct JSObject from possible namespaced function. JavaScript
	 * namespaces are typically implemented via global objects.
	 *
	 * @param function to call
	 * @return the matching JSObject
	 * @throws JSException if JavaScript object can't be found
	 */
	public JSObject getNamespacedJSObject(String function) throws JSException {
	    JSObject jsObject = this.jsObject;
	    String[] namespacesAndFunction = function.split("\\.");

	    for (int i = 0; i < (namespacesAndFunction.length - 1); i++) {
		jsObject = (JSObject) jsObject.getMember(namespacesAndFunction[i]);
	    }

	    return jsObject;
	}

	/**
	 * Get simple function from namespaced function.
	 *
	 * @param namespacedFunction to get function of
	 * @return simple function
	 */
	public static String getFunction(String namespacedFunction) {
	    String[] namespacesAndFunction = namespacedFunction.split("\\.");

	    return namespacesAndFunction[namespacesAndFunction.length - 1];
	}
    }

}
