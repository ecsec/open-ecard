/****************************************************************************
 * Copyright (C) 2014-2025 ecsec GmbH.
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
package org.openecard.addons.cg.ex

import org.openecard.addon.bind.AuxDataKeys
import org.openecard.addon.bind.BindingResult
import org.openecard.addon.bind.BindingResultCode
import org.openecard.common.I18nKey

/**
 * Exception indicating that a redirect of the caller will be performed.
 *
 * @author Tobias Wich
 */
abstract class RedirectionBaseError : ActivationError {
	constructor(errorUrl: String?, msg: String) : super(makeBindingResult(errorUrl), msg)

	constructor(errorUrl: String?, msg: String, ex: Throwable?) : super(makeBindingResult(errorUrl), msg, ex)

	constructor(errorUrl: String?, ex: Throwable) : super(makeBindingResult(errorUrl), ex)

	constructor(errorUrl: String?, key: I18nKey, vararg params: Any?) : super(
		makeBindingResult(errorUrl),
		key,
		*params,
	)

	constructor(errorUrl: String?, key: I18nKey, cause: Throwable?, vararg params: Any?) : super(
		makeBindingResult(
			errorUrl,
		),
		key,
		cause,
		*params,
	)

	companion object {
		private fun makeBindingResult(errorUrl: String?): BindingResult {
			val result = BindingResult(BindingResultCode.REDIRECT)
			return result.addAuxResultData(AuxDataKeys.REDIRECT_LOCATION, errorUrl)
		}
	}
}
