/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.control.module.status;

import generated.StatusChangeType;
import org.openecard.client.control.client.ClientResponse;


/**
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 * @author Benedikt Biallowons <benedikt.biallowons@ecsec.de>
 */
public final class StatusChangeResponse extends ClientResponse {

    private StatusChangeType statusChangeType;

    /**
     * Returns the status change type.
     * 
     * @return the status change type
     */
    public StatusChangeType getStatusChangeType() {
	return statusChangeType;
    }

    /**
     * Sets the status change type.
     * 
     * @param statusChangeType the status change type
     */
    public void setStatusChangeType(StatusChangeType statusChangeType) {
	this.statusChangeType = statusChangeType;
    }


}
