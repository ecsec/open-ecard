/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.*;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Set;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.common.util.FileUtils;
import org.openecard.ws.marshal.WSMarshallerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Filesystem monitor for the addon directory.
 * This implementation is based on the Java 7 {@link WatchService} based implementation.
 *
 * @author Tobias Wich
 */
public class AddonFileSystemMonitor {

    private static final Logger logger = LoggerFactory.getLogger(AddonFileSystemMonitor.class.getName());

    private final FileRegistry fileRegistry;
    private final AddonManager manager;

    private final Path addonDir;

    private WatchService ws;
    private Thread t;

    public AddonFileSystemMonitor(FileRegistry fileRegistry, AddonManager manager) throws IOException,
	    SecurityException {
	this.fileRegistry = fileRegistry;
	this.manager = manager;

	addonDir = FileUtils.getAddonsDir().toPath();
    }

    /**
     * Starts watching the addon directory.
     * This function starts a thread which evaluates the events.
     *
     * @throws IOException Thrown when the directory does not provide the functionality to register the monitor.
     * @throws SecurityException Thrown when there are missing privileges to start the monitor.
     */
    public void start() throws IOException, SecurityException {
	if (t != null) {
	    String msg = "Trying to start already running file watcher.";
	    logger.error(msg);
	    throw new IllegalStateException(msg);
	} else {
	    FileSystem fs = FileSystems.getDefault();
	    ws = fs.newWatchService();
	    addonDir.register(ws, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

	    // start thread taking care of the updates
	    t = new Thread(new Runner(), "Addon-File-Watcher");
	    t.setDaemon(true);
	    t.start();
	}
    }

    /**
     * Stops the filesystem monitor.
     * This method waits for at most one second and then tries to kill the thread by force.
     */
    public void stop() {
	if (t == null) {
	    String msg = "Trying to stop idling file watcher.";
	    logger.error(msg);
	    throw new IllegalStateException(msg);
	} else {
	    // try to terminate thread with stop flag, if that fails kill it explicitly
	    try {
		ws.close();
		t.join(1000);
		// check if thread is still alive
		if (t.isAlive()) {
		    t.interrupt();
		}
	    } catch (IOException ex) {
		logger.error("Failed to close file watcher, trying to close by force.");
		t.interrupt();
	    } catch (InterruptedException ex) {
		logger.error("File watcher failed to terminate in time, killing it forcedly.");
		t.interrupt();
	    }
	}
    }

    private class Runner implements Runnable {

	@Override
	public void run() {
	    try {
		while (true) {
		    WatchKey wk = ws.take();
		    for (WatchEvent<?> evt : wk.pollEvents()) {
			Object ctx = evt.context();
			if (ctx instanceof Path) {
			    Path p = (Path) ctx;
			    p = addonDir.resolve(p);

			    // TODO: add code to find out if the files are currently being written to and only perform
			    // an action when this is not the case
			    String evtName = evt.kind().name();
			    logger.debug("Hit file watcher event {}.", evtName);
			    if (ENTRY_CREATE.name().equals(evtName)) {
				addAddon(p);
			    } else if (ENTRY_DELETE.name().equals(evtName)) {
				removeAddon(p);
			    } else if (ENTRY_MODIFY.name().equals(evtName)) {
				replaceAddon(p);
			    }
			}
		    }

		    // reset key and try again
		    wk.reset();
		}
	    } catch (WSMarshallerException ex) {
		logger.error("Failed to deserialize Addon manifest, Terminating file monitor.");
	    } catch (ClosedWatchServiceException | InterruptedException ex) {
		logger.info("Watch service closed while waiting for changes.");
	    }
	}

    }

    private void addAddon(Path file) throws WSMarshallerException {
	logger.info("Trying to register addon {}.", file.getFileName());
	String fName = file.toFile().getName();
	try {
	    AddonSpecification spec = extractSpec(file);
	    if (spec != null) {
		fileRegistry.register(spec, file.toFile());
		manager.loadLoadOnStartupActions(spec);
		logger.info("Successfully registered {} as addon.", fName);
	    } else {
		logger.error("The jar file {} does not seem to be an add-on.", fName);
	    }
	} catch (MultipleAddonRegistration ex) {
	    logger.error("The jar file {} is an already registered add-on.", fName);
	}
    }

    private void removeAddon(Path file) {
	logger.info("Trying to remove addon {}.", file.getFileName());
	// check if we are dealing with a registered jar file
	AddonSpecification spec = getCurrentSpec(file);
	if (spec != null) {
	    // call the destroy method of all actions and protocols
	    manager.unloadAddon(spec);
	    // remove configuration file
	    AddonProperties addonProps = new AddonProperties(spec);
	    addonProps.removeConfiguration();
	    // remove from file registry
	    fileRegistry.unregister(file.toFile());
	    logger.info("Succesfully removed add-on {}.", file.toFile().getName());
	}
    }

    private void replaceAddon(Path file) throws WSMarshallerException {
	try {
	    // try to look up spec, only perform replace if there is not already a registered addon with the same id
	    AddonSpecification spec = extractSpec(file);
	    if (spec != null) {
		removeAddon(file);
		addAddon(file);
	    }
	} catch (MultipleAddonRegistration ex) {
	    // addon is already registered properly, so do nothing
	    String fName = file.toFile().getName();
	    logger.error("The jar file {} is an already registered add-on.", fName);
	}
    }

    private AddonSpecification extractSpec(Path file) throws WSMarshallerException, MultipleAddonRegistration {
	if (isJarFile(file, true)) {
	    // now check if there is a manifest
	    ManifestExtractor mfEx = new ManifestExtractor();
	    AddonSpecification spec = mfEx.getAddonSpecificationFromFile(file.toFile());
	    // return the manifest if a valid addon file was found
	    if (spec != null) {
		Set<AddonSpecification> plugins = fileRegistry.listAddons();
		// check that there is not already a registered instance
		// TODO: this is in general a problem, because it may replace the addon on next start
		for (AddonSpecification desc : plugins) {
		    if (desc.getId().equals(spec.getId())) {
			String msg = String.format("The addon with id %s is already registered.", desc.getId());
			logger.debug("Addon '{}' is already registered by another bundle.", file.toFile().getName());
			throw new MultipleAddonRegistration(msg, spec);
		    }
		}
		return spec;
	    }
	}

	return null;
    }

    private AddonSpecification getCurrentSpec(Path file) {
	if (isJarFile(file, false)) {
	    AddonSpecification spec = fileRegistry.getAddonSpecByFileName(file.toFile().getName());
	    return spec;
	}

	return null;
    }

    private boolean isJarFile(Path path, boolean testType) {
	File file = path.toFile();
	boolean result = new JARFileFilter().accept(file);
	if (testType) {
	    result = result && file.isFile();
	}
	return result;
    }

}
