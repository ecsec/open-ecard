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
 */
package org.openecard.common

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.util.Promise
import java.util.concurrent.ConcurrentSkipListMap

private val logger = KotlinLogging.logger { }

/**
 * Thread local dynamic context implemented as a singleton.
 * Dynamic context information is needed at various places in the app. Perhaps the most important use case is the
 * eService certificate validation as defined in TR-03112-7.<br></br>
 * The underlying datastructure does permit `null` values to be saved.
 *
 * @author Tobias Wich
 */
class DynamicContext private constructor() {
	private val context: MutableMap<String, Promise<Any>?> =
		HashMap()

	/**
	 * Gets the promise which represents the given key.
	 * If no promise exists yet, a new one will be created. Retrieving the promise value blocks until a value is
	 * delivered to it.
	 *
	 * @param key Key for which the promise should be retrieved.
	 * @return Promise for the given key.
	 */
	@Synchronized
	fun getPromise(key: String): Promise<Any> {
		var p = context[key]
		if (p != null) {
			return p
		} else {
			p = Promise()
			context[key] = p
			return p
		}
	}

	/**
	 * Gets the value saved for the given key.
	 * The method does not block and returns null if no value is in the promise represented by this key
	 *
	 * @see Map.get
	 * @param key Key for which the value should be retrieved.
	 * @return The value for the given key, or `null` if no value is defined yet or `null` is mapped.
	 */
	fun get(key: String): Any? {
		val p = getPromise(key)
		return p.derefNonblocking()
	}

	/**
	 * Saves the given value for the given key.
	 * This method writes the value into the promise of the given key, or in case the promise already has a value,
	 * overwrites the promise and sets the value in the new instance. By doing that, the function mimics the exact
	 * behaviour of the Map class.
	 *
	 * @see Map.put
	 * @param key Key for which the value should be saved.
	 * @param value Value which should be saved for the given key.
	 * @return The previous value for key or `null` if there was no previous mapping or `null` was mapped.
	 */
	@Synchronized
	fun put(
		key: String,
		value: Any?,
	): Any? {
		var p = getPromise(key)
		if (p.isDelivered) {
			remove(key)
			p = getPromise(key)
		}
		p.deliver(value)
		return value
	}

	/**
	 * Saves the given promise so that it its result is available for later use.
	 * This function does not use the default [Promise] class and allows to override the behaviour of the
	 * Promise. An example of such a modified behaviour can be found in [FuturePromise].
	 *
	 * @see .put
	 * @param key Key for which the value should be saved.
	 * @param p Promise yielding the value which should be saved for the given key.
	 */
	@Synchronized
	fun putPromise(
		key: String,
		p: Promise<Any>,
	) {
		check(context[key] == null) { "Promise already exists and can therefore not be delivered anymore." }
		context[key] = p
	}

	/**
	 * Removes the mapping for the given key.
	 *
	 * @see Map.remove
	 * @param key Key for which to remove the mapping.
	 * @return The previous value for key or `null` if there was no previous mapping or `null` was mapped.
	 */
	fun remove(key: String): Any? {
		val p = context.remove(key)
		if (p != null) {
			return p.derefNonblocking()
		}
		return null
	}

	/**
	 * Removes all mappings from the context.
	 *
	 * @see Map.clear
	 */
	fun clear() {
		context.clear()
	}

	companion object {
		@Suppress("ktlint:standard:property-naming")
		private var LOCAL_MAP: InheritableThreadLocal<MutableMap<String, DynamicContext>> =
			object :
				InheritableThreadLocal<MutableMap<String, DynamicContext>>() {
				override fun initialValue(): MutableMap<String, DynamicContext> = ConcurrentSkipListMap()

				override fun childValue(parentValue: MutableMap<String, DynamicContext>): MutableMap<String, DynamicContext> =
					parentValue
			}

		/**
		 * Gets the thread local instance of the context.
		 * If no instance exists yet, a new one is created possibly based on the one from the parent thread.
		 *
		 * @param key Lookup key for the desired variable.
		 * @return The DynamicContext instance of this thread.
		 */
		@JvmStatic
		fun getInstance(key: String): DynamicContext? {
			val local: MutableMap<String, DynamicContext> = LOCAL_MAP.get()
			synchronized(local) {
				val inst: DynamicContext?
				if (local.containsKey(key)) {
					inst = local[key]
				} else {
					inst = DynamicContext()
					local[key] = inst
				}
				return inst
			}
		}

		/**
		 * Removes the value from this thread.
		 * This does not clear the values saved in the context, it just makes the context inaccessible for further invocations
		 * of the [.getInstance] method.
		 *
		 * @see ThreadLocal.remove
		 */
		@JvmStatic
		@Synchronized
		fun remove() {
			logger.debug { "${"Removing DynamicContext which contains {} map entries."} ${LOCAL_MAP.get().size}" }
			LOCAL_MAP.remove()
		}
	}
}
