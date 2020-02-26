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

package org.openecard.ws.jaxb;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javax.annotation.Nonnull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wrapper for JAXB marshaller and unmarshaller capable of modifying the supported JAXB types on the fly.
 *
 * @author Tobias Wich
 */
public class MarshallerImpl {

    private static final Logger LOG = LoggerFactory.getLogger(MarshallerImpl.class);

    private static final ArrayList<Class<?>> baseXmlElementClasses;
    private static final FutureTask<JAXBContext> baseJaxbContext;
    private static final HashMap<String, JAXBContext> specificContexts;

    private boolean userOverride;
    private final TreeSet<Class<?>> userClasses;

    private Marshaller marshaller;
    private Unmarshaller unmarshaller;

    static {
	// load predefined classes
	final Class<?>[] jaxbClasses = getJaxbClasses();
	baseXmlElementClasses = new ArrayList<>(jaxbClasses.length);
	baseXmlElementClasses.addAll(Arrays.asList(jaxbClasses));

	baseJaxbContext = new FutureTask<>(new Callable<JAXBContext>() {
	    @Override
	    public JAXBContext call() throws Exception {
		try {
		    return JAXBContext.newInstance(jaxbClasses);
		} catch (JAXBException ex) {
		    LOG.error("Failed to create JAXBContext instance.", ex);
		    throw new RuntimeException("Failed to create JAXBContext.");
		}
	    }
	});
	new Thread(baseJaxbContext, "JAXB-Classload").start();

	specificContexts = new HashMap<>();
    }


    /**
     * Creates a MarshallerImpl instance based on the JAXB types specified in the classpath resource classes.lst.
     */
    public MarshallerImpl() {
	userOverride = false;
	userClasses = new TreeSet<>(new ClassComparator());
    }


    /**
     * Adds the specified JAXB element types class to the list of supported JAXB types.
     * This method triggers a recreation of the wrapped marshaller and unmarshaller.
     *
     * @param c Class of the JAXB element type.
     */
    public synchronized void addXmlClass(Class<?> c) {
	addBaseClasses();
	if (! userClasses.contains(c)) {
	    //addJaxbClasses(c);
	    userClasses.add(c);
	    // class added to set
	    userOverride = true;
	    resetMarshaller();
	}
    }

    private void addBaseClasses() {
	// add all base classes if the user did not delete them before
	if (! userOverride) {
	    userClasses.addAll(baseXmlElementClasses);
	}
    }

    /**
     * Remove all JAXB element types from this instance.
     * New types must be added first before this instance is usable for marshalling and unmarshalling again.
     */
    public synchronized void removeAllClasses() {
	userOverride = true;
	userClasses.clear();
	resetMarshaller();
    }


    /**
     * Gets the wrapped JAXB marshaller instance.
     *
     * @return The wrapped JAXB marshaller instance.
     * @throws JAXBException If the marshaller could not be created.
     */
    public Marshaller getMarshaller() throws JAXBException {
	if (marshaller == null) {
	    loadInstances();
	}
	return marshaller;
    }

    /**
     * Gets the wrapped JAXB unmarshaller instance.
     *
     * @return The wrapped JAXB unmarshaller instance.
     * @throws JAXBException If the unmarshaller could not be created.
     */
    public Unmarshaller getUnmarshaller() throws JAXBException {
	if (unmarshaller == null) {
	    loadInstances();
	}
	return unmarshaller;
    }


    private void resetMarshaller() {
	marshaller = null;
	unmarshaller = null;
    }

    private synchronized void loadInstances() throws JAXBException {
	JAXBContext jaxbCtx;
	if (userOverride) {
	    String classHash = calculateClassesHash();
	    synchronized (specificContexts) {
		if (! specificContexts.containsKey(classHash)) {
		    jaxbCtx = JAXBContext.newInstance(userClasses.toArray(new Class<?>[userClasses.size()]));
		    specificContexts.put(classHash, jaxbCtx);
		} else {
		    jaxbCtx = specificContexts.get(classHash);
		}
	    }
	} else {
	    try {
		jaxbCtx = baseJaxbContext.get();
	    } catch (ExecutionException ex) {
		LOG.error("Failed to create JAXBContext instance.", ex);
		throw new RuntimeException("Failed to create JAXBContext.");
	    } catch (InterruptedException ex) {
		LOG.error("Thread terminated waiting for the JAXBContext to be created..", ex);
		throw new RuntimeException("Thread interrupted during waiting on the creation of the JAXBContext.");
	    }
	}
	marshaller = jaxbCtx.createMarshaller();
	unmarshaller = jaxbCtx.createUnmarshaller();
    }



    private static Class<?>[] getJaxbClasses() {
	ClassLoader cl = Thread.currentThread().getContextClassLoader();
	List<Class<?>> classes = new LinkedList<>();
	InputStream classListStream = cl.getResourceAsStream("classes.lst");
	InputStream classListStreamC = cl.getResourceAsStream("/classes.lst");

	try {
	    if (classListStream == null && classListStreamC == null) {
		throw new IOException("Failed to load classes.lst.");
	    } else {
		// select the one stream that is set
		classListStream = (classListStream != null) ? classListStream : classListStreamC;

		LineNumberReader r = new LineNumberReader(new InputStreamReader(classListStream));
		String next;
		// read all entries from file
		while ((next = r.readLine()) != null) {
		    try {
			// load class and see if it is a JAXB class
			Class<?> c = cl.loadClass(next);
			if (isJaxbClass(c)) {
			    classes.add(c);
			}
		    } catch (ClassNotFoundException ex) {
			LOG.error("Failed to load class: {}", next, ex);
		    }
		}
	    }
	} catch (IOException ex) {
	    LOG.error("Failed to read classes from file classes.lst.", ex);
	}

	return classes.toArray(new Class<?>[classes.size()]);
    }

    private static boolean isJaxbClass(Class<?> c) {
	return c.isAnnotationPresent(XmlType.class) ||
		c.isAnnotationPresent(XmlRegistry.class);
    }

    private String calculateClassesHash() {
	try {
	    MessageDigest md = MessageDigest.getInstance("MD5");
	    for (Class<?> c : userClasses) {
		md.update(c.getName().getBytes());
	    }
	    byte[] digest = md.digest();
	    return toHexString(digest);
	} catch (NoSuchAlgorithmException ex) {
	    throw new RuntimeException("MD5 hash algorithm is not supported on your platform.", ex);
	}
    }

    private static String toHexString(@Nonnull byte[] bytes) {
	StringWriter writer = new StringWriter(bytes.length * 2);
	PrintWriter out = new PrintWriter(writer);

	for (int i = 1; i <= bytes.length; i++) {
	    out.printf("%02X", bytes[i - 1]);
	}

	return writer.toString();
    }

}
