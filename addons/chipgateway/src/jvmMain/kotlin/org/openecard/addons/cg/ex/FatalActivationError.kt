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

import org.openecard.addon.bind.BindingResult
import org.openecard.common.I18nKey

/**
 * Specialization of an ActivationError which does not permit the user to continue after returning to the Browser.
 *
 * @author Tobias Wich
 */
abstract class FatalActivationError : ActivationError {
	constructor(result: BindingResult, message: String) : super(result, message)

	constructor(result: BindingResult, message: String, cause: Throwable?) : super(
		result,
		message,
		cause,
	)

	constructor(result: BindingResult, key: I18nKey, vararg params: Any?) : super(result, key, *params)

	constructor(result: BindingResult, key: I18nKey, cause: Throwable?, vararg params: Any?) : super(
		result,
		key,
		cause,
		*params,
	)
}
