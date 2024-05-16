/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

package org.openecard.httpcore;

import org.apache.http.HttpException;


/**
 * Exception indicating an errornous result.
 * Results with codes greater than 400 are errors. In some circumstances it is more convenient to raise an error to
 * signal the invalid result. This class helps to do so.
 *
 * @author Tobias Wich
 */
public class InvalidResultStatus extends HttpException {

    private static final long serialVersionUID = 1L;

    private int code;
    private String reason;

    public InvalidResultStatus(int code, String reason, String message) {
	super(message);
    }

    public InvalidResultStatus(int code, String reason, String message, Throwable cause) {
	super(message, cause);
    }

    public int getCode() {
	return code;
    }

    public String getReason() {
	return reason;
    }

}
