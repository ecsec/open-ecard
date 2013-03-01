/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.plugins;

import java.io.FilePermission;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.PropertyPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Extends {@code Policy} to override the implies-Method with our own implementation. Code that comes from the path
 * specified in the constructor (the directory containing all plugins) will be checked against the permissions specified
 * in here. Code coming from other locations will be granted all rights.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PluginPolicy extends Policy {

    private static final Logger logger = LoggerFactory.getLogger(PluginPolicy.class);
    private final PermissionCollection allowedPermissions;
    private final String pluginPath;

    /**
     * Creates a new PluginPolicy that restricts permissions for plugins with the given path as source location.
     * 
     * @param path The path to the plugin directory
     */
    public PluginPolicy(String path) {
	// replace for windows
	pluginPath = path.replace("\\", "/");
	allowedPermissions = new Permissions();

	// allow plugins to read everything 
	allowedPermissions.add(new FilePermission("<<ALL FILES>>", "read"));
	// needed for logger when testing with test-ng
	allowedPermissions.add(new PropertyPermission("line.separator", "read"));
    }

    @Override
    public boolean implies(ProtectionDomain domain, Permission permission) {
	CodeSource source = domain.getCodeSource();
	if (source.getLocation().toString().contains(pluginPath)) {
	    logger.debug("Plugin {} is requesting permission {}", source.getLocation(), permission);
	    boolean granted = allowedPermissions.implies(permission);
	    logger.debug("Access granted: {}", granted);
	    return granted;
	} else {
	    // alternatively we could use super.implies() here to support java policy files
	    return true;
	}
    }

}
