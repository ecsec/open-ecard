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
package org.openecard.common

/**
 * Localized exception based on I18n based translation.
 *
 * @author Tobias Wich
 */
abstract class I18nException : Exception {
	private val localMsg: String?

	/**
	 * Creates an I18nException.
	 *
	 * @param message Untranslated message.
	 */
	constructor(message: String?) : super(message) {
		this.localMsg = null
	}

	/**
	 * Creates an I18nException.
	 *
	 * @param cause Exception causing the problem.
	 */
	constructor(cause: Throwable) : super(cause) {
		this.localMsg = cause.localizedMessage
	}

	/**
	 * Creates an I18nException.
	 *
	 * @param message Untranslated message.
	 * @param cause Exception causing the problem.
	 */
	constructor(message: String?, cause: Throwable?) : super(cause) {
		this.localMsg = null
	}

	/**
	 * Creates an I18nException.
	 *
	 * @param lang I18n instance providing the translation database.
	 * @param key Key which is fed into the translation database.
	 * @param params Optional parameters for the translation.
	 */
	constructor(lang: I18n, key: String, vararg params: Any?) : super(lang.getOriginalMessage(key, *params)) {
		this.localMsg = lang.translationForKey(key, *params)
	}

	/**
	 * Creates an I18nException.
	 *
	 * @param lang I18n instance providing the translation database.
	 * @param key Key which is fed into the translation database.
	 * @param params Optional parameters for the translation.
	 * @param cause Exception causing the problem.
	 */
	constructor(lang: I18n, key: String, cause: Throwable?, vararg params: Any?) : super(
		lang.getOriginalMessage(
			key,
			*params,
		),
		cause,
	) {
		this.localMsg = lang.translationForKey(key, *params)
	}

	/**
	 * Creates an I18nException.
	 *
	 * @param lang I18n instance providing the translation database.
	 * @param key Key which is fed into the translation database.
	 * @param params Optional parameters for the translation.
	 */
	constructor(lang: I18n, key: I18nKey, vararg params: Any?) : super(lang.getOriginalMessage(key, *params)) {
		this.localMsg = lang.translationForKey(key, *params)
	}

	/**
	 * Creates an I18nException.
	 *
	 * @param lang I18n instance providing the translation database.
	 * @param key Key which is fed into the translation database.
	 * @param params Optional parameters for the translation.
	 * @param cause Exception causing the problem.
	 */
	constructor(lang: I18n, key: I18nKey, cause: Throwable?, vararg params: Any?) : super(
		lang.getOriginalMessage(
			key,
			*params,
		),
		cause,
	) {
		this.localMsg = lang.translationForKey(key, *params)
	}

	override fun getLocalizedMessage(): String = localMsg ?: super.getLocalizedMessage()
}
