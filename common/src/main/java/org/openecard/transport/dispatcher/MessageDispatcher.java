/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.RequestType;
import iso.std.iso_iec._24727.tech.schema.ResponseType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import org.openecard.common.event.ApiCallEventObject;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.Dispatchable;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.common.interfaces.DispatcherExceptionUnchecked;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.common.interfaces.InvocationTargetExceptionUnchecked;
import org.openecard.common.util.HandlerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of the {@code Dispatcher} interface.
 * This implementation defers its actual reflection work to the {@link Service} class.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public class MessageDispatcher implements Dispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(MessageDispatcher.class);

    private final Environment environment;
    /** Key is parameter classname */
    private final TreeMap<String, Service> serviceMap;
    /** Key is service interface classname */
    private final TreeMap<String, Method> serviceInstMap;

    private final List<String> availableServiceNames;

    private final boolean isFilter;

    /**
     * Creates a new MessageDispatcher instance and loads all definitions from the webservice interfaces in the
     * environment.
     *
     * @param environment The environment with the webservice interface getters.
     */
    public MessageDispatcher(Environment environment) {
	this.environment = environment;
	isFilter = false;
	serviceMap = new TreeMap<>();
	serviceInstMap = new TreeMap<>();
	initDefinitions();
	availableServiceNames = new ArrayList<>();
	createServiceList();
    }

    private MessageDispatcher(Environment environment, boolean isFilter) {
	this.environment = environment;
	this.isFilter = isFilter;
	serviceMap = new TreeMap<>();
	serviceInstMap = new TreeMap<>();
	initDefinitions();
	availableServiceNames = new ArrayList<>();
	createServiceList();
    }


    @Override
    public Object deliver(Object req) throws DispatcherException, InvocationTargetException {
	EventDispatcher disp = environment.getEventDispatcher();
	// send API CALL STARTED event
	ConnectionHandleType handle = HandlerUtils.extractHandle(req);
	if (disp != null && req instanceof RequestType) {
	    ApiCallEventObject startEvt = new ApiCallEventObject(handle, (RequestType) req);
	    LOG.debug("Sending API_CALL_STARTED event.");
	    disp.notify(EventType.API_CALL_STARTED, startEvt);
	}

	try {
	    Class<?> reqClass = req.getClass();
	    Service s = getService(reqClass);
	    Object serviceImpl = getServiceImpl(s);

	    LOG.debug("Delivering message of type: {}", req.getClass().getName());

	    Object result =  s.invoke(serviceImpl, req);

	    // send API CALL FINISHED event
	    if (disp != null && req instanceof RequestType && result instanceof ResponseType) {
		ApiCallEventObject finEvt = new ApiCallEventObject(handle, (RequestType) req);
		finEvt.setResponse((ResponseType) result);
		LOG.debug("Sending API_CALL_FINISHED event.");
		disp.notify(EventType.API_CALL_FINISHED, finEvt);
	    }

	    return result;
	} catch (IllegalAccessException | IllegalArgumentException ex) {
	    throw new DispatcherException(ex);
	}
    }

    @Override
    public Object safeDeliver(Object request) throws DispatcherExceptionUnchecked, InvocationTargetExceptionUnchecked {
	try {
	    return deliver(request);
	} catch (DispatcherException ex) {
	    throw new DispatcherExceptionUnchecked(ex.getMessage(), ex.getCause());
	} catch (InvocationTargetException ex) {
	    throw new InvocationTargetExceptionUnchecked(ex.getMessage(), ex.getCause());
	}
    }

    private Service getService(Class<?> reqClass) throws IllegalAccessException {
	if (! serviceMap.containsKey(reqClass.getName())) {
	    String msg = "No service with a method containing parameter type " + reqClass.getName() + " present.";
	    throw new IllegalAccessException(msg);
	}
	return serviceMap.get(reqClass.getName());
    }

    private Object getServiceImpl(Service s) throws IllegalAccessException, InvocationTargetException {
	Method m = serviceInstMap.get(s.getServiceInterface().getName());
	if (m == null) {
	    String msg = "The environment does not contain a service for class " + s.getServiceInterface().getName();
	    throw new IllegalAccessException(msg);
	}
	Object impl = m.invoke(environment);
	return impl;
    }


    private void initDefinitions() {
	// load all annotated service methods from environment
	Class<?> envClass = this.environment.getClass();
	Method[] envMethods = envClass.getMethods();

	// loop over methods and build index structure
	for (Method nextAccessor : envMethods) {
	    // is the method annotated?
	    if (nextAccessor.getAnnotation(Dispatchable.class) != null) {
		// check access rights and stuff
		int modifier = nextAccessor.getModifiers();
		if (Modifier.isAbstract(modifier)) {
		    continue;
		} else if (! Modifier.isPublic(modifier)) {
		    continue;
		} else if (Modifier.isStatic(modifier)) {
		    continue;
		}

		// try to read class from annotation, if not take return value
		Dispatchable methodAnnotation = nextAccessor.getAnnotation(Dispatchable.class);
		Class<?> returnType = methodAnnotation.interfaceClass();

		// check if the service is already defined
		if (this.serviceInstMap.containsKey(returnType.getName())) {
		    String msg = "Omitting service type {}, because its type already associated with another service.";
		    LOG.warn(msg, returnType.getName());
		    continue;
		}

		// add env method mapping
		this.serviceInstMap.put(returnType.getName(), nextAccessor);

		// update type mentioned in Dispatchable annotation to the actual type returned by the function
		Class<?> returnTypeImpl = returnType;
		try {
		    Object result = nextAccessor.invoke(environment);
		    if (result != null) {
			returnTypeImpl = result.getClass();
		    }
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
		    LOG.error("Actual type could not be retrieved from method {}.", nextAccessor, ex);
		    continue;
		}

		// create service and map its request parameters
		Service service = new Service(returnType, returnTypeImpl, isFilter);

		for (Class<?> reqClass : service.getRequestClasses()) {
		    if (serviceMap.containsKey(reqClass.getName())) {
			String msg = "Omitting method with parameter type {} in service interface {} because its ";
			msg += "type already associated with another service.";
			LOG.warn(msg, reqClass.getName(), returnType.getName());
		    } else {
			serviceMap.put(reqClass.getName(), service);
		    }
		}
	    }
	}
    }

    @Override
    public List<String> getServiceList() {
	return Collections.unmodifiableList(availableServiceNames);
    }

    @Override
    public Dispatcher getFilter() {
	if (isFilter) {
	    return this;
	}
	return new MessageDispatcher(this.environment, true);
    }

    private void createServiceList() {
	TreeSet<Service> services = new TreeSet<>();
	services.addAll(serviceMap.values());
	for (Service service : services) {
	    availableServiceNames.addAll(service.getActionList());
	}
    }

}
