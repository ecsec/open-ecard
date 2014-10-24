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

package org.openecard.binding.tctoken;

import org.openecard.common.I18n;


/**
 *
 * @author Tobias Wich
 */
public class ValidationError extends Exception {

    private static final I18n lang = I18n.getTranslation("tr03112");

    private final String msgCode;

    public ValidationError(String msg) {
	super(msg);
	msgCode = msg;
    }

    public ValidationError(String msg, Throwable cause) {
	super(msg, cause);
	msgCode = msg;
    }

    @Override
    public String getMessage() {
	if (msgCode != null && ! msgCode.isEmpty()) {
	    return lang.getOriginalMessage(msgCode);
	} else {
	    return super.getMessage();
	}
    }

    @Override
    public String getLocalizedMessage() {
	if (msgCode != null && ! msgCode.isEmpty()) {
	    return lang.translationForKey(msgCode);
	} else {
	    return super.getLocalizedMessage();
	}
    }

}
