/****************************************************************************
 * Copyright (C) 2012-2013 ecsec GmbH.
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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * Thread local dynamic context implemented as a singleton.
 * Dynamic context information is needed at various places in the app. Perhaps the most important use case is the
 * eService certificate validation as defined in TR-03112-7.<br/>
 * The underlying datastructure does permit {@null null} values to be saved.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class DynamicContext {

    private static final String prefix = "org.openecard.common.DynamicContext.";

    public static final String ESERVICE_CERTIFICATE = prefix + "eservice_certificate";

    private static InheritableThreadLocal<DynamicContext> local;
    static {
	// thread local which initializes a new instance per thread, copying the values from the parent thread
	local = new InheritableThreadLocal<DynamicContext>() {
	    @Override
	    protected DynamicContext initialValue() {
		return new DynamicContext();
	    }
	    @Override
	    protected DynamicContext childValue(DynamicContext parentValue) {
		return new DynamicContext(parentValue);
	    }
	};
    }

    /**
     * Gets the thread local instance of the context.
     * If no instance exists yet, a new one is created possibly based on the one from the parent thread.
     *
     * @return The DynamicContext instance of this thread.
     */
    @Nonnull
    public static DynamicContext getInstance() {
	return local.get();
    }

    private final Map<String, Object> context;

    private DynamicContext() {
	this.context = new HashMap<String, Object>();
    }
    private DynamicContext(DynamicContext parent) {
	this.context = new HashMap<String, Object>(parent.context);
    }


    /**
     * Gets the value saved for the given key.
     *
     * @see Map#get(java.lang.Object)
     * @param key Key for which the value should be retrieved.
     * @return The value for the given key, or {@code null} if no value is defined yet or {@code null} is mapped.
     */
    public @Nullable Object get(@Nonnull String key) {
	return context.get(key);
    }

    /**
     * Saves the given value for the given key.
     *
     * @see Map#put(java.lang.Object, java.lang.Object)
     * @param key Key for which the value should be saved.
     * @param value Value which should be saved for the given key.
     * @return The previous value for key or {@code null} if there was no previous mapping or {@code null} was mapped.
     */
    @Nullable
    public Object put(@Nonnull String key, @Nullable Object value) {
	return context.put(key, value);
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
	return context.remove(key);
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
