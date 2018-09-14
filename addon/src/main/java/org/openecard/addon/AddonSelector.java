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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.openecard.addon.bind.AppExtensionAction;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.ifd.IFDProtocol;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.addon.sal.SALProtocol;


/**
 * Selector class for add-ons working on top of the registries of the systems AddonManager.
 *
 * @author Tobias Wich
 */
public class AddonSelector {

    private final AddonManager manager;

    // TODO: implement caching
    private final Map<String, AddonSpecification> ifdCache;
    private final Map<String, AddonSpecification> salCache;
    private final Map<String, AddonSpecification> extensionCache;
    private final Map<String, AddonSpecification> pluginCache;

    private SelectionStrategy strategy;

    public AddonSelector(AddonManager manager) {
	this.manager = manager;

	ifdCache = new HashMap<>();
	salCache = new HashMap<>();
	extensionCache = new HashMap<>();
	pluginCache = new HashMap<>();

	setStrategy(new HighestVersionSelector());
    }

    public final void setStrategy(SelectionStrategy strategy) {
	this.strategy = strategy;
    }


    public IFDProtocol getIFDProtocol(@Nonnull String uri) throws AddonNotFoundException {
	Set<AddonSpecification> addons = manager.getRegistry().searchIFDProtocol(uri);
	if (addons.isEmpty()) {
	    throw new AddonNotFoundException("No Add-on for IFD protocol '" + uri + "' found.");
	}
	AddonSpecification addon = strategy.select(addons);
	return manager.getIFDProtocol(addon, uri);
    }

    public void returnIFDProtocol(IFDProtocol obj) {
	manager.returnIFDProtocol(obj);
    }

    public SALProtocol getSALProtocol(@Nonnull String uri) throws AddonNotFoundException {
	Set<AddonSpecification> addons = manager.getRegistry().searchSALProtocol(uri);
	if (addons.isEmpty()) {
	    throw new AddonNotFoundException("No Add-on for SAL protocol '" + uri + "' found.");
	}
	AddonSpecification addon = strategy.select(addons);
	return manager.getSALProtocol(addon, uri);
    }

    public void returnSALProtocol(SALProtocol obj, boolean force) {
	manager.returnSALProtocol(obj, force);
    }

    public AppExtensionAction getAppExtensionAction(@Nonnull String actionId) throws AddonNotFoundException {
	Set<AddonSpecification> addons = manager.getRegistry().searchByActionId(actionId);
	if (addons.isEmpty()) {
	    throw new AddonNotFoundException("No Add-on for action ID '" + actionId + "' found.");
	}
	AddonSpecification addon = strategy.select(addons);
	return manager.getAppExtensionAction(addon, actionId);
    }

    public void returnAppExtensionAction(AppExtensionAction obj) {
	manager.returnAppExtensionAction(obj);
    }

    public AppPluginAction getAppPluginAction(@Nonnull String resourceName) throws AddonNotFoundException {
	Set<AddonSpecification> addons = manager.getRegistry().searchByResourceName(resourceName);
	if (addons.isEmpty()) {
	    throw new AddonNotFoundException("No Add-on for resource '" + resourceName + "' found.");
	}
	AddonSpecification addon = strategy.select(addons);
	return manager.getAppPluginAction(addon, resourceName);
    }

    public void returnAppPluginAction(AppPluginAction obj) {
	manager.returnAppPluginAction(obj);
    }

}
