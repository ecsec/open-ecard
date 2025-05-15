/****************************************************************************
 * Copyright (C) 2013-2019 ecsec GmbH.
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
import org.openecard.addon.bind.AppExtensionAction
import org.openecard.addon.bind.AppExtensionActionProxy
import org.openecard.addon.bind.AppPluginAction
import org.openecard.addon.bind.AppPluginActionProxy
import org.openecard.addon.ifd.IFDProtocol
import org.openecard.addon.ifd.IFDProtocolProxy
import org.openecard.addon.manifest.AddonSpecification
import org.openecard.addon.manifest.AppExtensionSpecification
import org.openecard.addon.manifest.AppPluginSpecification
import org.openecard.addon.manifest.ProtocolPluginSpecification
import org.openecard.addon.sal.SALProtocol
import org.openecard.addon.sal.SALProtocolProxy
import org.openecard.addon.sal.SalStateView
import org.openecard.common.interfaces.Environment
import org.openecard.common.util.FacadeInvocationHandler
import org.openecard.gui.definition.ViewController
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

/**
 * Implementation of a AddonManager.
 *
 * The AddonManager takes care for the management of the add-on. This covers the initialization startup, registering of
 * new add-ons, unloading of add-ons on shut down and removal and the uninstalling of an add-on. Furthermore the
 * AddonManager provides methods to retrieve specific parts of a add-on.
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */

private val LOG = KotlinLogging.logger { }

class AddonManager(
	env: Environment,
	view: ViewController,
	registry: CombiningRegistry?,
	salStateView: SalStateView,
) {
	private val registry: CombiningRegistry?
	private val protectedRegistry: AddonRegistry
	private val env: Environment
	private val eventHandler: EventHandler
	private val viewController: ViewController
	private val salStateView: SalStateView

	// TODO: rework cache to have borrow and return semantic
	private val cache = Cache()

	/**
	 * Creates a new AddonManager.
	 *
	 * @param env
	 * @param view
	 * @param registry
	 * @param salStateView
	 * @throws WSMarshallerException
	 */
	init {
		if (registry == null) {
			this.registry = ClasspathAndFileRegistry(this)
		} else {
			this.registry = registry
		}
		this.protectedRegistry = getProtectedRegistry(this.registry)!!
		this.env = env
		this.eventHandler = EventHandler()
		this.env.eventDispatcher!!.add(eventHandler)
		this.viewController = view
		this.salStateView = salStateView

		Thread(
			Runnable {
				loadLoadOnStartAddons()
			},
			"Init-Addons",
		).start()
	}

	constructor(env: Environment, view: ViewController, salStateView: SalStateView) : this(
		env,
		view,
		null,
		salStateView,
	)

	/**
	 * Load all addons which contain an loadOnStart = true.
	 *
	 * @throws WSMarshallerException
	 */
	private fun loadLoadOnStartAddons() {
		// load plugins which have an loadOnStartup = true
		val specs = protectedRegistry.listAddons()
		for (addonSpec in specs!!) {
			loadLoadOnStartupActions(addonSpec)
		}
	}

	/**
	 * Load a single addon which contains a LoadOnStartup = true.
	 *
	 * @param addonSpec The [AddonSpecification] of the addon.
	 */
	fun loadLoadOnStartupActions(addonSpec: AddonSpecification) {
		if (!addonSpec.applicationActions.isEmpty()) {
			for (appExSpec in addonSpec.applicationActions) {
				if (appExSpec.isLoadOnStartup == true) {
					getAppExtensionAction(addonSpec, appExSpec.id!!)
				}
			}
		}

		if (!addonSpec.bindingActions.isEmpty()) {
			for (appPlugSpec in addonSpec.bindingActions) {
				if (appPlugSpec.isLoadOnStartup == true) {
					getAppPluginAction(addonSpec, appPlugSpec.resourceName!!)
				}
			}
		}

		if (!addonSpec.ifdActions.isEmpty()) {
			for (protPlugSpec in addonSpec.ifdActions) {
				if (protPlugSpec.isLoadOnStartup == true) {
					getIFDProtocol(addonSpec, protPlugSpec.uri!!)
				}
			}
		}

		if (!addonSpec.salActions.isEmpty()) {
			for (protPlugSpec in addonSpec.salActions) {
				if (protPlugSpec.isLoadOnStartup == true) {
					getSALProtocol(addonSpec, protPlugSpec.uri!!)
				}
			}
		}
	}

	/**
	 * Unload all add-ons.
	 */
	private fun unloadAllAddons() {
		val addons = protectedRegistry.listInstalledAddons()
		for (addonSpec in addons!!) {
			unloadAddon(addonSpec)
		}
	}

	/**
	 * Unload all actions and protocols of a specific add-on.
	 *
	 * @param addonSpec The [AddonSpecification] of the add-on to unload.
	 */
	fun unloadAddon(addonSpec: AddonSpecification) {
		val actionsAndProtocols = cache.getAllAddonData(addonSpec)
		for (obj in actionsAndProtocols!!) {
			obj.destroy(true)
		}

		cache.removeCompleteAddonCache(addonSpec)
	}

	/**
	 * Get the CombiningRigistry.
	 *
	 * @return A [AddonRegistry] object which provides access just to the interface methods of the
	 * [ClasspathAndFileRegistry].
	 */
	fun getRegistry(): AddonRegistry = protectedRegistry

	val builtinRegistry: AddonRegistry
		/**
		 * Get the ClasspathRegistry.
		 *
		 * @return A [AddonRegistry] object which provides access just to the interface methods of the
		 * [ClasspathRegistry].
		 */
		get() = getProtectedRegistry(registry?.classpathRegistry)!!

	val externalRegistry: AddonRegistry
		/**
		 * Get the FileRegistry.
		 *
		 * @return A [AddonRegistry] object which provides access just to the interface methods of the
		 * [FileRegistry].
		 */
		get() = getProtectedRegistry(registry?.fileRegistry)!!

	/**
	 * Register a new add-on which is located in the class path.
	 *
	 * @param desc [AddonSpecification] of the add-on which shall be registered.
	 */
	fun registerClasspathAddon(desc: AddonSpecification?) {
		// TODO: protect this method from the sandbox
		this.registry!!.classpathRegistry?.register(desc!!)
	}

	/**
	 * Get a specific IFDProtocol.
	 *
	 * @param addonSpec [AddonSpecification] which contains the description of the [IFDProtocol].
	 * @param uri The [ProtocolPluginSpecification.uri] to identify the requested IFDProtocol.
	 * @return The requested IFDProtocol object or NULL if no such object was found.
	 */
	fun getIFDProtocol(
		addonSpec: AddonSpecification,
		uri: String,
	): IFDProtocol? {
		val ifdProt = cache.getIFDProtocol(addonSpec, uri)

		// TODO: find a better way to deal with the reuse of protocol plugins
// 	if (ifdProt != null) {
// 	    // protocol cached so return it
// 	    return ifdProt;
// 	}
		val protoSpec = addonSpec.searchIFDActionByURI(uri)
		if (protoSpec == null) {
			LOG.error { "${"Requested IFD Protocol {} does not exist in Add-on {}."} $uri ${addonSpec.getId()}" }
		} else {
			val className = protoSpec.className
			try {
				val cl = registry!!.downloadAddon(addonSpec)
				val protoFactory = IFDProtocolProxy(className!!, cl!!)
				val aCtx = createContext(addonSpec)
				protoFactory.init(aCtx)
				cache.addIFDProtocol(addonSpec, uri, protoFactory)
				return protoFactory
			} catch (e: ActionInitializationException) {
				LOG.error(e) { "Initialization of IFD Protocol failed" }
			} catch (ex: AddonException) {
				LOG.error(ex) { "Failed to download Add-on." }
			}
		}
		return null
	}

	fun returnIFDProtocol(obj: IFDProtocol) {
		obj.destroy(false)
	}

	/**
	 * Get a specific SALProtocol.
	 *
	 * @param addonSpec [AddonSpecification] which contains the description of the [SALProtocol].
	 * @param uri The [ProtocolPluginSpecification.uri] to identify the requested SALProtocol.
	 * @return The requested SALProtocol object or NULL if no such object was found.
	 */
	fun getSALProtocol(
		addonSpec: AddonSpecification,
		uri: String,
	): SALProtocol? {
		val salProt = cache.getSALProtocol(addonSpec, uri)

		// TODO: find a better way to deal with the reuse of protocol plugins
// 	if (salProt != null) {
// 	    // protocol cached so return it
// 	    return salProt;
// 	}
		val protoSpec = addonSpec.searchSALActionByURI(uri)
		if (protoSpec == null) {
			LOG.error { "${"Requested SAL Protocol {} does not exist in Add-on {}."} $uri ${addonSpec.getId()}" }
		} else {
			val className = protoSpec.className
			try {
				val cl = registry!!.downloadAddon(addonSpec)
				val protoFactory = SALProtocolProxy(className!!, cl!!)
				val aCtx = createContext(addonSpec)
				protoFactory.init(aCtx)
				cache.addSALProtocol(addonSpec, uri, protoFactory)
				return protoFactory
			} catch (e: ActionInitializationException) {
				LOG.error(e) { "Initialization of SAL Protocol failed" }
			} catch (ex: AddonException) {
				LOG.error(ex) { "Failed to download Add-on." }
			}
		}
		return null
	}

	fun returnSALProtocol(
		obj: SALProtocol,
		force: Boolean,
	) {
		obj.destroy(force)
	}

	/**
	 * Get a specific AppExtensionAction.
	 *
	 * @param addonSpec [AddonSpecification] which contains the description of the [AppExtensionAction].
	 * @param actionId    The [AppExtensionSpecification.id] to identify the requested AppExtensionAction.
	 * @return The AppExtensionAction which corresponds the given `actionId` or NULL if no AppExtensionAction with
	 * the given `actionId` exists.
	 */
	fun getAppExtensionAction(
		addonSpec: AddonSpecification,
		actionId: String,
	): AppExtensionAction? {
		// get extension from cache
		val appExtAction = cache.getAppExtensionAction(addonSpec, actionId)

		// TODO: find a better way to deal with the reuse of protocol plugins
// 	if (appExtAction != null) {
// 	    // AppExtensionAction cached so return it
// 	    return appExtAction;
// 	}
		val protoSpec = addonSpec.searchByActionId(actionId)
		if (protoSpec == null) {
			error { "${"Requested Extension {} does not exist in Add-on {}."} $actionId ${addonSpec.getId()}" }
		} else {
			val className = protoSpec.className
			try {
				val cl = registry!!.downloadAddon(addonSpec)
				val protoFactory = AppExtensionActionProxy(className!!, cl!!)
				val aCtx = createContext(addonSpec)
				protoFactory.init(aCtx)
				cache.addAppExtensionAction(addonSpec, actionId, protoFactory)
				return protoFactory
			} catch (e: ActionInitializationException) {
				LOG.error(e) { "Initialization of AppExtensionAction failed" }
			} catch (ex: AddonException) {
				LOG.error(ex) { "Failed to download Add-on." }
			}
		}
		return null
	}

	fun returnAppExtensionAction(obj: AppExtensionAction) {
		obj.destroy(false)
	}

	/**
	 * Get a specific AppPluginAction.
	 *
	 * @param addonSpec [AddonSpecification] which contains the description of the [AppPluginAction].
	 * @param resourceName The [AppPluginSpecification.resourceName] to identify the @[AppPluginAction] to
	 * return.
	 * @return A AppPluginAction which corresponds to the [AddonSpecification] and the `resourceName`. If no
	 * such AppPluginAction exists NULL is returned.
	 */
	fun getAppPluginAction(
		addonSpec: AddonSpecification,
		resourceName: String,
	): AppPluginAction? {
		val appPluginAction = cache.getAppPluginAction(addonSpec, resourceName)

		// TODO: find a better way to deal with the reuse of protocol plugins
// 	if (appPluginAction != null) {
// 	    // AppExtensionAction cached so return it
// 	    return appPluginAction;
// 	}
		val protoSpec = addonSpec.searchByResourceName(resourceName)
		if (protoSpec == null) {
			LOG.error { "${"Plugin for resource {} does not exist in Add-on {}."} $resourceName ${addonSpec.getId()}" }
		} else {
			val className = protoSpec.className
			try {
				val cl = registry!!.downloadAddon(addonSpec)
				val protoFactory = AppPluginActionProxy(className!!, cl!!)
				val aCtx = createContext(addonSpec)
				protoFactory.init(aCtx)
				cache.addAppPluginAction(addonSpec, resourceName, protoFactory)
				return protoFactory
			} catch (e: ActionInitializationException) {
				LOG.error(e) { "Initialization of AppPluginAction failed" }
			} catch (ex: AddonException) {
				LOG.error(ex) { "Failed to download Add-on." }
			}
		}
		return null
	}

	fun returnAppPluginAction(obj: AppPluginAction) {
		obj.destroy(false)
	}

	private fun createContext(addonSpec: AddonSpecification): Context {
		val aCtx = Context(this, env, addonSpec, viewController, salStateView)
		aCtx.setCardRecognition(env.recognition)
		aCtx.setEventHandle(eventHandler)
		aCtx.userConsent = env.gui

		return aCtx
	}

	/**
	 * Shut down the AddonManager.
	 * The method unloads all installed add-ons.
	 */
	fun shutdown() {
		unloadAllAddons()
	}

	/**
	 * Uninstall an add-on.
	 * This is primarily a wrapper method for the [FileRegistry.uninstallAddon]
	 *
	 * @param addonSpec The specification of the add-on to uninstall.
	 */
	fun uninstallAddon(addonSpec: AddonSpecification) {
		// unloading is done by the PluginDirectoryAlterationListener
		val fileRegistry: FileRegistry? = registry!!.fileRegistry
		if (fileRegistry != null) {
			fileRegistry.uninstallAddon(addonSpec)
		}
	}

	companion object {
		/**
		 * This method returns an instance of the given registry where only the interface methods are accessible.
		 *
		 * @param registry Unprotected registry instance.
		 * @return Protected registry instance.
		 */
		private fun getProtectedRegistry(registry: AddonRegistry?): AddonRegistry? {
			if (registry != null) {
				val cl = AddonManager::class.java.getClassLoader()
				val interfaces: Array<Class<*>?> = arrayOf(AddonRegistry::class.java)
				val handler: InvocationHandler = FacadeInvocationHandler(registry)
				val o = Proxy.newProxyInstance(cl, interfaces, handler)
				return o as AddonRegistry
			}
			return null
		}
	}
}
