/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.android.system;


/**
 *
 * @author Mike Prechtl
 */
public interface ConnectionHandler {

    /**
     * This method indicates that establishing a connection to the service was successful.
     */
    void onConnectionSuccess();

    /**
     * This method indicates that establishing a connection to the service failed. The response contains
     * more information why it failed.
     *
     * @param response which contains more information to the failure.
     */
    void onConnectionFailure(ServiceErrorResponse response);

    /**
     * This method indicates that establishing a connection to the service failed. The response contains more
     * information why it failed. Warning responses can be fixed, for example by enabling nfc.
     *
     * @param response which contains more information to the failure.
     */
    void onConnectionFailure(ServiceWarningResponse response);

    /**
     * This method indicates that disconnecting from the service was successful.
     */
    void onDisconnectionSuccess();

    /**
     * This method indicates that disconnecting from the service failed. The response contains more information.
     *
     * @param response which contains more information to the failure.
     */
    void onDisconnectionFailure(ServiceErrorResponse response);

    /**
     * This method indicates that disconnecting from the service failed. The response contains more information.
     * Warning responses can be fixed.
     *
     * @param response which contains more information to the failure.
     */
    void onDisconnectionFailure(ServiceWarningResponse response);

}
