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

import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.common.I18nKey;


/**
 * Exception indicating an invalid TCToken.
 * Possible conditions are e.g. the TCToken is not present or could not be parsed.
 *
 * @author Tobias Wich
 */
public class InvalidTCTokenException extends FatalActivationError {

    public InvalidTCTokenException(String msg) {
	super(new BindingResult(BindingResultCode.RESOURCE_UNAVAILABLE), msg);
    }

    public InvalidTCTokenException(String msg, Throwable ex) {
	super(new BindingResult(BindingResultCode.RESOURCE_UNAVAILABLE), msg, ex);
    }

    public InvalidTCTokenException(I18nKey key, Object... params) {
	super(new BindingResult(BindingResultCode.RESOURCE_UNAVAILABLE), key, params);
    }

    public InvalidTCTokenException(I18nKey key, Throwable cause, Object... params) {
	super(new BindingResult(BindingResultCode.RESOURCE_UNAVAILABLE), key, cause, params);
    }

    protected InvalidTCTokenException(BindingResultCode code, String msg) {
	super(new BindingResult(code), msg);
    }

    protected InvalidTCTokenException(BindingResultCode code, String msg, Throwable ex) {
	super(new BindingResult(code), msg, ex);
    }

    protected InvalidTCTokenException(BindingResultCode code, I18nKey key, Object... params) {
	super(new BindingResult(code), key, params);
    }

    protected InvalidTCTokenException(BindingResultCode code, I18nKey key, Throwable cause, Object... params) {
	super(new BindingResult(code), key, cause, params);
    }

}
