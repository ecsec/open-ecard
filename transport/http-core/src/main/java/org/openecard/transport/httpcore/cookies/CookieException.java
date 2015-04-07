/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

package org.openecard.transport.httpcore.cookies;

import javax.annotation.Nonnull;


/**
 * Exception implementation which shall be used for the CookieManager and the Cookie class.
 *
 * @author Hans-Martin Haase
 */
public class CookieException extends Exception {

    /**
     * Create a new instance with the given message.
     *
     * @param message The exception message to set.
     */
    public CookieException(@Nonnull String message) {
	super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     *
     * @param message The exception message to set.
     * @param cause The cause of the exception to set.
     */
    public CookieException(@Nonnull String message, @Nonnull Throwable cause) {
	super(message, cause);
    }
}
