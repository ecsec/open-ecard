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

import java.util.Set;
import org.openecard.addon.manifest.AddonBundleDescription;


/**
 * This AddonRegistry is a combination of {@link ClasspathRegistry} and {@link FileRegistry}.
 * All calls to this registry are forwarded to the appropriate registry for a given addon.
 * In case of conflicts (e.g. same addon twice), the {@link ClasspathRegistry} has priority.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CombiningRegistry implements AddonRegistry {

    private static CombiningRegistry instance;
    private static ClasspathRegistry classpathRegistry;
    private static FileRegistry fileRegistry;

    public static CombiningRegistry getInstance() {
	if (instance == null) {
	    instance = new CombiningRegistry();
	}
	return instance;
    }

    private CombiningRegistry() {
	classpathRegistry = ClasspathRegistry.getInstance();
	fileRegistry = FileRegistry.getInstance();
    }

    public void register(AddonBundleDescription desc) {
	throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Set<AddonBundleDescription> listPlugins() {
	Set<AddonBundleDescription> list = classpathRegistry.listPlugins();
	list.addAll(fileRegistry.listPlugins());
	return list;
    }

    @Override
    public AddonBundleDescription search(String id) {
	AddonBundleDescription desc = classpathRegistry.search(id);
	if (desc == null) {
	    desc = fileRegistry.search(id);
	}
	return desc;
    }

    @Override
    public Set<AddonBundleDescription> searchByName(String name) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<AddonBundleDescription> searchProtocol(String uri) {
	Set<AddonBundleDescription> matchingAddons = classpathRegistry.searchProtocol(uri);
	if (matchingAddons.isEmpty()) {
	    matchingAddons = fileRegistry.searchProtocol(uri);
	}
	return matchingAddons;
    }

    @Override
    public ClassLoader downloadPlugin(String aId) {
	AddonBundleDescription desc = classpathRegistry.search(aId);
	if (desc != null) {
	    return classpathRegistry.downloadPlugin(aId);
	} else {
	    return fileRegistry.downloadPlugin(aId);
	}

    }

    @Override
    public Set<AddonBundleDescription> searchByResourceName(String resourceName) {
	Set<AddonBundleDescription> matchingAddons = classpathRegistry.searchByResourceName(resourceName);
	if (matchingAddons.isEmpty()) {
	    matchingAddons = fileRegistry.searchByResourceName(resourceName);
	}
	return matchingAddons;
    }

}
