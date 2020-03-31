/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package skid.mob.impl.auth;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import skid.mob.lib.AuthModuleCallback;
import skid.mob.lib.AuthModuleEacCallback;
import skid.mob.lib.EacModule;
import skid.mob.lib.AuthModuleCallbackBuilder;


/**
 *
 * @author Tobias Wich
 */
public final class AuthModuleCallbackBuilderImpl implements AuthModuleCallbackBuilder {

    private static final Logger LOG;
    private static final HashMap<Class<?>, Object> DEFAULTS;

    static {
	LOG = LoggerFactory.getLogger(AuthModuleCallbackBuilder.class);
	DEFAULTS = new HashMap<>();

	addDefault(AuthModuleEacCallback.class, m -> {
	    LOG.warn("AuthModuleCallback invoked with EAC module.");
	});
    }

    private final HashMap<Class<?>, Object> callbacks;

    public AuthModuleCallbackBuilderImpl() {
	this.callbacks = new HashMap<>();
    }

    private static <T> void addDefault(Class<T> cls, T impl) {
	DEFAULTS.put(cls, impl);
    }

    @Override
    public AuthModuleCallbackBuilder setCallback(AuthModuleEacCallback cb) {
	callbacks.put(AuthModuleEacCallback.class, cb);
	return this;
    }

    @Override
    public AuthModuleCallback build() {
	// copy the set of callbacks, to protect implementation against further modification of the builder
	HashMap<Class<?>, Object> implCopy = new HashMap<>(callbacks);

	// authmodule callback implementation with NOOP defaults
	return new AuthModuleCallback() {
	    private <T> T getOrDefault(Class<T> cls) {
		Object o = implCopy.get(cls);
		// use default if no implementation is set
		if (o == null) {
		    o = DEFAULTS.get(cls);
		}
		return (T) o;
	    }

	    @Override
	    public void doAuth(EacModule m) {
		getOrDefault(AuthModuleEacCallback.class).doAuth(m);
	    }
	};
    }

}
