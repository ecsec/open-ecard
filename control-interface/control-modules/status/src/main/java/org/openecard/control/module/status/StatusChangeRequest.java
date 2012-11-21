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

import org.openecard.control.client.ClientRequest;


/**
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public final class StatusChangeRequest extends ClientRequest {

    private final String sessionIdentifier;

    /**
     * Create a new StatusChangeReuquest.
     * @param sessionIdentfier session identifier
     */
    public StatusChangeRequest(String sessionIdentfier) {
	this.sessionIdentifier = sessionIdentfier;
    }

    /**
     * Returns the session identifier.
     * @return the session identifier
     */
    public String getSessionIdentifier() {
	return sessionIdentifier;
    }

}
