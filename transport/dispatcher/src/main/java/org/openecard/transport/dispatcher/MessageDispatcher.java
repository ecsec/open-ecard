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
import java.lang.reflect.Modifier;
import java.util.TreeMap;
import org.openecard.common.interfaces.Dispatchable;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.common.interfaces.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of the {@code Dispatcher} interface.
 * This implementation defers its actual reflection work to the {@link Service} class.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class MessageDispatcher implements Dispatcher {

    private static final Logger logger = LoggerFactory.getLogger(MessageDispatcher.class);

    private final Environment environment;
    /** Key is parameter classname */
    private final TreeMap<String, Service> serviceMap;
    /** Key is service interface classname */
    private final TreeMap<String, Method> serviceInstMap;

    /**
     * Creates a new MessageDispatcher instance and loads all definitions from the webservice interfaces in the
     * environment.
     *
     * @param environment The environment with the webservice interface getters.
     */
    public MessageDispatcher(Environment environment) {
	this.environment = environment;
	serviceMap = new TreeMap<String, Service>();
	serviceInstMap = new TreeMap<String, Method>();
	initDefinitions();
    }


    @Override
    public Object deliver(Object req) throws DispatcherException, InvocationTargetException {
	try {
	    Class reqClass = req.getClass();
	    Service s = getService(reqClass);
	    Object serviceImpl = getServiceImpl(s);
	    Object result =  s.invoke(serviceImpl, req);
	    return result;
	} catch (IllegalAccessException ex) {
	    throw new DispatcherException(ex);
	} catch (IllegalArgumentException ex) {
	    throw new DispatcherException(ex);
	}
    }

    private Service getService(Class reqClass) throws IllegalAccessException {
	if (! serviceMap.containsKey(reqClass.getName())) {
	    String msg = "No service with a method containing parameter type " + reqClass.getName() + " present.";
	    throw new IllegalAccessException();
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
	Class envClass = this.environment.getClass();
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
		Class returnType = methodAnnotation.interfaceClass();

		// check if the service is already defined
		if (this.serviceInstMap.containsKey(returnType.getName())) {
		    String msg = "Omitting service type {}, because its type already associated with another service.";
		    logger.warn(msg, returnType.getName());
		    continue;
		}
		// add env method mapping
		this.serviceInstMap.put(returnType.getName(), nextAccessor);

		// create service and map its request parameters
		Service service = new Service(returnType);
		for (Class reqClass : service.getRequestClasses()) {
		    if (serviceMap.containsKey(reqClass.getName())) {
			String msg = "Omitting method with parameter type {} in service interface {} because its ";
			msg += "type already associated with another service.";
			logger.warn(msg, reqClass.getName(), returnType.getName());
		    } else {
			serviceMap.put(reqClass.getName(), service);
		    }
		}
	    }
	}
    }

}
