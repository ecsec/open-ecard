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
 ***************************************************************************/

package org.openecard.common;


/**
 * Localized exception based on I18n based translation.
 *
 * @author Tobias Wich
 */
public abstract class I18nException extends Exception {

    private final String localMsg;

    /**
     * Creates an I18nException.
     *
     * @param message Untranslated message.
     */
    public I18nException(String message) {
	super(message);
	this.localMsg = null;
    }

    /**
     * Creates an I18nException.
     *
     * @param cause Exception causing the problem.
     */
    public I18nException(Throwable cause) {
	super(cause);
	this.localMsg = cause.getLocalizedMessage();
    }

    /**
     * Creates an I18nException.
     *
     * @param message Untranslated message.
     * @param cause Exception causing the problem.
     */
    public I18nException(String message, Throwable cause) {
	super(cause);
	this.localMsg = null;
    }

    /**
     * Creates an I18nException.
     *
     * @param lang I18n instance providing the translation database.
     * @param key Key which is fed into the translation database.
     * @param params Optional parameters for the translation.
     */
    public I18nException(I18n lang, String key, Object... params) {
	super(lang.getOriginalMessage(key, params));
	this.localMsg = lang.translationForKey(key, params);
    }

    /**
     * Creates an I18nException.
     *
     * @param lang I18n instance providing the translation database.
     * @param key Key which is fed into the translation database.
     * @param params Optional parameters for the translation.
     * @param cause Exception causing the problem.
     */
    public I18nException(I18n lang, String key, Throwable cause, Object... params) {
	super(lang.getOriginalMessage(key, params), cause);
	this.localMsg = lang.translationForKey(key, params);
    }

    /**
     * Creates an I18nException.
     *
     * @param lang I18n instance providing the translation database.
     * @param key Key which is fed into the translation database.
     * @param params Optional parameters for the translation.
     */
    public I18nException(I18n lang, I18nKey key, Object... params) {
	super(lang.getOriginalMessage(key, params));
	this.localMsg = lang.translationForKey(key, params);
    }

    /**
     * Creates an I18nException.
     *
     * @param lang I18n instance providing the translation database.
     * @param key Key which is fed into the translation database.
     * @param params Optional parameters for the translation.
     * @param cause Exception causing the problem.
     */
    public I18nException(I18n lang, I18nKey key, Throwable cause, Object... params) {
	super(lang.getOriginalMessage(key, params), cause);
	this.localMsg = lang.translationForKey(key, params);
    }

    @Override
    public String getLocalizedMessage() {
	if (localMsg != null) {
	    return localMsg;
	} else {
	    return super.getLocalizedMessage();
	}
    }

}
