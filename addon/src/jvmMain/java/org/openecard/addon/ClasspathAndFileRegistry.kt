/****************************************************************************
 * Copyright (C) 2013-2017 HS Coburg.
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

import org.openecard.addon.manifest.AddonSpecification

/**
 * This AddonRegistry is a combination of [ClasspathRegistry] and [FileRegistry].
 * All calls to this registry are forwarded to the appropriate registry for a given addon.
 * In case of conflicts (e.g. same addon twice), the [ClasspathRegistry] has priority.
 *
 * @author Dirk Petrautzki
 */
class ClasspathAndFileRegistry(
	manager: AddonManager,
) : CombiningRegistry {
	override val classpathRegistry: ClasspathRegistry = ClasspathRegistry()
	override val fileRegistry: FileRegistry = FileRegistry(manager)

	override fun listAddons(): MutableSet<AddonSpecification> {
		val list = classpathRegistry.listAddons()
		list.addAll(fileRegistry.listAddons())
		return list
	}

	override fun search(id: String): AddonSpecification? {
		var desc = classpathRegistry.search(id)
		if (desc == null) {
			desc = fileRegistry.search(id)
		}
		return desc
	}

	override fun searchByName(name: String): MutableSet<AddonSpecification>? {
		var searchByName = classpathRegistry.searchByName(name)
		if (searchByName.isEmpty()) {
			searchByName = fileRegistry.searchByName(name)!!
		}
		return searchByName
	}

	override fun searchIFDProtocol(uri: String): MutableSet<AddonSpecification>? {
		var matchingAddons = classpathRegistry.searchIFDProtocol(uri)
		if (matchingAddons.isEmpty()) {
			matchingAddons = fileRegistry.searchIFDProtocol(uri)!!
		}
		return matchingAddons
	}

	override fun searchSALProtocol(uri: String): MutableSet<AddonSpecification>? {
		var matchingAddons = classpathRegistry.searchSALProtocol(uri)
		if (matchingAddons.isEmpty()) {
			matchingAddons = fileRegistry.searchSALProtocol(uri)!!
		}
		return matchingAddons
	}

	@Throws(AddonException::class)
	override fun downloadAddon(addonSpec: AddonSpecification): ClassLoader? {
		val desc = classpathRegistry.search(addonSpec.getId())
		if (desc != null) {
			return classpathRegistry.downloadAddon(addonSpec)
		} else {
			return fileRegistry.downloadAddon(addonSpec)
		}
	}

	override fun searchByResourceName(resourceName: String): MutableSet<AddonSpecification>? {
		var matchingAddons = classpathRegistry.searchByResourceName(resourceName)
		if (matchingAddons.isEmpty()) {
			matchingAddons = fileRegistry.searchByResourceName(resourceName)!!
		}
		return matchingAddons
	}

	override fun searchByActionId(actionId: String): MutableSet<AddonSpecification>? {
		var matchingAddons = classpathRegistry.searchByActionId(actionId)
		if (matchingAddons.isEmpty()) {
			matchingAddons = fileRegistry.searchByResourceName(actionId)!!
		}
		return matchingAddons
	}

	override fun listInstalledAddons(): MutableSet<AddonSpecification>? {
		val addons = fileRegistry.listInstalledAddons()
		addons?.addAll(classpathRegistry.listInstalledAddons())
		return addons
	}
}
