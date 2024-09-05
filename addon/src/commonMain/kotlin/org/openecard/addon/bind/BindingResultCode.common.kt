/****************************************************************************
 * Copyright (C) 2013-2024 ecsec GmbH.
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

package org.openecard.addon.bind


/**
 * Class containing the available binding results which are mapped to HTTP status codes.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
enum class BindingResultCode {
    /**
     * Indicates that an depending host is not available.
     */
    DEPENDING_HOST_UNREACHABLE,

    /**
     * Indicates an internal error of the issuing component.
     */
    INTERNAL_ERROR,

    /**
     * Indicates the absence of a required parameter.
     */
    MISSING_PARAMETER,

    /**
     * Indicates that the request was processed successfully.
     */
    OK,

    /**
     * Indicates to redirect the caller.
     */
    REDIRECT,

    /**
     * Indicates that the resource is locked for example if a request arrives while an other one is currently processed.
     */
    RESOURCE_LOCKED,

    /**
     * Indicates that the requested resource is not available.
     */
    RESOURCE_UNAVAILABLE,

    /**
     * Indicates that the component has run into a timeout while the processing of the request.
     */
    TIMEOUT,

    /**
     * Indicates that there are to many requests to handle.
     */
    TOO_MANY_REQUESTS,

    /**
     * Indicates that a required parameter is wrong.
     */
    WRONG_PARAMETER,

    /**
     * Indicates a thread interruption, either explicitly by the user or implicitly by a shutdown of a
     * subsystem or the whole system.
     */
    INTERRUPTED,
	;
}
