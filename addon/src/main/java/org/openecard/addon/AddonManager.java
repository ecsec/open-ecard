/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

import org.openecard.addon.bind.AppExtensionAction;
import org.openecard.addon.bind.AppExtensionActionFactory;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.AppPluginActionFactory;
import org.openecard.addon.ifd.IFDProtocol;
import org.openecard.addon.ifd.IFDProtocolFactory;
import org.openecard.addon.manifest.AddonBundleDescription;
import org.openecard.addon.manifest.AppExtensionActionDescription;
import org.openecard.addon.manifest.AppPluginActionDescription;
import org.openecard.addon.manifest.ProtocolPluginDescription;
import org.openecard.addon.sal.SALProtocol;
import org.openecard.addon.sal.SALProtocolFactory;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.gui.UserConsent;
import org.openecard.recognition.CardRecognition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AddonManager {

    private static final Logger logger = LoggerFactory.getLogger(AddonManager.class);
    private AddonRegistry registry = CombiningRegistry.getInstance();
    private Dispatcher dispatcher;
    private UserConsent userConsent;
    private CardStateMap cardStates;
    private CardRecognition recognition;

    public AddonManager(Dispatcher dispatcher, UserConsent userConsent, CardStateMap cardStates, CardRecognition recognition) {
	this.dispatcher = dispatcher;
	this.userConsent = userConsent;
	this.cardStates = cardStates;
	this.recognition = recognition;
    }

    public AddonRegistry getRegistry() {
	return registry;
    }

    public IFDProtocol getIFDAction(String uri) {
	AddonBundleDescription addonBundleDescription = registry.searchProtocol(uri).iterator().next();
	ProtocolPluginDescription searchByResourceName = addonBundleDescription.searchSALActionByURI(uri);
	String className = searchByResourceName.getClassName();
	IFDProtocolFactory appPluginActionFactory = new IFDProtocolFactory(className, registry.downloadPlugin(addonBundleDescription.getId()));
	try {
	    appPluginActionFactory.init(new Context(dispatcher, userConsent, cardStates, recognition));
	    return appPluginActionFactory;
	} catch (FactoryInitializationException e) {
	    logger.error("Initialization of IFDAction failed", e);
	}
	return null;
    }

    public SALProtocol getSALAction(String uri) {
	AddonBundleDescription addonBundleDescription = registry.searchProtocol(uri).iterator().next();
	ProtocolPluginDescription searchByResourceName = addonBundleDescription.searchSALActionByURI(uri);
	String className = searchByResourceName.getClassName();
	SALProtocolFactory appPluginActionFactory = new SALProtocolFactory(className, registry.downloadPlugin(addonBundleDescription.getId()));
	try {
	    appPluginActionFactory.init(new Context(dispatcher, userConsent, cardStates, recognition));
	    return appPluginActionFactory;
	} catch (FactoryInitializationException e) {
	    logger.error("Initialization of SALAction failed", e);
	    return null;
	}
    }

    public AppExtensionAction getAppExtensionAction(String pluginId, String actionId) {
	AddonBundleDescription addonBundleDescription = registry.search(pluginId);
	AppExtensionActionDescription searchByResourceName = addonBundleDescription.searchByActionId(actionId);
	String className = searchByResourceName.getClassName();
	AppExtensionActionFactory appPluginActionFactory = new AppExtensionActionFactory(className, registry.downloadPlugin(pluginId));
	try {
	    appPluginActionFactory.init(new Context(dispatcher, userConsent, cardStates, recognition));
	    return appPluginActionFactory;
	} catch (FactoryInitializationException e) {
	    logger.error("Initialization of AppExtensionAction failed", e);
	    return null;
	}
    }

    public AppPluginAction getAppPluginAction(String pluginId, String resourceName) {
	AddonBundleDescription addonBundleDescription = registry.search(pluginId);
	AppPluginActionDescription searchByResourceName = addonBundleDescription.searchByResourceName(resourceName);
	String className = searchByResourceName.getClassName();
	AppPluginActionFactory appPluginActionFactory = new AppPluginActionFactory(className, registry.downloadPlugin(pluginId));
	try {
	    appPluginActionFactory.init(new Context(dispatcher, userConsent, cardStates, recognition));
	    return appPluginActionFactory;
	} catch (FactoryInitializationException e) {
	    logger.error("Initialization of AppPluginAction failed", e);
	    return null;
	}
    }

}
