/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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
package org.openecard.binding.tctoken.ex

import org.openecard.addon.bind.BindingResult
import org.openecard.common.I18n
import org.openecard.common.I18nException
import org.openecard.common.I18nKey

/**
 * The superclass of all errors which are visible to the activation action of the plug-in.
 * It has the capability to produce a BindingResult representing the error in an appropriate way.
 *
 * @author Tobias Wich
 */
abstract class ActivationError : I18nException {
	val bindingResult: BindingResult

	constructor(
		result: BindingResult,
		message: String,
	) : super(message) {
		this.bindingResult = result.setResultMessage(localizedMessage)
	}

	constructor(
		result: BindingResult,
		message: String,
		cause: Throwable?,
	) : super(message, cause) {
		this.bindingResult = result.setResultMessage(localizedMessage)
	}

	constructor(
		result: BindingResult,
		cause: Throwable?,
	) : super(cause) {
		this.bindingResult = result.setResultMessage(localizedMessage)
	}

	constructor(
		result: BindingResult,
		key: I18nKey?,
		vararg params: Any?,
	) : super(lang, key, *params) {
		this.bindingResult = result.setResultMessage(localizedMessage)
	}

	constructor(
		result: BindingResult,
		key: I18nKey?,
		cause: Throwable?,
		vararg params: Any?,
	) : super(
		lang,
		key,
		cause,
		*params,
	) {
		this.bindingResult = result.setResultMessage(localizedMessage)
	}

	companion object {
		protected val lang: I18n = I18n.getTranslation("tr03112")
	}
}
