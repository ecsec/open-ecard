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
 ***************************************************************************/

package org.openecard.addon;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collection;
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
import org.openecard.addon.sal.SalStateView;
import org.openecard.common.util.FacadeInvocationHandler;
import org.openecard.common.interfaces.Environment;
import org.openecard.gui.UserConsent;
import org.openecard.gui.definition.ViewController;
import org.openecard.ws.marshal.WSMarshallerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
public class AddonManager {

    private static final Logger LOG = LoggerFactory.getLogger(AddonManager.class);

    private final CombiningRegistry registry;
    private final AddonRegistry protectedRegistry;
    private final Environment env;
    private final UserConsent userConsent;
    private final EventHandler eventHandler;
    private final ViewController viewController;
    private final SalStateView salStateView;
    // TODO: rework cache to have borrow and return semantic
    private final Cache cache = new Cache();

    /**
     * Creates a new AddonManager.
     *
     * @param env
     * @param userConsent
     * @param view
     * @param registry
     * @param salStateView
     * @throws WSMarshallerException
     */
    public AddonManager(Environment env, UserConsent userConsent, ViewController view,
	    CombiningRegistry registry,
	    SalStateView salStateView) throws WSMarshallerException {

	if (registry == null) {
	    this.registry = new ClasspathAndFileRegistry(this);
	} else {
	    this.registry = registry;
	}
	this.protectedRegistry = getProtectedRegistry(this.registry);
	this.env = env;
	this.userConsent = userConsent;
	this.eventHandler = new EventHandler();
	this.env.getEventDispatcher().add(eventHandler);
	this.viewController = view;
	this.salStateView = salStateView;

	new Thread(() -> {
	    loadLoadOnStartAddons();
	}, "Init-Addons").start();
    }

    public AddonManager(Environment env, UserConsent userConsent, ViewController view, SalStateView salStateView)
	    throws WSMarshallerException {
	this(env, userConsent, view, null, salStateView);
    }

    /**
     * Load all addons which contain an loadOnStart = true.
     *
     * @throws WSMarshallerException
     */
    private void loadLoadOnStartAddons() {
	// load plugins which have an loadOnStartup = true
	Set<AddonSpecification> specs = protectedRegistry.listAddons();
	for (AddonSpecification addonSpec : specs) {
	    loadLoadOnStartupActions(addonSpec);
	}
    }

    /**
     * Load a single addon which contains a LoadOnStartup = true.
     *
     * @param addonSpec The {@link AddonSpecification} of the addon.
     */
    protected void loadLoadOnStartupActions(AddonSpecification addonSpec) {
	if (!addonSpec.getApplicationActions().isEmpty()) {
	    for (AppExtensionSpecification appExSpec : addonSpec.getApplicationActions()) {
		if (appExSpec.isLoadOnStartup()) {
		    getAppExtensionAction(addonSpec, appExSpec.getId());
		}
	    }
	}

	if (!addonSpec.getBindingActions().isEmpty()) {
	    for (AppPluginSpecification appPlugSpec : addonSpec.getBindingActions()) {
		if (appPlugSpec.isLoadOnStartup()) {
		    getAppPluginAction(addonSpec, appPlugSpec.getResourceName());
		}
	    }
	}

	if (!addonSpec.getIfdActions().isEmpty()) {
	    for (ProtocolPluginSpecification protPlugSpec : addonSpec.getIfdActions()) {
		if (protPlugSpec.isLoadOnStartup()) {
		    getIFDProtocol(addonSpec, protPlugSpec.getUri());
		}
	    }
	}

	if (!addonSpec.getSalActions().isEmpty()) {
	    for (ProtocolPluginSpecification protPlugSpec : addonSpec.getSalActions()) {
		if (protPlugSpec.isLoadOnStartup()) {
		    getSALProtocol(addonSpec, protPlugSpec.getUri());
		}
	    }
	}
    }

    /**
     * Unload all add-ons.
     */
    private void unloadAllAddons() {
	Set<AddonSpecification> addons = protectedRegistry.listInstalledAddons();
	for (AddonSpecification addonSpec : addons) {
	    unloadAddon(addonSpec);
	}
    }

    /**
     * Unload all actions and protocols of a specific add-on.
     *
     * @param addonSpec The {@link AddonSpecification} of the add-on to unload.
     */
    protected void unloadAddon(AddonSpecification addonSpec) {
	Collection<? extends LifecycleTrait> actionsAndProtocols = cache.getAllAddonData(addonSpec);
	for (LifecycleTrait obj : actionsAndProtocols) {
	    obj.destroy(true);
	}

	cache.removeCompleteAddonCache(addonSpec);
    }


    /**
     * This method returns an instance of the given registry where only the interface methods are accessible.
     *
     * @param registry Unprotected registry instance.
     * @return Protected registry instance.
     */
    private static AddonRegistry getProtectedRegistry(AddonRegistry registry) {
	if (registry != null) {
	    ClassLoader cl = AddonManager.class.getClassLoader();
	    Class<?>[] interfaces = new Class<?>[] { AddonRegistry.class };
	    InvocationHandler handler = new FacadeInvocationHandler(registry);
	    Object o = Proxy.newProxyInstance(cl, interfaces, handler);
	    return (AddonRegistry) o;
	}
	return null;
    }

    /**
     * Get the CombiningRigistry.
     *
     * @return A {@link AddonRegistry} object which provides access just to the interface methods of the
     * {@link ClasspathAndFileRegistry}.
     */
    public AddonRegistry getRegistry() {
	return protectedRegistry;
    }

    /**
     * Get the ClasspathRegistry.
     *
     * @return A {@link AddonRegistry} object which provides access just to the interface methods of the
     * {@link ClasspathRegistry}.
     */
    public AddonRegistry getBuiltinRegistry() {
	return getProtectedRegistry(registry.getClasspathRegistry());
    }

    /**
     * Get the FileRegistry.
     *
     * @return A {@link AddonRegistry} object which provides access just to the interface methods of the
     * {@link FileRegistry}.
     */
    public AddonRegistry getExternalRegistry() {
	return getProtectedRegistry(registry.getFileRegistry());
    }

    /**
     * Register a new add-on which is located in the class path.
     *
     * @param desc {@link AddonSpecification} of the add-on which shall be registered.
     */
    public void registerClasspathAddon(AddonSpecification desc) {
	// TODO: protect this method from the sandbox
	this.registry.getClasspathRegistry().register(desc);
    }

    /**
     * Get a specific IFDProtocol.
     *
     * @param addonSpec {@link AddonSpecification} which contains the description of the {@link IFDProtocol}.
     * @param uri The {@link ProtocolPluginSpecification#uri} to identify the requested IFDProtocol.
     * @return The requested IFDProtocol object or NULL if no such object was found.
     */
    public IFDProtocol getIFDProtocol(@Nonnull AddonSpecification addonSpec, @Nonnull String uri) {
	IFDProtocol ifdProt = cache.getIFDProtocol(addonSpec, uri);
	// TODO: find a better way to deal with the reuse of protocol plugins
//	if (ifdProt != null) {
//	    // protocol cached so return it
//	    return ifdProt;
//	}

	ProtocolPluginSpecification protoSpec = addonSpec.searchIFDActionByURI(uri);
	if (protoSpec == null) {
	    LOG.error("Requested IFD Protocol {} does not exist in Add-on {}.", uri, addonSpec.getId());
	} else {
	    String className = protoSpec.getClassName();
	    try {
		ClassLoader cl = registry.downloadAddon(addonSpec);
		IFDProtocolProxy protoFactory = new IFDProtocolProxy(className, cl);
		Context aCtx = createContext(addonSpec);
		protoFactory.init(aCtx);
		cache.addIFDProtocol(addonSpec, uri, protoFactory);
		return protoFactory;
	    } catch (ActionInitializationException e) {
		LOG.error("Initialization of IFD Protocol failed", e);
	    } catch (AddonException ex) {
		LOG.error("Failed to download Add-on.", ex);
	    }
	}
	return null;
    }

    public void returnIFDProtocol(IFDProtocol obj) {
	obj.destroy(false);
    }

    /**
     * Get a specific SALProtocol.
     *
     * @param addonSpec {@link AddonSpecification} which contains the description of the {@link SALProtocol}.
     * @param uri The {@link ProtocolPluginSpecification#uri} to identify the requested SALProtocol.
     * @return The requested SALProtocol object or NULL if no such object was found.
     */
    public SALProtocol getSALProtocol(@Nonnull AddonSpecification addonSpec, @Nonnull String uri) {
	SALProtocol salProt = cache.getSALProtocol(addonSpec, uri);
	// TODO: find a better way to deal with the reuse of protocol plugins
//	if (salProt != null) {
//	    // protocol cached so return it
//	    return salProt;
//	}

	ProtocolPluginSpecification protoSpec = addonSpec.searchSALActionByURI(uri);
	if (protoSpec == null) {
	    LOG.error("Requested SAL Protocol {} does not exist in Add-on {}.", uri, addonSpec.getId());
	} else {
	    String className = protoSpec.getClassName();
	    try {
		ClassLoader cl = registry.downloadAddon(addonSpec);
		SALProtocolProxy protoFactory = new SALProtocolProxy(className, cl);
		Context aCtx = createContext(addonSpec);
		protoFactory.init(aCtx);
		cache.addSALProtocol(addonSpec, uri, protoFactory);
		return protoFactory;
	    } catch (ActionInitializationException e) {
		LOG.error("Initialization of SAL Protocol failed", e);
	    } catch (AddonException ex) {
		LOG.error("Failed to download Add-on.", ex);
	    }
	}
	return null;
    }

    public void returnSALProtocol(SALProtocol obj, boolean force) {
	obj.destroy(force);
    }

    /**
     * Get a specific AppExtensionAction.
     *
     * @param addonSpec {@link AddonSpecification} which contains the description of the {@link AppExtensionAction}.
     * @param actionId	The {@link AppExtensionSpecification#id} to identify the requested AppExtensionAction.
     * @return The AppExtensionAction which corresponds the given {@code actionId} or NULL if no AppExtensionAction with
     * the given {@code actionId} exists.
     */
    public AppExtensionAction getAppExtensionAction(@Nonnull AddonSpecification addonSpec, @Nonnull String actionId) {
	// get extension from cache
	AppExtensionAction appExtAction = cache.getAppExtensionAction(addonSpec, actionId);
	// TODO: find a better way to deal with the reuse of protocol plugins
//	if (appExtAction != null) {
//	    // AppExtensionAction cached so return it
//	    return appExtAction;
//	}

	AppExtensionSpecification protoSpec = addonSpec.searchByActionId(actionId);
	if (protoSpec == null) {
	    LOG.error("Requested Extension {} does not exist in Add-on {}.", actionId, addonSpec.getId());
	} else {
	    String className = protoSpec.getClassName();
	    try {
		ClassLoader cl = registry.downloadAddon(addonSpec);
		AppExtensionActionProxy protoFactory = new AppExtensionActionProxy(className, cl);
		Context aCtx = createContext(addonSpec);
		protoFactory.init(aCtx);
		cache.addAppExtensionAction(addonSpec, actionId, protoFactory);
		return protoFactory;
	    } catch (ActionInitializationException e) {
		LOG.error("Initialization of AppExtensionAction failed", e);
	    } catch (AddonException ex) {
		LOG.error("Failed to download Add-on.", ex);
	    }
	}
	return null;
    }

    public void returnAppExtensionAction(AppExtensionAction obj) {
	obj.destroy(false);
    }

    /**
     * Get a specific AppPluginAction.
     *
     * @param addonSpec {@link AddonSpecification} which contains the description of the {@link AppPluginAction}.
     * @param resourceName The {@link AppPluginSpecification#resourceName} to identify the @{@link AppPluginAction} to
     * return.
     * @return A AppPluginAction which corresponds to the {@link AddonSpecification} and the {@code resourceName}. If no
     * such AppPluginAction exists NULL is returned.
     */
    public AppPluginAction getAppPluginAction(@Nonnull AddonSpecification addonSpec, @Nonnull String resourceName) {
	AppPluginAction appPluginAction = cache.getAppPluginAction(addonSpec, resourceName);
	// TODO: find a better way to deal with the reuse of protocol plugins
//	if (appPluginAction != null) {
//	    // AppExtensionAction cached so return it
//	    return appPluginAction;
//	}

	AppPluginSpecification protoSpec = addonSpec.searchByResourceName(resourceName);
	if (protoSpec == null) {
	    LOG.error("Plugin for resource {} does not exist in Add-on {}.", resourceName, addonSpec.getId());
	} else {
	    String className = protoSpec.getClassName();
	    try {
		ClassLoader cl = registry.downloadAddon(addonSpec);
		AppPluginActionProxy protoFactory = new AppPluginActionProxy(className, cl);
		Context aCtx = createContext(addonSpec);
		protoFactory.init(aCtx);
		cache.addAppPluginAction(addonSpec, resourceName, protoFactory);
		return protoFactory;
	    } catch (ActionInitializationException e) {
		LOG.error("Initialization of AppPluginAction failed", e);
	    } catch (AddonException ex) {
		LOG.error("Failed to download Add-on.", ex);
	    }
	}
	return null;
    }

    public void returnAppPluginAction(AppPluginAction obj) {
	obj.destroy(false);
    }

    private Context createContext(@Nonnull AddonSpecification addonSpec) {
	Context aCtx = new Context(this, env, addonSpec, viewController, salStateView);
	aCtx.setCardRecognition(env.getRecognition());
	aCtx.setEventHandle(eventHandler);
	aCtx.setUserConsent(userConsent);

	return aCtx;
    }

    /**
     * Shut down the AddonManager.
     * The method unloads all installed add-ons.
     */
    public void shutdown() {
	unloadAllAddons();
    }

    /**
     * Uninstall an add-on.
     * This is primarily a wrapper method for the {@link FileRegistry#uninstallAddon(AddonSpecification)}
     *
     * @param addonSpec The specification of the add-on to uninstall.
     */
    public void uninstallAddon(@Nonnull AddonSpecification addonSpec) {
	// unloading is done by the PluginDirectoryAlterationListener
	FileRegistry fileRegistry = registry.getFileRegistry();
	if (fileRegistry != null) {
	    fileRegistry.uninstallAddon(addonSpec);
	}
    }

}
