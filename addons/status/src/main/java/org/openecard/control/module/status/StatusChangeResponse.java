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

package org.openecard.control.module.status;

import org.openecard.ws.schema.StatusChange;


/**
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 * @author Benedikt Biallowons <benedikt.biallowons@ecsec.de>
 */
public final class StatusChangeResponse {

    private StatusChange statusChange;

    /**
     * Returns the status change element.
     *
     * @return the status change type
     */
    public StatusChange getStatusChange() {
	return statusChange;
    }

    /**
     * Sets the status change element.
     *
     * @param statusChange the status change element
     */
    public void setStatusChange(StatusChange statusChange) {
	this.statusChange = statusChange;
    }

}
