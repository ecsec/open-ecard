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

package org.openecard.client.connector.http;

import java.util.ArrayList;
import java.util.List;
import org.openecard.client.connector.http.header.EntityHeader;
import org.openecard.client.connector.http.header.GeneralHeader;
import org.openecard.client.connector.http.header.StartLine;


/**
 * Implements a HTTP message.
 * See RFC 2616, section 4 HTTP Message.
 * See http://tools.ietf.org/html/rfc2616#section-4
 *
 * HTTP-message = Request | Response
 *
 * generic-message = start-line
 * (message-header CRLF)
 * CRLF
 * [ message-body ]
 * start-line = Request-Line | Status-Line
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public abstract class HTTPMessage {

    /**
     * Stores the general-headers of a HTTP message.
     */
    protected List<GeneralHeader> generalHeaders = new ArrayList<GeneralHeader>();
    /**
     * Stores the entity-headers of a HTTP message.
     */
    protected List<EntityHeader> entityHeaders = new ArrayList<EntityHeader>();
    /**
     * Stores the start-line of a HTTP message.
     */
    protected StartLine startLine;
    /**
     * Stores the message body of a HTTP message.
     */
    protected byte[] messageBody;

}
