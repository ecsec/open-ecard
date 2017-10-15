/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.android.lib.utils;

import org.openecard.addon.AddonException;
import org.openecard.addon.FileRegistry;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.ws.marshal.WSMarshallerException;
import java.util.Set;


/**
 * Contains only the classpath registry for addons.
 *
 * @author Mike Prechtl
 */
public class ClasspathRegistry implements org.openecard.addon.CombiningRegistry {

    private final org.openecard.addon.ClasspathRegistry classpathRegistry;

    public ClasspathRegistry() throws WSMarshallerException {
        classpathRegistry = new org.openecard.addon.ClasspathRegistry();
    }

    @Override
    public org.openecard.addon.ClasspathRegistry getClasspathRegistry() {
        return classpathRegistry;
    }

    public FileRegistry getFileRegistry() {
        return null;
    }

    @Override
    public Set<AddonSpecification> listAddons() {
        return classpathRegistry.listAddons();
    }

    @Override
    public AddonSpecification search(String id) {
        return classpathRegistry.search(id);
    }

    @Override
    public Set<AddonSpecification> searchByName(String name) {
        return classpathRegistry.searchByName(name);
    }

    @Override
    public Set<AddonSpecification> searchIFDProtocol(String uri) {
        return classpathRegistry.searchIFDProtocol(uri);
    }

    @Override
    public Set<AddonSpecification> searchSALProtocol(String uri) {
        return classpathRegistry.searchSALProtocol(uri);
    }

    @Override
    public ClassLoader downloadAddon(AddonSpecification addonSpec) throws AddonException {
        AddonSpecification desc = classpathRegistry.search(addonSpec.getId());
        if (desc != null) {
            return classpathRegistry.downloadAddon(addonSpec);
        } else {
            return null;
        }
    }

    @Override
    public Set<AddonSpecification> searchByResourceName(String resourceName) {
        return classpathRegistry.searchByResourceName(resourceName);
    }

    @Override
    public Set<AddonSpecification> searchByActionId(String actionId) {
        return classpathRegistry.searchByActionId(actionId);
    }

    @Override
    public Set<AddonSpecification> listInstalledAddons() {
        return classpathRegistry.listInstalledAddons();
    }

}
