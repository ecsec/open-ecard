package org.openecard.client.transport.dispatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
	serviceMap = loadServiceDefinition();
	serviceInstMap = loadEnvDefinition();
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


    private static TreeMap<String,Service> loadServiceDefinition() {
	// get properties
	String packagePrefix = DispatcherProperties.getProperty("org.openecard.client.transport.dispatcher.pkg-prefix");
	String interfaceStr = DispatcherProperties.getProperty("org.openecard.client.transport.dispatcher.interfaces");
	// split input into class names or package names with classes
	Pattern p = Pattern.compile("([A-Za-z_]{1}[A-Za-z0-9_]*(\\.[A-Za-z_]{1}[A-Za-z0-9_]*)*)");
	Matcher matcher = p.matcher(interfaceStr);

	// check if the classes can be loaded
	ArrayList<String> classNames = new ArrayList<String>();
	while (matcher.find()) {
	    String nextMatch = matcher.group();
	    String nextWithPrefix = packagePrefix + "." + nextMatch;
	    // determine the type of the name
	    if (isClass(nextMatch) && isWebService(nextMatch)) {
		classNames.add(nextMatch);
	    } else if (isClass(nextWithPrefix) && isWebService(nextWithPrefix)) {
		classNames.add(nextWithPrefix);
	    } else {
		_logger.log(Level.WARNING, "Omitting interface with name {0}, because it could not be loaded or is not a Webservice interface.", new Object[]{nextMatch});
	    }
	}

	// loadServiceDefinition methods and create pairing
	TreeMap<String,Service> serviceMap = new TreeMap<String, Service>();
	for (String next : classNames) {
	    Class c;
	    try {
		c = MessageDispatcher.class.getClassLoader().loadClass(next);
		Service s = new Service(c);
		List<Class> reqClasses = s.getRequestClasses();
		for (Class cl : reqClasses) {
		    if (serviceMap.containsKey(cl.getName())) {
			_logger.log(Level.WARNING, "Omitting method with parameter type {0} in service interface {1} because its type already associated with another service.", new Object[]{cl.getName(), c.getName()});
		    } else {
			serviceMap.put(cl.getName(), s);
		    }
		}
	    } catch (ClassNotFoundException ex) {
	    }
	}

	return serviceMap;
    }

    private static boolean isClass(String name) {
	try {
	    // try to loadServiceDefinition class, if that fails it may lack a package name
	    MessageDispatcher.class.getClassLoader().loadClass(name);
	} catch (ClassNotFoundException ex) {
	    return false;
	}
	return true;
    }

    private static boolean isWebService(String className) {
	try {
	    // loadServiceDefinition class
	    Class clazz = MessageDispatcher.class.getClassLoader().loadClass(className);
	    // check if it is an interface
	    if (! clazz.isInterface()) {
		return false;
	    }
	    // check annotation for Webservice
	    if (! clazz.isAnnotationPresent(javax.jws.WebService.class)) {
		return false;
	    }
	} catch (ClassNotFoundException ex) {
	    return false;
	}
	return true;
    }


    private static TreeMap<String,Method> loadEnvDefinition() {
	Method[] allMethods = Environment.class.getMethods();
	TreeMap<String,Method> methods = new TreeMap<String,Method>();
	// filter methods
	for (Method m : allMethods) {
	    int modifiers = m.getModifiers();
	    // conditions that make the code ignore the parameter
	    if (  Modifier.isStatic(modifiers))      continue;
	    if (! Modifier.isPublic(modifiers))      continue;
	    if (  m.getParameterTypes().length != 0) continue;
	    if (! m.getReturnType().isInterface())   continue;
	    if (! m.getReturnType().isAnnotationPresent(javax.jws.WebService.class)) continue;
	    // seems we have a good one
	    methods.put(m.getReturnType().getName(), m);
	}

	return methods;
    }

}
