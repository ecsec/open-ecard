/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.addon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.ws.marshal.MarshallingTypeException;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


/**
 * This class implements an extractor which extracts the AddonSpecification from an addon jar file.
 *
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
public class ManifestExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ManifestExtractor.class);

    private static final String MANIFEST_XML = "META-INF/addon.xml";
    private final WSMarshaller marshaller;

    public ManifestExtractor() throws MarshallingTypeException, WSMarshallerException {
	marshaller = WSMarshallerFactory.createInstance();
	marshaller.removeAllTypeClasses();
	marshaller.addXmlTypeClass(AddonSpecification.class);
    }

    /**
     * Get an AddonSpecification object from the jar file containing the addon.
     *
     * @param file jar file to get the AddonSpecification from.
     * @return An {@link AddonSpecification} object corresponding to addon.
     */
    public AddonSpecification getAddonSpecificationFromFile(File file) {
	String name = file.getName();
	JarFile jarFile;
	AddonSpecification abd;
	try {
	    jarFile = new JarFile(file);
	} catch (IOException e) {
	    logger.error("File {} will not be registered as plugin because it's not a JarFile.", name);
	    return null;
	}
	try {
	    InputStream manifestStream = getPluginEntryClass(jarFile);

	    if (manifestStream == null) {
		logger.error("File {} will not be registered as plugin because it doesn't contain a addon.xml.", name);
		return null;
	    } else {
		marshaller.addXmlTypeClass(AddonSpecification.class);
		Document manifestDoc = marshaller.str2doc(manifestStream);
		abd = (AddonSpecification) marshaller.unmarshal(manifestDoc);
	    }
	} catch (IOException ex) {
	    logger.error("Failed to process addon.xml entry for file " + name, ex);
	    return null;
	} catch (MarshallingTypeException e) {
	    logger.error("Failed to process addon.xml entry for file " + name, e);
	    return null;
	} catch (SAXException e) {
	    logger.error("Failed to process addon.xml entry for file " + name, e);
	    return null;
	} catch (WSMarshallerException e) {
	    logger.error("Failed to process addon.xml entry for file " + name, e);
	    return null;
	} finally {
	    try {
		jarFile.close();
	    } catch (IOException ex) {
		logger.error("Failed to close jar file.", ex);
	    }
	}
	return abd;
    }

    /**
     * Get the addon.xml file as InputStream object from the jar file.
     *
     * @param jarFile The jar file which should contain the addon.xml file.
     * @return A InputStream object to the addon.xml file.
     * @throws IOException Thrown if the {@link InputStream} can't be returned.
     */
    private InputStream getPluginEntryClass(JarFile jarFile) throws IOException {
	ZipEntry manifest = jarFile.getEntry(MANIFEST_XML);
	if (manifest == null) {
	    return null;
	} else {
	    return jarFile.getInputStream(manifest);
	}
    }
}
