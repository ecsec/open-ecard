/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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

package org.openecard.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.common.util.FuturePromise;
import org.openecard.common.util.Promise;


/**
 * Thread local dynamic context implemented as a singleton.
 * Dynamic context information is needed at various places in the app. Perhaps the most important use case is the
 * eService certificate validation as defined in TR-03112-7.<br>
 * The underlying datastructure does permit {@code null} values to be saved.
 *
 * @author Tobias Wich
 */
public class DynamicContext {

    private static final InheritableThreadLocal<Map<String, DynamicContext>> LOCAL_MAP;

    private final Map<String, Promise<Object>> context;


    static {
	LOCAL_MAP = new InheritableThreadLocal<Map<String, DynamicContext>>() {
	    @Override
	    protected Map<String, DynamicContext> initialValue() {
		return new ConcurrentSkipListMap<>();
	    }
	    @Override
	    protected Map<String, DynamicContext> childValue(Map<String, DynamicContext> parentValue) {
		return parentValue;
	    }
	};
    }

    /**
     * Gets the thread local instance of the context.
     * If no instance exists yet, a new one is created possibly based on the one from the parent thread.
     *
     * @param key Lookup key for the desired variable.
     * @return The DynamicContext instance of this thread.
     */
    @Nonnull
    public static DynamicContext getInstance(@Nonnull String key) {
	final Map<String, DynamicContext> local = LOCAL_MAP.get();
	synchronized (local) {
	    DynamicContext inst;
	    if (local.containsKey(key)) {
		inst = local.get(key);
	    } else {
		inst = new DynamicContext();
		local.put(key, inst);
	    }
	    return inst;
	}
    }

    /**
     * Removes the value from this thread.
     * This first clears the values saved in the context, and makes the context inaccessible for further invocations
     * of the {@link #getInstance(String)} method.
     *
     * @see ThreadLocal#remove()
     */
    public static synchronized void remove() {
	LOCAL_MAP.get().clear();
	LOCAL_MAP.remove();
    }


    private DynamicContext() {
	this.context = new HashMap<>();
    }


    /**
     * Gets the promise which represents the given key.
     * If no promise exists yet, a new one will be created. Retrieving the promise value blocks until a value is
     * delivered to it.
     *
     * @param key Key for which the promise should be retrieved.
     * @return Promise for the given key.
     */
    public synchronized @Nonnull Promise<Object> getPromise(@Nonnull String key) {
	Promise<Object> p = context.get(key);
	if (p != null) {
	    return p;
	} else {
	    p = new Promise<>();
	    context.put(key, p);
	    return p;
	}
    }

    /**
     * Gets the value saved for the given key.
     * The method does not block and returns null if no value is in the promise represented by this key
     *
     * @see Map#get(java.lang.Object)
     * @param key Key for which the value should be retrieved.
     * @return The value for the given key, or {@code null} if no value is defined yet or {@code null} is mapped.
     */
    public @Nullable Object get(@Nonnull String key) {
	Promise<Object> p = getPromise(key);
	return p.derefNonblocking();
    }

    /**
     * Saves the given value for the given key.
     * This method writes the value into the promise of the given key, or in case the promise already has a value,
     * overwrites the promise and sets the value in the new instance. By doing that, the function mimics the exact
     * behaviour of the Map class.
     *
     * @see Map#put(java.lang.Object, java.lang.Object)
     * @param key Key for which the value should be saved.
     * @param value Value which should be saved for the given key.
     * @return The previous value for key or {@code null} if there was no previous mapping or {@code null} was mapped.
     */
    @Nullable
    public synchronized Object put(@Nonnull String key, @Nullable Object value) {
	Promise<Object> p = getPromise(key);
	if (p.isDelivered()) {
	    remove(key);
	    p = getPromise(key);
	}
	p.deliver(value);
	return value;
    }

    /**
     * Saves the given promise so that it its result is available for later use.
     * This function does not use the default {@link Promise} class and allows to override the behaviour of the
     * Promise. An example of such a modified behaviour can be found in {@link FuturePromise}.
     *
     * @see #put(java.lang.String, java.lang.Object)
     * @param key Key for which the value should be saved.
     * @param p Promise yielding the value which should be saved for the given key.
     */
    public synchronized void putPromise(@Nonnull String key, @Nonnull Promise p) {
	if (context.get(key) != null) {
	    throw new IllegalStateException("Promise already exists and can therefore not be delivered anymore.");
	} else {
	    context.put(key, p);
	}
    }

    /**
     * Removes the mapping for the given key.
     *
     * @see Map#remove(java.lang.Object)
     * @param key Key for which to remove the mapping.
     * @return The previous value for key or {@code null} if there was no previous mapping or {@code null} was mapped.
     */
    @Nullable
    public Object remove(@Nonnull String key) {
	Promise<Object> p = context.remove(key);
	if (p != null) {
	    return p.derefNonblocking();
	}
	return null;
    }

    /**
     * Removes all mappings from the context.
     *
     * @see Map#clear()
     */
    public void clear() {
	context.clear();
    }

}
