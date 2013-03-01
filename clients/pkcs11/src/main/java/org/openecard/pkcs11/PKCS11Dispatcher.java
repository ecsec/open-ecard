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

package org.openecard.pkcs11;

import ch.qos.logback.core.joran.spi.JoranException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumMap;
import javax.annotation.Nonnull;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Dispatcher implementation for PKCS11 functions.
 * The dispatcher takes PKCS11 function parameters and maps them to a previously loaded implementation.
 * <p>The implementation must contain public functions with the {@link PKCS11Dispatchable} annotation. The functions
 * must be named as in the pkcs11_f.h header. The available names are also defined in the {@link PKCS11Functions} enum.
 * <br/>The parameter type of the function must be <tt>{@link JSONObject} -> {@link PKCS11Result}</tt>.</p>
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PKCS11Dispatcher {

    private static final Logger logger = LoggerFactory.getLogger(PKCS11Dispatcher.class);
    
    private final Object instance;
    private EnumMap<PKCS11Functions, Method> functionIndex;

    static {
	try {
	    // load logger config from HOME if set
	    LogbackConfig.load();
	} catch (IOException ex) {
	    logger.error("Failed to load logback config from user config.", ex);
	} catch (JoranException ex) {
	    logger.error("Failed to load logback config from user config.", ex);
	}
    }

    /**
     * Creates a dispatcher instance using {@link PKCS11Impl} as the PKCS11 implementation.
     */
    public PKCS11Dispatcher() {
	instance = new PKCS11Impl();
	this.functionIndex = buildIndex(instance);
    }

    /**
     * Creates a dispatcher instance and uses the given instance as PKCS11 implementation.
     *
     * @param instance Instance of a class implementing some or all of the PKCS11 functions.
     */
    public PKCS11Dispatcher(Object instance) {
	this.instance = instance;
	this.functionIndex = buildIndex(instance);
    }

    /**
     * Dispatches the request data to the matching PKCS11 function in the underlying PKCS11 implementation.
     * The request data is converted to a JSON object and passed to the matching function. If no implementation of the
     * function exists, a result with an appropriate error and empty response data is returned.
     *
     * @param fName Name of the PKCS11 function as defined in the {@code pkcs11_f.h}.
     * @param request Parameter values for the PKCS11 function in the form of a JSON string.
     * @return Result containing a response code and optionally a JSON string with values to update reference
     *   parameters.
     * @throws JSONException Thrown in case the input data is not a valid JSON string.
     */
    @Nonnull
    public PKCS11Result dispatch(@Nonnull String fName, @Nonnull String request) throws JSONException {
	JSONObject reqData = new JSONObject(request);

	PKCS11Functions fType = PKCS11Functions.valueOf(fName);
	Method m = functionIndex.get(fType);
	if (m != null) {
	    try {
		return (PKCS11Result) m.invoke(instance, reqData);
	    } catch (IllegalAccessException ex) {
		String msg = String.format("Calling the function %s was permitted by JVM security mechanisms.", fName);
		logger.error(msg, ex);
		return new PKCS11Result(PKCS11ReturnCode.CKR_GENERAL_ERROR, msg);
	    } catch (InvocationTargetException ex) {
		String msg = String.format("The function %s threw an exception of type %s.", fName, ex.getClass());
		logger.error(msg, ex);
		return new PKCS11Result(PKCS11ReturnCode.CKR_GENERAL_ERROR, msg);
	    }
	} else {
	    String msg = String.format("Function %s is not implemented.", fName);
	    logger.error(msg);
	    return new PKCS11Result(PKCS11ReturnCode.CKR_GENERAL_ERROR, msg);
	}
    }


    private static EnumMap<PKCS11Functions, Method> buildIndex(Object impl) {
	EnumMap<PKCS11Functions, Method> functionIndex = new EnumMap<PKCS11Functions, Method>(PKCS11Functions.class);
	for (Method m : impl.getClass().getMethods()) {
	    if (m.isAnnotationPresent(PKCS11Dispatchable.class)) {
		String fName = m.getName();
		try {
		    PKCS11Functions func = PKCS11Functions.valueOf(fName);
		    if (functionIndex.containsKey(func)) {
			String msg = "Function overloading is not possible in the dispatcher. {} is defined twice.";
			logger.warn(msg, fName);
		    } else {
			boolean resultOk = PKCS11Result.class.isAssignableFrom(m.getReturnType());
			if (! resultOk) {
			    logger.warn("Skipping function {} because of invalid return type.", fName);
			    continue;
			}
			Class<?>[] params = m.getParameterTypes();
			boolean paramOk = params.length == 1 ? JSONObject.class.isAssignableFrom(params[0]) : false;
			if (! paramOk) {
			    logger.warn("Skipping function {} because of invalid parameter types.", fName);
			    continue;
			}
			functionIndex.put(func, m);
		    }
		} catch (IllegalArgumentException ex) {
		    logger.warn("No PKCS11 method defined for name {}.", fName);
		}
	    }
	}

	return functionIndex;
    }

}
