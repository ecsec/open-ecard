/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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
package org.openecard.scio

import org.openecard.common.ifd.scio.TerminalFactory
import org.openecard.ws.common.GenericFactoryException
import org.openecard.ws.common.GenericInstanceProvider

/**
 *
 * @author Neil Crossley
 */
class CachingTerminalFactoryBuilder<T : TerminalFactory>(
	private val delegate: GenericInstanceProvider<T>,
) : GenericInstanceProvider<TerminalFactory> {
	var previousInstance: T? = null
		private set

	@get:Throws(GenericFactoryException::class)
	override val instance: T
		get() {
			val next = delegate.instance
			previousInstance = next
			return next
		}
}
