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
package org.openecard.binding.tctoken

import org.openecard.common.I18n
import org.openecard.common.I18nException
import org.openecard.common.I18nKey

private val lang: I18n = I18n.getTranslation("tr03112")

/**
 *
 * @author Tobias Wich
 */
class ConnectionError : I18nException {
	constructor(msg: String) : super(lang, msg)

	constructor(msg: String, ex: Throwable?) : super(lang, msg, ex)

	constructor(key: I18nKey, vararg params: Any?) : super(lang, key, *params)

	constructor(key: I18nKey, cause: Throwable?, vararg params: Any?) : super(lang, key, cause, *params)
}
