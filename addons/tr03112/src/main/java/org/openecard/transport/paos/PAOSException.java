/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.transport.paos;

import org.openecard.common.I18n;


/**
 * Exception for the PAOS system.
 * This exception abstracts transport specific exceptions.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PAOSException extends Exception {

    private static final long serialVersionUID = 1L;
    private static final I18n lang = I18n.getTranslation("tr03112");
    private String errMsg;
    private Object[] params;

    /**
     * Creates an instance and initializes the exception with a message.
     *
     * @param msg The message describing the error.
     * @param params
     */
    public PAOSException(String msg, Object ... params) {
	super(msg);
	errMsg = msg;
	this.params = params;
    }

    /**
     * Creates an instance and initializes the exception with a cause.
     *
     * @param cause The exception causing the error.
     */
    public PAOSException(Throwable cause) {
	super(cause);
    }

    /**
     * Creates an instance and initializes the exception with a message and a cause.
     *
     * @param msg The message describing the error.
     * @param cause The exception causing the error.
     * @param params
     */
    public PAOSException(String msg, Throwable cause, Object ... params) {
	super(msg, cause);
	errMsg = msg;
	this.params = params;
    }

    @Override
    public String getMessage() {
	if (errMsg == null || errMsg.isEmpty()) {
	    return super.getCause().getMessage();
	} else {
	    if (params != null) {
		return lang.getOriginalMessage(errMsg, params);
	    } else {
		return lang.getOriginalMessage(errMsg);
	    }
	}
    }

    @Override
    public String getLocalizedMessage() {
	if (errMsg == null || errMsg.isEmpty()) {
	    return super.getCause().getLocalizedMessage();
	} else {
	    if (params == null ) {
		return lang.translationForKey(errMsg);
	    } else {
		return lang.translationForKey(errMsg, params);
	    }
	}
    }

}
