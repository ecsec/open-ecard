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

package org.openecard.binding.tctoken.ex;

import javax.annotation.Nonnull;
import org.openecard.addon.bind.BindingResult;
import org.openecard.common.I18n;


/**
 * The superclass of all errors which are visible to the activation action of the plug-in.
 * It has the capability to produce a BindingResult representing the error in an appropriate way.
 *
 * @author Tobias Wich
 */
public abstract class ActivationError extends Exception {

    private final BindingResult result;
    private final Object[] params;
    private final String msgCode;

    protected final I18n lang = I18n.getTranslation("tr03112");

    public ActivationError(@Nonnull BindingResult result, @Nonnull String message, Object ... params) {
	this(result, message, null, params);
    }

    public ActivationError(@Nonnull BindingResult result, @Nonnull String message, Throwable cause, Object ... params) {
	super(message, cause);
	this.result = result;
	this.params = params;
	this.msgCode = message;
    }

    public BindingResult getBindingResult() {
	return result;
    }

    @Override
    public String getMessage() {
	if (msgCode == null || msgCode.isEmpty()) {
	    if (super.getCause().getMessage() != null) {
		return super.getCause().getMessage();
	    } else {
		return "";
	    }
	} else {
	    if (params != null) {
		return lang.getOriginalMessage(msgCode, params);
	    } else {
		return lang.getOriginalMessage(msgCode);
	    }
	}
    }

    @Override
    public String getLocalizedMessage() {
	if (msgCode == null || msgCode.isEmpty()) {
	    return super.getCause().getLocalizedMessage();
	} else {
	    if (params != null) {
		return lang.translationForKey(msgCode, params);
	    } else {
		return lang.translationForKey(msgCode);
	    }
	}
    }

}
