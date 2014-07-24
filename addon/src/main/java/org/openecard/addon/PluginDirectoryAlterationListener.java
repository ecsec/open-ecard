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
import java.util.Set;
import org.apache.commons.jci.monitor.FilesystemAlterationListener;
import org.apache.commons.jci.monitor.FilesystemAlterationObserver;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.ws.marshal.WSMarshallerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple listener for changes in the plugin directory.
 * <br/>It will add or unload a plugin in the plugin manager if it detects a file creation or removal.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
final class PluginDirectoryAlterationListener implements FilesystemAlterationListener {

    private static final Logger logger = LoggerFactory.getLogger(PluginDirectoryAlterationListener.class.getName());
    private final FileRegistry fileRegistry;
    private final AddonManager manager;


    PluginDirectoryAlterationListener(FileRegistry fileRegistry, AddonManager addonManager) {
	this.fileRegistry = fileRegistry;
	manager = addonManager;
    }

    @Override
    public void onFileDelete(File file) {
	String name = file.getName();
	AddonSpecification abd = fileRegistry.getAddonSpecByFileName(name);
	// call the destroy method of all actions and protocols
	manager.unloadAddon(abd);
	// remove configuration file
	AddonProperties addonProps = new AddonProperties(abd);
	addonProps.removeConfFile();
	// remove from file registry
	fileRegistry.unregister(file);
    }

    @Override
    public void onFileCreate(File file) {
	String name = file.getName();
	AddonSpecification abd = null;

	try {
	    ManifestExtractor maniEx = new ManifestExtractor();
	    abd = maniEx.getAddonSpecificationFromFile(file);
	} catch (WSMarshallerException ex) {
	    logger.error("Failed to initalize marshaller for AddonSpecification marshalling.", ex);
	}

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
	manager.loadLoadOnStartupActions(abd);
	logger.debug("Successfully registered {} as addon", name);
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
