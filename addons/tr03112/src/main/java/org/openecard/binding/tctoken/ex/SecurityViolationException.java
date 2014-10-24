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


/**
 * Exception indicating a security violation.
 * This can be a violation of the same origin policy or something similar.
 *
 * @author Tobias Wich
 */
public class SecurityViolationException extends RedirectionBaseError {

    public SecurityViolationException(String errorUrl, String message) {
	super(errorUrl, message);
    }

    public SecurityViolationException(String errorUrl, String message, Throwable cause) {
	super(errorUrl, message, cause);
    }

    public SecurityViolationException(String errorUrl, Throwable cause) {
	super(errorUrl, cause);
    }

}
