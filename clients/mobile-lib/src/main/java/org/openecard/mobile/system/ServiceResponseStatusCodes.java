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

package org.openecard.mobile.system;


/**
 * This class contains status codes which are used in service responses.
 *
 * @author Mike Prechtl
 */
public class ServiceResponseStatusCodes {

    /**
     * indicates that nfc is not available on the corresponding device.
     */
    public static final int NFC_NOT_AVAILABLE = 100;

    /**
     * indicates that nfc is not enabled, please move to the device settings.
     */
    public static final int NFC_NOT_ENABLED = 101;

    /**
     * indicates that the corresponding smartphone device doesn't support nfc with extended length.
     */
    public static final int NFC_NO_EXTENDED_LENGTH = 103;

    /**
     * indicates that the initialization was successfully finished.
     */
    public static final int INIT_SUCCESS = 200;

    /**
     * indicates that the app was successfully terminated.
     */
    public static final int SHUTDOWN_SUCCESS = 200;

    /**
     * indicates that the corresponding device does not support the required API level.
     */
    public static final int NOT_REQUIRED_API_LEVEL = 102;

    /**
     * indicates other internal errors.
     */
    public static final int INTERNAL_ERROR = 500;

    /**
     * indicates that the shutdown of the app failed.
     */
    public static final int SHUTDOWN_FAILED = 501;

}
