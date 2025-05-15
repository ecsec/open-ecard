/****************************************************************************
 * Copyright (C) 2013-2014 ecsec GmbH.
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

import org.openecard.addon.bind.AppExtensionAction
import org.openecard.addon.bind.AppPluginAction
import org.openecard.addon.ifd.IFDProtocol
import org.openecard.addon.manifest.AddonSpecification
import org.openecard.addon.sal.SALProtocol

/**
 * Selector class for add-ons working on top of the registries of the systems AddonManager.
 *
 * @author Tobias Wich
 */
class AddonSelector(
	private val manager: AddonManager,
) {
	// TODO: implement caching
	private val ifdCache: MutableMap<String?, AddonSpecification?> = mutableMapOf()
	private val salCache: MutableMap<String?, AddonSpecification?> = mutableMapOf()
	private val extensionCache: MutableMap<String?, AddonSpecification?> = mutableMapOf()
	private val pluginCache: MutableMap<String?, AddonSpecification?> = mutableMapOf()

	private var strategy: SelectionStrategy? = null

	init {

		setStrategy(HighestVersionSelector())
	}

	fun setStrategy(strategy: SelectionStrategy) {
		this.strategy = strategy
	}

	@Throws(AddonNotFoundException::class)
	fun getIFDProtocol(uri: String): IFDProtocol? {
		val addons = manager.getRegistry().searchIFDProtocol(uri)
		if (addons!!.isEmpty()) {
			throw AddonNotFoundException("No Add-on for IFD protocol '$uri' found.")
		}
		val addon = strategy!!.select(addons)
		return manager.getIFDProtocol(addon!!, uri)
	}

	fun returnIFDProtocol(obj: IFDProtocol) {
		manager.returnIFDProtocol(obj)
	}

	@Throws(AddonNotFoundException::class)
	fun getSALProtocol(uri: String): SALProtocol? {
		val addons = manager.getRegistry().searchSALProtocol(uri)
		if (addons!!.isEmpty()) {
			throw AddonNotFoundException("No Add-on for SAL protocol '$uri' found.")
		}
		val addon = strategy!!.select(addons)
		return manager.getSALProtocol(addon!!, uri)
	}

	fun returnSALProtocol(
		obj: SALProtocol,
		force: Boolean,
	) {
		manager.returnSALProtocol(obj, force)
	}

	@Throws(AddonNotFoundException::class)
	fun getAppExtensionAction(actionId: String): AppExtensionAction? {
		val addons = manager.getRegistry().searchByActionId(actionId)
		if (addons!!.isEmpty()) {
			throw AddonNotFoundException("No Add-on for action ID '$actionId' found.")
		}
		val addon = strategy!!.select(addons)
		return manager.getAppExtensionAction(addon!!, actionId)
	}

	fun returnAppExtensionAction(obj: AppExtensionAction) {
		manager.returnAppExtensionAction(obj)
	}

	@Throws(AddonNotFoundException::class)
	fun getAppPluginAction(resourceName: String): AppPluginAction? {
		val addons = manager.getRegistry().searchByResourceName(resourceName)
		if (addons!!.isEmpty()) {
			throw AddonNotFoundException("No Add-on for resource '$resourceName' found.")
		}
		val addon = strategy!!.select(addons)
		return manager.getAppPluginAction(addon!!, resourceName)
	}

	fun returnAppPluginAction(obj: AppPluginAction) {
		manager.returnAppPluginAction(obj)
	}
}
