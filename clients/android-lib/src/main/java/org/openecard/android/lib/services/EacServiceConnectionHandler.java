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

package org.openecard.android.lib.services;

import org.openecard.android.lib.ServiceErrorResponse;
import org.openecard.gui.android.eac.types.ServerData;


/**
 *
 * @author Mike Prechtl
 */
public interface EacServiceConnectionHandler extends ServiceConnectionHandler {

    void onServerDataPresent(ServerData data);

    void onPINIsRequired();

    void onPINInputSuccess();

    void onPINInputFailure();

    /**
     * If the communication with the eac service is interrupted or an error occurs, then more detals are delivered
     * over this method.
     *
     * @param response
     */
    void onRemoteError(ServiceErrorResponse response);

}
