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

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.List;
import org.openecard.control.client.ClientResponse;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class StatusResponse extends ClientResponse {

    private List<ConnectionHandleType> connectionHandles;

    /**
     * Returns the list of connection handles.
     *
     * TODO: replace ConnectionHandleType with StatusType
     *
     * @return List of connection handles
     */
    public List<ConnectionHandleType> getConnectionHandles() {
	return connectionHandles;
    }

    /**
     * Sets the list of connection handles.
     *
     * TODO: replace ConnectionHandleType with StatusType
     *
     * @param connectionHandles List of connection handles
     */
    public void setConnectionHandles(List<ConnectionHandleType> connectionHandles) {
	this.connectionHandles = connectionHandles;
    }

}
