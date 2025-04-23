/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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
package org.openecard.common.interfaces

import java.lang.annotation.Inherited
import kotlin.reflect.KClass

/**
 * This annotation is used in the environment to mark the return value of getters as dispatchable type.
 * When the dispatcher is initialized, it searches the environment implementation for dispatchable types. All types
 * which are found are then loaded into the dispatcher, so that messages defined for them can be dispatched at a later
 * time.
 *
 * @author Tobias Wich
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Inherited
annotation class Dispatchable(
	/**
	 * Gets the class of the webservice interface associated with this getter.
	 * The class instance must be webservice interface and all of its methods must be webservice methods.
	 *
	 * @return Class object representing the actual webservice interface, the Object class if none is set.
	 */
	val interfaceClass: KClass<*>,
)
