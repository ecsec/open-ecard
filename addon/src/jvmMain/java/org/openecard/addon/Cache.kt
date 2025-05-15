/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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
 * The class implements a cache which stores loaded Actions and Protocols.
 *
 * @author Hans-Martin Haase
 */
class Cache {
	/**
	 * A TreeMap which caches all the protocols and actions.
	 */
	private val addonSpecAndId: MutableMap<AddonSpecification, MutableMap<String, LifecycleTrait>> =
		mutableMapOf()

	/**
	 * Adds a SALProtocol to the Cache.
	 *
	 * @param spec The [AddonSpecification] which identifies the add-on which contains the protocol.
	 * @param uri The [org.openecard.addon.manifest.ProtocolPluginSpecification.uri] which identifies the protocol in the add-ons context.
	 * @param protocol The [SALProtocol] to add.
	 */
	fun addSALProtocol(
		spec: AddonSpecification,
		uri: String,
		protocol: SALProtocol,
	) {
		addEntry(spec, uri, protocol)
	}

	/**
	 * Adds a IFDProtocol to the Cache.
	 *
	 * @param spec The [AddonSpecification] which identifies the add-on which contains the protocol.
	 * @param uri The [org.openecard.addon.manifest.ProtocolPluginSpecification.uri] which identifies the protocol in the add-ons context.
	 * @param protocol The [IFDProtocol] to add.
	 */
	fun addIFDProtocol(
		spec: AddonSpecification,
		uri: String,
		protocol: IFDProtocol,
	) {
		addEntry(spec, uri, protocol)
	}

	/**
	 * Adds a AppExtensionAction to the Cache.
	 *
	 * @param spec The [AddonSpecification] which identifies the add-on which contains the action.
	 * @param actionId The [org.openecard.addon.manifest.AppExtensionSpecification.id] which identifies the action in the add-ons context.
	 * @param action The [AppExtensionAction] to add.
	 */
	fun addAppExtensionAction(
		spec: AddonSpecification,
		actionId: String,
		action: AppExtensionAction,
	) {
		addEntry(spec, actionId, action)
	}

	/**
	 * Adds a AppPluginAction to the Cache.
	 *
	 * @param spec The [AddonSpecification] which identifies the add-on which contains the action.
	 * @param resourceId The [org.openecard.addon.manifest.AppPluginSpecification.resourceName] which identifies the action in the add-ons
	 * context.
	 * @param action The [AppPluginAction] to add.
	 */
	fun addAppPluginAction(
		spec: AddonSpecification,
		resourceId: String,
		action: AppPluginAction,
	) {
		addEntry(spec, resourceId, action)
	}

	/**
	 * Adds an entry to the Map.
	 *
	 * @param spec The [AddonSpecification] which identifies the add-on which contains the protocol/action.
	 * @param id Unique identifier which identifies the object in the context of the add-on.
	 * @param protocolOrAction The protocol or action to add.
	 */
	private fun <T : LifecycleTrait> addEntry(
		spec: AddonSpecification,
		id: String,
		protocolOrAction: T,
	) {
		var protocols = addonSpecAndId[spec]
		if (protocols == null) {
			protocols = mutableMapOf()
		}
		protocols.put(id, protocolOrAction)
		addonSpecAndId.put(spec, protocols)
	}

	/**
	 * Get a specific IFDProtocol.
	 *
	 * @param spec A [AddonSpecification] which identifies the add-on which shall contain the IFDProtocol.
	 * @param uri The [org.openecard.addon.manifest.ProtocolPluginSpecification.uri] which identifies the IFDProtocol in the add-ons context.
	 * @return The requested [IFDProtocol] or NULL if no such uri exists or if the uri links a object which is not
	 * of type IFDProtocol.
	 */
	fun getIFDProtocol(
		spec: AddonSpecification?,
		uri: String?,
	): IFDProtocol? {
		val protocolObject: Any? = getObject(spec, uri)
		if (protocolObject == null || protocolObject !is IFDProtocol) {
			return null
		}
		return protocolObject
	}

	/**
	 * Get a specific SALProtocol.
	 *
	 * @param spec A [AddonSpecification] which identifies the add-on which shall contain the SALProtocol.
	 * @param uri The [org.openecard.addon.manifest.ProtocolPluginSpecification.uri] which identifies the SALProtocol in the add-ons context.
	 * @return The requested [SALProtocol] or NULL if no such uri exists or if the uri links a object which is not
	 * of type SALProtocol.
	 */
	fun getSALProtocol(
		spec: AddonSpecification?,
		uri: String?,
	): SALProtocol? {
		val protocolObject: Any? = getObject(spec, uri)
		if (protocolObject == null || protocolObject !is SALProtocol) {
			return null
		}
		return protocolObject
	}

	/**
	 * Get a specific AppExtensionAction.
	 *
	 * @param spec A [AddonSpecification] which identifies the add-on which shall contain the AppExtensionAction.
	 * @param actionId The [org.openecard.addon.manifest.AppExtensionSpecification.id] which identifies the AppExtensionAction in the add-ons
	 * context.
	 * @return The requested [AppExtensionAction] or NULL if no such actionId exists or if the actionId links a
	 * object which is not of type AppExtensionAction.
	 */
	fun getAppExtensionAction(
		spec: AddonSpecification?,
		actionId: String?,
	): AppExtensionAction? {
		val protocolObject: Any? = getObject(spec, actionId)
		if (protocolObject == null || protocolObject !is AppExtensionAction) {
			return null
		}
		return protocolObject
	}

	/**
	 * Get a specific AppPluginAction.
	 *
	 * @param spec A [AddonSpecification] which identifies the add-on which shall contain the AppPluginAction.
	 * @param resourceName The [org.openecard.addon.manifest.AppPluginSpecification.resourceName] which identifies the AppPluginAction in the
	 * add-ons context.
	 * @return The requested [AppPluginAction] or NULL if no such resourceName exists or if the resourceName links
	 * a object which is not of type AppPluginAction.
	 */
	fun getAppPluginAction(
		spec: AddonSpecification?,
		resourceName: String?,
	): AppPluginAction? {
		val protocolObject: Any? = getObject(spec, resourceName)
		if (protocolObject == null || protocolObject !is AppPluginAction) {
			return null
		}
		return protocolObject
	}

	/**
	 * Get a stored object from the map which manages all entries.
	 *
	 * @param spec A [AddonSpecification] referencing an add-on.
	 * @param id A identifier of a Action or Protocol contained in the global map.
	 * @return A [Object] which is referenced by the `spec` and `id` or NULL if no Object is associated
	 * with the `spec` and `id`.
	 */
	private fun <T : LifecycleTrait?> getObject(
		spec: AddonSpecification?,
		id: String?,
	): T? {
		val protocols: MutableMap<String, in LifecycleTrait>? = addonSpecAndId[spec]
		if (protocols == null) {
			return null
		}

		return protocols[id] as T?
	}

	/**
	 * Get a collection containing all identifiers and Actions/Protocols of a specific add-on.
	 *
	 * @param spec The [AddonSpecification] which refers the add-on.
	 * @return A Collection containing all loaded Actions and Protocols of a add-on. If no entries for the given
	 * [AddonSpecification] exists an empty collection is returned.
	 */
	fun getAllAddonData(spec: AddonSpecification): List<LifecycleTrait>? {
		val data: Map<String, LifecycleTrait>? = addonSpecAndId[spec]
		return data?.values?.toList() ?: emptyList()
	}

	/**
	 * Removes an entry from the Cache.
	 *
	 * @param spec A [AddonSpecification] which identifies the add-on which contains the object to remove.
	 * @param id A identifier of an Action or Protocol which refers to specific object in the add-ons context.
	 */
	fun removeCacheEntry(
		spec: AddonSpecification?,
		id: String?,
	) {
		val protocols: MutableMap<String, out LifecycleTrait> = addonSpecAndId[spec]!!
		protocols.remove(id)
	}

	/**
	 * Remove all cached entries of a given add-on.
	 *
	 * @param spec A [AddonSpecification] which identifies the add-on.
	 */
	fun removeCompleteAddonCache(spec: AddonSpecification?) {
		addonSpecAndId.remove(spec)
	}
}
