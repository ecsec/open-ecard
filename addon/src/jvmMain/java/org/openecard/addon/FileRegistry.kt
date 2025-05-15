/****************************************************************************
 * Copyright (C) 2013-2015 HS Coburg.
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
import org.openecard.addon.manifest.AppExtensionSpecification
import org.openecard.addon.manifest.AppPluginSpecification
import org.openecard.addon.manifest.ProtocolPluginSpecification
import org.openecard.common.util.FileUtils.addonsDir
import org.openecard.ws.marshal.WSMarshallerException
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLClassLoader
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.FutureTask

/**
 * This registry provides access to all add-ons in the plug-ins directory.
 * Adding and removing add-on-files at runtime is supported.
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */

private val logger = KotlinLogging.logger { }

class FileRegistry(
	private val manager: AddonManager,
) : AddonRegistry {
	private val initComplete: Future<Void?>

	private var fsMonitor: AddonFileSystemMonitor? = null

	/**
	 * Creates a new FileRegistry.
	 * On the creation of the registry the add-on directory is retrieved and all existing add-ons are loaded.
	 * Furthermore a [AddonFileSystemMonitor] is started to be able to register newly added add-ons and
	 * remove add-ons.
	 *
	 * @param manager [AddonManager] which takes care for the installed add-ons.
	 */
	init {
		var initCompleteTmp: FutureTask<Void?>
		try {
			initCompleteTmp =
				FutureTask<Void?> {
					loadExistingAddons()
					startFileMonitor()
					null
				}
			Thread(initCompleteTmp, "Init-File-Addons").start()
		} catch (e: SecurityException) {
			val msg = "Failed to access add-on directory due to missing privileges. FileRegistry not working."
			logger.error(e) { msg }
			initCompleteTmp = this.completedFuture
		}

		this.initComplete = initCompleteTmp
	}

	private val completedFuture: FutureTask<Void?>
		get() {
			val f: FutureTask<Void?> =
				FutureTask<Void?> { null }
			f.run()
			return f
		}

	private fun blockUntilInit() {
		try {
			initComplete.get()
		} catch (ex: InterruptedException) {
			val msg = "Initialization of the file based Add-ons has been interrupted."
			logger.warn { msg }
			throw RuntimeException(msg)
		} catch (ex: ExecutionException) {
			val msg = "Initialization of the file based Add-ons yielded an error."
			logger.error(ex) { msg }
			throw RuntimeException(msg, ex.cause)
		}
	}

	private val addons: MutableMap<String?, AddonSpecification>
		get() {
			blockUntilInit()
			return registeredAddons
		}

	private val files: MutableMap<String?, File>
		get() {
			blockUntilInit()
			return Companion.files
		}

	/**
	 * Starts the addon filesystem monitor.
	 * The method sets up a [AddonFileSystemMonitor] instance for the addon directory.
	 * After the setup the monitor is started.
	 *
	 * @param addonPath Path to the directory which shall be monitored.
	 */
	private fun startFileMonitor() {
		try {
			val addonPath = addonsDir
			logger.debug { "${"Starting addon filesystem monitor on path: {}"} ${addonPath.path}" }
			fsMonitor = AddonFileSystemMonitor(this, manager)
			fsMonitor!!.start()
		} catch (ex: SecurityException) {
			logger.error(ex) { "SecurityException seems like you don't have permissions to access the addons directory." }
		} catch (ex: IOException) {
			logger.warn { "Failed to start file watcher on addon directory." }
		}
	}

	/**
	 * Registers a new add-on.
	 *
	 * @param desc The [AddonSpecification] of the add-on to add.
	 * @param file A [File] object which points to the add-ons jar file.
	 */
	fun register(
		desc: AddonSpecification,
		file: File,
	) {
		registeredAddons.put(file.getName(), desc)
		Companion.files.put(desc.getId(), file)
	}

	/**
	 * Unregister a specific add-on.
	 *
	 * @param file A [File] object which points to the jar file of the add-on.
	 */
	fun unregister(file: File) {
		val entrySet: MutableSet<MutableMap.MutableEntry<String?, File>> = this.files.entries
		val iterator: MutableIterator<MutableMap.MutableEntry<String?, File>> = entrySet.iterator()
		while (iterator.hasNext()) {
			val next = iterator.next()
			if (next.value == file) {
				val id = next.key
				registeredAddons.remove(file.getName())
				Companion.files.remove(id)
				logger.debug { "${"Successfully removed addon {}"} ${file.getName()}" }
				break
			}
		}
	}

	override fun listAddons(): MutableSet<AddonSpecification> {
		val list: MutableSet<AddonSpecification> = mutableSetOf()
		list.addAll(this.addons.values)
		return list
	}

	override fun search(id: String): AddonSpecification? {
		for (desc in this.addons.values) {
			if (desc.getId() == id) {
				return desc
			}
		}
		return null
	}

	override fun searchByName(name: String): MutableSet<AddonSpecification>? {
		val matchingAddons: MutableSet<AddonSpecification> = mutableSetOf()
		for (desc in this.addons.values) {
			for (s in desc.localizedName) {
				if (s.value == name) {
					matchingAddons.add(desc)
				}
			}
		}
		return matchingAddons
	}

	override fun searchIFDProtocol(uri: String): MutableSet<AddonSpecification>? {
		val matchingAddons: MutableSet<AddonSpecification> = mutableSetOf()
		for (desc in this.addons.values) {
			val protocolDesc: ProtocolPluginSpecification? = desc.searchIFDActionByURI(uri)
			if (protocolDesc != null) {
				matchingAddons.add(desc)
			}
		}
		return matchingAddons
	}

	override fun searchSALProtocol(uri: String): MutableSet<AddonSpecification>? {
		val matchingAddons: MutableSet<AddonSpecification> = mutableSetOf()
		for (desc in this.addons.values) {
			val protocolDesc: ProtocolPluginSpecification? = desc.searchSALActionByURI(uri)
			if (protocolDesc != null) {
				matchingAddons.add(desc)
			}
		}
		return matchingAddons
	}

	@Throws(AddonException::class)
	override fun downloadAddon(addonSpec: AddonSpecification): ClassLoader {
		val aId = addonSpec.getId()
		// TODO: use other own classloader impl with security features
		val cl = javaClass.getClassLoader()
		try {
			val url: Array<URL> =
				arrayOf(
					Companion.files[aId]!!
						.toURI()
						.toURL(),
				)
			val ucl = URLClassLoader(url, cl)
			return ucl
		} catch (e: MalformedURLException) {
			logger.error(e) { "${e.message}" }
			throw AddonException("Failed to convert Add-on location URI to URL.")
		}
	}

	override fun searchByResourceName(resourceName: String): MutableSet<AddonSpecification>? {
		val matchingAddons: MutableSet<AddonSpecification> = mutableSetOf()
		for (desc in this.addons.values) {
			val actionDesc: AppPluginSpecification? = desc.searchByResourceName(resourceName)
			if (actionDesc != null) {
				matchingAddons.add(desc)
			}
		}
		return matchingAddons
	}

	override fun searchByActionId(actionId: String): MutableSet<AddonSpecification>? {
		val matchingAddons: MutableSet<AddonSpecification> = mutableSetOf()
		for (desc in this.addons.values) {
			val actionDesc: AppExtensionSpecification? = desc.searchByActionId(actionId)
			if (actionDesc != null) {
				matchingAddons.add(desc)
			}
		}
		return matchingAddons
	}

	/**
	 * Register all add-ons which are already installed in the add-ons directory.
	 *
	 * @throws WSMarshallerException Thrown if the instantiation of the marshaler for the AddonSpecification marshaling
	 * failed.
	 */
	@Throws(WSMarshallerException::class)
	private fun loadExistingAddons() {
		try {
			val addonsDir = addonsDir
			var addons = addonsDir.listFiles(JARFileFilter())
			addons = addons ?: arrayOfNulls<File>(0)
			val mEx = ManifestExtractor()

			for (addon in addons) {
				val addonSpec = mEx.getAddonSpecificationFromFile(addon)
				if (addonSpec != null) {
					register(addonSpec, addon)
					logger.info { "${"Loaded external addon {}"} ${addon.getName()}" }
				}
			}
		} catch (ex: IOException) {
			logger.error(ex) { "Failed to load addons directory." }
		} catch (ex: SecurityException) {
			logger.error(ex) { "SecurityException seems like you don't have permissions to access the addons directory." }
		}
	}

	override fun listInstalledAddons(): MutableSet<AddonSpecification>? {
		// This registry does not provide a AppStore based system so just return the result of listAddons() method.
		return listAddons()
	}

	/**
	 * Get an AddonSpecification by the file name of the add-on.
	 *
	 * @param fileName Name of the add-ons jar file.
	 * @return The [AddonSpecification] of add-on with the name `fileName`.
	 */
	fun getAddonSpecByFileName(fileName: String?): AddonSpecification? = this.addons[fileName]

	/**
	 * Uninstall an add-on.
	 * The method removes the jar file containing the add-on. The cleanup is done by the
	 * [AddonManager]. This method is intended just for the [AddonManager] and should
	 * not be called in any other class.
	 *
	 * @param addonSpec The [AddonSpecification] of the add-on to uninstall.
	 */
	fun uninstallAddon(addonSpec: AddonSpecification) {
		val addonJar: File = Companion.files[addonSpec.getId()]!!
		addonJar.delete()
	}

	companion object {
		private val registeredAddons = mutableMapOf<String?, AddonSpecification>()
		private val files = mutableMapOf<String?, File>()
	}
}
