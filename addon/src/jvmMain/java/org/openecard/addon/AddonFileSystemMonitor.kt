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
 */
package org.openecard.addon

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addon.manifest.AddonSpecification
import org.openecard.common.util.FileUtils.addonsDir
import org.openecard.ws.marshal.WSMarshallerException
import java.io.IOException
import java.nio.file.ClosedWatchServiceException
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchService

/**
 * Filesystem monitor for the addon directory.
 * This implementation is based on the Java 7 [WatchService] based implementation.
 *
 * @author Tobias Wich
 */

private val logger = KotlinLogging.logger { }

class AddonFileSystemMonitor(
	private val fileRegistry: FileRegistry,
	private val manager: AddonManager,
) {
	private val addonDir: Path = addonsDir.toPath()

	private var ws: WatchService? = null
	private var t: Thread? = null

	/**
	 * Starts watching the addon directory.
	 * This function starts a thread which evaluates the events.
	 *
	 * @throws IOException Thrown when the directory does not provide the functionality to register the monitor.
	 * @throws SecurityExcep tion Thrown when there are missing privileges to start the monitor.
	 */
	fun start() {
		if (t != null) {
			val msg = "Trying to start already running file watcher."
			logger.error { msg }
			throw IllegalStateException(msg)
		} else {
			val fs = FileSystems.getDefault()
			ws = fs.newWatchService()
			addonDir.register(
				ws!!,
				StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.ENTRY_MODIFY,
			)

			// start thread taking care of the updates
			t = Thread(this.Runner(), "Addon-File-Watcher")
			t!!.setDaemon(true)
			t!!.start()
		}
	}

	/**
	 * Stops the filesystem monitor.
	 * This method waits for at most one second and then tries to kill the thread by force.
	 */
	fun stop() {
		if (t == null) {
			val msg = "Trying to stop idling file watcher."
			logger.error { msg }
			throw IllegalStateException(msg)
		} else {
			// try to terminate thread with stop flag, if that fails kill it explicitly
			try {
				ws!!.close()
				t!!.join(1000)
				// check if thread is still alive
				if (t!!.isAlive) {
					t!!.interrupt()
				}
			} catch (ex: IOException) {
				logger.error { "Failed to close file watcher, trying to close by force." }
				t!!.interrupt()
			} catch (ex: InterruptedException) {
				logger.error { "File watcher failed to terminate in time, killing it forcedly." }
				t!!.interrupt()
			}
		}
	}

	private inner class Runner : Runnable {
		override fun run() {
			try {
				while (true) {
					val wk = ws!!.take()
					for (evt in wk.pollEvents()) {
						val ctx: Any? = evt.context()
						if (ctx is Path) {
							var p = ctx
							p = addonDir.resolve(p)

							// TODO: add code to find out if the files are currently being written to and only perform
							// an action when this is not the case
							val evtName = evt.kind().name()
							logger.debug { "${"Hit file watcher event {}."} $evtName" }
							when (evtName) {
								StandardWatchEventKinds.ENTRY_CREATE.name() -> {
									addAddon(p)
								}
								StandardWatchEventKinds.ENTRY_DELETE.name() -> {
									removeAddon(p)
								}
								StandardWatchEventKinds.ENTRY_MODIFY.name() -> {
									replaceAddon(p)
								}
							}
						}
					}

					// reset key and try again
					wk.reset()
				}
			} catch (ex: WSMarshallerException) {
				logger.error { "Failed to deserialize Addon manifest, Terminating file monitor." }
			} catch (ex: ClosedWatchServiceException) {
				logger.info { "Watch service closed while waiting for changes." }
			} catch (ex: InterruptedException) {
				logger.info { "Watch service closed while waiting for changes." }
			}
		}
	}

	private fun addAddon(file: Path) {
		logger.info { "${"Trying to register addon {}."} ${file.fileName}" }
		val fName = file.toFile().getName()
		try {
			val spec = extractSpec(file)
			if (spec != null) {
				fileRegistry.register(spec, file.toFile())
				manager.loadLoadOnStartupActions(spec)
				logger.info { "${"Successfully registered {} as addon."} $fName" }
			} else {
				logger.error { "${"The jar file {} does not seem to be an add-on."} $fName" }
			}
		} catch (ex: MultipleAddonRegistration) {
			logger.error { "${"The jar file {} is an already registered add-on."} $fName" }
		}
	}

	private fun removeAddon(file: Path) {
		logger.info { "${"Trying to remove addon {}."} ${file.fileName}" }
		// check if we are dealing with a registered jar file
		val spec = getCurrentSpec(file)
		if (spec != null) {
			// call the destroy method of all actions and protocols
			manager.unloadAddon(spec)
			// remove configuration file
			val addonProps = AddonProperties(spec)
			addonProps.removeConfiguration()
			// remove from file registry
			fileRegistry.unregister(file.toFile())
			logger.info { "${"Succesfully removed add-on {}."} ${file.toFile().getName()}" }
		}
	}

	private fun replaceAddon(file: Path) {
		try {
			// try to look up spec, only perform replace if there is not already a registered addon with the same id
			val spec = extractSpec(file)
			if (spec != null) {
				removeAddon(file)
				addAddon(file)
			}
		} catch (ex: MultipleAddonRegistration) {
			// addon is already registered properly, so do nothing
			val fName = file.toFile().getName()
			logger.error { "${"The jar file {} is an already registered add-on."} $fName" }
		}
	}

	private fun extractSpec(file: Path): AddonSpecification? {
		if (isJarFile(file, true)) {
			// now check if there is a manifest
			val mfEx = ManifestExtractor()
			val spec = mfEx.getAddonSpecificationFromFile(file.toFile())
			// return the manifest if a valid addon file was found
			if (spec != null) {
				val plugins = fileRegistry.listAddons()
				// check that there is not already a registered instance
				// TODO: this is in general a problem, because it may replace the addon on next start
				for (desc in plugins) {
					if (desc.getId() == spec.getId()) {
						val msg = String.format("The addon with id %s is already registered.", desc.getId())
						logger.debug {
							"${"Addon '{}' is already registered by another bundle."} ${
								file.toFile().getName()
							}"
						}
						throw MultipleAddonRegistration(msg, spec)
					}
				}
				return spec
			}
		}

		return null
	}

	private fun getCurrentSpec(file: Path): AddonSpecification? {
		if (isJarFile(file, false)) {
			val spec = fileRegistry.getAddonSpecByFileName(file.toFile().getName())
			return spec
		}

		return null
	}

	private fun isJarFile(
		path: Path,
		testType: Boolean,
	): Boolean {
		val file = path.toFile()
		var result = JARFileFilter().accept(file)
		if (testType) {
			result = result && file.isFile()
		}
		return result
	}
}
