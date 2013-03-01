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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;


/**
 * Supply classloader responsible for all ws classes.
 * This functionality is needed to dynamically load and instantiate the ws clients.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public abstract class WSClassLoader {

    private static final ClassLoader inst = WSClassLoader.class.getClassLoader();

    protected static final Map<String,String> serviceClasses;
    protected static final Map<String,String> servicePorts;

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

    // the loadClass function can not be made typesafe, so ignore this warning and live with the error if it happens
    @SuppressWarnings("unchecked")
    private static Class<Service> loadClass(String serviceName) throws ClassNotFoundException {
	String className = serviceClasses.get(serviceName);
	ClassLoader cl = WSClassLoader.class.getClassLoader();
	return (Class<Service>) cl.loadClass(className);
    }

    private static WebServiceClient getClientAnnotation(Class<Service> clazz) {
	return clazz.getAnnotation(WebServiceClient.class);
    }

    private static URL getWSDL() {
	URL url = WSClassLoader.class.getResource("/ALL.wsdl");
	if (url == null) {
	    url = WSClassLoader.class.getResource("ALL.wsdl");
	}
	return url;
    }

    /**
     * Gets a set of all available services implementation identifiers.
     * The identifiers are freely chosen by the developer and thus are not related to the services QName. The
     * identifiers returned by this function may be used in the {@link #getClientService()} functions.
     *
     * @return Set with all supported services.
     */
    @Nonnull
    public static Set<String> getSupportedServices() {
	TreeSet<String> s = new TreeSet<String>(serviceClasses.keySet());
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

    /**
     * Gets a client service instance of the given name.
     * The name must be one of the names returned by {@link #getSupportedServices()}. The use of this method is
     * strongly encouraged, as it uses the WSDLs and XML schemas bundled with this class. This leads to lower load
     * times.<br/>
     * The service client is initialized with the values from the WSDL. In order to set other endpoints, use the other
     * {@code getClientService} functions.
     *
     * @param serviceName Name of the service. Must be one of the names returned by {@link #getSupportedServices()}.
     * @return Webservice client initialized with the settings from the WSDL.
     * @throws NoSuchMethodException In case there is no suitable constructor or {@code getPort()} method in the service
     *   class.
     * @throws InstantiationException In case the constructor call failed.
     * @throws IllegalAccessException In case the constructor call or the {@code getPort()} method failed.
     * @throws InvocationTargetException In case the constructor call or the {@code getPort()} method failed.
     * @throws ClassNotFoundException In case no such service is registered in this loader.
     * @see #getClientService(java.lang.String, java.lang.String)
     * @see #getClientService(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public static Object getClientService(@Nonnull String serviceName)
	    throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException,
	           ClassNotFoundException {
	// the relevant information to create a service is extracted from the service instance
	// get serviceloader instance
	Class<Service> loaderClass = loadClass(serviceName);
	WebServiceClient clientAnnotation = getClientAnnotation(loaderClass);
	Constructor<Service> constructor = loaderClass.getConstructor(URL.class, QName.class);
	QName serviceQname = new QName(clientAnnotation.targetNamespace(), clientAnnotation.name());
	Service serviceLoaderInst = constructor.newInstance(getWSDL(), serviceQname);
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

    /**
     * Gets a client service instance of the given name and set the given endpoint address.
     * A detailed explanation can be found in {@link #getClientService(java.lang.String).
     *
     * @param serviceName Name of the service. Must be one of the names returned by {@link #getSupportedServices()}.
     * @param schema Schema part of the URL (e.g. {@code https}).
     * @param host Host part of the URL.
     * @param port Port part of the URL.
     * @param resource Resource part of the URL (e.g. {@code /} or {@code /service/endpoint}).
     * @return Webservice client initialized with the settings from the WSDL and the endpoint set to the given value.
     * @throws NoSuchMethodException In case there is no suitable constructor or {@code getPort()} method in the service
     *   class.
     * @throws InstantiationException In case the constructor call failed.
     * @throws IllegalAccessException In case the constructor call or the {@code getPort()} method failed.
     * @throws InvocationTargetException In case the constructor call or the {@code getPort()} method failed.
     * @throws ClassNotFoundException In case no such service is registered in this loader.
     */
    public static Object getClientService(@Nonnull String serviceName, @Nonnull String schema, @Nonnull String host,
	    @Nonnull String port, @Nonnull String resource)
	    throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException,
	           ClassNotFoundException {
	String address = schema + "://" + host + ":" + port + resource;
	Object serviceInst = getClientService(serviceName, address);

	return serviceInst;
    }

    /**
     * Gets a client service instance of the given name and set the given endpoint address.
     * A detailed explanation can be found in {@link #getClientService(java.lang.String).
     *
     * @param serviceName Name of the service. Must be one of the names returned by {@link #getSupportedServices()}.
     * @param address URL pointing to the service endpoint.
     * @return Webservice client initialized with the settings from the WSDL and the endpoint set to the given value.
     * @throws NoSuchMethodException In case there is no suitable constructor or {@code getPort()} method in the service
     *   class.
     * @throws InstantiationException In case the constructor call failed.
     * @throws IllegalAccessException In case the constructor call or the {@code getPort()} method failed.
     * @throws InvocationTargetException In case the constructor call or the {@code getPort()} method failed.
     * @throws ClassNotFoundException In case no such service is registered in this loader.
     */
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
