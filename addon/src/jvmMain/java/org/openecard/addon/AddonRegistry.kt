/****************************************************************************
 * Copyright (C) 2013-2015 ecsec GmbH.
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
 * A registry which allows to search a AddonSpecification by several parameters.
 *
 *
 * Next to the search capabilities this interface provides also functions for listing addons and to get specific
 * ClassLoader.
 *
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 * @author Hans-Martin Haase
 */
interface AddonRegistry {
	/**
	 * List all available addons of the registry.
	 *
	 * @return A Set containing the [AddonSpecification] of all available addons.
	 */
	fun listAddons(): Set<AddonSpecification>

	/**
	 * List all installed addons.
	 *
	 *
	 * The method is interesting in case of an AppStore like registry which provides several addons which do not have to
	 * be installed. So this function lists only the addons which are installed at the users system.
	 *
	 *
	 * @return A Set containing the [AddonSpecification] of all installed addons.
	 */
	fun listInstalledAddons(): MutableSet<AddonSpecification>?

	/**
	 * Search an AddonSpecification by it's ID.
	 *
	 * @param id The ID of the [AddonSpecification] to search.
	 * @return The [AddonSpecification] according to the given `id` parameter.
	 * @throws AddonNotFoundException Thrown if no [AddonSpecification] with the parameter `id` was found.
	 */
	@Throws(AddonNotFoundException::class)
	fun search(id: String): AddonSpecification?

	/**
	 * Search an AddonSpecification by it's name.
	 *
	 * @param name The name of the [AddonSpecification] to search.
	 * @return A Set of [AddonSpecification] objects. The method returns a Set just in case there are different
	 * versions of an addon is available.
	 */
	fun searchByName(name: String): MutableSet<AddonSpecification>?

	/**
	 * Search an AddonSpecification by a specific IFD protocol uri.
	 *
	 * @param protocolUri An IFD protocol uri to search.
	 * @return A Set of [AddonSpecification] objects which contain the specified `protocolUri` object.
	 */
	fun searchIFDProtocol(protocolUri: String): MutableSet<AddonSpecification>?

	/**
	 * Search an AddonSpecification by a specific SAL protocolUri.
	 *
	 * @param protocolUri An SAL protocol uri to search.
	 * @return A Set of [AddonSpecification] objects which contain the specified `protocolUri` object.
	 */
	fun searchSALProtocol(protocolUri: String): MutableSet<AddonSpecification>?

	/**
	 * Search an AddonSpecification by a specific actionId.
	 *
	 * @param actionId A actionId to search.
	 * @return A Set of [AddonSpecification] objects which contain the specified `actionId` object.
	 */
	fun searchByActionId(actionId: String): MutableSet<AddonSpecification>?

	/**
	 * Search an AddonSpecification by a specific resource name.
	 *
	 * @param resourceName A resource name to search.
	 * @return A Set of [AddonSpecification] objects which contain the specified `resourceName` object.
	 */
	fun searchByResourceName(resourceName: String): MutableSet<AddonSpecification>?

	/**
	 * Get a ClassLoader by a specific AddonSpecification.
	 *
	 * @param addonSpec An [AddonSpecification] to search.
	 * @return A [ClassLoader] for the corresponding [AddonSpecification].
	 * @throws AddonException Thrown in case there is a problem loading the Add-on.
	 * @throws NullPointerException Thrown in case no specification was given or there are vital elemnts missing in the
	 * Add-on specification.
	 */
	@Throws(AddonException::class, NullPointerException::class)
	fun downloadAddon(addonSpec: AddonSpecification): ClassLoader?
}
