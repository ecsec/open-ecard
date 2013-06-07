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

package org.openecard.clients.applet;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.applet.Applet;
import java.io.UnsupportedEncodingException;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import org.json.JSONException;
import org.json.JSONObject;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.ProtocolInfo;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.ValueGenerators;
import org.openecard.control.ControlInterface;
import org.openecard.control.binding.javascript.JavaScriptBinding;
import org.openecard.control.binding.javascript.handler.JavaScriptStatusHandler;
import org.openecard.control.binding.javascript.handler.JavaScriptTCTokenHandler;
import org.openecard.control.binding.javascript.handler.JavaScriptWaitForChangeHandler;
import org.openecard.control.handler.ControlHandler;
import org.openecard.control.handler.ControlHandlers;
import org.openecard.control.module.status.EventHandler;
import org.openecard.control.module.status.GenericStatusHandler;
import org.openecard.control.module.status.GenericWaitForChangeHandler;
import org.openecard.control.module.tctoken.GenericTCTokenHandler;
import org.openecard.gui.UserConsent;
import org.openecard.recognition.CardRecognition;
import org.openecard.ws.marshal.MarshallingTypeException;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.openecard.ws.schema.StatusChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;


/**
 * JavaScript communication handler.
 *
 * This class is used to handle all types of communication (eg. events and messages) between JavaScript
 * and the applet.
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 * @author Benedikt Biallowons <benedikt.biallowons@ecsec.de>
 */
public class JSCommunicationHandler {

    private static final Logger logger = LoggerFactory.getLogger(JSCommunicationHandler.class);

    private final ExecutorService workerPool;
    private final JSObjectWrapper jsObjectWrapper;
    private final String jsStartedCallback;
    private final String jsEventCallback;
    private final String jsMessageCallback;

    private Future<?> eventThread;

    private JavaScriptBinding binding;
    private ControlInterface control;
    private HashMap<String, String> sessionMap; // this is just one session for waitForChange


    /**
     * Create a new JSCommunicationHandler.
     *
     * @param applet current applet
     * @param cardStates CardStateMap of the client
     * @param dispatcher dispatcher for sending messages
     * @param eventHandler to wait for status changes
     * @param gui to show card insertion dialog
     * @param protocols for SAL protocol registry
     * @param reg to get card information shown in insertion dialog
     */
    public JSCommunicationHandler(ECardApplet applet, CardStateMap cardStates, Dispatcher dispatcher,
	    EventHandler eventHandler, UserConsent gui, ProtocolInfo protocols, CardRecognition reg) {
	workerPool = Executors.newCachedThreadPool();
	jsObjectWrapper = new JSObjectWrapper(applet);
	jsStartedCallback = applet.getParameter("jsStartedCallback");
	jsEventCallback = applet.getParameter("jsEventCallback");
	jsMessageCallback = applet.getParameter("jsMessageCallback");
	binding = new JavaScriptBinding();
	sessionMap = new HashMap<String, String>();
	sessionMap.put("session", ValueGenerators.generateSessionID());
	setupJSBinding(cardStates, dispatcher, eventHandler, gui, protocols, reg);
    }

    /**
     * Prepare the JavaScript internal binding.
     *
     * @param cardStates CardStateMap of the client
     * @param dispatcher dispatcher for sending messages
     * @param eventHandler to wait for status changes
     * @param gui to show card insertion dialog
     * @param reg to get card information shown in insertion dialog
     */
    private void setupJSBinding(CardStateMap cardStates, Dispatcher dispatcher, EventHandler eventHandler,
	    UserConsent gui, ProtocolInfo protocols, CardRecognition reg) {
	try {
	    ControlHandlers handler = new ControlHandlers();
	    GenericTCTokenHandler genericTCTokenHandler = new GenericTCTokenHandler(cardStates, dispatcher, gui, reg);
	    GenericStatusHandler genericStatusHandler = new GenericStatusHandler(cardStates, eventHandler, protocols, reg);
	    GenericWaitForChangeHandler genericWaitForChangeHandler = new GenericWaitForChangeHandler(eventHandler);
	    ControlHandler tcTokenHandler = new JavaScriptTCTokenHandler(genericTCTokenHandler);
	    ControlHandler statusHandler = new JavaScriptStatusHandler(genericStatusHandler);
	    ControlHandler waitForChangeHandler = new JavaScriptWaitForChangeHandler(genericWaitForChangeHandler);
	    handler.addControlHandler(tcTokenHandler);
	    handler.addControlHandler(statusHandler);
	    handler.addControlHandler(waitForChangeHandler);
	    control = new ControlInterface(binding, handler);
	    control.start();

	    // send initial Status and thereby register session
	    binding.handle("getStatus", sessionMap);
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	}
    }

    /**
     * Stop all running worker threads.
     */
    public void stop() {
	try {
	    eventThread.cancel(true);
	    workerPool.shutdownNow();
	    control.stop();
	} catch (Exception ex) {
	    logger.error(ex.getMessage(), ex);
	}
    }

    /**
     * Start event polling and push available events to the JavaScript frontend.
     */
    public void startEventPush() {
	if (jsEventCallback == null) {
	    return;
	}

	eventThread = workerPool.submit(new Runnable() {
	    @Override
	    public void run() {
		JSObject jsObject = jsObjectWrapper.getNamespacedJSObject(jsEventCallback);
		String function = JSObjectWrapper.getFunction(jsEventCallback);

		WSMarshaller m = null;
		try {
		    m = WSMarshallerFactory.createInstance();
		    m.removeAllTypeClasses();
		    m.addXmlTypeClass(StatusChange.class);
		} catch (WSMarshallerException e) {
		    logger.error(e.getMessage(), e);
		    throw new RuntimeException(e);
		}

		while (!Thread.currentThread().isInterrupted()) {
		    Object[] waitForChangeResponse = binding.handle("waitForChange", sessionMap);

		    if (waitForChangeResponse != null) {
			StatusChange statusChange;
			try {
			    statusChange = (StatusChange)  m.unmarshal(m.str2doc(waitForChangeResponse[0].toString()));
			    Object[] response = buildEvent(statusChange);
			    jsObject.call(function, response);
			} catch (MarshallingTypeException e) {
			    logger.error("Marshalling of WaitForChange-Response failed", e);
			} catch (WSMarshallerException e) {
			    logger.error("Marshalling of WaitForChange-Response failed", e);
			} catch (SAXException e) {
			    logger.error("Marshalling of WaitForChange-Response failed", e);
			} catch (JSException ignore) {
			}
		    }
		}
	    }
	});
    }

    /**
     * Send a started event to the JavaScript frontend.
     */
    public void sendStarted() {
	if (jsStartedCallback == null) {
	    return;
	}

	try {
	    jsObjectWrapper.call(jsStartedCallback, this);
	} catch (JSException ignore) {
	}
    }

    /**
     * Send a message to the JavaScript frontend.
     *
     * @param message containing desired information
     */
    public void sendMessage(String message) {
	if (jsMessageCallback == null) {
	    return;
	}

	try {
	    jsObjectWrapper.call(jsMessageCallback, message);
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
    public void handle(final String callback, final String id, final String data) {
	try {
	    final Map<String, String> map = parseParameterJSONMap(data);

	    workerPool.submit(new Runnable() {
		@Override
		public void run() {
		    JSObject jsObject = jsObjectWrapper.getNamespacedJSObject(callback);
		    String function = JSObjectWrapper.getFunction(callback);

		    // Some methods triggered by JavaScript calls need privileged access to various
		    // resources like network connections to external hosts (eg. to fetch the TcToken).
		    Object[] response = AccessController.doPrivileged(new PrivilegedAction<Object[]>() {
			@Override
			public Object[] run() {
			    return binding.handle(id, map);
			}
		    });

		    try {
			jsObject.call(function, response);
		    } catch (JSException ignore) {
		    }
		}
	    });
	} catch (JSONException ex) {
	    logger.error(ex.getMessage(), ex);
	}
    }

    /**
     * Parse parameter map from JSON data
     *
     * @param jsonData from JavaScript
     * @return parameter hash map
     * @throws JSONException
     */
    private static Map<String, String> parseParameterJSONMap(String jsonData) throws JSONException {
	JSONObject json = new JSONObject(jsonData);
	Map<String, String> map = new HashMap<String, String>();

	for (String key : JSONObject.getNames(json)) {
	    map.put(key, json.getString(key));
	}

	return map;
    }

    /**
     * Build the JavaScript event callback parameter array.
     *
     * @param statusChange that occurred
     * @return JavaScript parameters array
     */
    private static Object[] buildEvent(StatusChange statusChange) {
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
     * Helper method to generate a JavaScript compatible id from a string input.
     * Used by the JavaScript frontend.
     *
     * @param input to generate id
     * @return unique id
     */
    private static String makeId(String input) {
	try {
	    MessageDigest md = MessageDigest.getInstance("SHA");
	    byte[] bytes = md.digest(input.getBytes("UTF-8"));
	    return ByteUtils.toHexString(bytes);
	} catch (NoSuchAlgorithmException ex) {
	    return input.replaceAll(" ", "_");
	} catch (UnsupportedEncodingException ex) {
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
	public JSObjectWrapper(Applet applet) {
	    this.jsObject = JSObject.getWindow(applet);
	}

	/**
	 * Call namespaced JavaScript function with single parameter.
	 *
	 * @param function to call
	 * @param parameter to use with the JavaScript function call
	 * @throws JSException thrown by underlying JSObject.call() or namespace resolution
	 */
	public void call(String function, Object parameter) {
	    call(function, new Object[] { parameter });
	}

	/**
	 * Call namespaced JavaScript function with parameters array.
	 *
	 * @param function to call
	 * @param parameters to use with the JavaScript function call
	 * @throws JSException thrown by underlying JSObject.call() or namespace resolution
	 */
	public void call(String function, Object[] parameters) {
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
	public JSObject getNamespacedJSObject(String function) {
	    JSObject jsObj = this.jsObject;
	    String[] namespacesAndFunction = function.split("\\.");

	    for (int i = 0; i < (namespacesAndFunction.length - 1); i++) {
		jsObj = (JSObject) jsObj.getMember(namespacesAndFunction[i]);
	    }

	    return jsObj;
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
