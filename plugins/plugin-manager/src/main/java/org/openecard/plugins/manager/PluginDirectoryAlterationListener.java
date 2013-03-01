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

package org.openecard.plugins.manager;

import java.io.File;
import org.apache.commons.jci.monitor.FilesystemAlterationListener;
import org.apache.commons.jci.monitor.FilesystemAlterationObserver;
import org.openecard.plugins.PluginInterface;


/**
 * Simple listener for changes in the plugin directory.
 * <br/> It will add or unload a plugin in the plugin manager if it detects a file creation or removal.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
final class PluginDirectoryAlterationListener implements FilesystemAlterationListener {

    private final PluginManager pluginManager;

    PluginDirectoryAlterationListener(PluginManager pluginManager) {
	this.pluginManager = pluginManager;
    }

    @Override
    public void onFileDelete(File file) {
	PluginInterface instance = pluginManager.getJarPaths().get(file.getPath());
	if (instance != null) {
	    pluginManager.unloadPlugin(instance);
	}
    }

    @Override
    public void onFileCreate(File file) {
	// add if it is not already added
	if (pluginManager.getJarPaths().get(file.getPath()) == null) {
	    pluginManager.addPlugin(file);
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
