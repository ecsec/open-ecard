/****************************************************************************
 * Copyright (C) 2013-2015 HS Coburg.
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.addon.manifest.AppExtensionSpecification;
import org.openecard.addon.manifest.AppPluginSpecification;
import org.openecard.addon.manifest.LocalizedString;
import org.openecard.addon.manifest.ProtocolPluginSpecification;
import org.openecard.common.util.FileUtils;
import org.openecard.ws.marshal.WSMarshallerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This registry provides access to all add-ons in the plug-ins directory.
 * Adding and removing add-on-files at runtime is supported.
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
public class FileRegistry implements AddonRegistry {

    private static final Logger logger = LoggerFactory.getLogger(FileRegistry.class.getName());

    private static final HashMap<String, AddonSpecification> registeredAddons = new HashMap<>();
    private static final HashMap<String, File> files = new HashMap<>();
    private final AddonManager manager;
    private final Future<Void> initComplete;

    /**
     * Creates a new FileRegistry.
     * On the creation of the registry the add-on directory is retrieved and all existing add-ons are loaded.
     * Furthermore a {@link FilesystemAlterationMonitor} is started to be able to register newly added add-ons and
     * remove add-ons.
     *
     * @param manager {@link AddonManager} which takes care for the installed add-ons.
     */
    public FileRegistry(AddonManager manager) {
	this.manager = manager;

	FutureTask<Void> initCompleteTmp;
	try {
	    final String addonPath = FileUtils.getAddonsDir() + File.separator;

	    initCompleteTmp = new FutureTask<>(new Callable<Void>() {
		@Override
		public Void call() throws Exception {
		    loadExistingAddons();
		    startFileMonitor(addonPath);
		    return null;
		}
	    });
	    new Thread(initCompleteTmp, "Init-File-Addons").start();
	} catch (SecurityException e) {
	    String msg = "Failed to access add-on directory due to missing privileges. FileRegistry not working.";
	    logger.error(msg, e);
	    initCompleteTmp = getCompletedFuture();
	} catch (IOException e) {
	    logger.error("Failed to access add-on directory. FileRegistry not work.", e);
	    initCompleteTmp = getCompletedFuture();
	}

	this.initComplete = initCompleteTmp;
    }

    private FutureTask<Void> getCompletedFuture() {
	FutureTask<Void> f = new FutureTask(new Callable<Void>() {
	    @Override
	    public Void call() throws Exception {
		return null;
	    }
	});
	f.run();
	return f;
    }

    private void blockUntilInit() {
	try {
	    initComplete.get();
	} catch (InterruptedException ex) {
	    String msg = "Initialization of the file based Add-ons has been interrupted.";
	    logger.warn(msg);
	    throw new RuntimeException(msg);
	} catch (ExecutionException ex) {
	    String msg = "Initialization of the file based Add-ons yielded an error.";
	    logger.error(msg, ex);
	    throw new RuntimeException(msg, ex.getCause());
	}
    }

    private HashMap<String, AddonSpecification> getAddons() {
	blockUntilInit();
	return registeredAddons;
    }

    private HashMap<String, File> getFiles() {
	blockUntilInit();
	return files;
    }

    /**
     * Starts the FilesystemAlterationMonitor.
     * The method sets up a {@link FilesystemAlterationMonitor} and registers a {@link PluginDirectoryAlterationListener}.
     * After the setup the monitor is started.
     *
     * @param addonPath Path to the directory which shall be monitored.
     */
    private void startFileMonitor(String addonPath) {
	try {
	    File f = new File(addonPath);
	    logger.debug("Starting file alteration monitor on path: {}", f.getPath());
	    FilesystemAlterationMonitor fam = new FilesystemAlterationMonitor();
	    fam.addListener(f, new PluginDirectoryAlterationListener(this, manager));
	    fam.start();
	} catch (SecurityException ex) {
	    logger.error("SecurityException seems like you don't have permissions to access the addons directory.", ex);
	}
    }

    /**
     * Registers a new add-on.
     *
     * @param desc The {@link AddonSpecification} of the add-on to add.
     * @param file A {@link File} object which points to the add-ons jar file.
     */
    public void register(AddonSpecification desc, File file) {
	registeredAddons.put(file.getName(), desc);
	files.put(desc.getId(), file);
    }

    /**
     * Unregister a specific add-on.
     *
     * @param file A {@link File} object which points to the jar file of the add-on.
     */
    public void unregister(File file) {
	Set<Entry<String, File>> entrySet = getFiles().entrySet();
	Iterator<Entry<String, File>> iterator = entrySet.iterator();
	while (iterator.hasNext()) {
	    Entry<String, File> next = iterator.next();
	    if (next.getValue().equals(file)) {
		String id = next.getKey();
		registeredAddons.remove(file.getName());
		files.remove(id);
		logger.debug("Successfully removed addon {}", file.getName());
		break;
	    }
	}
    }

    @Override
    public Set<AddonSpecification> listAddons() {
	Set<AddonSpecification> list = new HashSet<>();
	list.addAll(getAddons().values());
	return list;
    }

    @Override
    public AddonSpecification search(String id) {
	for (AddonSpecification desc : getAddons().values()) {
	    if (desc.getId().equals(id)) {
		return desc;
	    }
	}
	return null;
    }

    @Override
    public Set<AddonSpecification> searchByName(String name) {
	Set<AddonSpecification> matchingAddons = new HashSet<>();
	for (AddonSpecification desc : getAddons().values()) {
	    for (LocalizedString s : desc.getLocalizedName()) {
		if (s.getValue().equals(name)) {
		    matchingAddons.add(desc);
		}
	    }
	}
	return matchingAddons;
    }

    @Override
    public Set<AddonSpecification> searchIFDProtocol(String uri) {
	Set<AddonSpecification> matchingAddons = new HashSet<>();
	for (AddonSpecification desc : getAddons().values()) {
	    ProtocolPluginSpecification protocolDesc = desc.searchIFDActionByURI(uri);
	    if (protocolDesc != null) {
		matchingAddons.add(desc);
	    }
	}
	return matchingAddons;
    }

    @Override
    public Set<AddonSpecification> searchSALProtocol(String uri) {
	Set<AddonSpecification> matchingAddons = new HashSet<>();
	for (AddonSpecification desc : getAddons().values()) {
	    ProtocolPluginSpecification protocolDesc = desc.searchSALActionByURI(uri);
	    if (protocolDesc != null) {
		matchingAddons.add(desc);
	    }
	}
	return matchingAddons;
    }

    @Override
    public ClassLoader downloadAddon(AddonSpecification addonSpec) throws AddonException {
	String aId = addonSpec.getId();
	// TODO: use other own classloader impl with security features
	ClassLoader cl = getClass().getClassLoader();
	try {
	    URL[] url = new URL[] { files.get(aId).toURI().toURL() };
	    URLClassLoader ucl = new URLClassLoader(url, cl);
	    return ucl;
	} catch (MalformedURLException e) {
	    logger.error(e.getMessage(), e);
	    throw new AddonException("Failed to convert Add-on location URI to URL.");
	}
    }

    @Override
    public Set<AddonSpecification> searchByResourceName(String resourceName) {
	Set<AddonSpecification> matchingAddons = new HashSet<>();
	for (AddonSpecification desc : getAddons().values()) {
	    AppPluginSpecification actionDesc = desc.searchByResourceName(resourceName);
	    if (actionDesc != null) {
		matchingAddons.add(desc);
	    }
	}
	return matchingAddons;
    }

    @Override
    public Set<AddonSpecification> searchByActionId(String actionId) {
	Set<AddonSpecification> matchingAddons = new HashSet<>();
	for (AddonSpecification desc : getAddons().values()) {
	    AppExtensionSpecification actionDesc = desc.searchByActionId(actionId);
	    if (actionDesc != null) {
		matchingAddons.add(desc);
	    }
	}
	return matchingAddons;
    }

    /**
     * Register all add-ons which are already installed in the add-ons directory.
     *
     * @throws WSMarshallerException Thrown if the instantiation of the marshaler for the AddonSpecification marshaling
     * failed.
     */
    private void loadExistingAddons() throws WSMarshallerException {
	try {
	    File addonsDir = FileUtils.getAddonsDir();
	    File[] addons = addonsDir.listFiles(new JARFileFilter());
	    addons = addons == null ? new File[0] : addons;
	    ManifestExtractor mEx = new ManifestExtractor();

	    for (File addon : addons) {
		AddonSpecification addonSpec = mEx.getAddonSpecificationFromFile(addon);
		if (addonSpec != null) {
		    register(addonSpec, addon);
		    logger.info("Loaded external addon {}", addon.getName());
		}
	    }
	} catch (IOException ex) {
	    logger.error("Failed to load addons directory.", ex);
	} catch (SecurityException ex) {
	    logger.error("SecurityException seems like you don't have permissions to access the addons directory.", ex);
	}
    }

    @Override
    public Set<AddonSpecification> listInstalledAddons() {
	// This registry does not provide a AppStore based system so just return the result of listAddons() method.
	return listAddons();
    }

    /**
     * Get an AddonSpecification by the file name of the add-on.
     *
     * @param fileName Name of the add-ons jar file.
     * @return The {@link AddonSpecification} of add-on with the name {@code fileName}.
     */
    protected AddonSpecification getAddonSpecByFileName(String fileName) {
	return getAddons().get(fileName);
    }

    /**
     * Uninstall an add-on.
     * The method removes the jar file containing the add-on. The cleanup is done by the
     * {@link PluginDirectoryAlterationListener}. This method is intended just for the {@link AddonManager} and should
     * not be called in any other class.
     *
     * @param addonSpec The {@link AddonSpecification} of the add-on to uninstall.
     */
    protected void uninstallAddon(AddonSpecification addonSpec) {
	File addonJar = files.get(addonSpec.getId());
	addonJar.delete();
    }

}
