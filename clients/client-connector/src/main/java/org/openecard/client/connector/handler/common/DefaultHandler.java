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

package org.openecard.client.connector.handler.common;

import org.openecard.client.connector.handler.ConnectorCommonHandler;
import org.openecard.client.connector.http.HTTPRequest;
import org.openecard.client.connector.http.HTTPResponse;
import org.openecard.client.connector.http.HTTPStatusCode;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class DefaultHandler extends ConnectorCommonHandler {

    /**
     * Creates a new default handler.
     */
    public DefaultHandler() {
	super("*");
    }

    @Override
    public HTTPResponse handle(HTTPRequest httpRequest) throws Exception {
	// Always return 404 Not Found
	HTTPResponse httpResponse = new HTTPResponse(HTTPStatusCode.NOT_FOUND_404);
	return httpResponse;
    }

}
