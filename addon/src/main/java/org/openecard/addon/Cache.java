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
 ***************************************************************************/

package org.openecard.addon;

import java.util.Collection;
import java.util.TreeMap;
import org.openecard.addon.bind.AppExtensionAction;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.ifd.IFDProtocol;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.addon.manifest.AppExtensionSpecification;
import org.openecard.addon.manifest.AppPluginSpecification;
import org.openecard.addon.manifest.ProtocolPluginSpecification;
import org.openecard.addon.sal.SALProtocol;


/**
 * The class implements a cache which stores loaded Actions and Protocols.
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class Cache {

    /**
     * A TreeMap which caches all the protocols and actions.
     */
    private final TreeMap<AddonSpecification, TreeMap<String, Object>> addonSpecAndId;

    /**
     *	Creates a new Cache object.
     */
    public Cache() {
	addonSpecAndId = new TreeMap<>();
    }

    /**
     * Adds a SALProtocol to the Cache.
     *
     * @param spec The {@link AddonSpecification} which identifies the add-on which contains the protocol.
     * @param uri The {@link ProtocolPluginSpecification#uri} which identifies the protocol in the add-ons context.
     * @param protocol The {@link SALProtocol} to add.
     */
    public void addSALProtocol(AddonSpecification spec, String uri, SALProtocol protocol) {
	addEntry(spec, uri, protocol);
    }

    /**
     * Adds a IFDProtocol to the Cache.
     *
     * @param spec The {@link AddonSpecification} which identifies the add-on which contains the protocol.
     * @param uri The {@link ProtocolPluginSpecification#uri} which identifies the protocol in the add-ons context.
     * @param protocol The {@link IFDProtocol} to add.
     */
    public void addIFDProtocol(AddonSpecification spec, String uri, IFDProtocol protocol) {
	addEntry(spec, uri, protocol);
    }

    /**
     * Adds a AppExtensionAction to the Cache.
     *
     * @param spec The {@link AddonSpecification} which identifies the add-on which contains the action.
     * @param actionId The {@link AppExtensionSpecification#id} which identifies the action in the add-ons context.
     * @param action The {@link AppExtensionAction} to add.
     */
    public void addAppExtensionAction(AddonSpecification spec, String actionId, AppExtensionAction action) {
	addEntry(spec, actionId, action);
    }

    /**
     * Adds a AppPluginAction to the Cache.
     *
     * @param spec The {@link AddonSpecification} which identifies the add-on which contains the action.
     * @param resourceId The {@link AppPluginSpecification#resourceName} which identifies the action in the add-ons
     * context.
     * @param action The {@link AppPluginAction} to add.
     */
    public void addAppPluginAction(AddonSpecification spec, String resourceId, AppPluginAction action) {
	addEntry(spec, resourceId, action);
    }

    /**
     * Adds an entry to the Map.
     *
     * @param spec The {@link AddonSpecification} which identifies the add-on which contains the protocol/action.
     * @param id Unique identifier which identifies the object in the context of the add-on.
     * @param protocolOrAction The protocol or action to add.
     */
    private void addEntry(AddonSpecification spec, String id, Object protocolOrAction) {
	TreeMap<String, Object> protocols = addonSpecAndId.get(spec);
	if (protocols == null) {
	    protocols = new TreeMap<>();
	}
	protocols.put(id, protocolOrAction);
	addonSpecAndId.put(spec, protocols);
    }

    /**
     * Get a specific IFDProtocol.
     *
     * @param spec A {@link AddonSpecification} which identifies the add-on which shall contain the IFDProtocol.
     * @param uri The {@link ProtocolPluginSpecification#uri} which identifies the IFDProtocol in the add-ons context.
     * @return The requested {@link IFDProtocol} or NULL if no such uri exists or if the uri links a object which is not
     * of type IFDProtocol.
     */
    public IFDProtocol getIFDProtocol(AddonSpecification spec, String uri) {
	Object protocolObject = getObject(spec, uri);
	if (protocolObject == null || ! (protocolObject instanceof IFDProtocol)) {
	    return null;
	}
	return (IFDProtocol) protocolObject;
    }

    /**
     * Get a specific SALProtocol.
     *
     * @param spec A {@link AddonSpecification} which identifies the add-on which shall contain the SALProtocol.
     * @param uri The {@link ProtocolPluginSpecification#uri} which identifies the SALProtocol in the add-ons context.
     * @return The requested {@link SALProtocol} or NULL if no such uri exists or if the uri links a object which is not
     * of type SALProtocol.
     */
    public SALProtocol getSALProtocol(AddonSpecification spec, String uri) {
	Object protocolObject = getObject(spec, uri);
	if (protocolObject == null || ! (protocolObject instanceof SALProtocol)) {
	    return null;
	}
	return (SALProtocol) protocolObject;
    }

    /**
     * Get a specific AppExtensionAction.
     *
     * @param spec A {@link AddonSpecification} which identifies the add-on which shall contain the AppExtensionAction.
     * @param actionId The {@link AppExtensionSpecification#id} which identifies the AppExtensionAction in the add-ons
     * context.
     * @return The requested {@link AppExtensionAction} or NULL if no such actionId exists or if the actionId links a
     * object which is not of type AppExtensionAction.
     */
    public AppExtensionAction getAppExtensionAction(AddonSpecification spec, String actionId) {
	Object protocolObject = getObject(spec, actionId);
	if (protocolObject == null || ! (protocolObject instanceof AppExtensionAction)) {
	    return null;
	}
	return (AppExtensionAction) protocolObject;
    }

    /**
     * Get a specific AppPluginAction.
     *
     * @param spec A {@link AddonSpecification} which identifies the add-on which shall contain the AppPluginAction.
     * @param resourceName The {@link AppPluginSpecification#resourceName} which identifies the AppPluginAction in the
     * add-ons context.
     * @return The requested {@link AppPluginAction} or NULL if no such resourceName exists or if the resourceName links
     * a object which is not of type AppPluginAction.
     */
    public AppPluginAction getAppPluginAction(AddonSpecification spec, String resourceName) {
	Object protocolObject = getObject(spec, resourceName);
	if (protocolObject == null || ! (protocolObject instanceof AppPluginAction)) {
	    return null;
	}
	return (AppPluginAction) protocolObject;
    }

    /**
     * Get a stored object from the map which manages all entries.
     *
     * @param spec A {@link AddonSpecification} referencing an add-on.
     * @param id A identifier of a Action or Protocol contained in the global map.
     * @return A {@link Object} which is referenced by the {@code spec} and {@code id} or NULL if no Object is associated
     * with the {@code spec} and {@code id}.
     */
    private Object getObject(AddonSpecification spec, String id) {
	TreeMap<String, Object> protocols = addonSpecAndId.get(spec);
	if (protocols == null) {
	    return null;
	}

	return protocols.get(id);
    }

    /**
     * Get a collection containing all identifiers and Actions/Protocols of a specific add-on.
     *
     * @param spec The {@link AddonSpecification} which refers the add-on.
     * @return A Collection containing all loaded Actions and Protocols of a add-on.
     */
    protected Collection<Object> getAllAddonData(AddonSpecification spec) {
	return addonSpecAndId.get(spec).values();
    }

    /**
     * Removes an entry from the Cache.
     *
     * @param spec A {@link AddonSpecification} which identifies the add-on which contains the object to remove.
     * @param id A identifier of an Action or Protocol which refers to specific object in the add-ons context.
     */
    public void removeCacheEntry(AddonSpecification spec, String id) {
	TreeMap<String, Object> protocols = addonSpecAndId.get(spec);
	protocols.remove(id);
    }

    /**
     * Remove all cached entries of a given add-on.
     *
     * @param spec A {@link AddonSpecification} which identifies the add-on.
     */
    public void removeCompleteAddonCache(AddonSpecification spec) {
	addonSpecAndId.remove(spec);
    }

}
