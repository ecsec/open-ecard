package org.openecard.client.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Properties;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class GenericFactory <T> {

    private final Constructor<T> constructor;

    public GenericFactory(Properties properties, String key) throws GenericFactoryException {
	String className = properties.getProperty(key);
	if (className == null) {
	    throw new GenericFactoryException("No factory class defined for the specified key '" + key + "'.");
	}

	try {
	    constructor = loadClass(className);
	} catch (ClassNotFoundException ex) {
	    throw new GenericFactoryException(ex);
	} catch (NoSuchMethodException ex) {
	    throw new GenericFactoryException(ex);
	}
    }


    public T getInstance() throws GenericFactoryException {
	try {
	    T o = constructor.newInstance(); // null because it is static
	    return o; // type is asserted by method definition
	} catch (Throwable t) {
	    throw new GenericFactoryException(t);
	}
    }


    private Constructor<T> loadClass(String className) throws ClassNotFoundException, GenericFactoryException, NoSuchMethodException {
	ClassLoader cl = this.getClass().getClassLoader();
	Class c = cl.loadClass(className);
	Constructor<T> m = c.getConstructor();
	if (Modifier.isPublic(m.getModifiers())) {
	    return m;
	} else {
	    throw new GenericFactoryException("Constructor of class " + className + " is not publicly available.");
	}
    }

}
