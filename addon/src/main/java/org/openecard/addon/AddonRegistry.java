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
 ***************************************************************************/

package org.openecard.addon;

import java.util.Set;
import org.openecard.addon.manifest.AddonSpecification;


/**
 * A registry which allows to search a AddonSpecification by several parameters.
 * <p>
 * Next to the search capabilities this interface provides also functions for listing addons and to get specific
 * ClassLoader.
 * </p>
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public interface AddonRegistry {

    /**
     * List all available addons of the registry.
     *
     * @return A Set containing the {@link AddonSpecification} of all available addons.
     */
    Set<AddonSpecification> listAddons();

    /**
     * List all installed addons.
     * <p>
     * The method is interesting in case of an AppStore like registry which provides several addons which do not have to
     * be installed. So this function lists only the addons which are installed at the users system.
     * </p>
     *
     * @return A Set containing the {@link AddonSpecification} of all installed addons.
     */
    Set<AddonSpecification> listInstalledAddons();

    /**
     * Search an AddonSpecification by it's ID.
     *
     * @param id The ID of the {@link AddonSpecification} to search.
     * @return The {@link AddonSpecification} according to the given {@code id} parameter.
     * @throws AddonNotFoundException Thrown if no {@link AddonSpecification} with the parameter {@code id} was found.
     */
    AddonSpecification search(String id) throws AddonNotFoundException;

    /**
     * Search an AddonSpecification by it's name.
     *
     * @param name The name of the {@link AddonSpecification} to search.
     * @return A Set of {@link AddonSpecification} objects. The method returns a Set just in case there are different
     * versions of an addon is available.
     */
    Set<AddonSpecification> searchByName(String name);

    /**
     * Search an AddonSpecification by a specific IFD protocol uri.
     *
     * @param protocolUri An IFD protocol uri to search.
     * @return A Set of {@link AddonSpecification} objects which contain the specified {@code protocolUri} object.
     */
    Set<AddonSpecification> searchIFDProtocol(String protocolUri);

    /**
     * Search an AddonSpecification by a specific SAL protocolUri.
     *
     * @param protocolUri An SAL protocol uri to search.
     * @return A Set of {@link AddonSpecification} objects which contain the specified {@code protocolUri} object.
     */
    Set<AddonSpecification> searchSALProtocol(String protocolUri);

    /**
     * Search an AddonSpecification by a specific actionId.
     *
     * @param actionId A actionId to search.
     * @return A Set of {@link AddonSpecification} objects which contain the specified {@code actionId} object.
     */
    Set<AddonSpecification> searchByActionId(String actionId);

    /**
     * Search an AddonSpecification by a specific resource name.
     *
     * @param resourceName A resource name to search.
     * @return A Set of {@link AddonSpecification} objects which contain the specified {@code resourceName} object.
     */
    Set<AddonSpecification> searchByResourceName(String resourceName);

    /**
     * Get a ClassLoader by a specific AddonSpecification.
     *
     * @param addonSpec An {@link AddonSpecification} to search.
     * @return A {@link ClassLoader} for the corresponding {@link AddonSpecification}.
     */
    ClassLoader downloadAddon(AddonSpecification addonSpec);

}
