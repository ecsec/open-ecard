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

package org.openecard.ws.marshal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;


/**
 * Supply classloader responsible for all ws classes. <br/>
 * This is needed for the clojure scripts to find the ws classes.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public abstract class WSClassLoader {

    private static final ClassLoader inst = WSClassLoader.class.getClassLoader();

    protected static final Map<String,String> serviceClasses;
    protected static final Map<String,String> servicePorts;

    private static Class loadClass(String serviceName) throws ClassNotFoundException {
	String className = serviceClasses.get(serviceName);
	return inst.loadClass(className);
    }

    public static Set<String> getSupportedServices() {
	TreeSet<String> s = new TreeSet(serviceClasses.keySet());
	Iterator<String> it = s.iterator();
	while (it.hasNext()) {
	    String nextService = it.next();
	    try {
		Class c = loadClass(nextService);
	    } catch (ClassNotFoundException ex) {
		// no such class, remove this service
		it.remove();
	    }
	}
	return s;
    }

    static {
	// load service maps
	serviceClasses = new HashMap<String, String>();
	servicePorts = new HashMap<String, String>();

	serviceClasses.put("SAL", "org.openecard.ws.SAL_Service");
	servicePorts  .put("SAL", "SALPort");

	serviceClasses.put("Publish", "org.openecard.ws.Publish_Service");
	servicePorts  .put("Publish", "Publish");

	serviceClasses.put("Subscribe", "org.openecard.ws.Subscribe_Service");
	servicePorts  .put("Subscribe", "Subscribe");

	serviceClasses.put("ISO24727-Protocols", "org.openecard.ws.ISO24727Protocols_Service");
	servicePorts  .put("ISO24727-Protocols", "ISO24727ProtocolsPort");

	serviceClasses.put("IFD", "org.openecard.ws.IFD_Service");
	servicePorts  .put("IFD", "IFDPort");

	serviceClasses.put("IFDCallback", "org.openecard.ws.IFDCallback_Service");
	servicePorts  .put("IFDCallback", "IFDCallbackPort");

	serviceClasses.put("GetCardInfoOrACD", "org.openecard.ws.GetCardInfoOrACD_Service");
	servicePorts  .put("GetCardInfoOrACD", "GetCardInfoOrACD");

	serviceClasses.put("GetRecognitionTree", "org.openecard.ws.GetRecognitionTree_Service");
	servicePorts  .put("GetRecognitionTree", "GetRecognitionTree");

	serviceClasses.put("Management", "org.openecard.ws.Management_Service");
	servicePorts  .put("Management", "ManagementPort");

	serviceClasses.put("UpdateService", "org.openecard.ws.UpdateService_Service");
	servicePorts  .put("UpdateService", "UpdateServicePort");
    }

    private static javax.xml.ws.WebServiceClient getClientAnnotation(Class clazz) {
	return (WebServiceClient) clazz.getAnnotation(WebServiceClient.class);
    }

    private static URL getWSDL(Class serviceClass) {
	URL url = WSClassLoader.class.getResource("/ALL.wsdl");
	if (url == null) {
	    url = WSClassLoader.class.getResource("ALL.wsdl");
	}
	return url;
    }

    // this is very fancy stuff, the relevant information to create a service is extracted from the static service instance
    public static Object getClientService(String serviceName)
	    throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
	// get serviceloader instance
	Class loaderClass = loadClass(serviceName);
	javax.xml.ws.WebServiceClient clientAnnotation = getClientAnnotation(loaderClass);
	Constructor constructor = loaderClass.getConstructor(URL.class, QName.class);
	Service serviceLoaderInst = (Service) constructor.newInstance(getWSDL(loaderClass), new QName(clientAnnotation.targetNamespace(), clientAnnotation.name()));
	//Constructor constructor = loaderClass.getConstructor();
	//Service serviceLoaderInst = (Service) constructor.newInstance();

	// get portmethod and call it to get actual service
	String portName = servicePorts.get(serviceName);
	Method portMethod = serviceLoaderInst.getClass().getMethod("get" + portName, WebServiceFeature[].class);
	//WebServiceFeature validator = (WebServiceFeature) inst.loadClass("com.sun.xml.internal.ws.developer.SchemaValidationFeature").getConstructor().newInstance();
	//Object serviceInst = portMethod.invoke(serviceLoaderInst, new Object[]{ new WebServiceFeature[] {validator} });
	Object serviceInst = portMethod.invoke(serviceLoaderInst, new Object[]{ new WebServiceFeature[] {} });

	return serviceInst;
    }

    public static Object getClientService(String serviceName, String schema, String host, String port, String resource)
	    throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
	String address = schema + "://" + host + ":" + port + resource;
	Object serviceInst = getClientService(serviceName, address);

	return serviceInst;
    }

    public static Object getClientService(String serviceName, String address)
	    throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
	// get client
	Object serviceInst = getClientService(serviceName);
	// set endpoint
	BindingProvider bp = (BindingProvider) serviceInst;
	Map<String,Object> context = bp.getRequestContext();
	context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address);

	return serviceInst;
    }

}
