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

package org.openecard.addons.status;


/**
 * Implements an exception for connector errors.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class StatusException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Create a new StatusException.
     *
     * @param message Message
     */
    public StatusException(String message) {
	super(message);
    }

    /**
     * Create a new StatusException.
     *
     * @param message Message
     * @param throwable Throwable
     */
    public StatusException(String message, Throwable throwable) {
	super(message, throwable);
    }

}
