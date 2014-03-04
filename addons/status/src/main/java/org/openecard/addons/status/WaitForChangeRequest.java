/****************************************************************************
 * Copyright (C) 2012-2014 ecsec GmbH.
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

package org.openecard.addons.status;

import java.util.Map;
import javax.annotation.Nonnull;


/**
 * Wrapper for the wait for change request message.
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public final class WaitForChangeRequest {

    private final String sessionIdentifier;

    /**
     * Create a new StatusChangeReuquest.
     *
     * @param sessionIdentfier Session identifier. Must not be null.
     * @throws NullPointerException Thrown in case the session identifier is null.
     */
    public WaitForChangeRequest(@Nonnull String sessionIdentfier) {
	if (sessionIdentfier == null) {
	    throw new NullPointerException("Session identifier is null.");
	}
	this.sessionIdentifier = sessionIdentfier;
    }

    @Nonnull
    public String getSessionIdentifier() {
	return sessionIdentifier;
    }

    /**
     * Check the request parameters and wrap them in a {@code WaitForChangeRequest} class.
     *
     * @param parameters The request parameters.
     * @return A WaitForChangeRequest wrapping the parameters.
     * @throws StatusException Thrown in case not all required parameters are present.
     */
    public static WaitForChangeRequest convert(Map<String, String> parameters) throws StatusException {
	String session = null;

	if (parameters.containsKey("session")) {
	    session = parameters.get("session");
	}
	if (session == null || session.isEmpty()) {
	    throw new StatusException("Mandatory parameter session is missing.");
	}

	WaitForChangeRequest request = new WaitForChangeRequest(session);
	return request;
    }

}
