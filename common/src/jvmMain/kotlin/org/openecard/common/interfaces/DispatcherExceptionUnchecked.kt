/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

/**
 * Exception class indicating reflection or runtime errors in the dispatcher.
 * This exception indicates a failure to read webservice interface definitions and invocations with unknown types.
 * This exception is unchecked to make it easier to call interfaces such as the SAL which are definitely present.
 *
 * @author Tobias Wich
 */
class DispatcherExceptionUnchecked : RuntimeException {
	/**
	 * Creates an instance and initializes the exception with a message and a cause.
	 *
	 * @param message The message describing the error.
	 * @param cause The exception causing the error.
	 */
	@JvmOverloads
	constructor(message: String?, cause: Throwable? = null) : super(message, cause)
}
