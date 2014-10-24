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


/**
 * Specialization of an ActivationError which does not permit the user to continue after returning to the Browser.
 *
 * @author Tobias Wich
 */
public abstract class FatalActivationError extends ActivationError {

    public FatalActivationError(BindingResultCode code, String msg, Object ... params) {
	super(new BindingResult(code).setResultMessage(msg), msg, params);
    }

    public FatalActivationError(BindingResultCode code, String msg, Throwable ex, Object ... params) {
	super(new BindingResult(code).setResultMessage(msg), msg, ex, params);
    }

}
