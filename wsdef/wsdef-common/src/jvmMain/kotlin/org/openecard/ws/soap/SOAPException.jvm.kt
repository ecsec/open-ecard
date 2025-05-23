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
package org.openecard.ws.soap

import org.openecard.ws.marshal.WSMarshallerException

/**
 *
 * @author Tobias Wich
 */
class SOAPException : WSMarshallerException {
	constructor(message: String) : super(message)

	constructor(cause: Throwable) : super(cause)

	constructor(message: String, cause: Throwable) : super(message, cause)

	companion object {
		private const val serialVersionUID = 1L
	}
}
