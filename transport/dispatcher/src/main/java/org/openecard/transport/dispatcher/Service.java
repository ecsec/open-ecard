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
import java.util.List;
import java.util.TreeMap;
import org.openecard.common.interfaces.DispatcherException;
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

    private final Class iface;
    private final ArrayList<Class> requestClasses;
    private final TreeMap<String, Method> requestMethods;

    /**
     * Creates a new Service instance and initializes it with the given webservice interface class.
     *
     * @param iface The webservice interface class.
     */
    public Service(Class iface) {
	this.iface = iface;

	requestClasses = new ArrayList<Class>();
	requestMethods = new TreeMap<String, Method>();
	Method[] methods = iface.getDeclaredMethods();
	for (Method m : methods) {
	    if (isReqParam(m)) {
		Class reqClass = getReqParamClass(m);
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
    public Class getServiceInterface() {
	return iface;
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
	    Class reqClass = req.getClass();
	    Method m = getMethod(reqClass.getName());
	    // invoke method
	    return m.invoke(ifaceImpl, req);
	} catch (IllegalAccessException ex) {
	    throw new DispatcherException(ex);
	} catch (NoSuchMethodException ex) {
	    throw new DispatcherException(ex);
	} catch (IllegalArgumentException ex) {
	    throw new DispatcherException(ex);
	}
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
	    String msg = "Method containing parameter with class '" + paramClass + "' does not exist in interface '";
	    msg += iface.getName() + "'.";
	    throw new NoSuchMethodException(msg);
	}
	return m;
    }

}
