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
import org.openecard.common.I18nException;
import org.openecard.common.I18nKey;


/**
 * The superclass of all errors which are visible to the activation action of the plug-in.
 * It has the capability to produce a BindingResult representing the error in an appropriate way.
 *
 * @author Tobias Wich
 */
public abstract class ActivationError extends I18nException {

    private final BindingResult result;

    protected static final I18n lang = I18n.getTranslation("tr03112");

    public ActivationError(@Nonnull BindingResult result, @Nonnull String message) {
	super(message);
	this.result = result.setResultMessage(getLocalizedMessage());
    }

    public ActivationError(@Nonnull BindingResult result, @Nonnull String message, Throwable cause) {
	super(message, cause);
	this.result = result.setResultMessage(getLocalizedMessage());
    }

    public ActivationError(@Nonnull BindingResult result, Throwable cause) {
        super(cause);
        this.result = result.setResultMessage(getLocalizedMessage());
    }

    public ActivationError(@Nonnull BindingResult result, I18nKey key, Object... params) {
	super(lang, key, params);
	this.result = result.setResultMessage(getLocalizedMessage());
    }

    public ActivationError(@Nonnull BindingResult result, I18nKey key, Throwable cause, Object... params) {
	super(lang, key, cause, params);
	this.result = result.setResultMessage(getLocalizedMessage());
    }

    public BindingResult getBindingResult() {
	return result;
    }

}
