/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
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
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.apache.commons.jci.monitor.FilesystemAlterationListener;
import org.apache.commons.jci.monitor.FilesystemAlterationObserver;
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
 * Simple listener for changes in the plugin directory.
 * <br/>It will add or unload a plugin in the plugin manager if it detects a file creation or removal.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
final class PluginDirectoryAlterationListener implements FilesystemAlterationListener {

    private static final Logger logger = LoggerFactory.getLogger(PluginDirectoryAlterationListener.class.getName());

    private static final String MANIFEST_XML = "META-INF/Addon.xml";
    private final FileRegistry fileRegistry;
    private final WSMarshaller marshaller;

    PluginDirectoryAlterationListener(FileRegistry fileRegistry) throws WSMarshallerException {
	this.fileRegistry = fileRegistry;
	marshaller = WSMarshallerFactory.createInstance();
	marshaller.addXmlTypeClass(AddonSpecification.class);
    }

    @Override
    public void onFileDelete(File file) {
	fileRegistry.unregister(file);
    }

    @Override
    public void onFileCreate(File file) {
	String name = file.getName();
	AddonSpecification abd = getAddonSpecificationFromFile(file);
	if (abd == null) {
	    return;
	}
	Set<AddonSpecification> plugins = fileRegistry.listAddons();
	for (AddonSpecification desc : plugins) {
	    if (desc.getId().equals(abd.getId())) {
		logger.debug("Addon {} is already registered", name);
		return;
	    }
	}
	fileRegistry.register(abd, file);
	logger.debug("Successfully registered {} as addon", name);
    }

    private AddonSpecification getAddonSpecificationFromFile(File file) {
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
		logger.error("File {} will not be registered as plugin because it doesn't contain a Manifest.xml.", name);
		return null;
	    } else {
		marshaller.addXmlTypeClass(AddonSpecification.class);
		Document manifestDoc = marshaller.str2doc(manifestStream);
		abd = (AddonSpecification) marshaller.unmarshal(manifestDoc);
	    }
	} catch (IOException ex) {
	    logger.error("Failed to process Manifest.xml entry for file " + name, ex);
	    return null;
	} catch (MarshallingTypeException e) {
	    logger.error("Failed to process Manifest.xml entry for file " + name, e);
	    return null;
	} catch (SAXException e) {
	    logger.error("Failed to process Manifest.xml entry for file " + name, e);
	    return null;
	} catch (WSMarshallerException e) {
	    logger.error("Failed to process Manifest.xml entry for file " + name, e);
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

    private InputStream getPluginEntryClass(JarFile jarFile) throws IOException {
	ZipEntry manifest = jarFile.getEntry(MANIFEST_XML);
	if (manifest == null) {
	    return null;
	} else {
	    return jarFile.getInputStream(manifest);
	}
    }

    @Override
    public void onStop(FilesystemAlterationObserver observer) {
	// ignore
    }

    @Override
    public void onStart(FilesystemAlterationObserver observer) {
	// ignore
    }

    @Override
    public void onFileChange(File file) {
	// ignore
    }

    @Override
    public void onDirectoryDelete(File file) {
	// ignore
    }

    @Override
    public void onDirectoryCreate(File file) {
	// ignore
    }

    @Override
    public void onDirectoryChange(File file) {
	// ignore
    }

}
