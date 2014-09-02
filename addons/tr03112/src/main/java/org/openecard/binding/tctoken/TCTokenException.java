/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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

import org.openecard.addon.bind.BindingResultCode;


/**
 * Implements an exception for TCToken verification errors.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public class TCTokenException extends Exception {

    private static final long serialVersionUID = 1L;
    private static final BindingResultCode DEFAULT_CODE = BindingResultCode.RESOURCE_UNAVAILABLE;

    private final BindingResultCode code;

    /**
     * Creates an new TCTokenException.
     *
     * @param message Message
     */
    public TCTokenException(String message) {
	this(message, DEFAULT_CODE);
    }

    /**
     * Creates an new TCTokenException.
     *
     * @param message Message
     * @param code Code used in the {@link TCTokenAction} to produce a result for the binding.
     */
    public TCTokenException(String message, BindingResultCode code) {
	super(message);
	this.code = code;
    }

    /**
     * Creates an new TCTokenException.
     *
     * @param message Message
     * @param throwable Throwable
     */
    public TCTokenException(String message, Throwable throwable) {
	this(message, DEFAULT_CODE, throwable);
    }

    /**
     * Creates an new TCTokenException.
     *
     * @param message Message
     * @param code Code used in the {@link TCTokenAction} to produce a result for the binding.
     * @param throwable Throwable
     */
    public TCTokenException(String message, BindingResultCode code, Throwable throwable) {
	super(message, throwable);
	this.code = code;
    }

}
