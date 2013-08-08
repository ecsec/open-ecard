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
import org.openecard.addon.manifest.AddonSpecification;


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

    public void register(AddonSpecification desc) {
	throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Set<AddonSpecification> listPlugins() {
	Set<AddonSpecification> list = classpathRegistry.listPlugins();
	list.addAll(fileRegistry.listPlugins());
	return list;
    }

    @Override
    public AddonSpecification search(String id) {
	AddonSpecification desc = classpathRegistry.search(id);
	if (desc == null) {
	    desc = fileRegistry.search(id);
	}
	return desc;
    }

    @Override
    public Set<AddonSpecification> searchByName(String name) {
	Set<AddonSpecification> searchByName = classpathRegistry.searchByName(name);
	if (searchByName.isEmpty()) {
	    searchByName = fileRegistry.searchByName(name);
	}
	return searchByName;
    }

    @Override
    public Set<AddonSpecification> searchIFDProtocol(String uri) {
	Set<AddonSpecification> matchingAddons = classpathRegistry.searchIFDProtocol(uri);
	if (matchingAddons.isEmpty()) {
	    matchingAddons = fileRegistry.searchIFDProtocol(uri);
	}
	return matchingAddons;
    }

    @Override
    public Set<AddonSpecification> searchSALProtocol(String uri) {
	Set<AddonSpecification> matchingAddons = classpathRegistry.searchSALProtocol(uri);
	if (matchingAddons.isEmpty()) {
	    matchingAddons = fileRegistry.searchSALProtocol(uri);
	}
	return matchingAddons;
    }

    @Override
    public ClassLoader downloadPlugin(AddonSpecification addonSpec) {
	AddonSpecification desc = classpathRegistry.search(addonSpec.getId());
	if (desc != null) {
	    return classpathRegistry.downloadPlugin(addonSpec);
	} else {
	    return fileRegistry.downloadPlugin(addonSpec);
	}
    }

    @Override
    public Set<AddonSpecification> searchByResourceName(String resourceName) {
	Set<AddonSpecification> matchingAddons = classpathRegistry.searchByResourceName(resourceName);
	if (matchingAddons.isEmpty()) {
	    matchingAddons = fileRegistry.searchByResourceName(resourceName);
	}
	return matchingAddons;
    }

    @Override
    public Set<AddonSpecification> searchByActionId(String actionId) {
	Set<AddonSpecification> matchingAddons = classpathRegistry.searchByActionId(actionId);
	if (matchingAddons.isEmpty()) {
	    matchingAddons = fileRegistry.searchByResourceName(actionId);
	}
	return matchingAddons;
    }

}
