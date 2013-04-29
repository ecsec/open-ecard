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

package org.openecard.control.module.tctoken;

import iso.std.iso_iec._24727.tech.schema.StartPAOSResponse;
import java.net.URL;
import java.util.concurrent.Future;
import org.openecard.control.client.ClientResponse;


/**
 * Implements a TCTokenResponse.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenResponse extends ClientResponse {

    private URL refreshAddress;
    private Future<StartPAOSResponse> paosTask;

    /**
     * Returns the refresh address.
     *
     * @return Refresh address
     */
    public URL getRefreshAddress() {
	return refreshAddress;
    }

    /**
     * Sets the refresh address.
     *
     * @param refreshAddress Refresh address
     */
    public void setRefreshAddress(URL refreshAddress) {
	this.refreshAddress = refreshAddress;
    }

    public void setPAOSTask(Future<StartPAOSResponse> paosTask) {
	this.paosTask = paosTask;
    }

    public Future<StartPAOSResponse> getPAOSTask() {
	return paosTask;
    }

}
