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

package org.openecard.ws.jaxb;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class MarshallerImpl {

    private static final Logger logger = LoggerFactory.getLogger(MarshallerImpl.class);

    private static final List<Class<?>> baseXmlElementClasses;
    private static final JAXBContext baseJaxbContext;

    private boolean userOverride;
    private final Set<Class<?>> userClasses;

    private Marshaller marshaller;
    private Unmarshaller unmarshaller;

    static {
	// load predefined classes
	Class<?>[] jaxbClasses = getJaxbClasses();
	baseXmlElementClasses = new ArrayList<>(jaxbClasses.length);
	baseXmlElementClasses.addAll(Arrays.asList(jaxbClasses));

	try {
	    baseJaxbContext = JAXBContext.newInstance(jaxbClasses);
	} catch (JAXBException ex) {
	    logger.error("Failed to create JAXBContext instance.", ex);
	    throw new RuntimeException("Failed to create JAXBContext.");
	}
    }


    /**
     * Creates a MarshallerImpl instance based on the JAXB types specified in the classpath resource classes.lst.
     */
    public MarshallerImpl() {
	userOverride = false;
	userClasses = new HashSet<>(baseXmlElementClasses);
    }


    /**
     * Adds the specified JAXB element types class to the list of supported JAXB types.
     * This method triggers a recreation of the wrapped marshaller and unmarshaller.
     *
     * @param c Class of the JAXB element type.
     */
    public synchronized void addXmlClass(Class<?> c) {
	if (! userClasses.contains(c)) {
	    //addJaxbClasses(c);
	    userClasses.add(c);
	    // class added to set
	    userOverride = true;
	    resetMarshaller();
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
    public synchronized Marshaller getMarshaller() throws JAXBException {
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
    public synchronized Unmarshaller getUnmarshaller() throws JAXBException {
	if (unmarshaller == null) {
	    loadInstances();
	}
	return unmarshaller;
    }


    private void resetMarshaller() {
	marshaller = null;
	unmarshaller = null;
    }

    private void loadInstances() throws JAXBException {
	JAXBContext jaxbCtx;
	if (userOverride) {
	    jaxbCtx = JAXBContext.newInstance(userClasses.toArray(new Class<?>[userClasses.size()]));
	} else {
	    jaxbCtx = baseJaxbContext;
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
			logger.error("Failed to load class: " + next, ex);
		    }
		}
	    }
	} catch (IOException ex) {
	    logger.error("Failed to read classes from file classes.lst.", ex);
	}

	return classes.toArray(new Class<?>[classes.size()]);
    }

    private static boolean isJaxbClass(Class<?> c) {
	return c.isAnnotationPresent(XmlType.class) ||
		c.isAnnotationPresent(XmlRegistry.class);
    }

}
