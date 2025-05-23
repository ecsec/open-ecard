/****************************************************************************
 * Copyright (C) 2013-2024 ecsec GmbH.
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

package org.openecard.common.util

/**
 * Collection of utility functions for exception processing.
 *
 * @author Tobias Wich
 */
object ExceptionUtils {
	/**
	 * Checks the given exception's cause path for the given types.
	 * The types are checked in reverse order, meaning the last given type is the expected type of first cause. When the
	 * cause path is as expected, the returned object is cast to the expected type.
	 *
	 * The function works when the given types are not [Throwable] classes, however in that case it will never
	 * return an object.
	 *
	 * @param <T> Return type of the expected Throwable object.
	 * @param <C> Typed class of the expected Throwable object.
	 * @param ex The root exception whose cause path should be checked.
	 * @param returnClass Typed class of the expected Throwable object.
	 * @param cs Classes of the Throwables in between the path.
	 * @return The expected Throwable object, or null if the path is not correctly typed or does not exist.
	 */
	@JvmStatic
	fun <T : Throwable, C : Class<T>> matchPath(
		ex: Throwable,
		returnClass: C,
		vararg cs: Class<*>,
	): T? {
		// TODO: optimize reverse processing
		var curEx = ex
		val classList = listOf(*cs).reversed()
		// trace back exceptions in list
		for (next in classList) {
			val nextEx = curEx.cause
			if (!(nextEx != null && next.isInstance(nextEx))) {
				// instance not of the desired type or not existing
				return null
			}
			curEx = nextEx
		}
		// check last instance for the desired type
		val nextEx = curEx.cause
		if (!(nextEx != null && returnClass.isInstance(nextEx))) {
			// instance not of the desired type or not existing
			return null
		}
		return returnClass.cast(nextEx)
	}
}
