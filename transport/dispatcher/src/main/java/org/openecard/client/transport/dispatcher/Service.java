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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.logging.LogManager;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Service {

    private static final Logger _logger = LogManager.getLogger(Service.class.getName());

    private final Class iface;
    private final ArrayList<Class> requestClasses;
    private final TreeMap<String,Method> requestMethods;

    public Service(Class iface) {
	this.iface = iface;

	requestClasses = new ArrayList<Class>();
	requestMethods = new TreeMap<String, Method>();
	Method[] methods = iface.getDeclaredMethods();
	for (Method m : methods) {
	    if (isReqParam(m)) {
		Class reqClass = getReqParamClass(m);
		if (requestMethods.containsKey(reqClass.getName())) {
		    _logger.log(Level.WARNING, "Omitting method {0} in service interface {1} because its parameter type is already associated with another method.", new Object[]{m.getName(), iface.getName()});
		} else {
		    requestClasses.add(reqClass);
		    requestMethods.put(reqClass.getName(), m);
		}
	    }
	}
    }


    public Class getServiceInterface() {
	return iface;
    }

    public Object invoke(Object ifaceImpl, Object req) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	Class reqClass = req.getClass();
	Method m = getMethod(reqClass.getName());
	// invoke method
	return m.invoke(ifaceImpl, req);
    }


    private Class getReqParamClass(Method m) {
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

    public List<Class> getRequestClasses() {
	return Collections.unmodifiableList(requestClasses);
    }

    private Method getMethod(String paramClass) throws NoSuchMethodException {
	Method m = requestMethods.get(paramClass);
	if (m == null) {
	    throw new NoSuchMethodException("Method containing parameter with class '" + paramClass + "' does not exist in interface '" + iface.getName() + "'.");
	}
	return m;
    }

}
