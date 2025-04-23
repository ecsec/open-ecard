/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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

import org.openecard.common.I18n
import org.openecard.common.I18nException
import org.openecard.common.I18nKey

/**
 * Exception class indicating reflection or runtime errors in the dispatcher.
 * This exception indicates a failure to read webservice interface definitions and invocations with unknown types.
 *
 * @author Tobias Wich
 */
open class DispatcherException : I18nException {
	/**
	 * Creates an instance and initializes the exception with a message and a cause.
	 *
	 * @param message The message describing the error.
	 * @param cause The exception causing the error.
	 */
	@JvmOverloads
	constructor(message: String?, cause: Throwable? = null) : super(message, cause)

	/**
	 * Creates a DispatcherException.
	 *
	 * @param lang I18n instance providing the translation database.
	 * @param key Key which is fed into the translation database.
	 * @param params Optional parameters for the translation.
	 */
	protected constructor(lang: I18n, key: I18nKey, vararg params: Any) : super(lang, key, *params)

	/**
	 * Creates a DispatcherException.
	 *
	 * @param lang I18n instance providing the translation database.
	 * @param key Key which is fed into the translation database.
	 * @param params Optional parameters for the translation.
	 * @param cause Exception causing the problem.
	 */
	protected constructor(lang: I18n, key: I18nKey, cause: Throwable, vararg params: Any) : super(
		lang,
		key,
		cause,
		*params,
	)
}
