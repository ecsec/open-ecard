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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.apache.commons.jci.monitor.FilesystemAlterationMonitor;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.gui.UserConsent;
import org.openecard.plugins.PluginInterface;
import org.openecard.plugins.wrapper.PluginDispatcher;
import org.openecard.plugins.wrapper.PluginUserConsent;
import org.openecard.recognition.CardRecognition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The PluginManager takes care of loading plugins and holds information about currently loaded plugins.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PluginManager {

    private static final Logger logger = LoggerFactory.getLogger(PluginManager.class.getName());

    private static final String FALSE = Boolean.toString(false);
    private static final String TRUE = Boolean.toString(true);
    private static final String PLUGIN_ATTRIBUTE_NAME = "plugin-main";

    // Contains the loaded plugins and maps them to their activation status
    private static Map<PluginInterface, Boolean> loadedPlugins = new HashMap<PluginInterface, Boolean>();
    // maps a jar path to the corresponding plugin
    private static Map<String, PluginInterface> jarPaths = new HashMap<String, PluginInterface>(); 

    private final PluginDispatcher pluginDispatcher;
    private final PluginUserConsent gui;
    private final CardRecognition recognition;
    private final CardStateMap cardStates;
    private final String pluginsPath;

    private static PluginManager instance;

    private boolean jarLoadingSupported;

    /**
     * Create a new instance of PluginManager.
     * 
     * @param dispatcher PluginDispatcher wrapper the dispatcher to use
     * @param gui PluginUserConsent wrapping the UserConsent to use
     * @param recognition CardRecognition to use
     * @param states CardStateMap of the client
     * @param pluginsPath The path to the clients plugin directory, may be null or empty to disable support for
     * loading jars.
     */
    public PluginManager(Dispatcher dispatcher, UserConsent gui, CardRecognition recognition,
	    CardStateMap states, String pluginsPath) {
	instance = this;
	this.pluginDispatcher = new PluginDispatcher(dispatcher);
	this.gui = new PluginUserConsent(gui);
	this.recognition = recognition;
	this.cardStates = states;
	this.pluginsPath = pluginsPath;

	try {
	    PluginProperties.loadProperties();
	} catch (IOException ex) {
	    logger.error("Properties coudn't be loaded.", ex);
	}
	jarLoadingSupported = (pluginsPath != null) && ! pluginsPath.trim().isEmpty();

	if (jarLoadingSupported) {
	    createPluginsDirectoryIfNotExistent(pluginsPath);
	}
	if (jarLoadingSupported) {
	    startFileMonitor(pluginsPath);
	}
    }

    public Map<String, PluginInterface> getJarPaths() {
	return Collections.unmodifiableMap(jarPaths);
    }

    private void startFileMonitor(String pluginsPath2) {
	File f = new File(pluginsPath);
	logger.debug("Starting FilesystemAlterationMonitor on Path: {}", f.getPath());
	FilesystemAlterationMonitor fam = new FilesystemAlterationMonitor();
	fam.addListener(f, new PluginDirectoryAlterationListener(this));
	fam.start();
    }

    public static PluginManager getInstance() {
	return instance;
    }

    /**
     * Deactivates the given plugin by calling it's stop method.
     * 
     * @param instance The plugin that should be deactivated
     * @return {@code true} if the plugin was deactivated; {@code false} otherwise
     */
    public boolean deactivatePlugin(PluginInterface instance) {
	if (! loadedPlugins.containsKey(instance)) {
	    logger.debug("Plugin {} coudn't be deactivated because it is not loaded.", instance.getName());
	    return false;
	}
	if (! loadedPlugins.get(instance)) {
	    logger.debug("Plugin {} is already deactivated.", instance.getName());
	    return true;
	}
	instance.stop();
	loadedPlugins.put(instance, false);
	PluginProperties.setProperty(instance.getClass().getCanonicalName(), FALSE);
	return true;
    }

    /**
     * Activates the given plugin by calling it's initialize method.
     * 
     * @param instance The plugin that should be activated
     * @return {@code true} if the plugin was activated; {@code false} otherwise
     */
    public boolean activatePlugin(PluginInterface instance) {
	if (! loadedPlugins.containsKey(instance)) {
	    logger.debug("Plugin {} coudn't be activated because it is not loaded.", instance.getName());
	    return false;
	}
	if (loadedPlugins.get(instance)) {
	    logger.debug("Plugin {} is already activated.", instance.getName());
	    return true;
	}
	instance.initialize(pluginDispatcher, gui, recognition, cardStates);
	loadedPlugins.put(instance, true);
	PluginProperties.setProperty(instance.getClass().getCanonicalName(), TRUE);
	return true;
    }

    private void createPluginsDirectoryIfNotExistent(String pluginsPath) {
	File f = new File(pluginsPath);
	if (! f.exists()) {
	    boolean directoryCreated = f.mkdir();
	    if (! directoryCreated) {
		logger.error("Directory {} coudn't be created. Disabling jar loading support.", f.getPath());
		jarLoadingSupported = false;
	    }
	}
    }

    public static Map<PluginInterface, Boolean> getLoadedPlugins() {
	return Collections.unmodifiableMap(loadedPlugins);
    }

    /**
     * Add the plugin specified through the given JarFile.
     * 
     * @param file JarFile to add as plugin.
     * @return True if the plugin was added, else false.
     */
    public boolean addPlugin(File file) {
	if (! jarLoadingSupported) {
	    return false;
	}
	JarFile jarFile;
	URL[] url = new URL[1];
	String classToLoad;
	File destination;
	try {
	    jarFile = new JarFile(file);
	} catch (IOException e) {
	    logger.error("Provided File is not a JarFile.", e);
	    return false;
	}
	try {
	    classToLoad = getPluginEntryClass(jarFile);

	    if (classToLoad == null) {
		logger.error("Manifest did not contain an entry for plugin main class: {}.", classToLoad);
		return false;
	    } else {
		destination = new File(pluginsPath + file.getName());
		// only copy file to destination path if it's not already there
		if (!file.getPath().equals(destination.getPath())) {
		    boolean copySuccess = copyFile(file, destination);
		    if (!copySuccess) {
			return false;
		    }
		}

		url[0] = destination.toURI().toURL();
	    }
	} catch (IOException ex) {
	    logger.error("Failed to read manifest entry for plugin main class.", ex);
	    return false;
	} finally {
	    try {
		jarFile.close();
	    } catch (IOException ex) {
		logger.error("Failed to close jar file.", ex);
	    }
	}

	URLClassLoader ucl = new URLClassLoader(url);
	try {

	    Class<?> clazz = ucl.loadClass(classToLoad);
	    Constructor<?>[] constructors = clazz.getConstructors();
	    if (constructors.length != 1) {
		logger.error("There's only one constructor allowed in Plugin main class.");
		return false;
	    }
	    Constructor<?> constructor = clazz.getConstructors()[0];
	    PluginInterface instance = (PluginInterface) constructor.newInstance(new Object[0]);

	    if (TRUE.equals(PluginProperties.getProperty(instance.getClass().getCanonicalName()))) {
		instance.initialize(pluginDispatcher, gui, recognition, cardStates);
		loadedPlugins.put(instance, true);
	    } else {
		loadedPlugins.put(instance, false);
	    }
	    jarPaths.put(destination.getPath(), instance);
	} catch (ClassNotFoundException ex) {
	    logger.error("Failed to load plugin main class: {}.", classToLoad, ex);
	    return false;
	} catch (InstantiationException ex) {
	    logger.error("Failed to call constructor of plugin main class: {}.", classToLoad, ex);
	    return false;
	} catch (IllegalAccessException ex) {
	    logger.error("Failed to call constructor of plugin main class: {}.", classToLoad, ex);
	    return false;
	} catch (IllegalArgumentException ex) {
	    logger.error("Failed to call constructor of plugin main class: {}.", classToLoad, ex);
	    return false;
	} catch (InvocationTargetException ex) {
	    logger.error("Failed to call constructor of plugin main class: {}.", classToLoad, ex);
	    return false;
	}
	return true;
    }

    /**
     * Copies a file to the specified destination. Existing files will be overriden.
     * @param file File to copy
     * @param destination Destination to copy the file to
     * @return {@code true} if the file was successfully copied; {@code false} otherwise
     */
    private boolean copyFile(File file, File destination) {
	try {
	    InputStream in = new FileInputStream(file);
	    OutputStream out = new FileOutputStream(destination);

	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
		out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	    logger.debug("Copied File {} to {}.", file.getPath(), destination.getPath());
	    return true;
	} catch (FileNotFoundException e) {
	    logger.error("Opening of Input- or Outputstream failed.", e);
	    return false;
	} catch (IOException e) {
	    logger.error("Reading/Writing of Input-/Outputstream failed.", e);
	    return false;
	}
    }

    /**
     * Add the plugin specified through the given URI.
     * 
     * @param url URL from where to get the plugin
     * @return True if the plugin was added, else false.
     */
    public boolean addPlugin(URL url) {
	if (! jarLoadingSupported) {
	    return false;
	}
	try {
	    InputStream input = url.openStream();
	    String fileName;
	    try {
		fileName = getFileNameFromURL(url.toString(), ".jar");
	    } catch (IndexOutOfBoundsException ex) {
		logger.error("Couldn't get filename from URL: {}", url, ex);
		return false;
	    }
	    return addPlugin(input, fileName);
	} catch (IOException e) {
	    logger.error("Couldn't open Inputstream for {}", url, e);
	    return false;
	}
    }

    private String getFileNameFromURL(String string, String fileEnding) {
	int slashIndex = string.lastIndexOf('/');
	int endingIndex = string.lastIndexOf(fileEnding);
	return string.substring(slashIndex + 1, endingIndex + fileEnding.length());
    }

    /**
     * Add the plugin specified through the given InputStream.
     * 
     * @param in InputStream to load the plugin from
     * @param fileName Name of the file the InputStream will be saved to
     * @return True if the plugin was added, else false.
     */
    public boolean addPlugin(InputStream in, String fileName) {
	if (! jarLoadingSupported) {
	    return false;
	}
	File destination = new File(pluginsPath + fileName);
	try {
	    // Overrides an existing file
	    OutputStream out = new FileOutputStream(destination);

	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
		out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	} catch (FileNotFoundException e) {
	    logger.error("Opening of Input- or Outputstream failed.", e);
	    return false;
	} catch (IOException e) {
	    logger.error("Reading/Writing of Input-/Outputstream failed.", e);
	    return false;
	}

	return true;
    }

    /**
     * Add the specified object instance as plugin.
     * 
     * @param instance Instance of the plugin to add.
     * @return True if the plugin was added, else false.
     */
    public boolean addPlugin(PluginInterface instance) {
	if (TRUE.equals(PluginProperties.getProperty(instance.getClass().getCanonicalName()))) {
	    instance.initialize(pluginDispatcher, gui, recognition, cardStates);
	    loadedPlugins.put(instance, true);
	} else {
	    loadedPlugins.put(instance, false);
	}
	return true;
    }

    /**
     * Remove the specified object instance as plugin.
     * 
     * @param instance Instance of the plugin to unload
     * @return {@code true} if the plugin was unloaded; {@code false} otherwise
     */
    public boolean unloadPlugin(PluginInterface instance) {
	if (loadedPlugins.get(instance)) {
	    instance.stop();
	}
	loadedPlugins.remove(instance);
	return true;
    }

    /**
     * Removes the given plugin from the plugin directory.
     * <br/> The plugin must be unloaded before it can be removed.
     * 
     * @param jar File pointing to the plugin jar
     * @return {@code true} if the plugin was deleted; {@code false} otherwise
     */
    public boolean removePluginJAR(File jar) {
	if (! jar.exists()) {
	    logger.debug("No plugin with the given name in the plugin directory.");
	    return false;
	}
	return jar.delete();
    } 

    /**
     * Add all jar files in the specified path as plugin if they have a plugin entry in their manifest.
     * 
     * @param path Path of the plugins to add
     */
    public void addPlugins(String path) {
	if (! jarLoadingSupported) {
	    return;
	}
	File directory = new File(path);

	if (!directory.exists() || !directory.isDirectory()) {
	    logger.error("{} is not a directory or does not exist.", directory);
	    return;
	}

	File[] jarFiles = directory.listFiles(new JARFileFilter());

	for (File f : jarFiles) {
	    addPlugin(f);
	}
    }

    /**
     * Read the name of the plugin main class from the jar files manifest and return it.
     * 
     * @param jarFile the jar file for which to return the plugin entry class
     * @return Returns the name of the plugin entry class or null if not found
     * @throws IOException if the jar files manifest could not be read
     */
    private String getPluginEntryClass(JarFile jarFile) throws IOException {
	Manifest manifest = jarFile.getManifest();
	Attributes attributes = manifest.getMainAttributes();
	return attributes.getValue(PLUGIN_ATTRIBUTE_NAME);
    }

    /**
     * Saves the current activation state of the plugins and afterwards deactivates them.
     */
    public void shutDown() {
	for (PluginInterface instance : loadedPlugins.keySet()) {
	    PluginProperties.setProperty(instance.getClass().getCanonicalName(), Boolean.toString(loadedPlugins.get(instance)));
	}
	try {
	    PluginProperties.saveProperties();
	} catch (IOException e) {
	    logger.error("Properties coudn't be saved.", e);
	}
	for (PluginInterface instance : loadedPlugins.keySet()) {
	    deactivatePlugin(instance);
	}
    }

}
