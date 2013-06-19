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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
import org.openecard.addon.manifest.AddonBundleDescription;
import org.openecard.addon.manifest.AppPluginActionDescription;
import org.openecard.addon.manifest.ProtocolPluginDescription;
import org.openecard.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This registry provides access to all addons in the plugins directory.
 * Adding and removing addon-files at runtime is supported.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class FileRegistry implements AddonRegistry {

    private static final Logger logger = LoggerFactory.getLogger(FileRegistry.class.getName());

    private static final ArrayList<AddonBundleDescription> registeredAddons = new ArrayList<AddonBundleDescription>();
    private static final HashMap<String, File> files = new HashMap<String, File>();
    private static FileRegistry instance;

    private FileRegistry() {
	String pluginsPath;
	try {
	    pluginsPath = FileUtils.getHomeConfigDir() + File.separator + "plugins" + File.separator;
	} catch (SecurityException e) {
	    logger.error("Failed to get plugin directory; FileRegistry won't work.", e);
	    return;
	} catch (IOException e) {
	    logger.error("Failed to get plugin directory; FileRegistry won't work.", e);
	    return;
	}
	startFileMonitor(pluginsPath);
    }

    private void startFileMonitor(String pluginsPath) {
	File f = new File(pluginsPath);
	logger.debug("Starting FilesystemAlterationMonitor on Path: {}", f.getPath());
	FilesystemAlterationMonitor fam = new FilesystemAlterationMonitor();
	fam.addListener(f, new PluginDirectoryAlterationListener(this));
	fam.start();
    }

    public static FileRegistry getInstance() {
	if (instance == null) {
	    instance = new FileRegistry();
	}
	return instance;
    }

    public void register(AddonBundleDescription desc, File file) {
	registeredAddons.add(desc);
	files.put(desc.getId(), file);
    }

    public void unregister(File file) {
	Set<Entry<String, File>> entrySet = files.entrySet();
	Iterator<Entry<String, File>> iterator = entrySet.iterator();
	while (iterator.hasNext()) {
	    Entry<String, File> next = iterator.next();
	    if (next.getValue().equals(file)) {
		String id = next.getKey();
		AddonBundleDescription desc = this.search(id);
		registeredAddons.remove(desc);
		files.remove(id);
		logger.debug("Successfully removed addon {}", file.getName());
		break;
	    }
	}
    }

    @Override
    public Set<AddonBundleDescription> listPlugins() {
	Set<AddonBundleDescription> list = new HashSet<AddonBundleDescription>();
	list.addAll(registeredAddons);
	return list;
    }

    @Override
    public AddonBundleDescription search(String id) {
	for (AddonBundleDescription desc : registeredAddons) {
	    if (desc.getId().equals(id)) {
		return desc;
	    }
	}
	return null;
    }

    @Override
    public Set<AddonBundleDescription> searchByName(String name) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<AddonBundleDescription> searchProtocol(String uri) {
	Set<AddonBundleDescription> matchingAddons = new HashSet<AddonBundleDescription>();
	for (AddonBundleDescription desc : registeredAddons) {
	    ProtocolPluginDescription protocolDesc = desc.searchIFDActionByURI(uri);
	    if (protocolDesc != null) {
		matchingAddons.add(desc);
	    }
	    protocolDesc = desc.searchSALActionByURI(uri);
	    if (protocolDesc != null) {
		matchingAddons.add(desc);
	    }
	}
	return matchingAddons;
    }

    @Override
    public ClassLoader downloadPlugin(String aId) {
	// TODO use other own classloader impl with security features
	URL[] url = new URL[1];
	try {
	    url[0] = files.get(aId).toURI().toURL();
	} catch (MalformedURLException e) {
	    // TODO will this ever happen?
	    e.printStackTrace();
	}
	URLClassLoader ucl = new URLClassLoader(url);
	return ucl;
    }

    @Override
    public Set<AddonBundleDescription> searchByResourceName(String resourceName) {
	Set<AddonBundleDescription> matchingAddons = new HashSet<AddonBundleDescription>();
	for (AddonBundleDescription desc : registeredAddons) {
	    AppPluginActionDescription actionDesc = desc.searchByResourceName(resourceName);
	    if (actionDesc != null) {
		matchingAddons.add(desc);
	    }
	}
	return matchingAddons;
    }

}
