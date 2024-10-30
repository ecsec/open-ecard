/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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

package org.openecard.mobile.activation;

import org.openecard.robovm.annotations.FrameworkEnum;


/**
 * This class contains status codes which are used in service responses.
 *
 * @author Mike Prechtl
 */
@FrameworkEnum
public enum ServiceErrorCode {

    /**
     * indicates that nfc is not available on the corresponding device.
     */
    NFC_NOT_AVAILABLE,

	/**
	 * indicates that no connection to a service could be established
	 */
	NO_CONNECTION,
	/**
	 * indicates that a connection to a service was lost
	 */
	LOST_CONNECTION,

	/**
	 * indicates that no authorized connection could be established
	 */
	NOT_AUTHORIZED,
    /**
     * indicates that nfc is not enabled, please move to the device settings.
     */
    NFC_NOT_ENABLED,

    /**
     * indicates that the corresponding smartphone device doesn't support nfc with extended length.
     */
    NFC_NO_EXTENDED_LENGTH,

    /**
     * indicates that the corresponding device does not support the required API level.
     */
    NOT_REQUIRED_API_LEVEL,

    ALREADY_STARTED,
    ALREADY_STOPPED,
    /**
     * indicates other internal errors.
     */
    INTERNAL_ERROR,

    /**
     * indicates that the shutdown of the app failed.
     */
    SHUTDOWN_FAILED;
}
