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

package org.openecard.transport.dispatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import javax.xml.transform.TransformerException;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Service class encapsulating one webservice for the {@link MessageDispatcher}.
 * This class takes care of the actual interface analysis and reflection part.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
class Service {

    private static final Logger logger = LoggerFactory.getLogger(Service.class);

    private final Class<?> iface;
    private final ArrayList<Class<?>> requestClasses;
    private final TreeMap<String, Method> requestMethods;
    private final HashMap<Class<?>, MessageLogger> objectLoggers;

    /**
     * Creates a new Service instance and initializes it with the given webservice interface class.
     *
     * @param iface The webservice interface class.
     */
    public Service(Class<?> iface) {
	this.iface = iface;

	requestClasses = new ArrayList<Class<?>>();
	requestMethods = new TreeMap<String, Method>();
	objectLoggers = new HashMap<Class<?>, MessageLogger>();

	Method[] methods = iface.getDeclaredMethods();
	for (Method m : methods) {
	    if (isReqParam(m)) {
		Class<?> reqClass = getReqParamClass(m);
		if (requestMethods.containsKey(reqClass.getName())) {
		    String msg = "Omitting method {} in service interface {}, because its parameter type is already ";
		    msg += "associated with another method.";
		    logger.warn(msg, m.getName(), iface.getName());
		} else {
		    requestClasses.add(reqClass);
		    requestMethods.put(reqClass.getName(), m);
		}
	    }
	}
    }


    /**
     * Gets the webservice interface class this instance is initialized with.
     *
     * @return The webservice interface belonging to this instance.
     */
    public Class<?> getServiceInterface() {
	return iface;
    }

    /**
     * Gets the logger for the given object.
     * This method creates a new logger if none is present yet. After the logger is created, always the same logger is
     * returned. This method is thread safe.
     *
     * @param ifaceImpl Implementation for which the logger is requested.
     * @return The requested logger.
     */
    private MessageLogger getLogger(Object ifaceImpl) {
	Class<?> implClass = ifaceImpl.getClass();
	if (objectLoggers.containsKey(implClass)) {
	    return objectLoggers.get(implClass);
	} else {
	    synchronized (this) {
		MessageLogger implLogger = new MessageLogger(ifaceImpl.getClass());
		objectLoggers.put(implClass, implLogger);
		return implLogger;
	    }
	}
    }

    /**
     * Invokes the webservice method related to the request object in the given webservice class instance.
     *
     * @param ifaceImpl The instance implementing the webservice interface this instance is responsible for.
     * @param req The request object to dispatch.
     * @return The result of the method invocation.
     * @throws DispatcherException In case an error happens in the reflections part of the dispatcher.
     * @throws InvocationTargetException In case the dispatched method throws en exception.
     */
    public Object invoke(Object ifaceImpl, Object req) throws DispatcherException, InvocationTargetException {
	try {
	    MessageLogger l = getLogger(ifaceImpl);
	    Class<?> reqClass = req.getClass();
	    Method m = getMethod(reqClass.getName());
	    // invoke method
	    l.logRequest(req);
	    Object res = m.invoke(ifaceImpl, req);
	    l.logResponse(res);
	    return res;
	} catch (IllegalAccessException ex) {
	    throw new DispatcherException(ex);
	} catch (NoSuchMethodException ex) {
	    throw new DispatcherException(ex);
	} catch (IllegalArgumentException ex) {
	    throw new DispatcherException(ex);
	}
    }


    private Class<?> getReqParamClass(Method m) {
	// get parameters of this method
	Class[] params = m.getParameterTypes();
	// methods must have exactly one parameter
	if (params.length != 1) {
	    return null;
	}
	// TODO: add other checks

	return params[0];
    }

    private boolean isReqParam(Method m) {
	return getReqParamClass(m) != null;
    }

    public List<Class<?>> getRequestClasses() {
	return Collections.unmodifiableList(requestClasses);
    }

    private Method getMethod(String paramClass) throws NoSuchMethodException {
	Method m = requestMethods.get(paramClass);
	if (m == null) {
	    String msg = "Method containing parameter with class '" + paramClass + "' does not exist in interface '";
	    msg += iface.getName() + "'.";
	    throw new NoSuchMethodException(msg);
	}
	return m;
    }


    /**
     * Internal logger class for request and response objects.
     * It only logs
     */
    private class MessageLogger {

	private final Logger l;

	private final String reqLogMsg;
	private final String resLogMsg;

	public MessageLogger(Class<?> receiverClass) {
	    this.l = LoggerFactory.getLogger(receiverClass);

	    this.reqLogMsg = String.format("Delivering request object to %s:", receiverClass.getName());
	    this.resLogMsg = "Returning response object:";
	}

	public void logRequest(Object msgObj) {
	    logObject(l, reqLogMsg, msgObj);
	}
	public void logResponse(Object msgObj) {
	    logObject(l, resLogMsg, msgObj);
	}

	private void logObject(Logger l, String msg, Object msgObj) {
	    try {
		if (l.isTraceEnabled()) {
		    WSMarshaller m = WSMarshallerFactory.createInstance();
		    String msgObjStr = m.doc2str(m.marshal(msgObj));
		    l.trace("{}\n{}", msg, msgObjStr);
		} else if (logger.isTraceEnabled()) {
		    // check if the message needs to be logged in the dispatcher class
		    WSMarshaller m = WSMarshallerFactory.createInstance();
		    String msgObjStr = m.doc2str(m.marshal(msgObj));
		    logger.trace("{}\n{}", msg, msgObjStr);
		}
	    } catch (TransformerException ex) {
		logger.error("Failed to log message.");
	    } catch (WSMarshallerException ex) {
		logger.error("Failed to log message.");
	    }
	}

    }

}
