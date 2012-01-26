/*
 * Copyright 2012 Tobias Wich ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.transport.dispatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.interfaces.Dispatchable;
import org.openecard.client.common.interfaces.Dispatcher;
import org.openecard.client.common.interfaces.Environment;
import org.openecard.client.common.logging.LogManager;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class MessageDispatcher implements Dispatcher {

    private static final Logger _logger = LogManager.getLogger(MessageDispatcher.class.getName());

    private final Environment environment;
    /** Key is parameter classname */
    private final TreeMap<String,Service> serviceMap;
    /** Key is service interface classname */
    private final TreeMap<String,Method> serviceInstMap;

    public MessageDispatcher(Environment environment) {
	this.environment = environment;
	serviceMap = new TreeMap<String, Service>();
	serviceInstMap = new TreeMap<String, Method>();
	initDefinitions();
    }


    @Override
    public Object deliver(Object req) throws IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
	Class reqClass = req.getClass();
	Service s = getService(reqClass);
	Object serviceImpl = getServiceImpl(s);
	Object result =  s.invoke(serviceImpl, req);
	return result;
    }

    private Service getService(Class reqClass) throws IllegalAccessException {
	if (! serviceMap.containsKey(reqClass.getName())) {
	    throw new IllegalAccessException("No service with a method containing parameter type " + reqClass.getName() + " present.");
	}
	return serviceMap.get(reqClass.getName());
    }

    private Object getServiceImpl(Service s) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	Method m = serviceInstMap.get(s.getServiceInterface().getName());
	if (m == null) {
	    throw new IllegalAccessException("The environment does not contain a service for class " + s.getServiceInterface().getName());
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
	    if (nextAccessor.isAnnotationPresent(Dispatchable.class)) {
		// try to read class from annotation, if not take return value
		Dispatchable methodAnnotation = nextAccessor.getAnnotation(Dispatchable.class);
		Class returnType = methodAnnotation.interfaceClass();
		if (returnType == null) {
		    returnType = nextAccessor.getReturnType();
		}

		// check if the service is already defined
		if (this.serviceInstMap.containsKey(returnType.getName())) {
		    _logger.log(Level.WARNING, "Omitting service with type {0} because its type already associated with another service.", returnType.getName());
		    continue;
		}
		// add env method mapping
		this.serviceInstMap.put(returnType.getName(), nextAccessor);

		// create service and map its request parameters
		Service service = new Service(returnType);
		for (Class reqClass : service.getRequestClasses()) {
		    if (serviceMap.containsKey(reqClass.getName())) {
			_logger.log(Level.WARNING, "Omitting method with parameter type {0} in service interface {1} because its type already associated with another service.", new Object[]{reqClass.getName(), returnType.getName()});
		    } else {
			serviceMap.put(reqClass.getName(), service);
		    }
		}
	    }
	}
    }

}
