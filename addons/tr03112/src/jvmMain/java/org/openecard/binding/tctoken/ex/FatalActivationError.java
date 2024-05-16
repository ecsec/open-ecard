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
import org.openecard.common.I18nKey;


/**
 * Specialization of an ActivationError which does not permit the user to continue after returning to the Browser.
 *
 * @author Tobias Wich
 */
public abstract class FatalActivationError extends ActivationError {

    public FatalActivationError(@Nonnull BindingResult result, @Nonnull String message) {
	super(result, message);
    }

    public FatalActivationError(@Nonnull BindingResult result, @Nonnull String message, Throwable cause) {
	super(result, message, cause);
    }

    public FatalActivationError(@Nonnull BindingResult result, I18nKey key, Object... params) {
	super(result, key, params);
    }

    public FatalActivationError(@Nonnull BindingResult result, I18nKey key, Throwable cause, Object... params) {
	super(result, key, cause, params);
    }

}
