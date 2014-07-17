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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Set;
import javax.annotation.Nonnull;
import org.openecard.addon.bind.AppExtensionAction;
import org.openecard.addon.bind.AppExtensionActionProxy;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.AppPluginActionProxy;
import org.openecard.addon.ifd.IFDProtocol;
import org.openecard.addon.ifd.IFDProtocolProxy;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.addon.manifest.AppExtensionSpecification;
import org.openecard.addon.manifest.AppPluginSpecification;
import org.openecard.addon.manifest.ProtocolPluginSpecification;
import org.openecard.addon.sal.SALProtocol;
import org.openecard.addon.sal.SALProtocolProxy;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.EventManager;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.common.util.FacadeInvocationHandler;
import org.openecard.gui.UserConsent;
import org.openecard.recognition.CardRecognition;
import org.openecard.ws.marshal.WSMarshallerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class AddonManager {

    private static final Logger logger = LoggerFactory.getLogger(AddonManager.class);

    private final CombiningRegistry registry;
    private final AddonRegistry protectedRegistry;
    private final Dispatcher dispatcher;
    private final UserConsent userConsent;
    private final CardStateMap cardStates;
    private final CardRecognition recognition;
    private final EventManager eventManager;
    private final EventHandler eventHandler;

    public AddonManager(Dispatcher dispatcher, UserConsent userConsent, CardStateMap cardStates,
	    CardRecognition recognition, EventManager eventManager) throws WSMarshallerException {

	this.registry = new CombiningRegistry();
	this.protectedRegistry = getProtectedRegistry(registry);
	this.dispatcher = dispatcher;
	this.userConsent = userConsent;
	this.cardStates = cardStates;
	this.recognition = recognition;
	this.eventManager = eventManager;
	this.eventHandler = new EventHandler(eventManager);

	loadLoadOnStartAddons();
    }

    /**
     * The method loads all addons which contain an loadOnStart = true.
     *
     * @throws WSMarshallerException
     */
    private void loadLoadOnStartAddons() throws WSMarshallerException {
	// load plugins which have an loadOnStartup = true
	Set<AddonSpecification> specs = protectedRegistry.listAddons();
	for (AddonSpecification addonSpec : specs) {
	    if (! addonSpec.getApplicationActions().isEmpty()) {
		for (AppExtensionSpecification appExSpec : addonSpec.getApplicationActions()) {
		    if (appExSpec.isLoadOnStartup()) {
			getAppExtensionAction(addonSpec, appExSpec.getId());
		    }
		}
	    }

	    if (! addonSpec.getBindingActions().isEmpty()) {
		for (AppPluginSpecification appPlugSpec : addonSpec.getBindingActions()) {
		    if (appPlugSpec.isLoadOnStartup()) {
			getAppPluginAction(addonSpec, appPlugSpec.getResourceName());
		    }
		}
	    }

	    if (! addonSpec.getIfdActions().isEmpty()) {
		for (ProtocolPluginSpecification protPlugSpec : addonSpec.getIfdActions()) {
		    if (protPlugSpec.isLoadOnStartup()) {
			getIFDProtocol(addonSpec, protPlugSpec.getUri());
		    }
		}
	    }

	    if (! addonSpec.getSalActions().isEmpty()) {
		for (ProtocolPluginSpecification protPlugSpec : addonSpec.getSalActions()) {
		    if (protPlugSpec.isLoadOnStartup()) {
			getSALProtocol(addonSpec, protPlugSpec.getUri());
		    }
		}
	    }
	}
    }

    /**
     * This method returns an instance of the given registry where only the interface methods are accessible.
     *
     * @param registry Unprotected registry instance.
     * @return Protected registry instance.
     */
    private static AddonRegistry getProtectedRegistry(AddonRegistry registry) {
	ClassLoader cl = AddonManager.class.getClassLoader();
	Class<?>[] interfaces = new Class<?>[] { AddonRegistry.class };
	InvocationHandler handler = new FacadeInvocationHandler(registry);
	Object o = Proxy.newProxyInstance(cl, interfaces, handler);
	return (AddonRegistry) o;
    }

    public AddonRegistry getRegistry() {
	return protectedRegistry;
    }

    public AddonRegistry getBuiltinRegistry() {
	return getProtectedRegistry(registry.getClasspathRegistry());
    }
    public AddonRegistry getExternalRegistry() {
	return getProtectedRegistry(registry.getFileRegistry());
    }

    public void registerClasspathAddon(AddonSpecification desc) {
	// TODO: protect this method from the sandbox
	this.registry.getClasspathRegistry().register(desc);
    }

    public IFDProtocol getIFDProtocol(@Nonnull AddonSpecification addonSpec, @Nonnull String uri) {
	ProtocolPluginSpecification protoSpec = addonSpec.searchIFDActionByURI(uri);
	String className = protoSpec.getClassName();
	ClassLoader cl = registry.downloadAddon(addonSpec);
	IFDProtocolProxy protoFactory = new IFDProtocolProxy(className, cl);
	try {
	    Context aCtx = new Context(this, dispatcher, eventManager, addonSpec);
	    aCtx.setCardRecognition(recognition);
	    aCtx.setCardStateMap(cardStates);
	    aCtx.setEventHandle(eventHandler);
	    aCtx.setUserConsent(userConsent);
	    protoFactory.init(aCtx);
	    return protoFactory;
	} catch (ActionInitializationException e) {
	    logger.error("Initialization of IFD Protocol failed", e);
	}
	return null;
    }

    public SALProtocol getSALProtocol(@Nonnull AddonSpecification addonSpec, @Nonnull String uri) {
	ProtocolPluginSpecification protoSpec = addonSpec.searchSALActionByURI(uri);
	String className = protoSpec.getClassName();
	ClassLoader cl = registry.downloadAddon(addonSpec);
	SALProtocolProxy protoFactory = new SALProtocolProxy(className, cl);
	try {
	    Context aCtx = new Context(this, dispatcher, eventManager, addonSpec);
	    aCtx.setCardRecognition(recognition);
	    aCtx.setCardStateMap(cardStates);
	    aCtx.setEventHandle(eventHandler);
	    aCtx.setUserConsent(userConsent);
	    protoFactory.init(aCtx);
	    return protoFactory;
	} catch (ActionInitializationException e) {
	    logger.error("Initialization of SAL Protocol failed", e);
	}
	return null;
    }

    public AppExtensionAction getAppExtensionAction(@Nonnull AddonSpecification addonSpec, @Nonnull String actionId) {
	AppExtensionSpecification protoSpec = addonSpec.searchByActionId(actionId);
	String className = protoSpec.getClassName();
	ClassLoader cl = registry.downloadAddon(addonSpec);
	AppExtensionActionProxy protoFactory = new AppExtensionActionProxy(className, cl);
	try {
	    Context aCtx = new Context(this, dispatcher, eventManager, addonSpec);
	    aCtx.setCardRecognition(recognition);
	    aCtx.setCardStateMap(cardStates);
	    aCtx.setEventHandle(eventHandler);
	    aCtx.setUserConsent(userConsent);
	    protoFactory.init(aCtx);
	    return protoFactory;
	} catch (ActionInitializationException e) {
	    logger.error("Initialization of AppExtensionAction failed", e);
	}
	return null;
    }

    public AppPluginAction getAppPluginAction(@Nonnull AddonSpecification addonSpec, @Nonnull String resourceName) {
	AppPluginSpecification protoSpec = addonSpec.searchByResourceName(resourceName);
	String className = protoSpec.getClassName();
	ClassLoader cl = registry.downloadAddon(addonSpec);
	AppPluginActionProxy protoFactory = new AppPluginActionProxy(className, cl);
	try {
	    Context aCtx = new Context(this, dispatcher, eventManager, addonSpec);
	    aCtx.setCardRecognition(recognition);
	    aCtx.setCardStateMap(cardStates);
	    aCtx.setEventHandle(eventHandler);
	    aCtx.setUserConsent(userConsent);
	    protoFactory.init(aCtx);
	    return protoFactory;
	} catch (ActionInitializationException e) {
	    logger.error("Initialization of AppPluginAction failed", e);
	}
	return null;
    }

}
