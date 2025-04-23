/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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
package org.openecard.ws.common

import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.util.*

/**
 * Generic factory capable of creating instances for a type defined in a config file.
 * The config file must be present in Java properties form and the key naming the class of the type that is to be
 * created must be known.
 *
 * @param T Type the factory creates instances for.
 *
 * @author Tobias Wich
 */
class GenericFactory<T>
	@Throws(GenericFactoryException::class)
	constructor(
		private val typeClass: Class<T>,
		properties: Properties,
		key: String,
	) : GenericInstanceProvider<T> {
		private val actualClass: Class<out T>
		private val constructor: Constructor<out T>

		init {
			val className =
				properties.getProperty(key)
					?: throw GenericFactoryException("No factory class defined for the specified key '$key'.")

			try {
				actualClass = loadClass(className)
				constructor = getConstructor(actualClass)
			} catch (ex: ClassNotFoundException) {
				throw GenericFactoryException(ex)
			} catch (ex: NoSuchMethodException) {
				throw GenericFactoryException(ex)
			}
		}

		@get:Throws(GenericFactoryException::class)
		override val instance: T
			get() {
				try {
					val o = constructor.newInstance() // default constructor
					return o // type is asserted by method definition
				} catch (ex: InstantiationException) {
					throw GenericFactoryException(ex)
				} catch (ex: IllegalAccessException) {
					throw GenericFactoryException(ex)
				} catch (ex: IllegalArgumentException) {
					throw GenericFactoryException(ex)
				} catch (ex: InvocationTargetException) {
					throw GenericFactoryException(ex)
				}
			}

		@Throws(GenericFactoryException::class, NoSuchMethodException::class)
		private fun getConstructor(clazz: Class<out T>): Constructor<out T> {
			val m = clazz.getConstructor()
			if (Modifier.isPublic(m.modifiers)) {
				return m
			} else {
				val msg = String.format("Constructor of class %s is not publicly available.", clazz.name)
				throw GenericFactoryException(msg)
			}
		}

		@Throws(ClassNotFoundException::class, GenericFactoryException::class)
		private fun loadClass(className: String): Class<out T> {
			val c = Class.forName(className)
			try {
				val c2 = c.asSubclass(typeClass)
				return c2
			} catch (ex: ClassCastException) {
				val msg = String.format("Referenced class %s is not a compatible subtype for this factory.", c.name)
				throw GenericFactoryException(msg)
			}
		}
	}
