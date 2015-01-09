/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

package org.openecard.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Properties;


/**
 * Generic factory capable of creating instances for a type defined in a config file.
 * The config file must be present in Java properties form and the key naming the class of the type that is to be
 * created must be known.
 *
 * @param <T> Type the factory creates instances for.
 *
 * @author Tobias Wich
 */
public class GenericFactory <T> {

    private final Class<T> typeClass;
    private final Class<? extends T> actualClass;
    private final Constructor<? extends T> constructor;

    public GenericFactory(Class<T> typeClass, Properties properties, String key) throws GenericFactoryException {
	this.typeClass = typeClass;

	final String className = properties.getProperty(key);
	if (className == null) {
	    throw new GenericFactoryException("No factory class defined for the specified key '" + key + "'.");
	}

	try {
	    actualClass = loadClass(className);
	    constructor = getConstructor(actualClass);
	} catch (ClassNotFoundException ex) {
	    throw new GenericFactoryException(ex);
	} catch (NoSuchMethodException ex) {
	    throw new GenericFactoryException(ex);
	}
    }


    public T getInstance() throws GenericFactoryException {
	try {
	    T o = constructor.newInstance(); // default constructor
	    return o; // type is asserted by method definition
	} catch (InstantiationException ex) {
	    throw new GenericFactoryException(ex);
	} catch (IllegalAccessException ex) {
	    throw new GenericFactoryException(ex);
	} catch (IllegalArgumentException ex) {
	    throw new GenericFactoryException(ex);
	} catch (InvocationTargetException ex) {
	    throw new GenericFactoryException(ex);
	}
    }


    private Constructor<? extends T> getConstructor(Class<? extends T> clazz) throws GenericFactoryException, NoSuchMethodException {
	Constructor<? extends T> m = clazz.getConstructor();
	if (Modifier.isPublic(m.getModifiers())) {
	    return m;
	} else {
	    String msg = String.format("Constructor of class %s is not publicly available.", clazz.getName());
	    throw new GenericFactoryException(msg);
	}
    }

    private Class<? extends T> loadClass(String className) throws ClassNotFoundException, GenericFactoryException {
	Class<?> c = Class.forName(className);
	try {
	    Class<? extends T> c2 = c.asSubclass(typeClass);
	    return c2;
	} catch (ClassCastException ex) {
	    String msg = String.format("Referenced class %s is not a compatible subtype for this factory.", c.getName());
	    throw new GenericFactoryException(msg);
	}
    }

}
