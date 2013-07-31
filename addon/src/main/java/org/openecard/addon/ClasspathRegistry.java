/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.openecard.addon.manifest.AddonBundleDescription;
import org.openecard.addon.manifest.AppPluginActionDescription;
import org.openecard.addon.manifest.LocalizedString;
import org.openecard.addon.manifest.ProtocolPluginDescription;


/**
 * Addon registry serving add-ons from the classpath of the base app.
 * This type of registry works for JNLP and integrated plugins.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ClasspathRegistry implements AddonRegistry {

    private static final ArrayList<AddonBundleDescription> registeredAddons = new ArrayList<AddonBundleDescription>();
    private static ClasspathRegistry instance;

    private ClasspathRegistry() {
    }

    public static ClasspathRegistry getInstance() {
	if (instance == null) {
	    instance = new ClasspathRegistry();
	}
	return instance;
    }

    public void register(AddonBundleDescription desc) {
	registeredAddons.add(desc);
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
	Set<AddonBundleDescription> matchingAddons = new HashSet<AddonBundleDescription>();
	for (AddonBundleDescription desc : registeredAddons) {
	    for (LocalizedString s : desc.getLocalizedName()) {
		if (s.getValue().equals(name)) {
		    matchingAddons.add(desc);
		}
	    }
	}
	return matchingAddons;
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
	return this.getClass().getClassLoader();
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
