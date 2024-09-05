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
 */
package org.openecard.addon

import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.reflect.InvocationTargetException

private val logger = KotlinLogging.logger {}

/**
 *
 * @param <C>
 * @author Dirk Petrautzki
 */
open class AbstractFactory<C : LifecycleTrait>(private val implClass: String, private val classLoader: ClassLoader) {
    @Throws(ActionInitializationException::class)
    protected fun loadInstance(ctx: Context, clazz: Class<C>): C {
        try {
            val classToLoad = classLoader.loadClass(implClass)
            val typedClass = classToLoad.asSubclass(clazz)
            val ctor = typedClass.getConstructor()
            ctor.isAccessible = true
            val c = typedClass.cast(ctor.newInstance())
            c.init(ctx)
            return c
        } catch (e: InstantiationException) {
            logger.error(e) { "Given class could not be instantiated." }
            throw ActionInitializationException(e)
        } catch (e: InvocationTargetException) {
            logger.error(e) { "Exception in nullary constructor of given class." }
            throw ActionInitializationException(e)
        } catch (e: IllegalAccessException) {
            logger.error(e) { "The class or its nullary constructor is not accessible." }
            throw ActionInitializationException(e)
        } catch (e: ClassNotFoundException) {
            logger.error(e) { "Given class could not be found." }
            throw ActionInitializationException(e)
        } catch (e: ClassCastException) {
            logger.error(e) { "Given class does not extend FactoryBaseType." }
            throw ActionInitializationException(e)
        } catch (e: NoSuchMethodException) {
            logger.error(e) { "Default constructor does not exist in action implementation." }
            throw ActionInitializationException(e)
        }
    }

}
