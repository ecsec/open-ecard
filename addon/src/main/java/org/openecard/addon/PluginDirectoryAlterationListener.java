/****************************************************************************
 * Copyright (C) 2013-2014 HS Coburg.
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
import java.util.Set;
import org.apache.commons.jci.monitor.FilesystemAlterationListener;
import org.apache.commons.jci.monitor.FilesystemAlterationObserver;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.common.util.FileUtils;
import org.openecard.ws.marshal.WSMarshallerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple listener for changes in the plug-in directory.
 * <br/>It will add or unload a plug-in in the plug-in manager if it detects a file creation or removal.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
final class PluginDirectoryAlterationListener implements FilesystemAlterationListener {

    private static final Logger logger = LoggerFactory.getLogger(PluginDirectoryAlterationListener.class.getName());

    private final FileRegistry fileRegistry;
    private final AddonManager manager;

    private String pathString = null;

    PluginDirectoryAlterationListener(FileRegistry fileRegistry, AddonManager addonManager) {
	this.fileRegistry = fileRegistry;
	manager = addonManager;
	try {
	    pathString = FileUtils.getAddonsDir().getCanonicalPath();
	} catch (IOException | SecurityException ex) {
	    pathString = null;
	    logger.error("Can not get the add-ons directory either because of a permission problem or an other IO related "
		    + "problem.", ex);
	}
    }

    @Override
    public void onFileDelete(File file) {
	// Seems like the Monitor monitor the directory recursively so just become active if a jar file was deleted.
	AddonSpecification spec = checkFileOnDelete(file);
	if (spec != null) {
	    // call the destroy method of all actions and protocols
	    manager.unloadAddon(spec);
	    // remove configuration file
	    AddonProperties addonProps = new AddonProperties(spec);
	    addonProps.removeConfiguration();
	    // remove from file registry
	    fileRegistry.unregister(file);
	    logger.debug("Succesfully removed add-on {}.", file.getName());
	}
    }

    @Override
    public void onFileCreate(File file) {
	String name = file.getName();
	AddonSpecification abd;
	try {
	    abd = checkFileOnCreate(file);

	    if (abd != null) {
		fileRegistry.register(abd, file);
		manager.loadLoadOnStartupActions(abd);
		logger.debug("Successfully registered {} as addon", name);
	    } else {
		logger.error("The .jar file with the name {} does not seem to be an add-on or is an already registered "
			+ "add-on.", name);
	    }
	} catch (WSMarshallerException ex) {
	    logger.error("Failed to initalize marshaller for AddonSpecification marshalling.", ex);
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

    /**
     * Check a newly created file whether it is an .jar file and whether it contains a addon.xml file.
     *
     * @param file A File object which points to a the newly created file.
     * @return An AddonSpecification in case there was an addon.xml file in the jar file in any other case {@code NULL}
     * is returned.
     * @throws WSMarshallerException Thrown if the initialization of the {@link ManifestExtractor} failed.
     */
    private AddonSpecification checkFileOnCreate(File file) throws WSMarshallerException {
	if (pathCheck(file, true)) {
	    // now check if there is a manifest
	    ManifestExtractor mfEx = new ManifestExtractor();
	    AddonSpecification spec = mfEx.getAddonSpecificationFromFile(file);
	    // return the manifest if a valid addon file was found
	    if (spec != null) {
		Set<AddonSpecification> plugins = fileRegistry.listAddons();
		for (AddonSpecification desc : plugins) {
		    if (desc.getId().equals(spec.getId())) {
			logger.debug("Addon {} is already registered", file.getName());
			return null;
		    }
		}
		return spec;
	    }
	}

	return null;
    }

    /**
     * Check whether the deleted file was an .jar file and whether it is registered in the file registry.
     *
     * @param file The file object to test.
     * @return The AddonSpecification which corresponds to the files name. If no such .jar file is registered {@code NULL}
     * is returned.
     */
    private AddonSpecification checkFileOnDelete(File file) {
	AddonSpecification spec = null;

	if (pathCheck(file, false)) {
	    spec = fileRegistry.getAddonSpecByFileName(file.getName());
	}

	return spec;
    }

    /**
     * Check whether the given File object points to an .jar file which is located in the add-ons directory.
     *
     * @param file The file object to check.
     * @return @{code TRUE} if the file is an .jar file and is located in the add-ons directory.
     */
    private boolean pathCheck(File file, boolean isCreate) {
	if (file.getName().endsWith(".jar")) {
	    try {
		if (isCreate) {
		    if (file.getParentFile().getCanonicalPath().equals(pathString) && file.isFile()) {
			return true;
		    }
		} else {
		    if (file.getParentFile().getCanonicalPath().equals(pathString)) {
			return true;
		    }
		}
	    } catch (IOException ex) {
		logger.error("Can't get parent path of the add .jar file.", ex);
	    }
	} else {
	    logger.info("The file {} is no .jar file. Ignoring the file.", file.getName());
	}

	return false;
    }

}
