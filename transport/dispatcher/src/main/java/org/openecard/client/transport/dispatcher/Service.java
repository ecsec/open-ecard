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

package org.openecard.client.transport.dispatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Service {

    private static final Logger _logger = LoggerFactory.getLogger(Service.class);

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
		    _logger.warn("Omitting method {} in service interface {} because its parameter type is already associated with another method.", m.getName(), iface.getName());
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
