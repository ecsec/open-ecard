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
package org.openecard.common.util

import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 *
 * @author Tobias Wich
 */
class FacadeInvocationHandler(
	private val target: Any,
) : InvocationHandler {
	override fun invoke(
		proxy: Any,
		method: Method,
		args: Array<Any>?,
	): Any {
		try {
			return method.invoke(target, *(args ?: arrayOf()))
		} catch (ex: InvocationTargetException) {
			throw ex.cause!!
		}
	}
}
