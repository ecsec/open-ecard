/****************************************************************************
 * Copyright (C) 2016-2024 ecsec GmbH.
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

import org.openecard.ws.marshal.MarshallingTypeException
import org.openecard.ws.marshal.WSMarshallerException
import org.openecard.ws.marshal.WSMarshallerFactory.Companion.createInstance
import javax.annotation.Nonnull

/**
 *
 * @author Tobias Wich
 */
object JAXBUtils {
	@JvmStatic
	@Nonnull
	@Throws(MarshallingTypeException::class, WSMarshallerException::class)
	fun <T : Any> deepCopy(
		@Nonnull input: T,
	): T {
		val m = createInstance()
		val d = m.marshal(input)
		val out = m.unmarshal(d)
		return input.javaClass.cast(out)
	}
}
