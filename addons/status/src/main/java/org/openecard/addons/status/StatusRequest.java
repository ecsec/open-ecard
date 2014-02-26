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
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wrapper for the status request message.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public final class StatusRequest {

    private static final Logger logger = LoggerFactory.getLogger(StatusRequest.class);

    private final String sessionIdentifier;

    public StatusRequest(@Nullable String sessionIdentifier) {
	this.sessionIdentifier = sessionIdentifier;
    }

    public boolean hasSessionIdentifier() {
	return sessionIdentifier != null;
    }

    /**
     * Returns the session identifier.
     *
     * @return The session identifier.
     * @throws NullPointerException In case this request contains no session ID.
     */
    @Nonnull
    public String getSessionIdentifier() {
	if (sessionIdentifier == null) {
	    throw new NullPointerException("No session ID available in this request.");
	}
	return sessionIdentifier;
    }

    /**
     * Check the request parameters and wrap them in a {@code StatusRequest} class.
     *
     * @param parameters The request parameters.
     * @return A StatusRequest wrapping the parameters.
     */
    public static StatusRequest convert(Map<String, String> parameters) {
	String session = null;

	if (parameters.containsKey("session")) {
	    String value = parameters.get("session");
	    if (value != null && ! value.isEmpty()) {
		session = value;
	    }
	}

	StatusRequest statusRequest = new StatusRequest(session);
	return statusRequest;
    }

}
