/****************************************************************************
 * Copyright (C) 2014-2015 ecsec GmbH.
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
package org.openecard.transport.paos

import org.openecard.binding.tctoken.ex.ErrorTranslations
import org.openecard.common.I18n
import org.openecard.common.I18nException
import org.openecard.common.I18nKey

private val lang: I18n = I18n.getTranslation("tr03112")

/**
 * Localized ConnectionException.
 *
 * @author Hans-Martin Haase
 */
class PAOSConnectionException : I18nException {
	/**
	 * Creates an instance and initialize the exception with an static localized message.
	 */
	constructor() : super(lang, ErrorTranslations.PAOS_CONNECTION_EXCEPTION)

	/**
	 * Creates an instance and initialize the exception with an previous exception and a static localized exception.
	 *
	 * @param cause The exception causing the error.
	 */
	constructor(cause: Throwable?) : super(lang, ErrorTranslations.PAOS_CONNECTION_EXCEPTION, cause)

	/**
	 * Creates an instance and initializes the exception with a localized message.
	 *
	 * @param key Translation key.
	 * @param params Parameters adding values into the translation.
	 */
	constructor(key: I18nKey, vararg params: Any?) : super(lang, key, *params)
}
