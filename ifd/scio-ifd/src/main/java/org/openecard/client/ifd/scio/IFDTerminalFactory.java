package org.openecard.client.ifd.scio;

import org.openecard.client.ifd.IFDException;
import org.openecard.client.ifd.IFDProperties;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.openecard.client.common.ifd.TerminalFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class IFDTerminalFactory {

    private static Class factoryImplClass = null;
    private static Method factoryImplMethod = null;

    public static TerminalFactory getInstance() throws IFDException {
	try {
	    if (factoryImplClass == null || !factoryImplClass.getName().equals(getClassName())) {
		loadClass();
	    }

	    Object o = factoryImplMethod.invoke(null); // null because it is static
	    if (o != null) {
		return (TerminalFactory) o; // type is asserted by method definition
	    } else {
		throw new NullPointerException("TerminalFactory creation method returned null.");
	    }
	} catch (Throwable t) {
	    throw new IFDException(t);
	}
    }

    private static void loadClass() throws ClassNotFoundException, NoSuchMethodException {
	factoryImplClass = null;
	factoryImplMethod = null;

	String typeName = getClassName();
	ClassLoader cl = IFDTerminalFactory.class.getClassLoader();
	Class c = cl.loadClass(typeName);
	Method m = c.getMethod("getInstance", new Class[0]);
	if (Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())) {
	    Class returnClass = m.getReturnType();
	    if (TerminalFactory.class.isAssignableFrom(returnClass)) {
		factoryImplClass = c;
		factoryImplMethod = m;
	    } else {
		throw new NoSuchMethodException("getInstance method of class " + typeName + " has a return value which is incompatible with class " + TerminalFactory.class.getName() + ".");
	    }
	} else {
	    throw new NoSuchMethodException("getInstance method of class " + typeName + " is not static.");
	}
    }

    private static String getClassName() {
	return IFDProperties.getProperty("de.ecsec.ifd.scio.factory.impl");
    }

}
